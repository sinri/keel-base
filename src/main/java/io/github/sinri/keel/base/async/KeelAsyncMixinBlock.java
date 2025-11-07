package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.vertx.core.*;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

interface KeelAsyncMixinBlock extends KeelAsyncMixinLogic {
    private boolean isInNonBlockContext() {
        Context currentContext = Vertx.currentContext();
        return currentContext != null && currentContext.isEventLoopContext();
    }

    @TechnicalPreview(since = "4.1.1", notice = "Require JDK 21+")
    @Nonnull
    default Future<Void> runInVerticleOnVirtualThread(@Nonnull Supplier<Future<Void>> function) {
        return KeelVerticle.instant(promise -> Future.succeededFuture()
                                                     .compose(v -> function.get())
                                                     .onComplete(promise)
                           )
                           .deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
                           .compose(s -> Future.succeededFuture());
    }

    default <R> Future<R> asyncTransformCompletableFuture(@Nonnull CompletableFuture<R> completableFuture) {
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

    default <R> Future<R> asyncTransformRawFuture(@Nonnull java.util.concurrent.Future<R> rawFuture) {
        if (isInNonBlockContext()) {
            return Keel.getVertx().executeBlocking(rawFuture::get);
        } else {
            try {
                var r = rawFuture.get();
                return Future.succeededFuture(r);
            } catch (InterruptedException | ExecutionException e) {
                return Future.failedFuture(e);
            }
        }
    }

    default <R> Future<R> asyncTransformRawFuture(@Nonnull java.util.concurrent.Future<R> rawFuture, long sleepTime) {
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

    default <T> T blockAwait(Future<T> longTermAsyncProcessFuture) {
        if (isInNonBlockContext()) {
            throw new IllegalCallerException("Cannot call blockAwait in event loop context");
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
