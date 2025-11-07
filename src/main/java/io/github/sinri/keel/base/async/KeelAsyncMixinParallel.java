package io.github.sinri.keel.vertx.async;

import io.github.sinri.keel.facade.KeelInstance;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Use {@link KeelInstance#Keel} to use the methods defined by this interface
 *
 * @see KeelInstance#Keel
 * @since 4.1.0
 */
interface KeelAsyncMixinParallel extends KeelAsyncMixinCore {
    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when all individual futures produced by the function have successfully
     * completed.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllSuccess(@Nonnull Iterable<T> collection,
                                                   @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllSuccess(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when all individual futures produced by the function have successfully
     * completed.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllSuccess(@Nonnull Iterator<T> iterator,
                                                   @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.all(futures)
                     .mapEmpty();
    }

    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when any of the individual futures produced by the function has successfully
     * completed.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when any of the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAnySuccess(@Nonnull Iterable<T> collection,
                                                   @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAnySuccess(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when any of the individual futures produced by the function has successfully
     * completed.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when any of the futures returned by
     *         the itemProcessor have succeeded
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAnySuccess(@Nonnull Iterator<T> iterator,
                                                   @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.any(futures)
                     .mapEmpty();
    }

    /**
     * Executes a given function in parallel for all items in the provided
     * collection, returning a future that completes
     * when all individual futures produced by the function have completed,
     * regardless of success or failure.
     *
     * @param <T>           the type of elements in the collection
     * @param collection    the iterable collection of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have completed
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllComplete(@Nonnull Iterable<T> collection,
                                                    @Nonnull Function<T, Future<Void>> itemProcessor) {
        return parallelForAllComplete(collection.iterator(), itemProcessor);
    }

    /**
     * Executes a given function in parallel for all items in the provided iterator,
     * returning a future that completes
     * when all individual futures produced by the function have completed,
     * regardless of success or failure.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator of items to process
     * @param itemProcessor the function to apply to each item, which returns a
     *                      future
     * @return a Future that completes with Void when all the futures returned by
     *         the itemProcessor have completed
     * @since 4.0.2
     */
    default <T> Future<Void> parallelForAllComplete(@Nonnull Iterator<T> iterator,
                                                    @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        while (iterator.hasNext()) {
            Future<Void> f = itemProcessor.apply(iterator.next());
            futures.add(f);
        }
        if (futures.isEmpty()) {
            return Future.succeededFuture();
        }
        return Future.join(futures)
                     .mapEmpty();
    }
}
