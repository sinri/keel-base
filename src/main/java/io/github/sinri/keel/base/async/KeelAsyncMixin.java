package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.annotations.KeelPrivate;

public interface KeelAsyncMixin extends KeelAsyncMixinParallel, KeelAsyncMixinLock, KeelAsyncMixinBlock {
    @KeelPrivate
    static KeelAsyncMixin getInstance() {
        return KeelAsyncMixinImpl.instance;
    }
}
