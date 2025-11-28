package io.github.sinri.keel.base.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 配置树。
 * <p>
 * 配置树是配置节点的根节点，提供了便捷的读取方法，支持通过键链（keychain）读取不同类型的配置值。
 * <p>
 * 如果配置不存在或无法解析，将抛出 {@link NotConfiguredException} 异常。
 *
 * @since 5.0.0
 */
public class ConfigTree extends ConfigElement {
    /**
     * 构造一个空的配置树。
     */
    public ConfigTree() {
        super("");
    }

    /**
     * 从现有的配置节点构造配置树。
     *
     * @param configElement 作为根节点的配置节点
     */
    public ConfigTree(@NotNull ConfigElement configElement) {
        super(configElement);
    }

    /**
     * 通过键链读取字符串配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的字符串值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    @NotNull
    public String readString(@NotNull List<String> keychain) throws NotConfiguredException {
        var x = extract(keychain);
        if (x == null) {
            throw new NotConfiguredException(keychain);
        }
        String valueAsString = x.getValueAsString();
        if (valueAsString == null) {
            throw new NotConfiguredException(keychain);
        }
        return valueAsString;
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
    public boolean readBoolean(@NotNull List<String> keychain) throws NotConfiguredException {
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
    public int readInteger(@NotNull List<String> keychain) throws NotConfiguredException {
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
    public long readLong(@NotNull List<String> keychain) throws NotConfiguredException {
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
    public float readFloat(@NotNull List<String> keychain) throws NotConfiguredException {
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
    public double readDouble(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Double.parseDouble(value);
    }

    /**
     * 配置未配置异常。
     * <p>
     * 当尝试读取不存在的配置项时抛出此异常。
     */
    public static class NotConfiguredException extends Exception {
        /**
         * 使用指定的键链构造异常。
         *
         * @param keychain 未配置的配置项键链
         */
        public NotConfiguredException(@NotNull List<String> keychain) {
            super("Provided Keychain Not Configured: " + String.join(".", keychain));
        }
    }
}
