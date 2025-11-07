package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.KeelVertxKeeper;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nullable;


interface KeelAsyncMixinCore {
    /**
     * Asynchronously sleeps for a specified amount of time.
     *
     * @param time the duration to sleep in milliseconds
     * @return a Future that completes after the specified time has elapsed
     */
    default Future<Void> asyncSleep(long time) {
        return asyncSleep(time, null);
    }

    /**
     * Asynchronously sleeps for a specified amount of time, with an optional
     * interrupter.
     *
     * @param time        the duration to sleep in milliseconds. If less than 1, it
     *                    will be set to 1.
     * @param interrupter an optional Promise that, when completed, will cancel the
     *                    sleep and complete the returned
     *                    Future.
     * @return a Future that completes after the specified time has elapsed, or is
     *         interrupted by the interrupter.
     */
    default Future<Void> asyncSleep(long time, @Nullable Promise<Void> interrupter) {
        Promise<Void> promise = Promise.promise();
        time = Math.max(1, time);
        long timer_id = KeelVertxKeeper.getVertx().setTimer(time, timerID -> {
            promise.complete();
        });
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                KeelVertxKeeper.getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        return promise.future();
    }
}
