package io.github.sinri.keel.base.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 配置项，对应 {@code .properties} 文件的一行。
 *
 * @since 5.0.0
 */
public class ConfigProperty {
    private final List<String> keychain = new ArrayList<>();
    private @NotNull String value = "";

    public ConfigProperty() {
    }

    public final ConfigProperty setKeychain(@NotNull List<String> keychain) {
        this.keychain.addAll(keychain);
        return this;
    }

    public final ConfigProperty addToKeychain(@NotNull String key) {
        this.keychain.add(key);
        return this;
    }

    public final ConfigProperty setValue(@Nullable String value) {
        this.value = Objects.requireNonNullElse(value, "");
        return this;
    }


    @NotNull
    public String getPropertyName() {
        return String.join(".", keychain);
    }


    @NotNull
    public String getPropertyValue() {
        return value;
    }

    @Override
    public final String toString() {
        return String.join(".", keychain) + "=" + value;
    }
}
