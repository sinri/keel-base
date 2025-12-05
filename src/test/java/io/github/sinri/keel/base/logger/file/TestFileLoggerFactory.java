package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.logger.api.factory.BaseLoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;

public class TestFileLoggerFactory extends BaseLoggerFactory {
    public TestFileLoggerFactory(@NotNull Keel keel) {
        super(new TestFileLogWriter(keel));
        ((TestFileLogWriter) (this.sharedAdapter()))
                .deployMe(new DeploymentOptions()
                        .setThreadingModel(ThreadingModel.WORKER))
                .onSuccess(s -> System.out.println("Deployed: " + s));
    }
}
