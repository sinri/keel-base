package io.github.sinri.keel.base.logger.metric;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个基本的定量指标记录器实现，可供继承重载以完善。
 *
 * @since 5.0.0
 */
abstract public class AbstractMetricRecorder extends AbstractKeelVerticle implements MetricRecorder, Closeable {
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    private final Queue<MetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public void recordMetric(@NotNull MetricRecord metricRecord) {
        this.metricRecordQueue.add(metricRecord);
    }

    /**
     * 按需重载以改写缓冲区大小。
     *
     * @return 缓冲区大小，即每次处理多少条定量指标。
     */
    protected int bufferSize() {
        return 1000;
    }

    /**
     * Override this to change the topic of metric recorder.
     *
     */
    protected String topic() {
        return "metric";
    }

    @Override
    protected Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> {
                  List<MetricRecord> buffer = new ArrayList<>();

                  while (true) {
                      MetricRecord metricRecord = metricRecordQueue.poll();
                      if (metricRecord == null) break;

                      buffer.add(metricRecord);
                      if (buffer.size() >= bufferSize()) break;
                  }

                  if (!buffer.isEmpty()) {
                      return handleForTopic(topic(), buffer);
                  }

                  return Future.succeededFuture();
              })
              .andThen(ar -> {
                  if (!endSwitch.get()) {
                      context.owner().setTimer(1000L, id -> start());
                  }
              });
        return Future.succeededFuture();
    }

    /**
     * Closes the metric recorder by marking the end of metric recording operations.
     * Once this method is invoked, the recorder will stop processing and no additional
     * metrics will be recorded.
     * <p>
     * This method should be called to properly terminate the recorder's lifecycle
     * and release associated resources.
     * <p>
     * The override method should call the super method to ensure proper resource closure.
     *
     */
    @Override
    public void close() {
        endSwitch.set(true);
    }

    abstract protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer);
}
