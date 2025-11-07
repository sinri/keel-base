package io.github.sinri.keel.vertx.async;

import io.github.sinri.keel.facade.KeelInstance;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Use {@link KeelInstance#Keel} to use the methods defined by this interface
 *
 * @see KeelInstance#Keel
 * @since 4.1.0
 */
interface KeelAsyncMixinLogic extends KeelAsyncMixinCore {
    /**
     * Initiates a task that will be called repeatedly until the task itself signals
     * to stop.
     *
     * @param repeatedlyCallTask the task to be executed repeatedly, which includes
     *                           a processor function and a stopping
     *                           condition
     * @return a Future that completes with Void when the repeatedly called task has
     *         finished its execution
     */
    private Future<Void> asyncCallRepeatedly(@Nonnull RepeatedlyCallTask repeatedlyCallTask) {
        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(repeatedlyCallTask, promise);
        return promise.future();
    }

    /**
     * Initiates a task that will be called repeatedly until the task itself signals
     * to stop.
     *
     * @param processor the function that defines the task to be executed and
     *                  returns a future. The function is provided
     *                  with
     *                  a {@link RepeatedlyCallTask} instance, which can be used to
     *                  signal the task to stop by calling
     *                  its
     *                  {@code stop()} method.
     * @return a Future that completes with Void when the repeatedly called task has
     *         finished its execution.
     */
    default Future<Void> asyncCallRepeatedly(@Nonnull Function<RepeatedlyCallTask, Future<Void>> processor) {
        return asyncCallRepeatedly(new RepeatedlyCallTask(processor));
    }

    /**
     * Processes items from the given iterator in batches, invoking a provided
     * processor function for each batch.
     * The method continues to process items until the iterator is exhausted. The
     * size of each batch is determined
     * by the batchSize parameter. If the iterator has fewer items than the
     * specified batch size, the remaining items
     * are processed as the final batch.
     *
     * @param <T>            the type of elements in the iterator
     * @param iterator       the iterator of items to process
     * @param itemsProcessor the function to apply to each batch of items, which
     *                       returns a future
     * @param batchSize      the number of items to process in each batch
     * @return a Future that completes with Void when all the futures returned by
     *         the itemsProcessor have completed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
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
     * Asynchronously processes items from the provided iterator in batches.
     *
     * @param iterator       the iterator that provides the items to be processed
     * @param itemsProcessor a function that takes a list of items and returns a
     *                       future representing the asynchronous
     *                       processing of these items
     * @param batchSize      the number of items to process in each batch
     * @return a future that completes when all items have been processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<List<T>, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(
                iterator,
                (ts, repeatedlyCallTask) -> itemsProcessor.apply(ts),
                batchSize);
    }

    /**
     * Asynchronously processes items from the given iterable in batches.
     *
     * @param <T>            the type of elements in the iterable
     * @param iterable       the iterable containing items to be processed
     * @param itemsProcessor a function that takes a list of items and a task, and
     *                       returns a {@code Future<Void>}
     * @param batchSize      the size of each batch to process
     * @return a Future representing the completion of the processing
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<List<T>, RepeatedlyCallTask, Future<Void>> itemsProcessor,
            int batchSize) {
        return asyncCallIteratively(iterable.iterator(), itemsProcessor, batchSize);
    }

    /**
     * Asynchronously processes items from an iterator using a provided item
     * processor function.
     * The processing continues until the iterator is exhausted.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator to process items from
     * @param itemProcessor a function that takes an item from the iterator and a
     *                      RepeatedlyCallTask,
     *                      and returns a {@link Future<Void>} representing the
     *                      asynchronous processing of the item
     * @return a {@link Future<Void>} that completes when all items have been
     *         processed or fails if any processing step
     *         fails
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
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
     * Processes each item in the given iterator asynchronously and iteratively.
     *
     * @param <T>           the type of elements in the iterator
     * @param iterator      the iterator containing items to be processed
     * @param itemProcessor a function that takes an item from the iterator and
     *                      returns a Future representing the
     *                      asynchronous processing of that item
     * @return a Future that completes when all items have been processed, or fails
     *         if any item processing fails
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterator<T> iterator,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(
                iterator,
                (t, repeatedlyCallTask) -> itemProcessor.apply(t));
    }

    /**
     * Asynchronously processes each item in the provided iterable using the
     * specified item processor function.
     * The items are processed sequentially, ensuring that the next item is only
     * processed after the current one has
     * completed.
     *
     * @param iterable      an iterable containing the items to be processed
     * @param itemProcessor a function that takes an item from the iterable and
     *                      returns a Future representing the
     *                      asynchronous processing of the item
     * @return a Future that completes when all items in the iterable have been
     *         processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull Function<T, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * Asynchronously processes each item in the given iterable using the provided
     * item processor function.
     *
     * @param <T>           the type of elements in the iterable
     * @param iterable      the iterable containing items to be processed
     * @param itemProcessor a function that takes an item and a task, and returns a
     *                      Future representing the asynchronous
     *                      processing of the item
     * @return a Future that completes when all items have been processed
     */
    default <T> Future<Void> asyncCallIteratively(
            @Nonnull Iterable<T> iterable,
            @Nonnull BiFunction<T, RepeatedlyCallTask, Future<Void>> itemProcessor) {
        return asyncCallIteratively(iterable.iterator(), itemProcessor);
    }

    /**
     * Executes a series of asynchronous calls in a stepwise manner, from the start
     * value to the end value, with a
     * specified step.
     * As of 4.0.9, fix the bug about range break.
     *
     * @param start     the starting value for the stepwise execution
     * @param end       the ending value for the stepwise execution, must be equal
     *                  to or greater than `start`; if the pointer is stepped to be
     *                  equal to or greater than `end`, the task will be stopped.
     * @param step      the step size to increment or decrement at each call, must
     *                  be greater than 0.
     * @param processor a function that processes the current value and a task,
     *                  returning a Future
     * @return a Future that completes when all stepwise calls have been processed
     * @throws IllegalArgumentException if the step is 0
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
     * Executes a given processor function stepwise for a specified number of times.
     *
     * @param times     the total number of times to execute the processor function
     * @param processor the function to be executed, which takes the current step
     *                  and a task as arguments and returns a
     *                  Future
     * @return a Future that completes when all steps have been processed
     */
    default Future<Void> asyncCallStepwise(long times, BiFunction<Long, RepeatedlyCallTask, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, processor);
    }

    /**
     * Executes a given asynchronous task multiple times in a stepwise manner.
     *
     * @param times     the number of times to execute the task
     * @param processor the function that returns a future, representing the
     *                  asynchronous task to be executed
     * @return a Future that completes when all invocations of the processor have
     *         completed
     */
    default Future<Void> asyncCallStepwise(long times, Function<Long, Future<Void>> processor) {
        if (times <= 0) {
            return Future.succeededFuture();
        }
        return asyncCallStepwise(0, times, 1, (aLong, repeatedlyCallTask) -> processor.apply(aLong));
    }

    /**
     * Repeatedly calls the given supplier to execute an asynchronous job.
     * The job is executed in a loop until it is manually stopped.
     * Each call to the supplier should return a {@code Future<Void>} which
     * represents the completion of the asynchronous task.
     *
     * <p>
     * <strong>Note:</strong> This method will continue executing even if the
     * supplier
     * fails, as failures are converted to success to keep the loop running.
     * If you need to stop on failure, use {@link #asyncCallRepeatedly(Function)}
     * instead.
     *
     * @param supplier An async job handler that provides a {@code Future<Void>}.
     * @since 3.0.1
     */
    default void asyncCallEndlessly(@Nonnull Supplier<Future<Void>> supplier) {
        asyncCallRepeatedly(routineResult -> Future.succeededFuture()
                                                   .compose(v -> supplier.get())
                                                   .eventually(() -> {
                                                       // Convert failures to success to keep the loop running
                                                       return Future.succeededFuture();
                                                   }));
    }

}
