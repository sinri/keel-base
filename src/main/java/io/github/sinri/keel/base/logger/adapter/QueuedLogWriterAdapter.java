package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.adapter.PersistentLogWriterAdapter;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个基于队列处理的持久性日志写入适配器实现。
 *
 * @since 5.0.0
 */
public abstract class QueuedLogWriterAdapter extends AbstractKeelVerticle implements PersistentLogWriterAdapter {
    @NotNull
    private final Map<String, Queue<SpecificLog<?>>> queueMap = new ConcurrentHashMap<>();
    @NotNull
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    public QueuedLogWriterAdapter(@NotNull Keel keel) {
        super(keel);
    }

    /**
     * 按需重载以改写缓冲区大小。
     *
     * @return 缓冲区大小，即每次处理多少条日志记录。
     */
    protected int bufferSize() {
        return 128;
    }

    @NotNull
    abstract protected Future<Void> processLogRecords(@NotNull String topic, @NotNull List<SpecificLog<?>> batch);

    @Override
    protected @NotNull Future<Void> startVerticle() {
        getKeel().asyncCallRepeatedly(repeatedlyCallTask -> {
                Set<String> topics = this.queueMap.keySet();
                AtomicInteger counter = new AtomicInteger(0);
                     return getKeel().asyncCallIteratively(topics, topic -> {
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
                                   return getKeel().asyncSleep(100L);
                               } else {
                                   return Future.succeededFuture();
                               }
                           });
            })
                 .onComplete(ar -> {
                this.undeployMe();
            });
        return Future.succeededFuture();
    }

    @Override
    public void accept(@NotNull String topic, @NotNull SpecificLog<?> log) {
        this.queueMap.computeIfAbsent(topic, k -> new SynchronousQueue<>())
                     .add(log);
    }

    @Override
    public void close() {
        closeFlag.set(true);
    }
}
