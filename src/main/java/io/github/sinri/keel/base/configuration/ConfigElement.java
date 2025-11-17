package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 配置节点。
 * <p>
 * 配置节点是树形结构中的一个节点，可以包含子节点和值。
 *
 * @since 5.0.0
 */
public class ConfigElement implements JsonObjectConvertible {
    /**
     * 配置节点的名称
     */
    @NotNull
    private final String name;
    /**
     * 配置节点的子节点，以子节点名称为键的一个 Map。
     */
    @NotNull
    private final Map<String, ConfigElement> children;
    /**
     * 配置节点的值
     */
    @Nullable
    private String value;

    /**
     * 以给定的名称构建一个配置节点。
     *
     * @param name 配置节点的名称
     */
    public ConfigElement(@NotNull String name) {
        this.name = name;
        this.value = null;
        this.children = new ConcurrentHashMap<>();
    }

    /**
     * 复刻一个配置节点。
     * <p>
     * 使用浅拷贝，各字段均与原字段一致。
     *
     * @param another 被复刻的配置节点
     */
    public ConfigElement(@NotNull ConfigElement another) {
        this.name = another.name;
        this.children = another.children;
        this.value = another.value;
    }

    /**
     * 从一个 JSON 对象生成一个配置节点。
     * <p>
     * 给定的 JSON 对象需要有 name、value、children 三个字段；
     * children 应为一个 JSON 数组，其中每个元素为一个 JSON 对象，描述一个子节点。
     *
     * @param jsonObject JSON 对象描述的配置信息
     * @return 生成的配置节点
     */
    public static ConfigElement fromJsonObject(@NotNull JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        Objects.requireNonNull(name, "name should not be null");
        ConfigElement configElement = new ConfigElement(name);
        if (jsonObject.containsKey("value")) {
            Object v = jsonObject.getValue("value");
            configElement.value = (v == null ? null : v.toString());
        }
        JsonArray children = jsonObject.getJsonArray("children");
        children.forEach(child -> {
            if (child instanceof JsonObject) {
                configElement.addChild(fromJsonObject((JsonObject) child));
            } else {
                throw new IllegalArgumentException("Child should be a JSON object");
            }
        });
        return configElement;
    }

    /**
     * 基于 Vert.x Config 提供的能力，获取配置并组装为配置节点对象。
     *
     * @param configRetrieverOptions Vert.x Config 配置
     * @return 异步返回的配置节点对象
     * @see <a href="https://vertx.io/docs/vertx-config/java/">Vert.x Config</a>
     */
    public static Future<ConfigElement> retrieve(@NotNull ConfigRetrieverOptions configRetrieverOptions) {
        ConfigRetriever configRetriever = ConfigRetriever.create(Keel.getVertx(), configRetrieverOptions);
        return configRetriever.getConfig()
                              .compose(jsonObject -> {
                                  ConfigElement element = fromJsonObject(jsonObject);
                                  return Future.succeededFuture(element);
                              })
                              .andThen(ar -> configRetriever.close());
    }

    /**
     * 以给定的配置节点为根节点，执行深度优先搜索，对具有非空值的配置项构建清单，均以字典序编列。
     *
     * @param node 给定的配置节点
     * @param path 给定的配置节点在完整的配置树中的路径，自根节点到当前节点
     * @param out  通过遍历收集到的配置项将加入到这个 {@link ConfigProperty} 列表中
     */
    private static void dfsTransform(@NotNull ConfigElement node,
                                     @NotNull List<String> path,
                                     @NotNull List<ConfigProperty> out) {
        // 当前节点若有值，则输出一条属性
        if (node.value != null) {
            out.add(new ConfigProperty()
                    .setKeychain(path)
                    .setValue(node.value));
        }
        // 继续遍历子节点
        if (!node.children.isEmpty()) {
            List<String> keys = new ArrayList<>(node.children.keySet());
            Collections.sort(keys);
            for (String k : keys) {
                ConfigElement child = node.children.get(k);
                if (child != null) {
                    List<String> nextPath = new ArrayList<>(path);
                    nextPath.add(k);
                    dfsTransform(child, nextPath, out);
                }
            }
        }
    }


    @NotNull
    public String getName() {
        return name;
    }

    /**
     * 获取本配置节点的值。
     *
     * @return 本配置节点的值。
     */
    @Nullable
    public String getValueAsString() {
        return value;
    }

    /**
     * 获取本配置节点的值，并在值为空时返回给定的默认值。
     *
     * @param def 配置为空时使用的默认值
     * @return 本配置节点的值，如果配置为空则返回默认值。
     */
    @Nullable
    public String getValueAsStringElse(@Nullable String def) {
        return Objects.requireNonNullElse(value, def);
    }

    /**
     * 获取本配置节点的值。
     *
     * @return 本配置节点的值。
     */
    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public String readString() {
        return readString(List.of());
    }

    /**
     * 尝试自以本节点为根节点的树形结构，以给定的路径，读取目标配置节点的值。
     * <p>
     * 如果给定的路径无法对应上一个配置节点，则返回 null。
     *
     * @param keychain 查询路径，为空列表时表示停留在本节点，每增加一项即向下搜索一轮。
     * @return 目标配置节点的值，在无法找到时为空
     */
    @Nullable
    public String readString(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsString();
    }

    /**
     * 尝试自以本节点为根节点的树形结构，以给定的路径，读取目标配置节点的值。
     * <p>
     * 如果给定的路径无法对应上一个配置节点，或这个节点，则返回给定的默认值。
     *
     * @param keychain 查询路径，为空列表时表示停留在本节点，每增加一项即向下搜索一轮。
     * @param def      默认值
     * @return 目标配置节点的值，值为空或响应路径无法找到配置节点时为空
     */
    @Nullable
    public String readString(@NotNull List<String> keychain, @Nullable String def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsStringElse(def);
    }

    /**
     * Reads the string value of the configuration element based on the provided
     * keychain.
     *
     * @param keychain the single key to traverse in the configuration hierarchy,
     *                 must not be null
     * @param def      the default value to return if the value of the configuration
     *                 element is not set
     * @return the value of the configuration element as a string, or the provided
     *         default value if the value is not set
     *         or the keychain does not match any child
     */
    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public String readString(@NotNull String keychain, @Nullable String def) {
        return readString(List.of(keychain), def);
    }

    /**
     * 获取本配置节点值，并尝试转换为整数。
     *
     * @return 本配置节点值对应的整数。
     * @throws NumberFormatException 无法转为整数
     */
    @Nullable
    public Integer getValueAsInteger() {
        if (value == null)
            return null;
        return Integer.parseInt(value);
    }

    /**
     * 获取本配置节点值，并尝试转换为整数，如果无法转换就使用默认值。
     *
     * @param def 默认值
     * @return 本配置节点值对应的整数，或默认值。
     */
    public int getValueAsIntegerElse(int def) {
        if (value == null)
            return def;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads the integer value of the configuration element.
     * If no keychain is provided, it attempts to read the value from the current
     * element.
     *
     * @return the integer value of the configuration element, or null if the value
     *         is not set or cannot be parsed as an
     *         integer
     */
    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public Integer readInteger() {
        return readInteger(List.of());
    }


    @Nullable
    public Integer readInteger(@NotNull List<String> keychain) {
        var x = this.extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsInteger();
    }


    public int readInteger(@NotNull List<String> keychain, int def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsIntegerElse(def);
    }


    @Deprecated(since = "5.0.0", forRemoval = true)
    public int readInteger(@NotNull String keychain, int def) {
        return readInteger(List.of(keychain), def);
    }


    @Nullable
    public Long getValueAsLong() {
        if (value == null)
            return null;
        return Long.parseLong(value);
    }


    public long getValueAsLongElse(long def) {
        if (value == null)
            return def;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads a long value from the input source.
     *
     * @return the long value read, or null if the value cannot be read or is not
     *         available
     */
    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public Long readLong() {
        return readLong(List.of());
    }

    @Nullable
    public Long readLong(@NotNull List<String> keychain) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted != null) {
            return extracted.getValueAsLong();
        } else {
            return null;
        }
    }


    public long readLong(@NotNull List<String> keychain, long def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsLongElse(def);
    }

    @Deprecated(since = "5.0.0", forRemoval = true)
    public long readLong(@NotNull String keychain, long def) {
        return readLong(List.of(keychain), def);
    }


    @Nullable
    public Float getValueAsFloat() {
        if (value == null)
            return null;
        return Float.parseFloat(value);
    }


    public float getValueAsFloatElse(float def) {
        if (value == null)
            return def;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }


    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public Float readFloat() {
        return readFloat(List.of());
    }


    @Nullable
    public Float readFloat(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsFloat();
    }


    public float readFloat(@NotNull List<String> keychain, float def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsFloatElse(def);
    }


    @Deprecated(since = "5.0.0", forRemoval = true)
    public float readFloat(@NotNull String keychain, float def) {
        return readFloat(List.of(keychain), def);
    }


    @Nullable
    public Double getValueAsDouble() {
        if (value == null)
            return null;
        return Double.parseDouble(value);
    }


    public double getValueAsDoubleElse(double def) {
        if (value == null)
            return def;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }


    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public Double readDouble() {
        return readDouble(List.of());
    }


    @Nullable
    public Double readDouble(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsDouble();
    }


    public double readDouble(@NotNull List<String> keychain, double def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsDoubleElse(def);
    }

    @Deprecated(since = "5.0.0", forRemoval = true)
    public double readDouble(@NotNull String keychain, double def) {
        return readDouble(List.of(keychain), def);
    }


    @Nullable
    public Boolean getValueAsBoolean() {
        if (value == null)
            return null;
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }


    public boolean getValueAsBooleanElse(boolean def) {
        if (value == null)
            return def;
        return "YES".equalsIgnoreCase(value)
                || "TRUE".equalsIgnoreCase(value)
                || "ON".equalsIgnoreCase(value)
                || "1".equalsIgnoreCase(value);
    }

    @Deprecated(since = "5.0.0", forRemoval = true)
    @Nullable
    public Boolean readBoolean() {
        return readBoolean(List.of());
    }


    @Nullable
    public Boolean readBoolean(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsBoolean();
    }


    public boolean readBoolean(@NotNull List<String> keychain, boolean def) {
        ConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsBooleanElse(def);
    }

    @Deprecated(since = "5.0.0", forRemoval = true)
    public boolean readBoolean(@NotNull String keychain, boolean def) {
        return readBoolean(List.of(keychain), def);
    }

    /**
     * 获取一个给定名称的子配置节点；如果尚不存在，则新建之。
     *
     * @param childName 子配置节点的名称
     * @return 子配置节点
     */
    public ConfigElement ensureChild(@NotNull String childName) {
        return this.children.computeIfAbsent(childName, x -> new ConfigElement(childName));
    }

    /**
     * 向当前配置节点新增一个子配置节点。
     * <p>
     * 如果对应名称的子配置节点已存在，则会直接覆盖之。
     *
     * @param child 子配置节点
     * @return 本配置节点
     */
    public ConfigElement addChild(@NotNull ConfigElement child) {
        this.children.put(child.getName(), child);
        return this;
    }

    /**
     * 移除某个子配置节点。
     *
     * @param child 子配置节点
     * @return 本配置节点
     */
    public ConfigElement removeChild(@NotNull ConfigElement child) {
        this.removeChild(child.getName());
        return this;
    }

    /**
     * 移除某个子配置节点。
     *
     * @param childName 子配置节点的名称
     * @return 本配置节点
     */
    public ConfigElement removeChild(@NotNull String childName) {
        this.children.remove(childName);
        return this;
    }

    /**
     * 设置本配置节点的值
     *
     * @param value 配置节点的值
     * @return 本配置节点
     */
    public ConfigElement setValue(@NotNull String value) {
        this.value = value;
        return this;
    }


    @NotNull
    public Map<String, ConfigElement> getChildren() {
        return children;
    }

    /**
     * 获取本配置节点的子配置节点
     *
     * @param childName 子配置节点的名称
     * @return 子配置节点
     */
    @Nullable
    public ConfigElement getChild(@NotNull String childName) {
        return children.get(childName);
    }

    /**
     *
     * Converts the current object into a JSON representation.
     *
     * @return a JsonObject that represents the current object, including its name,
     *         children, and value if present.
     */
    @Override
    @NotNull
    public JsonObject toJsonObject() {
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
    public String toJsonExpression() {
        return toJsonObject().encode();
    }

    @Override
    public String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    /**
     * 以本节点为根节点，根据路径下钻到子配置节点。
     *
     * @param path 路径。如果为空，表示停留在这个配置节点本身。
     * @return 子配置节点；如果找不到，则返回空。
     */
    public @Nullable ConfigElement extract(@NotNull List<String> path) {
        if (path.isEmpty())
            return this;
        if (path.size() == 1)
            return this.children.get(path.get(0));
        ConfigElement configElement = this.children.get(path.get(0));
        if (configElement == null) {
            return null;
        }
        for (int i = 1; i < path.size(); i++) {
            configElement = configElement.getChild(path.get(i));
            if (configElement == null) {
                return null;
            }
        }
        return configElement;
    }

    /**
     * 以本节点为根节点，根据路径下钻到子配置节点。
     *
     * @param path 路径。如果为空，表示停留在这个配置节点本身。
     * @return 子配置节点；如果找不到，则返回空。
     */
    public @Nullable ConfigElement extract(@NotNull String... path) {
        List<String> list = Arrays.asList(path);
        return this.extract(list);
    }

    /**
     * 通过给定的 {@link Properties} ，更新本配置节点的数据。
     * <p>
     * 通过遍历 {@link Properties} 里的键值对，将键根据其中的 {@code .} 分割为路径进行配置节点生成。
     *
     * @param properties 给定的 {@link Properties} 实例
     * @return 本配置节点
     */
    @NotNull
    public ConfigElement loadProperties(@NotNull Properties properties) {
        properties.forEach((k, v) -> {
            String fullKey = k.toString();
            String[] keyArray = fullKey.split("\\.");
            if (keyArray.length > 0) {
                ConfigElement configElement = children.computeIfAbsent(
                        keyArray[0],
                        x -> new ConfigElement(keyArray[0]));
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

    /**
     * 通过给定的 {@code .properties} 文件名称（路径），以 UTF-8 编码加载并更新本配置节点的数据。
     *
     * @param propertiesFileName {@code .properties} 文件名称（路径）
     * @return 本配置节点
     */
    @NotNull
    public ConfigElement loadPropertiesFile(@NotNull String propertiesFileName) throws IOException {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    /**
     * 通过给定的 {@code .properties} 文件名称（路径），以指定字符编码加载并更新本配置节点的数据。
     *
     * @param propertiesFileName {@code .properties} 文件名称（路径）
     * @param charset            字符编码
     * @return 本配置节点
     */
    @NotNull
    public ConfigElement loadPropertiesFile(@NotNull String propertiesFileName, @NotNull Charset charset)
            throws IOException {
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(propertiesFileName, charset));
        } catch (IOException e) {
            // Keel.getLogger().debug("Cannot read the file config.properties, use the embedded one.");
            System.err.println("[WARNING] Cannot read the file " + propertiesFileName + ". Use the embedded one.");
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
            if (resourceAsStream == null) {
                throw new IOException("The embedding properties file is not found.");
            }
            properties.load(resourceAsStream);
            // if the embedded file is not found, throw an IOException
        }

        return loadProperties(properties);
    }

    /**
     * 通过给定的 {@code .properties} 文件内容，加载并更新本配置节点的数据。
     *
     * @param content {@code .properties} 文件内容
     * @return 本配置节点
     */
    @NotNull
    public ConfigElement loadPropertiesFileContent(@NotNull String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return loadProperties(properties);
    }

    /**
     * 将本配置节点的各有效配置项转化为配置项列表。
     *
     * @return 配置项 {@link ConfigProperty} 列表
     */
    public List<ConfigProperty> transformChildrenToPropertyList() {
        List<ConfigProperty> properties = new ArrayList<>();
        // 为了输出稳定，按字典序遍历同级子节点
        List<String> keys = new ArrayList<>(this.children.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            ConfigElement child = this.children.get(key);
            if (child != null) {
                dfsTransform(child, new ArrayList<>(List.of(key)), properties);
            }
        }
        return properties;
    }

}
