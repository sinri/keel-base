package io.github.sinri.keel.base;

import io.github.sinri.keel.base.annotations.KeelPrivate;
import io.vertx.core.Vertx;

import javax.annotation.Nonnull;
import java.util.Objects;

@KeelPrivate
public final class KeelVertxKeeper {
    private static Vertx vertx;

    @Nonnull
    public static Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    public static void setVertx(@Nonnull Vertx vertx) {
        KeelVertxKeeper.vertx = vertx;
    }
}
