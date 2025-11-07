package io.github.sinri.keel.base;

import io.github.sinri.keel.base.annotations.KeelPrivate;
import io.github.sinri.keel.base.configuration.KeelConfigElement;
import io.vertx.core.Vertx;

import javax.annotation.Nonnull;
import java.util.Objects;

@KeelPrivate
public final class KeelBase {
    private static final @Nonnull KeelConfigElement configuration = new KeelConfigElement("");
    private static Vertx vertx;

    @Nonnull
    public static Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    public static void setVertx(@Nonnull Vertx vertx) {
        KeelBase.vertx = vertx;
    }

    @Nonnull
    public static KeelConfigElement getConfiguration() {
        return configuration;
    }
}
