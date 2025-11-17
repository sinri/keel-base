package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.vertx.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * 本接口包含了方法定义，支持将同步阻塞调用异步化。
 *
 * @since 5.0.0
 */
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
    @NotNull
    default Future<Void> runInVerticleOnVirtualThread(@NotNull Supplier<Future<Void>> function) {
        return KeelVerticle.instant(promise -> Future.succeededFuture()
                                                     .compose(v -> function.get())
                                                     .onComplete(promise)
                           )
                           .deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
                           .compose(s -> Future.succeededFuture());
    }

    /**
     * 将 {@link CompletableFuture} 转换为 {@link Future}。
     *
     * @param completableFuture 给定的 {@link CompletableFuture}
     * @param <R>               异步返回值的类型
     * @return 转换好的 {@link Future}
     */
    default <R> Future<R> asyncTransformCompletableFuture(@NotNull CompletableFuture<R> completableFuture) {
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
    default <R> Future<R> asyncTransformRawFuture(@NotNull java.util.concurrent.Future<R> rawFuture) {
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
    default <R> Future<R> asyncTransformRawFuture(@NotNull java.util.concurrent.Future<R> rawFuture, long sleepTime) {
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
     * 阻塞等待一个异步任务完成。
     * <p>
     * 本方法不应该在 EventLoop 里执行。
     *
     * @param longTermAsyncProcessFuture 一个耗时的异步任务所返回的 {@link Future}
     * @param <T>                        异步任务返回的值的类型
     * @return 异步任务返回的值
     */
    default <T> T blockAwait(Future<T> longTermAsyncProcessFuture) {
        if (isInNonBlockContext()) {
            throw new IllegalThreadStateException("Cannot call blockAwait in event loop context");
        }

        CompletableFuture<T> cf = new CompletableFuture<>();
        longTermAsyncProcessFuture.onComplete(ar -> {
            if (ar.succeeded()) {
                cf.complete(ar.result());
            } else {
                cf.completeExceptionally(ar.cause());
            }
        });
        try {
            return cf.get(); // 阻塞等待
        } catch (ExecutionException e) {
            throw new RuntimeException("Error occurred while executing", e.getCause());
        } catch (InterruptedException e) {
            // Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting", e);
        }
    }


}
