package io.github.sinri.keel.base.annotations;

import javax.annotation.Nonnull;


public interface SelfInterface<T> {
    @Nonnull
    T getImplementation();
}
