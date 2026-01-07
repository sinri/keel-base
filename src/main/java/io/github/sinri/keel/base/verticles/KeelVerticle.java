package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * 这是一个介于 {@link Deployable} 和 {@link VerticleBase} 之间的拟合接口，通过 {@link KeelAsyncMixin} 提供了基于 Vertx 的异步特性。
 * <p>
 *     通常，应尽可能使用 {@link KeelVerticleBase} 作为父类实现所需的类；除非唯一父类被占用。
 * @since 5.0.0
 */
public interface KeelVerticle extends Deployable, KeelAsyncMixin {
    /**
     * Initialise the verticle.
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.
     *
     * @param vertx   the deploying Vert.x instance
     * @param context the context of the verticle
     */
    void init(Vertx vertx, Context context);

    /**
     * Get the deployment ID of the verticle deployment
     *
     * @return the deployment ID
     */
    String deploymentID();

    /**
     * Get the configuration of the verticle.
     * This can be specified when the verticle is deployed.
     *
     * @return the configuration
     */
    @Nullable JsonObject config();

    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @return a future signalling the start-up completion
     */
    Future<?> start() throws Exception;

    /**
     * Stop the verticle.<p>
     * This is called by Vert.x when the verticle instance is un-deployed. Don't call it yourself.<p>
     *
     * @return a future signalling the clean-up completion
     */
    Future<?> stop() throws Exception;

    /**
     * 在部署后，可获取当前 verticle 的信息，包括配置、部署ID等。
     *
     * @return 当前 verticle 的信息
     */
    default JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("identity", getVerticleIdentity())
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID())
                .put("thread_model", getCurrentThreadingModel().name());
    }

    /**
     * 获取当前 verticle 类的唯一标识。
     *
     * @return 当前 verticle 类的唯一标识
     */
    default String getVerticleIdentity() {
        return this.getClass().getName();
    }

    /**
     * 获取当前 verticle 实例的唯一标识。
     *
     * @return 当前 verticle 实例的身份
     */
    default String getVerticleInstanceIdentity() {
        return String.format("%s@%s", getVerticleIdentity(), deploymentID());
    }

    ThreadingModel getCurrentThreadingModel();

    default Future<String> deployMe(Vertx vertx, DeploymentOptions deploymentOptions) {
        return vertx.deployVerticle(this, deploymentOptions);
    }

    default Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        return getVertx().undeploy(deploymentID);
    }

}
