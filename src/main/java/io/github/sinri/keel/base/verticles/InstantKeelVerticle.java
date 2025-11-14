package io.github.sinri.keel.base.verticles;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * A concrete implementation of the {@link KeelVerticle} interface, extending
 * from {@link AbstractKeelVerticle}.
 * This class is designed to wrap a supplier that provides a future representing
 * the start operation of the verticle.
 * <p>
 * The primary responsibility of this class is to manage the startup process by
 * delegating the start operation to the
 * provided supplier. It ensures that the `startVerticle` method, which is part
 * of the lifecycle management, calls the
 * supplier to get the future and completes the startup process.
 *
 * @since 4.0.2
 */
public final class InstantKeelVerticle extends AbstractKeelVerticle {

    @NotNull
    private final Supplier<Future<Void>> startFutureSupplier;

    /**
     * A promise that will be completed when the verticle needs to be stopped.
     * This is used in the second constructor to provide a way to trigger the verticle's undeployment.
     *
     * @since 4.0.12
     */
    @Nullable
    private final Promise<Void> stopperPromise;

    /**
     * Creates a new instance of KeelVerticleWrap with a simple start future supplier.
     * This constructor is used when the verticle's lifecycle is managed externally.
     *
     * @param startFutureSupplier a supplier that provides a future representing the start operation
     * @since 4.0.2
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
     * @since 4.0.12
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
     * @since 4.0.2
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
