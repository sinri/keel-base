package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * 异步并行逻辑
 *
 * @since 5.0.0
 */
interface KeelAsyncMixinParallel extends KeelAsyncMixinCore {
    @NotNull
    private <T> List<@NotNull Future<Void>> buildFutures(
            @NotNull Iterator<T> iterator,
            @NotNull Function<T, @NotNull Future<Void>> itemProcessor
    ) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        return futures;
    }

    @NotNull
    private <T> List<Future<Void>> buildFutures(@NotNull Iterable<T> iterable, @NotNull Function<T, @NotNull Future<Void>> itemProcessor) {
        return buildFutures(iterable.iterator(), itemProcessor);
    }

    /**
     * 基于一个可迭代物，迭代触发异步逻辑进行并行执行；各异步任务均成功才视为本次调用成功。
     *
     * @param <T>           可迭代物的迭代对象的类型
     * @param collection    可迭代物
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，各异步任务均成功才返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAllSuccess(@NotNull Iterable<T> collection,
                                                   @NotNull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllSuccess(collection.iterator(), itemProcessor);
    }

    /**
     * 基于一个迭代器，迭代触发异步逻辑进行并行执行；各异步任务均成功才视为本次调用成功。
     *
     * @param <T>           迭代器的迭代对象的类型
     * @param iterator      迭代器
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，各异步任务均成功才返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAllSuccess(@NotNull Iterator<T> iterator,
                                                   @NotNull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutures(iterator, itemProcessor);
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.all(futures)
                     .mapEmpty();
    }

    /**
     * 基于一个可迭代物，迭代触发异步逻辑进行并行执行；有一个异步任务成功即视为本次调用成功。
     *
     * @param <T>           可迭代物的迭代对象的类型
     * @param collection    可迭代物
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，有一个异步任务成功即返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAnySuccess(@NotNull Iterable<T> collection,
                                                   @NotNull Function<T, @NotNull Future<Void>> itemProcessor) {
        return parallelForAnySuccess(collection.iterator(), itemProcessor);
    }

    /**
     * 基于一个迭代器，迭代触发异步逻辑进行并行执行；有一个异步任务成功即视为本次调用成功。
     *
     * @param <T>           迭代器的迭代对象的类型
     * @param iterator      迭代器
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，有一个异步任务成功即返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAnySuccess(@NotNull Iterator<T> iterator,
                                                   @NotNull Function<T, @NotNull Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutures(iterator, itemProcessor);
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.any(futures)
                     .mapEmpty();
    }

    /**
     * 基于一个可迭代物，迭代触发异步逻辑进行并行执行；所有一个异步任务都执行完毕后视为本次调用成功。
     *
     * @param <T>           可迭代物的迭代对象的类型
     * @param collection    可迭代物
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，所有一个异步任务都执行完毕后返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAllComplete(@NotNull Iterable<T> collection,
                                                    @NotNull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllComplete(collection.iterator(), itemProcessor);
    }

    /**
     * 基于一个迭代器，迭代触发异步逻辑进行并行执行；所有一个异步任务都执行完毕后视为本次调用成功。
     *
     * @param <T>           迭代器的迭代对象的类型
     * @param iterator      迭代器
     * @param itemProcessor 针对迭代对象的异步处理逻辑
     * @return 一个异步结果，所有一个异步任务都执行完毕后返回成功，否则返回失败
     */
    @NotNull
    default <T> Future<Void> parallelForAllComplete(@NotNull Iterator<T> iterator,
                                                    @NotNull Function<T, @NotNull Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutures(iterator, itemProcessor);
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.join(futures).mapEmpty();
    }
}
