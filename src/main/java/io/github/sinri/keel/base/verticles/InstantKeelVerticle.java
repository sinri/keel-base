package io.github.sinri.keel.base.verticles;

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

    InstantKeelVerticle(@NotNull Function<KeelVerticle, Future<Void>> verticleStartFunc) {
        this.verticleStartFunc = verticleStartFunc;
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        return this.verticleStartFunc.apply(this);
    }
}
