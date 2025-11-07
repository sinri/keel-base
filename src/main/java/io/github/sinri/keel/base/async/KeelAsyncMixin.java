package io.github.sinri.keel.vertx.async;

import io.github.sinri.keel.facade.KeelInstance;

/**
 * KeelAsyncMixin is a mixin interface that provides utility methods for
 * performing asynchronous operations in a more
 * structured and convenient manner. It includes methods for processing
 * collections and iterators, handling repeated
 * and iterative tasks, and managing exclusive access to resources.
 *
 * <p>
 * Use {@link KeelInstance#Keel} to use the methods defined by this interface
 *
 * @see KeelInstance#Keel
 * @since 4.0.0
 */
public interface KeelAsyncMixin extends KeelAsyncMixinParallel, KeelAsyncMixinLock, KeelAsyncMixinBlock {

}
