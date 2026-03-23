package io.github.sinri.keel.base.internal.async;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.VertxWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Keel 体系中 {@link Keel} 的默认实现。
 * <p>
 * 本实现基于 {@link VertxWrapper} 对 Vert.x 的 {@link Vertx} 能力进行包装，并提供全局共享 Keel 实例的存取能力。
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelImpl extends VertxWrapper implements Keel {
    private static final AtomicReference<@Nullable Keel> sharedKeelRef = new AtomicReference<>();

    public KeelImpl(Vertx vertx) {
        this((VertxInternal) vertx);
    }

    public KeelImpl(VertxInternal delegate) {
        super(delegate);
    }

    public static Keel shared() {
        Keel keel = sharedKeelRef.get();
        if (keel == null) {
            throw new IllegalStateException("Shared Keel has not been initialized yet");
        }
        return keel;
    }

    public static void share(Keel keel) {
        sharedKeelRef.set(keel);
    }

    public static void share(Vertx vertx) {
        if (vertx instanceof Keel keel) {
            sharedKeelRef.set(keel);
        } else {
            sharedKeelRef.set(new KeelImpl(vertx));
        }
    }

    public static Keel ensureShared(Supplier<Keel> supplier) {
        Keel existing = sharedKeelRef.get();
        if (existing != null) {
            return existing;
        }
        Keel created = supplier.get();
        if (sharedKeelRef.compareAndSet(null, created)) {
            return created;
        }
        return sharedKeelRef.get();
    }
}
