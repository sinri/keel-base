package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.base.json.JsonObjectReloadable;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public interface ConfigNode extends JsonObjectConvertible, JsonObjectReloadable {
    @NotNull
    static ConfigNode create(@NotNull String name) {
        return new ConfigNodeImpl(name);
    }

    @NotNull
    static ConfigNode create(@NotNull ConfigNode another) {
        return new ConfigNodeImpl(another);
    }

    /**
     * 从一个 JSON 对象生成一个配置节点。
     * <p>
     * 给定的 JSON 对象需要有 name、value、children 三个字段；
     * children 应为一个 JSON 数组，其中每个元素为一个 JSON 对象，描述一个子节点。
     * <p>
     * 曾用名：fromJsonObject
     *
     * @param jsonObject JSON 对象描述的配置信息
     * @return 生成的配置节点
     * @throws IllegalArgumentException 给定的 JSON 对象格式不正确
     */
    static @NotNull ConfigNode decodeFromJsonObject(@NotNull JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        Objects.requireNonNull(name, "name should not be null");
        ConfigNode configElement = ConfigNode.create(name);
        if (jsonObject.containsKey("value")) {
            Object v = jsonObject.getValue("value");
            configElement.setValue((v == null ? null : v.toString()));
        }
        JsonArray children = jsonObject.getJsonArray("children");
        children.forEach(child -> {
            if (child instanceof JsonObject x) {
                configElement.addChild(decodeFromJsonObject(x));
            } else {
                throw new IllegalArgumentException("Child should be a JSON object");
            }
        });
        return configElement;
    }

    @NotNull String getName();

    @Nullable String getValue();

    @NotNull ConfigNode setValue(@Nullable String value);

    @NotNull Map<String, ConfigNode> getChildren();

    @NotNull ConfigNode ensureChild(@NotNull String childName);

    @NotNull ConfigNode addChild(@NotNull ConfigNode child);

    @NotNull ConfigNode removeChild(@NotNull String childName);

    @Nullable ConfigNode getChild(@NotNull String childName);

    @NotNull ConfigNode loadProperties(@NotNull Properties properties);

    default @NotNull ConfigNode loadPropertiesFile(@NotNull String propertiesFileName, @NotNull Charset charset) throws IOException {
        File file = new File(propertiesFileName);
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(file, charset));
        } catch (IOException e) {
            StdoutLoggerFactory.getInstance().createLogger(getClass().getName())
                               .warning("Cannot read the file %s. Use the embedded one.".formatted(file.getAbsolutePath()));
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
            if (resourceAsStream == null) {
                // if the embedded file is not found, throw an IOException
                throw new IOException("The embedding properties file is not found.");
            }
            properties.load(resourceAsStream);
        }

        return loadProperties(properties);
    }

    default @NotNull ConfigNode loadPropertiesFile(@NotNull String propertiesFileName) throws IOException {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    default @NotNull ConfigNode loadPropertiesFileContent(@NotNull String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return loadProperties(properties);
    }
}
