package io.github.sinri.keel.base.annotations;

import org.jetbrains.annotations.NotNull;


public interface SelfInterface<T> {
    @NotNull
    T getImplementation();
}
