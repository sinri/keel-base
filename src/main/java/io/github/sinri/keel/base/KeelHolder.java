package io.github.sinri.keel.base;

import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

/**
 * 一个接口，符合该接口的继承者内含了 {@link Keel} 实例。
 *
 * @since 5.0.0
 */
public interface KeelHolder extends VertxHolder {
    @NotNull Keel getKeel();

    default @NotNull Vertx getVertx() {
        return getKeel().getVertx();
    }
}
