package io.github.sinri.keel.base.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 4.1.0
 */
public class KeelConfigProperty {
    private final List<String> keychain = new ArrayList<>();
    private @Nonnull String value = "";

    public KeelConfigProperty() {
    }

    public final KeelConfigProperty setKeychain(@Nonnull List<String> keychain) {
        this.keychain.addAll(keychain);
        return this;
    }

    public final KeelConfigProperty addToKeychain(@Nonnull String key) {
        this.keychain.add(key);
        return this;
    }

    public final KeelConfigProperty setValue(@Nullable String value) {
        this.value = Objects.requireNonNullElse(value, "");
        return this;
    }

    /**
     * /**
     * Returns the property name represented by this configuration property.
     * <p>
     * The property name is constructed by joining all elements of the keychain with a dot ('.').
     * <p>
     * For example, if the keychain is ["database", "url"], the property name will be "database.url".
     *
     * @return the dot-separated property name
     * @since 4.1.1
     */
    @Nonnull
    public String getPropertyName() {
        return String.join(".", keychain);
    }

    /**
     * /**
     * Returns the value associated with this configuration property.
     * <p>
     * If the value was not set, an empty string is returned.
     *
     * @return the property value as a string, never null
     * @since 4.1.1
     */
    @Nonnull
    public String getPropertyValue() {
        return value;
    }

    @Override
    public final String toString() {
        return String.join(".", keychain) + "=" + value;
    }
}
