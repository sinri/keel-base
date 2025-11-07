package io.github.sinri.keel.core.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * An abstract implementation of the {@link KeelVerticle} interface, extending from {@link AbstractVerticle}.
 * This class provides a structured way to implement custom verticles within the Keel framework.
 * <p>
 * The primary responsibility of this class is to manage the lifecycle of the verticle, ensuring that
 * the `start` method is called in a consistent and reliable manner. It also enforces that subclasses
 * must provide an implementation for the `startVerticle` method, which contains the specific logic
 * to be executed when the verticle starts.
 * <p>
 * Subclasses should focus on implementing the `startVerticle` method to define their specific behavior.
 * The `start` method is overridden to ensure that it is not used directly, and the
 * {@code start(Promise<Void> startPromise)}
 * method is implemented to handle the asynchronous startup process, including calling the `startVerticle` method
 * and handling the completion or failure of the startup process.
 * <p>
 * Note: a possibility to base this class on {@link io.vertx.core.VerticleBase}.
 *
 * @since 4.0.2 remove the logging embeddings.
 */
public abstract class KeelVerticleImpl extends AbstractVerticle implements KeelVerticle {

    private String deploymentInstanceCode;

    /**
     * Retrieves the threading model associated with the current context of the verticle.
     *
     * @return the threading model of the current context
     * @since 4.1.3
     */
    @Override
    @Nullable

    public final ThreadingModel contextThreadModel() {
        if (this.context == null) return null;
        return this.context.threadingModel();
    }

    /**
     * @since 4.1.5
     */
    @Nullable
    @Override
    public String deploymentID() {
        if (this.context == null) return null;
        return context.deploymentID();
    }

    /**
     * @since 4.1.5
     */
    @Nullable
    @Override
    public JsonObject config() {
        if (this.context == null) return null;
        return context.config();
    }

    /**
     * This method is the entry point for the verticle's lifecycle. It is called by the Vert.x framework when the
     * verticle
     * is deployed. The implementation in this class is final and does nothing, as it is intended to be not overridden
     * by
     * subclasses that extend {@link KeelVerticleImpl}. Subclasses should implement the {@code startVerticle} method
     * instead,
     * which is called within the asynchronous startup process.
     */
    @Override
    public final void start() {
    }

    /**
     * Asynchronously starts the verticle and handles the startup process.
     * This method is called by the Vert.x framework when the verticle is deployed.
     * It ensures that the `startVerticle` method, which contains the specific logic
     * to be executed during the startup, is called in a consistent and reliable manner.
     *
     * @param startPromise a promise that will be completed when the verticle starts successfully,
     *                     or failed with an exception if the startup fails
     */
    @Override
    public final void start(Promise<Void> startPromise) {
        Future.succeededFuture()
              .compose(v -> {
                  // start();
                  deploymentInstanceCode = UUID.randomUUID().toString();
                  return startVerticle();
              })
              .andThen(ar -> {
                  if (ar.succeeded()) {
                      startPromise.complete();
                  } else {
                      startPromise.fail(ar.cause());
                  }
              });
    }

    /**
     * Starts the verticle and returns a future that completes when the verticle has started successfully,
     * or fails with an exception if the startup process encounters an error.
     *
     * @return a {@link Future} that completes with {@code null} when the verticle starts successfully,
     *         or fails with an exception if the startup process fails
     */
    protected abstract Future<Void> startVerticle();

    @Nonnull
    @Override
    public String verticleIdentity() {
        return KeelVerticle.super.verticleIdentity()
                + ":" + deploymentInstanceCode;
    }
}
