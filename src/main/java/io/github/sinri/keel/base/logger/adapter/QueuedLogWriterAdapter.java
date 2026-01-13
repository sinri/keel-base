package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.adapter.LogWriterAdapter;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个基于队列处理的持久性日志写入适配器实现。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class QueuedLogWriterAdapter extends KeelVerticleBase implements LogWriterAdapter {
    private final Map<String, Queue<SpecificLog<?>>> queueMap = new ConcurrentHashMap<>();
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);
    private final Promise<Void> endedPromise = Promise.promise();

    public QueuedLogWriterAdapter() {
        super();
    }

    /**
     * 按需重载以改写缓冲区大小。
     *
     * @return 缓冲区大小，即每次处理多少条日志记录。
     */
    protected int bufferSize() {
        return 128;
    }

    abstract protected Future<Void> processLogRecords(String topic, List<SpecificLog<?>> batch);

    @Override
    protected Future<Void> startVerticle() {
        System.out.println("Starting LogWriterAdapter preparation");
        return prepareForLoop()
                .compose(v -> {
                    System.out.println("Starting LogWriterAdapter prepared");
                    runLoop();
                    System.out.println("Started Loop of LogWriterAdapter");
                    return Future.succeededFuture();
                });
    }

    protected abstract Future<Void> prepareForLoop();

    private void runLoop() {
        asyncCallRepeatedly(repeatedlyCallTask -> {
            Set<String> topics = this.queueMap.keySet();
            AtomicInteger counter = new AtomicInteger(0);
            return asyncCallIteratively(topics, topic -> {
                Queue<SpecificLog<?>> queue = this.queueMap.get(topic);
                List<SpecificLog<?>> bufferOfTopic = new ArrayList<>();
                while (true) {
                    SpecificLog<?> r = queue.poll();
                    if (r == null) break;
                    bufferOfTopic.add(r);
                    counter.incrementAndGet();
                    if (bufferOfTopic.size() >= this.bufferSize()) {
                        break;
                    }
                }

                if (bufferOfTopic.isEmpty()) return Future.succeededFuture();

                return processLogRecords(topic, bufferOfTopic);
            })
                    .eventually(() -> {
                        if (counter.get() == 0) {
                            if (closeFlag.get()) {
                                repeatedlyCallTask.stop();
                                return Future.succeededFuture();
                            }
                            return asyncSleep(100L);
                        } else {
                            return Future.succeededFuture();
                        }
                    });
        })
                .onComplete(endedPromise::handle);
    }

    @Override
    public void accept(String topic, SpecificLog<?> log) {
        if (closeFlag.get()) return;
        this.queueMap.computeIfAbsent(topic, k -> new ConcurrentLinkedQueue<>())
                     .add(log);
    }

    @Override
    protected Future<?> stopVerticle() {
        closeFlag.set(true);
        return endedPromise.future()
                           .eventually(Future::succeededFuture);
    }
}
