package io.github.sinri.keel.base.verticles;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 一个开箱即用的 {@link KeelVerticle} 实现，基于 {@link AbstractKeelVerticle}。
 *
 * @since 5.0.0
 */
public final class InstantKeelVerticle extends AbstractKeelVerticle {

    @NotNull
    private final Supplier<Future<Void>> startFutureSupplier;

    /**
     * A promise that will be completed when the verticle needs to be stopped.
     * This is used in the second constructor to provide a way to trigger the verticle's undeployment.
     *
     */
    @Nullable
    private final Promise<Void> stopperPromise;

    /**
     * Creates a new instance of KeelVerticleWrap with a simple start future supplier.
     * This constructor is used when the verticle's lifecycle is managed externally.
     *
     * @param startFutureSupplier a supplier that provides a future representing the start operation
     */
    InstantKeelVerticle(@NotNull Supplier<Future<Void>> startFutureSupplier) {
        this.stopperPromise = null;
        this.startFutureSupplier = startFutureSupplier;
    }

    /**
     * Creates a new instance of KeelVerticleWrap with a starter function that accepts a stop promise.
     * This constructor is used when the verticle needs to handle its own lifecycle management.
     * The provided starter function can use the stop promise to trigger the verticle's undeployment.
     *
     * @param starter a function that takes a stop promise and returns a future representing the start operation
     */
    InstantKeelVerticle(@NotNull Function<Promise<Void>, Future<Void>> starter) {
        this.stopperPromise = Promise.promise();
        this.startFutureSupplier = () -> starter.apply(stopperPromise);
    }

    /**
     * Starts the verticle by calling the start future supplier and setting up the stop handling if needed.
     * If a stopper promise is present, it will set up a handler to undeploy the verticle when the promise is completed.
     * Any failures during the undeployment process will be logged but won't affect the start process.
     *
     * @return a future that completes when the verticle has started successfully
     */
    @Override
    protected Future<Void> startVerticle() {
        return this.startFutureSupplier
                .get()
                .compose(v -> {
                    if (stopperPromise != null) {
                        stopperPromise.future()
                                      .andThen(stopped -> {
                                          Keel.getVertx().setTimer(100L, timer -> {
                                              String deploymentID = deploymentID();
                                              if (deploymentID != null) {
                                                  this.undeployMe().onFailure(throwable -> {
                                                      System.err.println("Try to undeploy verticle [" + deploymentID + "] failed");
                                                  });
                                              }
                                          });
                                      });

                    }
                    return Future.succeededFuture();
                });
    }
}
