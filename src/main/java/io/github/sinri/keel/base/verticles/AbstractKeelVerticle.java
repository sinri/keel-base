package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 *
 * 接口 {@link KeelVerticle} 的基础实现，基于 {@link AbstractVerticle}。
 * <p>
 * Note: a possibility to base this class on {@link io.vertx.core.VerticleBase}.
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractKeelVerticle extends AbstractVerticle implements KeelVerticle {
    private final Keel keel;
    private KeelVerticleRunningStateEnum runningState;
    @Nullable
    private String deploymentInstanceCode;

    public AbstractKeelVerticle(Keel keel) {
        this.runningState = KeelVerticleRunningStateEnum.BEFORE_RUNNING;
        this.keel = keel;
    }

    @Override
    public final Keel getKeel() {
        return keel;
    }

    @Override
    public final Vertx getVertx() {
        Vertx v = super.getVertx();
        if (v == null) {
            return keel.getVertx();
        }
        return v;
    }

    @Override
    @Nullable
    public final ThreadingModel contextThreadModel() {
        if (this.context == null) return null;
        return this.context.threadingModel();
    }

    @Nullable
    @Override
    public String deploymentID() {
        if (this.context == null) return null;
        return context.deploymentID();
    }

    @Nullable
    @Override
    public JsonObject config() {
        if (this.context == null) return null;
        return context.config();
    }

    /**
     * 封印。
     */
    @Override
    public final void start() {
    }

    /**
     * 固有实现。
     */
    @Override
    public final void start(Promise<Void> startPromise) {
        runningState = KeelVerticleRunningStateEnum.RUNNING;
        deploymentInstanceCode = UUID.randomUUID().toString();
        Future.succeededFuture()
              .compose(v -> startVerticle())
              .andThen(ar -> {
                  if (ar.succeeded()) {
                      startPromise.complete();
                  } else {
                      runningState = KeelVerticleRunningStateEnum.RUNNING_FAILED;
                      startPromise.fail(ar.cause());
                  }
              });
    }

    /**
     * 启动逻辑。
     *
     * @return 如果正常异步返回则部署正常运作；否则部署失败。
     */
    protected abstract Future<Void> startVerticle();

    /**
     * 封印
     */
    @Override
    public final void stop() {

    }

    /**
     * 固有实现
     */
    @Override
    public final void stop(Promise<Void> stopPromise) {
        stopVerticle()
                .onComplete(ar -> {
                    runningState = KeelVerticleRunningStateEnum.AFTER_RUNNING;
                    stopPromise.complete();
                });
    }

    /**
     * 停止逻辑。
     *
     * @return 如果正常异步返回则解除部署正常结束；否则解除部署失败。
     */
    protected Future<Void> stopVerticle() {
        return Future.succeededFuture();
    }

    @Override
    public String verticleIdentity() {
        return "%s:%s".formatted(KeelVerticle.super.verticleIdentity(), deploymentInstanceCode);
    }

    @Override
    public KeelVerticleRunningStateEnum getRunningState() {
        return runningState;
    }
}
