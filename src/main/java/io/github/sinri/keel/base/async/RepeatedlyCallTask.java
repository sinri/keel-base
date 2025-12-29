package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


/**
 * 异步循环任务。
 * <p>
 * 包含一个异步的循环体，以间隔 1 毫秒的节奏循环执行；
 * 每次任务循环结束时，检查任务结束标记确认是否结束循环；
 * 当循环执行任务中抛出异常引发异步失败，则循环强制结束并向外抛出该根因异常。
 *
 * @see #start(Vertx, RepeatedlyCallTask, Promise)
 * @see #stop()
 * @since 5.0.0
 */
public final class RepeatedlyCallTask {
    @NotNull
    private final Function<@NotNull RepeatedlyCallTask, @NotNull Future<Void>> processor;
    private volatile boolean toStop = false;

    public RepeatedlyCallTask(@NotNull Function<@NotNull RepeatedlyCallTask, @NotNull Future<Void>> processor) {
        this.processor = processor;
    }

    public static void start(@NotNull Vertx vertx, @NotNull RepeatedlyCallTask thisTask, @NotNull Promise<Void> finalPromise) {
        Future.succeededFuture()
              .compose(v -> {
                  if (thisTask.toStop) {
                      return Future.succeededFuture();
                  }
                  return thisTask.processor.apply(thisTask);
              })
              .andThen(shouldStopAR -> {
                  if (shouldStopAR.succeeded()) {
                      if (thisTask.toStop) {
                          finalPromise.complete();
                      } else {
                          vertx.setTimer(1L, x -> start(vertx, thisTask, finalPromise));
                      }
                  } else {
                      finalPromise.fail(shouldStopAR.cause());
                  }
              });
    }

    public void stop() {
        toStop = true;
    }
}
