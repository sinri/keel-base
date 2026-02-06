package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.logger.api.factory.BaseLoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 文件日志工厂的测试实现。
 * <p>
 * 本类用于在测试中创建并持有一个基于 {@link TestFileLogWriter} 的 LoggerFactory，
 * 并将写入适配器部署到指定的 Vertx 实例上。
 *
 * @since 5.0.0
 */
@NullMarked
public class TestFileLoggerFactory extends BaseLoggerFactory {
    public TestFileLoggerFactory(Vertx vertx) {
        super(new TestFileLogWriter());
        ((TestFileLogWriter) (this.sharedAdapter()))
                .deployMe(
                        vertx,
                        new DeploymentOptions()
                                .setThreadingModel(ThreadingModel.WORKER)
                )
                .onSuccess(s -> System.out.println("Deployed: " + s));
    }
}
