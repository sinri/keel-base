package io.github.sinri.keel.base.logger.metric;

import io.github.sinri.keel.base.Keel;
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
    @NotNull
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    @NotNull
    private final Queue<MetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public AbstractMetricRecorder(@NotNull Keel keel) {
        super(keel);
    }

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
     * 重载以改变指标记录的主题。
     * @return 指标记录的主题
     */
    @NotNull
    protected String topic() {
        return "metric";
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> {
                  List<MetricRecord> buffer = new ArrayList<>();

                  while (true) {
                      MetricRecord metricRecord = metricRecordQueue.poll();
                      if (metricRecord == null)
                          break;

                      buffer.add(metricRecord);
                      if (buffer.size() >= bufferSize())
                          break;
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
     * 标记结束，停止记录。
     * <p>
     * 一旦调用此方法，记录器将停止处理，并且不会记录额外的指标。
     * <p>
     * 应该调用此方法来正确终止记录器的生命周期并释放相关资源，如有必要则重载以完善。
     */
    @Override
    public void close() {
        endSwitch.set(true);
    }

    /**
     * 处理指定主题下的一批指标记录。
     *
     * @param topic  主题
     * @param buffer 指标记录缓冲区
     * @return 处理结果
     */
    abstract protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer);
}
