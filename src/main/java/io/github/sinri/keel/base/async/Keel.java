package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.internal.async.KeelImpl;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

/**
 * Keel 体系下的异步能力入口。
 * <p>
 * 本接口用于在 Keel 体系中以 {@link Vertx} 的方式组织与扩展异步能力。
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
