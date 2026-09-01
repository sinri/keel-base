package io.github.sinri.keel.base.configuration;

import java.util.List;

public class NotConfiguredException extends Exception {
    /**
     * 使用指定的键链构造异常。
     *
     * @param keychain 未配置的配置项键链
     * @deprecated 自 5.0.6 起，请使用 {@link NotConfiguredRuntimeException}
     */
    @Deprecated(since = "5.0.6")
    public NotConfiguredException(List<String> keychain) {
        super("Provided Keychain Not Configured: " + String.join(".", keychain));
    }

    /**
     * 使用指定的父键链和键构造异常。
     *
     * @param parentKeychain 父配置项键链
     * @param key            未配置的配置项键
     * @deprecated 自 5.0.6 起，请使用 {@link NotConfiguredRuntimeException}
     */
    @Deprecated(since = "5.0.6")
    public NotConfiguredException(List<String> parentKeychain, String key) {
        this(ConfigElement.mergeListOfKeys(parentKeychain, key));
    }

    NotConfiguredException(NotConfiguredRuntimeException exception) {
        super(exception.getMessage());
    }
}
