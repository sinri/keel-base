package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.internal.async.KeelAsyncMixin;
import io.github.sinri.keel.base.internal.async.KeelImpl;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

/**
 * It is to work as {@link Vertx} in Keel Framework.
 */
@NullMarked
public interface Keel extends KeelAsyncMixin {

    static Keel create(Vertx vertx) {
        return new KeelImpl(vertx);
    }

    static Keel shared() {
        return KeelImpl.shared();
    }

    static void share(Keel keel) {
        KeelImpl.share(keel);
    }

    static void share(Vertx vertx) {
        KeelImpl.share(vertx);
    }

    static Keel ensureShared(Supplier<Keel> supplier) {
        return KeelImpl.ensureShared(supplier);
    }

}
