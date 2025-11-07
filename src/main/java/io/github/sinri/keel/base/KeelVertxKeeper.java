package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.annotations.KeelPrivate;
import io.vertx.core.Vertx;

import javax.annotation.Nonnull;
import java.util.Objects;

@KeelPrivate
public final class KeelVertxKeeper {
    private Vertx vertx;

    @Nonnull
    public Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    public void setVertx(@Nonnull Vertx vertx) {
        this.vertx = vertx;
    }
}
