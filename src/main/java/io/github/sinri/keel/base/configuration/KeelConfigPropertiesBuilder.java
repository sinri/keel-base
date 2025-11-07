package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.KeelBase;
import io.vertx.core.Future;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @since 4.1.0
 */
public class KeelConfigPropertiesBuilder {
    private List<KeelConfigProperty> configPropertyList;
    private @Nullable List<String> prefix;

    public KeelConfigPropertiesBuilder() {
        this.configPropertyList = new ArrayList<>();
    }

    /**
     * 所提供的 prefix 仅与 {@link KeelConfigPropertiesBuilder#add(List, String)} 配合使用时对keychain生效。
     */
    public final KeelConfigPropertiesBuilder setPrefix(@Nullable List<String> prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * 所提供的 prefix 仅与 {@link KeelConfigPropertiesBuilder#add(List, String)} 配合使用时对keychain生效。
     */
    public final KeelConfigPropertiesBuilder setPrefix(String... prefix) {
        return this.setPrefix(List.of(prefix));
    }

    /**
     * 当通过 {@link KeelConfigPropertiesBuilder#setPrefix(List)} 给定非空的前缀时，keychain将自动持有该前缀。
     */
    public final KeelConfigPropertiesBuilder add(List<String> keychain, String value) {
        ArrayList<String> k = new ArrayList<>();
        if (prefix != null) {
            k.addAll(prefix);
        }
        k.addAll(keychain);
        KeelConfigProperty keelConfigProperty = new KeelConfigProperty();
        keelConfigProperty.setKeychain(k);
        keelConfigProperty.setValue(value);
        return add(keelConfigProperty);
    }

    /**
     * 当通过 {@link KeelConfigPropertiesBuilder#setPrefix(List)} 给定非空的前缀时，keychain将自动持有该前缀。
     *
     * @param keychainAsSingleString 用于构成单一元素的keychain
     */
    public final KeelConfigPropertiesBuilder add(String keychainAsSingleString, String value) {
        return add(List.of(keychainAsSingleString), value);
    }

    /**
     * 本方法不受通过 {@link KeelConfigPropertiesBuilder#setPrefix(List)} 给定的前缀影响。
     */
    protected final KeelConfigPropertiesBuilder add(KeelConfigProperty configProperty) {
        this.configPropertyList.add(configProperty);
        return this;
    }

    public final KeelConfigPropertiesBuilder setConfigPropertyList(List<KeelConfigProperty> configPropertyList) {
        this.configPropertyList = configPropertyList;
        return this;
    }

    public String writeToString() {
        if (configPropertyList.isEmpty()) {
            return "";
        }
        List<String> collect = configPropertyList.stream()
                                                 .map(KeelConfigProperty::toString)
                                                 .collect(Collectors.toList());
        return String.join("\n", collect);
    }

    public Future<Void> writeToFile(String filePath) {
        return KeelBase.getVertx()
                       .fileSystem()
                       .writeFile(
                                      filePath,
                                      io.vertx.core.buffer.Buffer.buffer(writeToString().getBytes(StandardCharsets.US_ASCII))
                              );
    }

    public Future<Void> appendToFile(String filePath) {
        return KeelBase.getVertx().fileSystem()
                       .open(filePath, new io.vertx.core.file.OpenOptions().setAppend(true))
                       .compose(file -> file.write(io.vertx.core.buffer.Buffer.buffer(writeToString().getBytes(StandardCharsets.US_ASCII)))
                                                   .compose(v -> file.close())
                                                   .onFailure(err -> file.close())); // Ensure file is closed even on failure

    }
}
