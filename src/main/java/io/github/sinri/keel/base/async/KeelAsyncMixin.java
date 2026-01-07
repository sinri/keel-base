package io.github.sinri.keel.base.async;

import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * Keel 异步能力调用的总成接口。
 *
 * @since 5.0.0
 */
@NullMarked
public interface KeelAsyncMixin extends KeelAsyncMixinParallel, KeelAsyncMixinLock, KeelAsyncMixinBlock {
    static KeelAsyncMixin wrap(Vertx vertx) {
        return () -> vertx;
    }
}
