package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;
import java.util.function.Supplier;


/**
 * 异步独占运行机制。
 *
 * @since 5.0.0
 */
@NullMarked
interface KeelAsyncMixinLock extends KeelAsyncMixinCore {
    /**
     * 在Vertx 的锁机制下，独占运行一段异步逻辑。
     * <p>
     * 尝试在制定时间限制内获取锁，并运行异步逻辑，执行完毕后释放锁。
     *
     * @param <T>                             异步逻辑的返回值类型
     * @param lockName                        锁名称
     * @param waitTimeForLock                 最长锁等待时间，以毫秒计
     * @param exclusiveSupplier               需要独占运行的异步逻辑
     * @param lockAcquireFailedHandleSupplier 锁获取失败时的处理函数，返回一个Future，用于替代锁获取失败的异常
     * @return 异步逻辑的结果；如果锁获取失败，则会异步返回相应失败。
     * @since 5.0.2
     */
    default <T> Future<T> asyncCallExclusively(
            String lockName,
            long waitTimeForLock,
            Supplier<Future<T>> exclusiveSupplier,
            Function<LockAcquireFailedException, Future<T>> lockAcquireFailedHandleSupplier
    ) {
        return sharedData()
                .getLockWithTimeout(lockName, waitTimeForLock)
                .compose(
                        lock -> Future.succeededFuture()
                                      .compose(v -> exclusiveSupplier.get())
                                      .andThen(ar -> lock.release()),
                        throwable -> {
                            LockAcquireFailedException lockAcquireFailedException = new LockAcquireFailedException(throwable.getMessage());
                            return lockAcquireFailedHandleSupplier.apply(lockAcquireFailedException);
                        }
                );
    }

    /**
     * 在Vertx 的锁机制下，独占运行一段异步逻辑。
     * <p>
     * 和 {@link KeelAsyncMixinLock#asyncCallExclusively(String, long, Supplier, Function)} 逻辑一致，
     * 获取锁异常不进行额外处理。
     */
    default <T> Future<T> asyncCallExclusively(
            String lockName,
            long waitTimeForLock,
            Supplier<Future<T>> exclusiveSupplier
    ) {
        return asyncCallExclusively(
                lockName,
                waitTimeForLock,
                exclusiveSupplier,
                Future::failedFuture
        );
    }

    /**
     * 在Vertx 的锁机制下，独占运行一段异步逻辑。
     * <p>
     * 和 {@link KeelAsyncMixinLock#asyncCallExclusively(String, long, Supplier, Function)} 逻辑一致，锁等待时间默认 1 秒。
     */
    default <T> Future<T> asyncCallExclusively(String lockName,
                                               Supplier<Future<T>> exclusiveSupplier) {
        return asyncCallExclusively(
                lockName,
                1_000L,
                exclusiveSupplier,
                Future::failedFuture
        );
    }

    /**
     * 锁获取失败异常
     *
     * @since 5.0.2
     */
    class LockAcquireFailedException extends RuntimeException {
        public LockAcquireFailedException(String cause) {
            super(cause);
        }
    }
}
