package io.github.sinri.keel.base.configuration;

import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 配置未配置异常。
 * <p>
 * 当尝试读取不存在的配置项时抛出此异常。
 */
@NullMarked
public class NotConfiguredException extends Exception {
    /**
     * 使用指定的键链构造异常。
     *
     * @param keychain 未配置的配置项键链
     */
    public NotConfiguredException(List<String> keychain) {
        super("Provided Keychain Not Configured: " + String.join(".", keychain));
    }

    /**
     * 使用指定的键链构造异常。
     *
     */
    public NotConfiguredException(List<String> parentKeychain, String key) {
        this(ConfigElement.mergeListOfKeys(parentKeychain, key));
    }


}
