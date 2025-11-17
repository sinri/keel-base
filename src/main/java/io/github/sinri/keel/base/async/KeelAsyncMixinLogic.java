package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 异步逻辑封装。
 *
 * @since 5.0.0
 */
interface KeelAsyncMixinLogic extends KeelAsyncMixinCore {
    /**
     * 执行基于给定的异步循环任务的异步循环调用。
     *
     * @param repeatedlyCallTask 给定的异步循环任务
     * @return 异步循环执行结果
     */
    private Future<Void> asyncCallRepeatedly(@NotNull RepeatedlyCallTask repeatedlyCallTask) {
        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(getVertx(), repeatedlyCallTask, promise);
        return promise.future();
    }

    /**
     * 执行基于给定的异步循环任务的异步循环调用。
     *
     * @param processor 用于构建异步循环任务的循环逻辑
     * @return 异步循环执行结果
     */
    default Future<Void> asyncCallRepeatedly(@NotNull Function<RepeatedlyCallTask, Future<Void>> processor) {
        return asyncCallRepeatedly(new RepeatedlyCallTask(processor));
    }

    /**
     * 针对一个迭代器，基于异步循环调用，进行异步批量迭代执行，并可以按需在迭代执行方法体里提前中断任务。
     *
     * @param <T>            迭代器内的迭代对象类型
     * @param iterator       迭代器
     * @param itemsProcessor 批量迭代执行逻辑
     * @param batchSize      批量执行量
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterator<T> iterator,
            @NotNull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize) {
        if (batchSize <= 0)
            throw new IllegalArgumentException("batchSize must be greater than 0");

        return asyncCallRepeatedly(repeatedlyCallTask -> {
            List<T> buffer = new ArrayList<>();

            while (buffer.size() < batchSize) {
                if (iterator.hasNext()) {
                    buffer.add(iterator.next());
                } else {
                    break;
                }
            }

            if (buffer.isEmpty()) {
                repeatedlyCallTask.stop();
                return Future.succeededFuture();
            }

            return itemsProcessor.apply(buffer, repeatedlyCallTask);
        });
    }

    /**
     * 针对一个迭代器，基于异步循环调用，进行异步批量迭代执行。
     *
     * @param iterator       迭代器
     * @param itemsProcessor 批量迭代执行逻辑
     * @param batchSize      批量执行量
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterator<T> iterator,
            @NotNull Function<List<T>, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(
                iterator,
                (ts, repeatedlyCallTask) -> itemsProcessor.apply(ts),
                batchSize);
    }

    /**
     * 针对一个可迭代物，基于异步循环调用，进行异步批量迭代执行。
     *
     * @param <T>            可迭代物的迭代对象的类型
     * @param iterable       可迭代物
     * @param itemsProcessor 批量迭代执行逻辑
     * @param batchSize      批量执行量
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterable<T> iterable,
            @NotNull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(iterable.iterator(), itemsProcessor, batchSize);
    }

    /**
     * 针对一个迭代器，基于异步循环调用，进行异步迭代执行，并可以按需在迭代执行方法体里提前中断任务。
     *
     * @param <T>           迭代器内的迭代对象类型
     * @param iterator      迭代器
     * @param itemProcessor 迭代执行逻辑
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterator<T> iterator,
            @NotNull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
        return asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                                                          .compose(v -> {
                                                              if (iterator.hasNext()) {
                                                                  return itemProcessor.apply(iterator.next(), routineResult);
                                                              } else {
                                                                  routineResult.stop();
                                                                  return Future.succeededFuture();
                                                              }
                                                          }));
    }

    /**
     * 针对一个迭代器，基于异步循环调用，进行异步迭代执行。
     *
     * @param <T>           迭代器内的迭代对象类型
     * @param iterator      迭代器
     * @param itemProcessor 迭代执行逻辑
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterator<T> iterator,
            @NotNull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(
                iterator,
                (t, repeatedlyCallTask) -> itemProcessor.apply(t));
    }

    /**
     * 针对一个可迭代物，基于异步循环调用，进行异步迭代执行。
     *
     * @param iterable      可迭代物
     * @param itemProcessor 迭代执行逻辑
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterable<T> iterable,
            @NotNull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * 针对一个可迭代物，基于异步循环调用，进行异步迭代执行，并可以按需在迭代执行方法体里提前中断任务
     *
     * @param <T>           迭代器内的迭代对象类型
     * @param iterable      可迭代物
     * @param itemProcessor 迭代执行逻辑
     * @return 异步循环执行结果
     */
    default <T> Future<Void> asyncCallIteratively(
            @NotNull Iterable<T> iterable,
            @NotNull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * 基于给定的起始、终止、步长数值，基于异步循环调用，进行异步步进循环。
     * <p>
     * 步进方向要求是增量且可达的；因此，如果起始数值大于终止数值，或步进数值小于等于 0，将抛出异常。
     *
     * @param start     起始数值。
     * @param end       终止数值
     * @param step      步长数值
     * @param processor 异步步进循环逻辑
     * @return 异步循环执行结果
     * @throws IllegalArgumentException 当步进不满足增量且可达时抛出
     */
    default Future<Void> asyncCallStepwise(long start, long end, long step,
                                           BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (step <= 0)
            throw new IllegalArgumentException("step must be greater than 0");
        if (start > end)
            throw new IllegalArgumentException("start must not be greater than end");
        AtomicLong ptr = new AtomicLong(start);
        return asyncCallRepeatedly(task -> Future.succeededFuture()
                                                 .compose(vv -> processor.apply(ptr.get(), task)
                                                                         .compose(v -> {
                                                                             long y = ptr.addAndGet(step);
                                                                             if (y >= end) {
                                                                                 task.stop();
                                                                             }
                                                                             return Future.succeededFuture();
                                                                         })));
    }

    /**
     * 基于异步循环调用，进行异步的指定次数步进循环，可以提前中断。
     * <p>
     * 基于{@link KeelAsyncMixinLogic#asyncCallStepwise(long, long, long, BiFunction)}，起始数值设定为 0、步长数值设定为 1。
     *
     * @param times     循环次数。即终止数值。当循环次数小于等于 0 时，将直接返回成功结果。
     * @param processor 异步步进循环逻辑
     * @return 异步循环执行结果
     */
    default Future<Void> asyncCallStepwise(long times, BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, processor);
    }

    /**
     * 基于异步循环调用，进行异步的指定次数步进循环。
     *
     * @param times     循环次数。即终止数值。当循环次数小于等于 0 时，将直接返回成功结果。
     * @param processor 异步步进循环逻辑
     * @return 异步循环执行结果
     */
    default Future<Void> asyncCallStepwise(long times, Function<Long, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, (aLong, repeatedlyCallTask) -> processor.apply(aLong));
    }

    /**
     * 无限循环执行一个异步逻辑，即时循环体抛出异常也不停止。
     * <p>
     * 使用本方法之前，应确保该逻辑符合要求且没有副作用。
     *
     * @param supplier 异步循环逻辑
     */
    default void asyncCallEndlessly(@NotNull Supplier<Future<Void>> supplier) {
        asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                                                   .compose(v -> supplier.get())
                                                   .eventually(Future::succeededFuture));
    }

}
