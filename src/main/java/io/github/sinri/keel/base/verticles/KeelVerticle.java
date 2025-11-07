package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelBase;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * An extension of the {@link Verticle} interface, providing additional functionality and convenience methods.
 * This interface is designed to be implemented by verticles that are part of the Keel framework.
 * <p>
 * Note: a possibility to base this class on {@link Deployable}.
 *
 * @since 3.2.0
 */
public interface KeelVerticle extends Verticle {


    String CONFIG_KEY_OF_VERTICLE_IDENTITY = "verticle_identity";

    /**
     * Creates a new instance of {@link KeelVerticle} that wraps the provided start future supplier.
     * <p>
     * If you need to know when the verticle is deployed, so that you can undeploy it, use {@link #instant(Function)}
     * instead.
     *
     * @param startFutureSupplier a supplier that provides a future which represents the start operation of the verticle
     * @return a new instance of {@link KeelVerticle} wrapping the provided start future supplier
     * @since 4.0.2
     */
    @Nonnull
    static KeelVerticle instant(@Nonnull Supplier<Future<Void>> startFutureSupplier) {
        return new KeelVerticleWrap(startFutureSupplier);
    }

    /**
     * Creates a new instance of {@link KeelVerticle} that wraps the provided starter function.
     * The starter function accepts a Promise that can be used to trigger the verticle's undeployment.
     *
     * @param starter a function that takes a stop promise and returns a future representing the start operation
     * @return a new instance of {@link KeelVerticle} wrapping the provided starter function
     * @since 4.0.12
     */
    @Nonnull
    static KeelVerticle instant(@Nonnull Function<Promise<Void>, Future<Void>> starter) {
        return new KeelVerticleWrap(starter);
    }

    /**
     * Retrieves the threading model associated with the current execution context of the verticle.
     *
     * @return the threading model of the current context, or {@code null} if not deployed yet.
     * @since 4.1.5
     */
    @Nullable
    ThreadingModel contextThreadModel();

    /**
     * Returns the unique identifier for the deployment of this verticle.
     *
     * @return the deployment ID as a string, or {@code null} if not deployed yet.
     * @since 2.8
     */
    @Nullable
    String deploymentID();

    /**
     * Retrieves the configuration for this verticle.
     * <p>
     * As a declaration of {@link AbstractVerticle#config()}.
     *
     * @return a {@link JsonObject} containing the configuration settings for the verticle, might be null.
     */
    @Nullable
    JsonObject config();

    /**
     * Returns a JSON object containing information about the verticle.
     *
     * @return a {@link JsonObject} with the following fields:
     *         <ul>
     *             <li>class: the fully qualified name of the class implementing this verticle</li>
     *             <li>config: the configuration settings for the verticle as a {@link JsonObject}</li>
     *             <li>deployment_id: the unique identifier for the deployment of this verticle</li>
     *             <li>thread_model: the threading model of the current context, or {@code null} if not deployed yet</li>
     *         </ul>
     */
    default JsonObject getVerticleInfo() {
        ThreadingModel threadingModel = contextThreadModel();
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID())
                .put("thread_model", threadingModel == null ? null : threadingModel.name());
    }

    /**
     * Deploys the current verticle with the specified deployment options.
     *
     * @param deploymentOptions the options to use for deploying the verticle
     * @return a future that completes with the deployment ID if the deployment is successful, or fails with an
     *         exception if the deployment fails
     */
    default Future<String> deployMe(DeploymentOptions deploymentOptions) {
        String deploymentID = deploymentID();
        if (deploymentID != null) {
            throw new IllegalStateException("This verticle has been deployed already!");
        }
        return KeelBase.getVertx().deployVerticle(this, deploymentOptions);
    }

    /**
     * Undeploy the current verticle from the Vert.x instance.
     *
     * @return a future that completes when the undeployment is successful, or fails with an exception if the
     *         undeployment fails
     * @since 2.8 add default implementation
     */
    default Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        if (deploymentID == null) {
            throw new IllegalStateException("This verticle has not been deployed yet!");
        }
        return KeelBase.getVertx().undeploy(deploymentID);
    }

    /**
     * Retrieves the unique identifier or "identity" of the current verticle instance.
     * The identity is determined by checking the configuration for a specific key, and if not found,
     * it constructs a default identity string combining the fully qualified class name of the verticle
     * and its deployment ID.
     *
     * @return the identity of the verticle as a string. If the configuration key is absent or null,
     *         a string in the format of "className@deploymentID" is returned.
     * @since 4.1.5
     */
    @Nonnull
    default String verticleIdentity() {
        String mark = this.getClass().getName();
        JsonObject config = config();
        if (config != null) {
            String s = config.getString(CONFIG_KEY_OF_VERTICLE_IDENTITY);
            if (s != null) {
                mark = s;
            }
        }

        return String.format("%s@%s", mark, deploymentID());
    }
}
