package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

@NullMarked
abstract public class KeelVerticleBase extends VerticleBase implements KeelAsyncMixin {
    private final LateObject<Keel> lateKeel = new LateObject<>();
    private KeelVerticleRunningStateEnum runningState;

    public KeelVerticleBase() {
        this.runningState = KeelVerticleRunningStateEnum.BEFORE_RUNNING;
    }

    public static KeelVerticleBase wrap(
            Function<KeelVerticleBase, Future<?>> startFunc,
            Function<KeelVerticleBase, Future<?>> stopFunc
    ) {
        return new KeelVerticleBase() {
            @Override
            protected Future<?> startVerticle() {
                return startFunc.apply(this);
            }

            @Override
            protected Future<?> stopVerticle() {
                return stopFunc.apply(this);
            }
        };
    }

    public static KeelVerticleBase wrap(Function<KeelVerticleBase, Future<?>> startFunc) {
        return wrap(startFunc, Future::succeededFuture);
    }

    public final Vertx getVertx() {
        if (vertx != null)
            return vertx;
        throw new IllegalStateException("Vertx of this verticle not initialized yet");
    }

    public final JsonObject getVerticleInfo() {
        if (context == null) {
            throw new IllegalStateException("Context of this verticle not initialized yet");
        }
        return new JsonObject()
                .put("identity", getVerticleIdentity())
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID())
                .put("thread_model", context.threadingModel().name());
    }

    public final KeelVerticleRunningStateEnum getRunningState() {
        return runningState;
    }

    /**
     * 在指定Vertx实例下部署当前 verticle 并使用指定的部署选项。
     *
     * @param vertx             Vertx 实例
     * @param deploymentOptions 部署选项
     * @return 一个异步完成，如果部署成功则返回部署唯一标识，如果部署失败则返回异常
     */
    public final Future<String> deployMe(Vertx vertx, DeploymentOptions deploymentOptions) {
        if (getRunningState() != KeelVerticleRunningStateEnum.BEFORE_RUNNING) {
            return Future.failedFuture(new IllegalStateException("current verticle status is " + getRunningState()));
        }
        return vertx.deployVerticle(this, deploymentOptions);
    }

    /**
     * 在当前已部署的verticle对应Vertx实例下解除部署。
     *
     * @return 一个异步完成，如果解除部署成功则返回，如果解除部署失败则返回异常
     */
    public final Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        return getVertx().undeploy(deploymentID);
    }

    /**
     * 获取当前 verticle 实例的唯一标识或 "身份"。
     *
     * @return 当前 verticle 实例的身份，格式为 "标识@部署ID"
     */
    public final String getVerticleInstanceIdentity() {
        return String.format("%s@%s", getVerticleIdentity(), deploymentID());
    }

    /**
     * 获取当前 verticle 类的唯一标识或 "身份"。
     */
    protected String getVerticleIdentity() {
        return this.getClass().getName();
    }

    @Override
    public final Future<?> start() throws Exception {
        runningState = KeelVerticleRunningStateEnum.RUNNING;
        return Future.succeededFuture()
                     .compose(v -> startVerticle())
                     .onFailure(failure -> runningState = KeelVerticleRunningStateEnum.DEPLOY_FAILED);
    }

    abstract protected Future<?> startVerticle();

    @Override
    public final Future<?> stop() throws Exception {
        return stopVerticle()
                .onSuccess(ar -> {
                    runningState = KeelVerticleRunningStateEnum.AFTER_RUNNING;
                });
    }

    protected Future<?> stopVerticle() {
        return Future.succeededFuture();
    }

    protected final Keel getKeel() {
        return lateKeel.ensure(() -> Keel.wrap(getVertx()));
        //        Vertx mappedVertx = getVertx();
        //        if (keel == null) {
        //            synchronized (this) {
        //                if (keel == null) {
        //                    keel = Keel.wrap(mappedVertx);
        //                }
        //            }
        //        }
        //        return Objects.requireNonNull(keel);
    }
}
