package io.github.sinri.keel.base.utils.value;


import org.jetbrains.annotations.Nullable;

/**
 * @since 4.1.1
 */
public interface ValueEnvelope {
    @Nullable
    String encrypt(@Nullable String raw);

    @Nullable
    String decrypt(@Nullable String decrypted);
}
