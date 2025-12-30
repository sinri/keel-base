package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigElement {
    private final @NotNull Map<String, ConfigElement> children;
    private final @NotNull String elementName;
    private @NotNull String elementValue = "";

    public ConfigElement(@NotNull String elementName) {
        this.elementName = elementName;
        this.elementValue = "";
        this.children = new ConcurrentHashMap<>();
    }

    public ConfigElement(@NotNull ConfigElement another) {
        this.elementName = another.elementName;
        this.elementValue = another.elementValue;
        this.children = another.children;
    }

    @NotNull
    public static Properties loadLocalPropertiesFile(@NotNull String propertiesFileName, @NotNull Charset charset) throws IOException {
        File file = new File(propertiesFileName);
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(file, charset));
        } catch (IOException e) {
            StdoutLoggerFactory.getInstance().createLogger(ConfigElement.class.getName())
                               .warning("Cannot read the file %s. Use the embedded one.".formatted(file.getAbsolutePath()));
            InputStream resourceAsStream = ConfigElement.class.getClassLoader().getResourceAsStream(propertiesFileName);
            if (resourceAsStream == null) {
                // if the embedded file is not found, throw an IOException
                throw new IOException("The embedding properties file is not found.");
            }
            properties.load(resourceAsStream);
        }
        return properties;
    }

    public void loadPropertiesFile(@NotNull String propertiesFileName) throws IOException {
        var x = loadLocalPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
        this.loadData(x);
    }

    /**
     * 获取配置节点的名称
     *
     * @return 当前配置节点的配置子项名称
     */
    public @NotNull String getElementName() {
        return this.elementName;
    }

    private boolean isLeafNode() {
        return children.isEmpty();
    }

    /**
     * 获取配置节点的值
     *
     * @return 当前配置节点的配置子项值，可以为 null。
     */
    public @NotNull String getElementValue() throws NotConfiguredException {
        if (!isLeafNode()) {
            throw new NotConfiguredException(List.of(elementName));
        }
        return elementValue;
    }

    /**
     * 设置配置节点的值
     *
     * @param value 设置当前配置节点的配置子项值
     */
    public void setElementValue(@NotNull String value) {
        this.elementValue = value;
    }

    /**
     * 获取配置节点的子项名称集合
     *
     * @return 当前配置节点的配置子项名称集合
     */
    public @NotNull Set<String> getChildNames() {
        return Collections.unmodifiableSet(children.keySet());
    }

    /**
     * 获取指定名称对应的配置节点的子项，如果不存在则创建。
     * <p>
     * 本方法确保当前配置节点在方法执行后拥有指定名称的子节点。
     *
     * @param childName 子节点的名称
     * @return 子节点，可能为新创建的或已存在的，不为 null
     */
    public @NotNull ConfigElement ensureChild(@NotNull String childName) {
        return children.computeIfAbsent(childName, ConfigElement::new);
    }

    /**
     * 创建指定名称对应的配置节点的子项
     *
     * @param child 子节点
     */
    public void addChild(@NotNull ConfigElement child) {
        this.children.put(child.getElementName(), child);
    }

    /**
     * 移除指定名称对应的配置节点的子项
     *
     * @param childName 子节点的名称
     */
    public void removeChild(@NotNull String childName) {
        this.children.remove(childName);
    }

    /**
     * 获取指定名称对应的配置节点的子项
     *
     * @param childName 子节点的名称
     * @return 子节点，如果不存在就返回 null
     */
    public @Nullable ConfigElement getChild(@NotNull String childName) {
        return children.get(childName);
    }

    /**
     * 从 Properties 对象中重新加载配置数据以完全更新所有子项。
     *
     * @param properties Properties 对象，包含配置数据
     */
    public void loadData(@NotNull Properties properties) {
        properties.forEach((k, v) -> {
            // System.out.println(k + "->" + v);
            if (k == null || v == null) return;
            String fullKey = k.toString();
            String[] keyArray = fullKey.split("\\.");
            if (keyArray.length > 0) {
                ConfigElement configElement = children.computeIfAbsent(
                        keyArray[0],
                        x -> new ConfigElement(keyArray[0]));
                if (keyArray.length == 1) {
                    configElement.setElementValue(v.toString());
                } else {
                    for (int i = 1; i < keyArray.length; i++) {
                        String key = keyArray[i];
                        configElement = configElement.ensureChild(key);
                        if (i == keyArray.length - 1) {
                            configElement.setElementValue(v.toString());
                        }
                    }
                }
            }
        });
    }

    /**
     * 根据指定的键链，层层抽取出以配置子项为根的配置节点。
     *
     * @param keychain 键链，表示要提取的子项路径
     * @return 提取的子项，如果不存在则返回 null
     */
    public @Nullable ConfigElement extract(@NotNull String... keychain) {
        return extract(java.util.Arrays.asList(keychain));
    }

    /**
     * 根据指定的键链，层层抽取出以配置子项为根的配置节点。
     *
     * @param keychain 键链，表示要提取的子项路径
     * @return 提取的子项，如果不存在则返回 null
     */
    public @Nullable ConfigElement extract(@NotNull List<@NotNull String> keychain) {
        ConfigElement configElement = this;
        for (String key : keychain) {
            configElement = configElement.getChild(key);
            if (configElement == null) {
                return null;
            }
        }
        return configElement;
    }

    /**
     * 将当前配置节点及其子节点转换为配置属性列表。
     *
     * @return 配置属性列表
     */
    public @NotNull List<@NotNull ConfigProperty> transformChildrenToPropertyList() {
        List<@NotNull ConfigProperty> properties = new ArrayList<>();
        // 为了输出稳定，按字典序遍历同级子节点
        List<String> keys = new ArrayList<>(this.getChildNames());
        Collections.sort(keys);
        for (String key : keys) {
            // System.out.println("transformChildrenToPropertyList for " + key);
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
            @NotNull List<@NotNull ConfigProperty> out
    ) {
        // System.out.println("dfsTransform on " + path);
        // 当前节点若有值，则输出一条属性
        if (node.isLeafNode()) {
            // System.out.println("dfsTransform add " + path);
            out.add(new ConfigProperty()
                    .setKeychain(path)
                    .setValue(node.elementValue));
        }
        // 继续遍历子节点
        List<String> keys = new ArrayList<>(node.getChildNames());
        Collections.sort(keys);
        for (String k : keys) {
            // System.out.println("dfsTransform for " + k);
            ConfigElement child = node.getChild(k);
            if (child != null) {
                List<String> nextPath = new ArrayList<>(path);
                nextPath.add(k);
                dfsTransform(child, nextPath, out);
            }
        }
    }

    /**
     * 通过键链读取字符串配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的字符串值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    public @NotNull String readString(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        ConfigElement extract = extract(keychain);

        if (extract != null && extract.isLeafNode()) {
            return extract.getElementValue();
        } else {
            throw new NotConfiguredException(keychain);
        }
    }

    /**
     * 通过键链读取布尔配置值。
     * <p>
     * 字符串值 "YES" 或 "TRUE"（不区分大小写）将被解析为 true，其他值将被解析为 false。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的布尔值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    public boolean readBoolean(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }

    /**
     * 通过键链读取整数配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的整数值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     * @throws NumberFormatException  如果字符串无法解析为整数
     */
    public int readInteger(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Integer.parseInt(value);
    }

    /**
     * 通过键链读取长整数配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的长整数值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     * @throws NumberFormatException  如果字符串无法解析为长整数
     */
    public long readLong(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Long.parseLong(value);
    }

    /**
     * 通过键链读取浮点数配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的浮点数值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     * @throws NumberFormatException  如果字符串无法解析为浮点数
     */
    public float readFloat(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Float.parseFloat(value);
    }

    /**
     * 通过键链读取双精度浮点数配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的双精度浮点数值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     * @throws NumberFormatException  如果字符串无法解析为双精度浮点数
     */
    public double readDouble(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Double.parseDouble(value);
    }

    public @Nullable String readString(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        try {
            return readString(List.of(split));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    String debugToString(int level) {
        StringBuilder sb = new StringBuilder("ConfigElement(" + elementName + ": `" + elementValue + "` :" + children.size() + ")");
        if (isLeafNode()) {
            return sb.toString();
        }
        for (String childName : children.keySet()) {
            sb.append("\n");
            sb.append("\t".repeat(Math.max(0, level)));
            sb.append("- ");
            sb.append(children.get(childName).debugToString(level + 1));
        }
        return sb.toString();
    }
}
