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
    private final @NotNull List<String> keychain = new ArrayList<>();
    private @NotNull String value = "";

    public ConfigProperty() {
    }

    public final @NotNull ConfigProperty setKeychain(@NotNull List<String> keychain) {
        this.keychain.addAll(keychain);
        return this;
    }

    public final @NotNull ConfigProperty addToKeychain(@NotNull String key) {
        this.keychain.add(key);
        return this;
    }

    public final @NotNull ConfigProperty setValue(@Nullable String value) {
        this.value = Objects.requireNonNullElse(value, "");
        return this;
    }

    public @NotNull String getPropertyName() {
        return String.join(".", keychain);
    }

    public @NotNull String getPropertyValue() {
        return value;
    }

    @Override
    public final @NotNull String toString() {
        return String.join(".", keychain) + "=" + value;
    }
}
