package io.github.sinri.keel.base.verticles;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 *
 * 接口 {@link KeelVerticle} 的基础实现，基于 {@link AbstractVerticle}。
 * <p>
 * Note: a possibility to base this class on {@link io.vertx.core.VerticleBase}.
 *
 * @since 5.0.0
 */
public abstract class AbstractKeelVerticle extends AbstractVerticle implements KeelVerticle {

    private KeelVerticleRunningStateEnum runningState;
    private String deploymentInstanceCode;

    public AbstractKeelVerticle() {
        this.runningState = KeelVerticleRunningStateEnum.BEFORE_RUNNING;
    }

    @Override
    @NotNull
    public Vertx vertx() {
        Objects.requireNonNull(context);
        return context.owner();
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
        Future.succeededFuture()
              .compose(v -> {
                  // start();
                  deploymentInstanceCode = UUID.randomUUID().toString();
                  return startVerticle();
              })
              .andThen(ar -> {
                  if (ar.succeeded()) {
                      runningState = KeelVerticleRunningStateEnum.RUNNING;
                      startPromise.complete();
                  } else {
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

    @NotNull
    @Override
    public String verticleIdentity() {
        return KeelVerticle.super.verticleIdentity()
                + ":" + deploymentInstanceCode;
    }

    @Override
    public KeelVerticleRunningStateEnum getRunningState() {
        return runningState;
    }
}
