package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Keel 体系下的标准 Verticle 基类，实现接口 {@link KeelVerticle}，期望部署于 {@link Keel} 所实现的 {@link  Vertx} 实例下。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class KeelVerticleBase implements KeelVerticle {
    private final LateObject<Keel> lateKeel = new LateObject<>();
    private final LateObject<Context> lateContext = new LateObject<>();
    private final Promise<Void> undeployPromise = Promise.promise();
    private volatile KeelVerticleRunningStateEnum runningState;

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

    @Override
    public final Keel getKeel() {
        return lateKeel.get();
    }

    public ThreadingModel getCurrentThreadingModel() {
        return getContext().threadingModel();
    }

    public final JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("identity", getVerticleIdentity())
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID())
                .put("thread_model", getCurrentThreadingModel().name());
    }

    public final KeelVerticleRunningStateEnum getRunningState() {
        return runningState;
    }

    public final Future<String> deployMe(Vertx vertx, DeploymentOptions deploymentOptions) {
        Keel x;
        if (vertx instanceof Keel keel) {
            x = keel;
        } else {
            x = Keel.create(vertx);
        }
        return deployMe(x, deploymentOptions);
    }

    /**
     * 在指定 Keel 实例下部署当前 Verticle，并使用指定的部署选项。
     *
     * @param keel              Keel 实例
     * @param deploymentOptions 部署选项
     * @return 一个异步结果；如果部署成功则返回部署 ID，如果部署失败则返回异常
     */
    public final Future<String> deployMe(Keel keel, DeploymentOptions deploymentOptions) {
        if (getRunningState() != KeelVerticleRunningStateEnum.BEFORE_RUNNING) {
            return Future.failedFuture(new IllegalStateException("current verticle status is " + getRunningState()));
        }
        return keel.deployVerticle(this, deploymentOptions);
    }

    /**
     * 在当前已部署的 Verticle 所属的 Vertx 实例下解除部署。
     *
     * @return 一个异步完成，如果解除部署成功则返回，如果解除部署失败则返回异常
     */
    public final Future<Void> undeployMe() {
        String deploymentID = deploymentID();
        return getKeel().undeploy(deploymentID);
    }

    /**
     * 获取当前 Verticle 实例的唯一标识或“身份”。
     *
     * @return 当前 Verticle 实例的身份，格式为 "标识@部署 ID"
     */
    public final String getVerticleInstanceIdentity() {
        return String.format("%s@%s", getVerticleIdentity(), deploymentID());
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (vertx instanceof Keel keel) {
            this.lateKeel.set(keel);
        } else {
            this.lateKeel.set(Keel.create(vertx));
        }

        lateContext.set(context);
    }

    public Context getContext() {
        return lateContext.get();
    }

    @Override
    public String deploymentID() {
        return getContext().deploymentID();
    }


    @Override
    public @Nullable JsonObject config() {
        return getContext().config();
    }

    @Override
    public final Future<?> start() {
        runningState = KeelVerticleRunningStateEnum.RUNNING;
        return Future.succeededFuture()
                     .compose(v -> startVerticle())
                     .onFailure(failure -> runningState = KeelVerticleRunningStateEnum.DEPLOY_FAILED);
    }

    abstract protected Future<?> startVerticle();

    @Override
    public final Future<?> stop() {
        return stopVerticle()
                .onComplete(ar -> {
                    runningState = KeelVerticleRunningStateEnum.AFTER_RUNNING;
                    if (ar.succeeded()) {
                        undeployPromise.tryComplete();
                    } else {
                        undeployPromise.tryFail(ar.cause());
                    }
                });
    }

    protected Future<?> stopVerticle() {
        return Future.succeededFuture();
    }

    @Override
    public final Future<Void> undeployed() {
        return undeployPromise.future();
    }

    @Override
    public final Future<?> deploy(Context context) {
        init(context.owner(), context);
        return start();
    }

    @Override
    public final Future<?> undeploy(Context context) {
        return stop();
    }
}
