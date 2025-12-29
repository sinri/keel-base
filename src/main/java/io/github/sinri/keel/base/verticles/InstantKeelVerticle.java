package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.Keel;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


/**
 * 一个开箱即用的 {@link KeelVerticle} 实现，基于 {@link AbstractKeelVerticle}。
 *
 * @since 5.0.0
 */
public final class InstantKeelVerticle extends AbstractKeelVerticle {
    private final @NotNull Function<KeelVerticle, Future<Void>> verticleStartFunc;

    /**
     * 使用指定的启动函数构造即时执行的 Keel Verticle 实例。
     *
     * @param verticleStartFunc 在 verticle 启动时执行的函数，接收当前 verticle 实例并返回异步完成结果
     */
    InstantKeelVerticle(@NotNull Keel keel, @NotNull Function<@NotNull KeelVerticle, @NotNull Future<Void>> verticleStartFunc) {
        super(keel);
        this.verticleStartFunc = verticleStartFunc;
    }

    /**
     * 启动 verticle。
     * <p>
     * 此方法会调用构造函数中提供的启动函数来执行实际的启动逻辑。
     *
     * @return 异步完成结果，表示启动操作的完成状态
     */
    @Override
    protected @NotNull Future<Void> startVerticle() {
        return this.verticleStartFunc.apply(this);
    }
}
