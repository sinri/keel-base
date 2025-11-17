package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;



interface KeelAsyncMixinLock extends KeelAsyncMixinCore {
    /**
     * Executes a supplier asynchronously while ensuring exclusive access to a given
     * lock.
     *
     * @param <T>               the type of the result returned by the supplier
     * @param lockName          the name of the lock to be used for ensuring
     *                          exclusivity
     * @param waitTimeForLock   the maximum time in milliseconds to wait for
     *                          acquiring the lock
     * @param exclusiveSupplier the supplier that provides a future, which will be
     *                          executed exclusively
     * @return a future representing the asynchronous computation result
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
     * Executes the given supplier asynchronously with an exclusive lock.
     *
     * @param <T>               the type of the result produced by the supplier
     * @param lockName          the name of the lock to be used for exclusivity
     * @param exclusiveSupplier the supplier that produces a future, which will be
     *                          executed exclusively
     * @return a future representing the asynchronous computation
     */
    default <T> Future<T> asyncCallExclusively(@NotNull String lockName,
                                               @NotNull Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(lockName, 1_000L, exclusiveSupplier);
    }
}
