package io.github.sinri.keel.base.configuration;

import org.jetbrains.annotations.Nullable;

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
public class ConfigPropertiesBuilder {
    private List<ConfigProperty> configPropertyList;
    private @Nullable List<String> prefix;

    public ConfigPropertiesBuilder() {
        this.configPropertyList = new ArrayList<>();
    }

    /**
     * 所提供的 prefix 仅与 {@link ConfigPropertiesBuilder#add(List, String)} 配合使用时对keychain生效。
     */
    public final ConfigPropertiesBuilder setPrefix(@Nullable List<String> prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 所提供的 prefix 仅与 {@link ConfigPropertiesBuilder#add(List, String)} 配合使用时对keychain生效。
     */
    public final ConfigPropertiesBuilder setPrefix(String... prefix) {
        return this.setPrefix(List.of(prefix));
    }

    /**
     * 当通过 {@link ConfigPropertiesBuilder#setPrefix(List)} 给定非空的前缀时，keychain将自动持有该前缀。
     */
    public final ConfigPropertiesBuilder add(List<String> keychain, String value) {
        ArrayList<String> k = new ArrayList<>();
        if (prefix != null) {
            k.addAll(prefix);
        }
        k.addAll(keychain);
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setKeychain(k);
        configProperty.setValue(value);
        return add(configProperty);
    }

    /**
     * 当通过 {@link ConfigPropertiesBuilder#setPrefix(List)} 给定非空的前缀时，keychain将自动持有该前缀。
     *
     * @param keychainAsSingleString 用于构成单一元素的keychain
     */
    public final ConfigPropertiesBuilder add(String keychainAsSingleString, String value) {
        return add(List.of(keychainAsSingleString), value);
    }

    /**
     * 本方法不受通过 {@link ConfigPropertiesBuilder#setPrefix(List)} 给定的前缀影响。
     */
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
