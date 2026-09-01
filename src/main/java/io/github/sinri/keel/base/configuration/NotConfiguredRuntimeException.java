package io.github.sinri.keel.base.configuration;

import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 配置未配置异常。
 * <p>
 * 当尝试读取不存在的配置项时抛出此异常。
 *
 * @since 5.0.6
 */
@NullMarked
public class NotConfiguredRuntimeException extends RuntimeException {
    /**
     * 使用指定的键链构造异常。
     *
     * @param keychain 未配置的配置项键链
     */
    public NotConfiguredRuntimeException(List<String> keychain) {
        super("Provided Keychain Not Configured: " + String.join(".", keychain));
    }

    /**
     * 使用指定的父键链和键构造异常。
     *
     * @param parentKeychain 父配置项键链
     * @param key            未配置的配置项键
     */
    public NotConfiguredRuntimeException(List<String> parentKeychain, String key) {
        this(ConfigElement.mergeListOfKeys(parentKeychain, key));
    }

    /**
     * @since 5.0.6
     */
    public NotConfiguredException toCheckedException() {
        return new NotConfiguredException(this);
    }
}
