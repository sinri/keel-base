package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.github.sinri.keel.logger.api.log.Log;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import java.util.List;

@NullMarked
public class QueuedLogWriterAdapterAnotherTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public QueuedLogWriterAdapterAnotherTest() {
        super();
    }

    @Test
    void test1(VertxTestContext testContext) throws Exception {
        AdapterImpl adapter = new AdapterImpl();
        adapter.deployMe(getVertx(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
               .compose(s -> {
                   getUnitTestLogger().info("deployMe: " + s);
                   adapter.accept("A", new Log()
                           .message("AAA"));
                   return asyncSleep(2000L);
               })
               .compose(v -> {
                   return adapter.undeployMe();
               })
               .onSuccess(v -> {
                   getUnitTestLogger().info("undeployMe");
                   testContext.completeNow();
               })
               .onFailure(testContext::failNow);
    }

    private static class AdapterImpl extends QueuedLogWriterAdapter {
        @Override
        protected Future<Void> processLogRecords(String topic, List<SpecificLog<?>> batch) {
            for (SpecificLog<?> specificLog : batch) {
                System.out.println(topic + " | " + specificLog.message());
            }
            return Future.succeededFuture();
        }

        @Override
        protected Future<Void> prepareForLoop() {
            return Future.succeededFuture();
        }
    }
}
