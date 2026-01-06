package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.logger.api.factory.BaseLoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

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
