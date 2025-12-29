package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.VertxOptions;

import java.io.IOException;

public class Usage {
    public static void main(String[] args) throws IOException, InterruptedException {
        KeelSampleImpl.Keel.initializeVertxStandalone(new VertxOptions());
        KeelSampleImpl.Keel.getConfiguration().loadPropertiesFile("config.properties");

        TestFileLoggerFactory loggerFactory = new TestFileLoggerFactory(KeelSampleImpl.Keel);
        Logger logger = loggerFactory.createLogger("test");
        logger.info("hello world");

        Thread.sleep(5000);

        KeelSampleImpl.Keel.close();
    }
}
