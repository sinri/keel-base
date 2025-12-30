package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public interface ConfigTree {
    @NotNull
    static ConfigTree wrap(@NotNull ConfigNode configNode) {
        return new ConfigTreeImpl(configNode);
    }

    @NotNull
    static ConfigTree create() {
        return wrap(ConfigNode.create(""));
    }

    @NotNull ConfigTree loadProperties(@NotNull Properties properties);

    default @NotNull ConfigTree loadPropertiesFile(@NotNull String propertiesFileName, @NotNull Charset charset) throws IOException {
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

    default @NotNull ConfigTree loadPropertiesFile(@NotNull String propertiesFileName) throws IOException {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    default @NotNull ConfigTree loadPropertiesFileContent(@NotNull String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return loadProperties(properties);
    }

    @Nullable ConfigTree extract(@NotNull List<@NotNull String> path);

    default @Nullable ConfigTree extract(@NotNull String... path) {
        List<String> list = Arrays.asList(path);
        return this.extract(list);
    }


    /**
     * 通过键链读取字符串配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的字符串值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    @NotNull String readString(@NotNull List<@NotNull String> keychain) throws NotConfiguredException;

    /**
     * 通过键链读取布尔配置值。
     * <p>
     * 字符串值 "YES" 或 "TRUE"（不区分大小写）将被解析为 true，其他值将被解析为 false。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的布尔值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    default boolean readBoolean(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
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
    default int readInteger(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
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
    default long readLong(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
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
    default float readFloat(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
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
    default double readDouble(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Double.parseDouble(value);
    }

}
