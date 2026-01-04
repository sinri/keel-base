package io.github.sinri.keel.base.configuration;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * {@code .properties} 格式的配置文件编写器。
 *
 * @since 5.0.0
 */
@NullMarked
public class ConfigPropertiesBuilder {
    private List<ConfigProperty> configPropertyList;

    public ConfigPropertiesBuilder() {
        this.configPropertyList = new ArrayList<>();
    }

    /**
     * 追加一个 .properties 格式下的配置行，即以{@code .}分隔的一组字符串构成的 key 和对应的 value。
     *
     * @param prefix   配置键的公共前缀部分
     * @param keychain 配置键的独立部分
     * @param value    配置值
     * @return 当前实例
     */
    public final ConfigPropertiesBuilder add(List<String> prefix, List<String> keychain, String value) {
        ArrayList<String> k = new ArrayList<>();
        k.addAll(prefix);
        k.addAll(keychain);
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setKeychain(k);
        configProperty.setValue(value);
        return add(configProperty);
    }

    /**
     * 如 {@link ConfigPropertiesBuilder#add(List, List, String)}，但默认无公共前缀。
     *
     * @param keychain 配置键
     * @param value    配置值
     * @return 当前实例
     */
    public final ConfigPropertiesBuilder add(List<String> keychain, String value) {
        return add(List.of(), keychain, value);
    }

    /**
     * 如 {@link ConfigPropertiesBuilder#add(List, List, String)}，但默认无公共前缀。
     *
     * @param prefix                 配置键的公共前缀部分
     * @param keychainAsSingleString 配置键，单个字符串
     * @param value                  配置值
     * @return 当前实例
     */
    public final ConfigPropertiesBuilder add(List<String> prefix, String keychainAsSingleString, String value) {
        return add(prefix, List.of(keychainAsSingleString), value);
    }

    /**
     * 如 {@link ConfigPropertiesBuilder#add(List, List, String)}，但默认无公共前缀。
     *
     * @param keychainAsSingleString 配置键，单个字符串
     * @param value                  配置值
     * @return 当前实例
     */
    public final ConfigPropertiesBuilder add(String keychainAsSingleString, String value) {
        return add(List.of(keychainAsSingleString), value);
    }

    protected final ConfigPropertiesBuilder add(ConfigProperty configProperty) {
        this.configPropertyList.add(configProperty);
        return this;
    }

    public final ConfigPropertiesBuilder setConfigPropertyList(List<ConfigProperty> configPropertyList) {
        this.configPropertyList = configPropertyList;
        return this;
    }

    public String writeToString() {
        if (configPropertyList.isEmpty()) {
            return "";
        }
        List<String> collect = configPropertyList.stream()
                                                 .map(ConfigProperty::toString)
                                                 .collect(Collectors.toList());
        return String.join("\n", collect);
    }

    public void writeToFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(
                path,
                writeToString(), StandardCharsets.US_ASCII
        );
    }

    public void appendToFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(
                path,
                writeToString(), StandardCharsets.US_ASCII,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}
