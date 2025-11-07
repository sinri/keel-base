package io.github.sinri.keel.vertx;

import javax.annotation.Nonnull;


public interface SelfInterface<T> {
    @Nonnull
    T getImplementation();
}
