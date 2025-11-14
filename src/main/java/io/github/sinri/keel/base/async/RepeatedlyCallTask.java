package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


/**
 * A utility class designed to repeatedly execute a task until it is explicitly
 * stopped.
 * The task is represented by a {@link Function} that takes an instance of this
 * class and
 * returns a {@code Future<Void>}. The task will continue to be executed with a
 * delay of 1 millisecond
 * between each execution, unless the stop method is called or the task itself
 * fails.
 *
 * @see #start(RepeatedlyCallTask, Promise)
 * @see #stop()
 * @since 4.1.0
 */
public final class RepeatedlyCallTask {
    @NotNull
    private final Function<RepeatedlyCallTask, Future<Void>> processor;
    private volatile boolean toStop = false;

    public RepeatedlyCallTask(@NotNull Function<RepeatedlyCallTask, Future<Void>> processor) {
        this.processor = processor;
    }

    public static void start(@NotNull RepeatedlyCallTask thisTask, @NotNull Promise<Void> finalPromise) {
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
                          Keel.getVertx().setTimer(1L, x -> start(thisTask, finalPromise));
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
