package io.github.sinri.keel.base.logger.metric;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.github.sinri.keel.logger.api.metric.MetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jspecify.annotations.NullMarked;

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
@NullMarked
public abstract class AbstractMetricRecorder extends KeelVerticleBase implements MetricRecorder {
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    private final Promise<Void> endedPromise = Promise.promise();
    private final Queue<MetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public AbstractMetricRecorder() {
        super();
    }

    public void recordMetric(MetricRecord metricRecord) {
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
     *
     * @return 指标记录的主题
     */
    protected String topic() {
        return "metric";
    }

    @Override
    protected Future<Void> startVerticle() {
        runLoop();
        return Future.succeededFuture();
    }

    private void runLoop() {
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
                      getVertx().setTimer(100L, id -> startVerticle());
                  } else {
                      endedPromise.handle(ar);
                  }
              });
    }

    @Override
    protected Future<?> stopVerticle() {
        endSwitch.set(true);
        return endedPromise.future()
                           .eventually(Future::succeededFuture);
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
