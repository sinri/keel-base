package io.github.sinri.keel.base;

import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 一个接口，符合该接口的继承者内含了 {@link Vertx} 实例。
 *
 * @since 5.0.0
 */
@NullMarked
public interface VertxHolder {
    Vertx getVertx();
}
