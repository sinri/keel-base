package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.adapter.PersistentLogWriterAdapter;
import io.github.sinri.keel.logger.api.log.Log;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.base.KeelInstance.Keel;

/**
 * 一个基于队列处理的持久性日志写入适配器实现。
 *
 * @since 5.0.0
 */
public abstract class QueuedLogWriterAdapter extends AbstractKeelVerticle implements PersistentLogWriterAdapter {
    private final Map<String, Queue<Log>> queueMap = new ConcurrentHashMap<>();
    private final AtomicBoolean closeFlag = new AtomicBoolean(false);

    public QueuedLogWriterAdapter() {

    }

    protected int bufferSize() {
        return 128;
    }

    @NotNull
    abstract protected Future<Void> processLogRecords(@NotNull String topic, @NotNull List<Log> batch);

    @Override
    protected Future<Void> startVerticle() {
        Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
                Set<String> topics = this.queueMap.keySet();
                AtomicInteger counter = new AtomicInteger(0);
                return Keel.asyncCallIteratively(topics, topic -> {
                               Queue<Log> queue = this.queueMap.get(topic);
                               List<Log> bufferOfTopic = new ArrayList<>();
                               while (true) {
                                   Log r = queue.poll();
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
                                   return Keel.asyncSleep(100L);
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
    public void accept(@NotNull String topic, @NotNull Log loggingEntity) {
        this.queueMap.computeIfAbsent(topic, k -> new SynchronousQueue<>())
                     .add(loggingEntity);
    }

    @Override
    public void close() throws IOException {
        closeFlag.set(true);
    }
}
