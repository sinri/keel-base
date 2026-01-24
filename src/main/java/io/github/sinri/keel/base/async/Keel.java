package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.VertxWrapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@NullMarked
public class Keel extends VertxWrapper implements KeelAsyncMixin {
    private static final AtomicReference<@Nullable Keel> sharedKeelRef = new AtomicReference<>();

    public Keel(Vertx vertx) {
        this((VertxInternal) vertx);
    }

    public Keel(VertxInternal delegate) {
        super(delegate);
    }

    public static Keel shared() {
        Keel keel = sharedKeelRef.get();
        if (keel == null) {
            throw new IllegalStateException("Shared Keel has been initialized");
        }
        return keel;
    }

    public static void share(Keel keel) {
        sharedKeelRef.set(keel);
    }

    public static void share(Vertx vertx) {
        if (vertx instanceof Keel keel) {
            sharedKeelRef.set(keel);
        } else {
            sharedKeelRef.set(new Keel(vertx));
        }
    }

    public static Keel ensureShared(Supplier<Keel> supplier) {
        Keel keel = sharedKeelRef.get();
        if (keel == null) {
            keel = supplier.get();
            sharedKeelRef.set(keel);
        }
        return keel;
    }

    @Override
    public final Vertx getVertx() {
        return this;
    }


}
