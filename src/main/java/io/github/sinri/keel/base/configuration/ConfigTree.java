package io.github.sinri.keel.base.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigTree extends ConfigElement {
    public ConfigTree() {
        super("");
    }

    public ConfigTree(@NotNull ConfigElement configElement) {
        super(configElement);
    }

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

    @NotNull
    public Boolean readBoolean(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }

    @NotNull
    public Integer readInteger(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Integer.parseInt(value);
    }

    @NotNull
    public Long readLong(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Long.parseLong(value);
    }

    @NotNull
    public Float readFloat(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Float.parseFloat(value);
    }

    @NotNull
    public Double readDouble(@NotNull List<String> keychain) throws NotConfiguredException {
        String value = readString(keychain);
        return Double.parseDouble(value);
    }

    public static class NotConfiguredException extends Exception {
        public NotConfiguredException(@NotNull List<String> keychain) {
            super("Provided Keychain Not Configured: " + String.join(".", keychain));
        }
    }
}
