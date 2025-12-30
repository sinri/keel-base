package io.github.sinri.keel.base.configuration.lab;

import io.github.sinri.keel.base.configuration.ConfigProperty;
import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class ConfigElementImpl extends JsonifiableDataUnitImpl implements ConfigElement {
    private static final String KEY_NAME = "name";
    private static final String KEY_CHILDREN = "children";
    private static final String KEY_VALUE = "value";

    public ConfigElementImpl() {
        super();
    }

    public ConfigElementImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    public ConfigElementImpl(String s) {
        this(new JsonObject().put(KEY_NAME, s));
    }

    @Override
    public @NotNull String getElementName() {
        return readStringRequired(KEY_NAME);
    }

    @Override
    public @Nullable String getElementValue() {
        return readString(KEY_VALUE);
    }

    @Override
    public void setElementValue(@Nullable String value) {
        ensureEntry(KEY_VALUE, value);
    }

    @Override
    public @NotNull Set<String> getChildNames() {
        JsonObject jsonObject = ensureJsonObject(KEY_CHILDREN);
        return Collections.unmodifiableSet(jsonObject.getMap().keySet());
    }

    @Override
    public @NotNull ConfigElement ensureChild(@NotNull String childName) {
        JsonObject children = ensureJsonObject(KEY_CHILDREN);
        JsonObject x = children.getJsonObject(childName);
        if (x == null) {
            var y = new ConfigElementImpl(childName);
            x = y.toJsonObject();
            children.put(childName, x);
            return y;
        } else {
            return new ConfigElementImpl(x);
        }
    }

    @Override
    public void addChild(@NotNull ConfigElement child) {
        JsonObject children = ensureJsonObject(KEY_CHILDREN);
        children.put(child.getElementName(), child.toJsonObject());
    }

    @Override
    public void removeChild(@NotNull String childName) {
        JsonObject children = ensureJsonObject(KEY_CHILDREN);
        children.remove(childName);
    }

    @Override
    public @Nullable ConfigElement getChild(@NotNull String childName) {
        JsonObject children = ensureJsonObject(KEY_CHILDREN);
        JsonObject j = children.getJsonObject(childName);
        if (j == null)
            return null;
        return new ConfigElementImpl(j);
    }

    @Override
    public void reloadData(@NotNull Properties properties) {
        var children = ensureJsonObject(KEY_CHILDREN);
        children.clear();
        properties.forEach((k, v) -> {
            String fullKey = k.toString();
            String stringifiedValue = v == null ? null : v.toString();
            String[] keyArray = fullKey.split("\\.");
            if (keyArray.length > 0) {
                JsonObject existed = children.getJsonObject(keyArray[0]);
                ConfigElement e;
                if (existed == null) {
                    e = new ConfigElementImpl(keyArray[0]);
                    children.put(keyArray[0], e.toJsonObject());
                } else {
                    e = new ConfigElementImpl(existed);
                }
                if (keyArray.length == 1) {
                    e.setElementValue(stringifiedValue);
                } else {
                    for (int i = 1; i < keyArray.length; i++) {
                        String key = keyArray[i];
                        e = e.ensureChild(key);
                        if (i == keyArray.length - 1) {
                            e.setElementValue(stringifiedValue);
                        }
                    }
                }
            }
        });
    }

    @Override
    public @Nullable ConfigElement extract(@NotNull String... keychain) {
        return extract(List.of(keychain));
    }

    @Override
    public @Nullable ConfigElement extract(@NotNull List<@NotNull String> path) {
        ConfigElement configElement = this;
        for (String key : path) {
            configElement = configElement.getChild(key);
            if (configElement == null) {
                return null;
            }
        }
        return configElement;
    }

    /**
     * 将本配置节点的各有效配置项转化为配置项列表。
     *
     * @return 配置项 {@link ConfigProperty} 列表
     */
    @Override
    public @NotNull List<@NotNull ConfigProperty> transformChildrenToPropertyList() {
        List<@NotNull ConfigProperty> properties = new ArrayList<>();
        // 为了输出稳定，按字典序遍历同级子节点
        List<String> keys = new ArrayList<>(this.getChildNames());
        Collections.sort(keys);
        for (String key : keys) {
            ConfigElement child = this.getChild(key);
            if (child != null) {
                dfsTransform(child, new ArrayList<>(List.of(key)), properties);
            }
        }
        return properties;
    }

    /**
     * 以给定的配置节点为根节点，执行深度优先搜索，对具有非空值的配置项构建清单，均以字典序编列。
     *
     * @param node 给定的配置节点
     * @param path 给定的配置节点在完整的配置树中的路径，自根节点到当前节点
     * @param out  通过遍历收集到的配置项将加入到这个 {@link ConfigProperty} 列表中
     */
    private void dfsTransform(
            @NotNull ConfigElement node,
            @NotNull List<String> path,
            @NotNull List<@NotNull ConfigProperty> out) {
        // 当前节点若有值，则输出一条属性
        if (node.getElementValue() != null) {
            out.add(new ConfigProperty()
                    .setKeychain(path)
                    .setValue(node.getElementValue()));
        }
        // 继续遍历子节点
        if (!node.getChildNames().isEmpty()) {
            List<String> keys = new ArrayList<>(node.getChildNames());
            Collections.sort(keys);
            for (String k : keys) {
                ConfigElement child = node.getChild(k);
                if (child != null) {
                    List<String> nextPath = new ArrayList<>(path);
                    nextPath.add(k);
                    dfsTransform(child, nextPath, out);
                }
            }
        }
    }
}
