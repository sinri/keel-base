package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.Nullable;

/**
 * Keel 异步能力调用的核心定义。
 *
 * @since 5.0.0
 */
interface KeelAsyncMixinCore {
    /**
     * 获得一个 Vertx 实例。所有异步任务均基于此 Vertx 实例进行。
     *
     * @return 用于一切异步逻辑的 Vertx 实例。
     */
    Vertx getVertx();

    /**
     * 非阻塞地{@code 睡眠}一段时间。
     * <p>
     * 注意，这并不是真正意义上的线程睡眠，而是通过定时回调机制实现的，因此并不会阻塞线程，也不会阻止进程退出。
     *
     * @param time 以毫秒计的时间，有效值最小为 1 毫秒；如果值无效会强制置为 1毫秒。
     * @return 一个{@link Future}，表示设定时间已到
     */
    default Future<Void> asyncSleep(long time) {
        return asyncSleep(time, null);
    }

    /**
     * 非阻塞地{@code 睡眠}一段时间，并允许（提前）主动中断。
     *
     * @param time        以毫秒计的时间，有效值最小为 1 毫秒；如果值无效会强制置为 1毫秒。
     * @param interrupter 一个可选的{@link Promise}，供异步中断
     * @return 一个{@link Future}，表示设定时间已到，或设置的中断被触发。
     */
    default Future<Void> asyncSleep(long time, @Nullable Promise<Void> interrupter) {
        Promise<Void> promise = Promise.promise();
        time = Math.max(1, time);
        long timer_id = getVertx().setTimer(time, timerID -> {
            promise.complete();
        });
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        return promise.future();
    }
}
