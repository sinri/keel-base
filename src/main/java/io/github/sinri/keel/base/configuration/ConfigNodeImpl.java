package io.github.sinri.keel.base.configuration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

class ConfigNodeImpl implements ConfigNode {
    /**
     * 配置节点的子节点，以子节点名称为键的一个 Map。
     */
    @NotNull
    private final Map<String, ConfigNode> children;
    /**
     * 配置节点的名称
     */
    @NotNull
    private String name;
    /**
     * 配置节点的值。
     * <p>
     * 如果 value 为 null，视为未配置。
     */
    @Nullable
    private String value;

    public ConfigNodeImpl(@NotNull String name) {
        this.name = name;
        this.value = null;
        this.children = new ConcurrentHashMap<>();
    }

    public ConfigNodeImpl(@NotNull ConfigNode anther) {
        this.name = anther.getName();
        this.value = anther.getValue();
        this.children = anther.getChildren();
    }

    @NotNull
    private static ConfigNodeImpl decodeJsonObject(@NotNull JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        Objects.requireNonNull(name, "name should not be null");
        ConfigNodeImpl configElement = new ConfigNodeImpl(name);
        if (jsonObject.containsKey("value")) {
            Object v = jsonObject.getValue("value");
            configElement.value = (v == null ? null : v.toString());
        }
        JsonArray children = jsonObject.getJsonArray("children");
        children.forEach(child -> {
            if (child instanceof JsonObject) {
                configElement.addChild(decodeJsonObject((JsonObject) child));
            } else {
                throw new IllegalArgumentException("Child should be a JSON object");
            }
        });
        return configElement;
    }

    @Override
    public final @NotNull String getName() {
        return name;
    }

    @Override
    public final @Nullable String getValue() {
        return value;
    }

    @Override
    public final @NotNull ConfigNode setValue(@Nullable String value) {
        this.value = value;
        return this;
    }

    @Override
    public final @NotNull Map<String, ConfigNode> getChildren() {
        return children;
    }

    @Override
    public @NotNull ConfigNode ensureChild(@NotNull String childName) {
        return children.computeIfAbsent(childName, ConfigNodeImpl::new);
    }

    @Override
    public @NotNull ConfigNode addChild(@NotNull ConfigNode child) {
        this.children.put(child.getName(), child);
        return this;
    }

    @Override
    public @NotNull ConfigNode removeChild(@NotNull String childName) {
        this.children.remove(childName);
        return this;
    }

    @Override
    public @Nullable ConfigNode getChild(@NotNull String childName) {
        return this.children.get(childName);
    }

    @Override
    public @NotNull JsonObject toJsonObject() {
        JsonArray childArray = new JsonArray();
        children.forEach((cName, c) -> childArray.add(c.toJsonObject()));
        var x = new JsonObject()
                .put("name", name)
                .put("children", childArray);
        if (value != null) {
            x.put("value", value);
        }
        return x;
    }

    @Override
    public void reloadData(@NotNull JsonObject jsonObject) {
        ConfigNodeImpl configElement = decodeJsonObject(jsonObject);
        this.name = configElement.name;
        this.children.clear();
        configElement.children.values().forEach(this::addChild);
        this.value = configElement.value;
    }

    @Override
    public @NotNull String toJsonExpression() {
        return toJsonObject().encode();
    }

    @Override
    public @NotNull String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    @Override
    @NotNull
    public ConfigNode loadProperties(@NotNull Properties properties) {
        properties.forEach((k, v) -> {
            String fullKey = k.toString();
            String[] keyArray = fullKey.split("\\.");
            if (keyArray.length > 0) {
                ConfigNode configElement = children.computeIfAbsent(
                        keyArray[0],
                        x -> new ConfigNodeImpl(keyArray[0]));
                if (keyArray.length == 1) {
                    configElement.setValue(v.toString());
                } else {
                    for (int i = 1; i < keyArray.length; i++) {
                        String key = keyArray[i];
                        configElement = configElement.ensureChild(key);
                        if (i == keyArray.length - 1) {
                            configElement.setValue(v.toString());
                        }
                    }
                }
            }
        });
        return this;
    }
}
