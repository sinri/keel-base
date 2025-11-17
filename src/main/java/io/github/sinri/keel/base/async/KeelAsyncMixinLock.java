package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;


/**
 * 异步独占运行机制。
 *
 * @since 5.0.0
 */
interface KeelAsyncMixinLock extends KeelAsyncMixinCore {
    /**
     * 在Vertx 的锁机制下，独占运行一段异步逻辑。
     * <p>
     * 尝试在制定时间限制内获取锁，并运行异步逻辑，执行完毕后释放锁。
     *
     * @param <T>               异步逻辑的返回值类型
     * @param lockName          锁名称
     * @param waitTimeForLock   最长锁等待时间，以毫秒计
     * @param exclusiveSupplier 需要独占运行的异步逻辑
     * @return 异步逻辑的结果；如果锁获取失败，则会异步返回相应失败。
     */
    default <T> Future<T> asyncCallExclusively(@NotNull String lockName, long waitTimeForLock,
                                               @NotNull Supplier<Future<T>> exclusiveSupplier) {
        return getVertx().sharedData()
                         .getLockWithTimeout(lockName, waitTimeForLock)
                         .compose(lock -> Future.succeededFuture()
                                                .compose(v -> exclusiveSupplier.get())
                                                .andThen(ar -> lock.release()));
    }

    /**
     * 在Vertx 的锁机制下，独占运行一段异步逻辑。
     * <p>
     * 和 {@link KeelAsyncMixinLock#asyncCallExclusively(String, long, Supplier)} 逻辑一致，锁等待时间默认 1 秒。
     */
    default <T> Future<T> asyncCallExclusively(@NotNull String lockName,
                                               @NotNull Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(lockName, 1_000L, exclusiveSupplier);
    }
}
