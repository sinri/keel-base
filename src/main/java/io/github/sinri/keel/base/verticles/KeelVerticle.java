package io.github.sinri.keel.base.verticles;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * Keel 体系下的 {@link Verticle} 强化标准接口。
 * <p>
 * Note: a possibility to base this class on {@link Deployable}.
 *
 * @since 5.0.0
 */
public interface KeelVerticle extends Verticle {


    String CONFIG_KEY_OF_VERTICLE_IDENTITY = "verticle_identity";

    /**
     * 根据给定的启动逻辑创建一个 {@link KeelVerticle} 实例。
     *
     * @param startFutureSupplier 启动逻辑，返回一个异步完成
     */
    @NotNull
    static KeelVerticle instant(@NotNull Supplier<Future<Void>> startFutureSupplier) {
        return new InstantKeelVerticle(startFutureSupplier);
    }

    /**
     * 根据给定的启动逻辑创建一个 {@link KeelVerticle} 实例，并提供外部中断的异步操作透出。
     *
     * @param starter 启动逻辑，给定一个停止用的{@link Promise}，返回一个异步完成
     */
    @NotNull
    static KeelVerticle instant(@NotNull Function<Promise<Void>, Future<Void>> starter) {
        return new InstantKeelVerticle(starter);
    }

    @NotNull
    Vertx vertx();

    /**
     * Retrieves the threading model associated with the current execution context of the verticle.
     *
     * @return the threading model of the current context, or {@code null} if not deployed yet.
     */
    @Nullable
    ThreadingModel contextThreadModel();

    /**
     * Returns the unique identifier for the deployment of this verticle.
     *
     * @return the deployment ID as a string, or {@code null} if not deployed yet.
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
        return Keel.getVertx().deployVerticle(this, deploymentOptions);
    }

    /**
     * Undeploy the current verticle from the Vert.x instance.
     *
     * @return a future that completes when the undeployment is successful, or fails with an exception if the
     *         undeployment fails
     */
    default Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        if (deploymentID == null) {
            throw new IllegalStateException("This verticle has not been deployed yet!");
        }
        return Keel.getVertx().undeploy(deploymentID);
    }

    /**
     * Retrieves the unique identifier or "identity" of the current verticle instance.
     * The identity is determined by checking the configuration for a specific key, and if not found,
     * it constructs a default identity string combining the fully qualified class name of the verticle
     * and its deployment ID.
     *
     * @return the identity of the verticle as a string. If the configuration key is absent or null,
     *         a string in the format of "className@deploymentID" is returned.
     */
    @NotNull
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

    KeelVerticleRunningStateEnum getRunningState();
}
