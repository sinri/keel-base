package io.github.sinri.keel.vertx.async;

import io.github.sinri.keel.facade.KeelInstance;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Use {@link KeelInstance#Keel} to use the methods defined by this interface
 *
 * @see KeelInstance#Keel
 * @since 4.1.0
 */
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
    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName, long waitTimeForLock,
                                               @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return Keel.getVertx().sharedData()
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
    default <T> Future<T> asyncCallExclusively(@Nonnull String lockName,
                                               @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(lockName, 1_000L, exclusiveSupplier);
    }
}
