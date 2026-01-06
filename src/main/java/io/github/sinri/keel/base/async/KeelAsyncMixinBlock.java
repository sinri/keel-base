package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.vertx.core.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * 本接口包含了方法定义，支持将同步阻塞调用异步化。
 *
 * @since 5.0.0
 */
@NullMarked
interface KeelAsyncMixinBlock extends KeelAsyncMixinLogic {
    private boolean isInNonBlockContext() {
        Context currentContext = Vertx.currentContext();
        return currentContext != null && currentContext.isEventLoopContext();
    }

    /**
     * 在虚拟线程中运行给定的异步逻辑。
     *
     * @param function 一个需要在虚拟线程中运行的异步逻辑
     * @return 在虚拟线程中运行给定逻辑之后的 Future，或相关失败 Future。
     */
    @TechnicalPreview(notice = "Require JDK 21+")
    default Future<Void> runInVerticleOnVirtualThread(Keel keel, Supplier<Future<Void>> function) {
        return KeelVerticleBase
                .wrap(keelVerticle -> function
                        .get()
                        .onComplete(ar -> keelVerticle.getVertx()
                                                      .setTimer(10, timer -> {
                                                          keelVerticle.undeployMe();
                                                      }))
                )
                .deployMe(
                        keel.getVertx(),
                        new DeploymentOptions()
                                .setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
                )
                .compose(s -> Future.succeededFuture());
    }

    /**
     * 将 {@link CompletableFuture} 转换为 {@link Future}。
     *
     * @param completableFuture 给定的 {@link CompletableFuture}
     * @param <R>               异步返回值的类型
     * @return 转换好的 {@link Future}
     */
    default <R> Future<R> asyncTransformCompletableFuture(CompletableFuture<R> completableFuture) {
        Promise<R> promise = Promise.promise();
        Context currentContext = Vertx.currentContext();

        completableFuture.whenComplete((r, t) -> {
            Runnable completeAction = () -> {
                try {
                    if (t != null) {
                        promise.fail(t);
                    } else {
                        promise.complete(r);
                    }
                } catch (Exception e) {
                    promise.tryFail(e);
                }
            };

            // 如果没有上下文或者已经在正确的事件循环线程中，直接执行
            if (currentContext == null) {
                completeAction.run();
            } else if (currentContext.isEventLoopContext()) {
                currentContext.runOnContext(v -> completeAction.run());
            } else {
                // 在工作线程中，直接执行
                completeAction.run();
            }
        });

        return promise.future();
    }

    /**
     * 将 {@link java.util.concurrent.Future} 转换为 {@link Future}。
     *
     * @param rawFuture 给定的 {@link java.util.concurrent.Future}
     * @param <R>       异步返回值的类型
     * @return 转换好的 {@link Future}
     */
    default <R> Future<R> asyncTransformRawFuture(java.util.concurrent.Future<R> rawFuture) {
        if (isInNonBlockContext()) {
            return getVertx().executeBlocking(rawFuture::get);
        } else {
            try {
                var r = rawFuture.get();
                return Future.succeededFuture(r);
            } catch (InterruptedException | ExecutionException e) {
                return Future.failedFuture(e);
            }
        }
    }

    /**
     * 将 {@link java.util.concurrent.Future} 转换为 {@link Future}。
     *
     * @param rawFuture 给定的 {@link java.util.concurrent.Future}
     * @param sleepTime 等待时间，单位毫秒
     * @param <R>       异步返回值的类型
     * @return 转换好的 {@link Future}
     */
    default <R> Future<R> asyncTransformRawFuture(java.util.concurrent.Future<R> rawFuture, long sleepTime) {
        return asyncCallRepeatedly(repeatedlyCallTask -> {
            if (rawFuture.isDone()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            }
            return this.asyncSleep(sleepTime);
        })
                .compose(over -> {
                    if (rawFuture.isCancelled()) {
                        return Future
                                .failedFuture(new java.util.concurrent.CancellationException("Raw Future Cancelled"));
                    }
                    try {
                        var r = rawFuture.get();
                        return Future.succeededFuture(r);
                    } catch (InterruptedException | ExecutionException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    /**
     * 阻塞等待一个异步任务完成，并返回其结果。
     * <p>
     * 本方法使用 {@link CountDownLatch} 实现阻塞等待，避免 CPU 空转。
     * 方法会阻塞当前线程直到异步任务完成（成功或失败）。
     * <p>
     * <strong>重要限制：</strong>
     * <ul>
     *   <li>本方法<strong>严禁</strong>在 EventLoop 线程中调用，否则会抛出 {@link IllegalThreadStateException}</li>
     *   <li>本方法只能在 Worker 线程、虚拟线程或普通线程中调用</li>
     *   <li>建议在 {@link ThreadingModel#WORKER} 或 {@link ThreadingModel#VIRTUAL_THREAD} 的 Verticle 中使用</li>
     * </ul>
     * <p>
     * <strong>异常处理：</strong>
     * <ul>
     *   <li>如果异步任务失败，本方法会抛出 {@link RuntimeException}，原始异常会被包装或直接抛出（如果已经是 RuntimeException）</li>
     *   <li>如果当前线程在等待过程中被中断，会抛出 {@link RuntimeException}，并恢复线程的中断状态</li>
     *   <li>如果在 EventLoop 线程中调用，会立即抛出 {@link IllegalThreadStateException}</li>
     * </ul>
     * <p>
     * <strong>返回值：</strong>
     * <ul>
     *   <li>如果异步任务成功完成，返回任务的结果（可能为 {@code null}）</li>
     *   <li>如果异步任务失败，抛出异常而不是返回 {@code null}</li>
     * </ul>
     * <p>
     *     非必要不使用此方法，使用前需要确认符合场景。
     *
     * @param longTermAsyncProcessFuture 一个耗时的异步任务所返回的 {@link Future}，不能为 {@code null}
     * @param <T>                        异步任务返回的值的类型
     * @return 异步任务返回的值，如果任务成功完成但结果为 {@code null}，则返回 {@code null}
     * @throws IllegalThreadStateException 如果在 EventLoop 线程中调用本方法
     * @throws RuntimeException            如果异步任务失败，或当前线程在等待过程中被中断
     */
    @Nullable
    default <T> T blockAwait(Future<T> longTermAsyncProcessFuture) {
        if (isInNonBlockContext()) {
            throw new IllegalThreadStateException("Cannot call blockAwait in event loop context");
        }

        // 使用 CountDownLatch 替代忙等待，避免 CPU 空转
        CountDownLatch latch = new CountDownLatch(1);
        longTermAsyncProcessFuture.onComplete(ar -> latch.countDown());

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for async task", e);
        }

        // 检查任务是否失败，如果有异常则抛出，而不是返回 null
        if (longTermAsyncProcessFuture.failed()) {
            Throwable cause = longTermAsyncProcessFuture.cause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }

        return longTermAsyncProcessFuture.result();
    }
}
