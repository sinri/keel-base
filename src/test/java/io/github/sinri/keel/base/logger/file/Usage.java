package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.KeelInstance;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.VertxOptions;

import java.io.IOException;

public class Usage {
    public static void main(String[] args) throws IOException, InterruptedException {
        KeelInstance.Keel.initializeVertxStandalone(new VertxOptions());
        KeelInstance.Keel.getConfiguration().loadPropertiesFile("config.properties");

        TestFileLoggerFactory loggerFactory = new TestFileLoggerFactory(KeelInstance.Keel);
        Logger logger = loggerFactory.createLogger("test");
        logger.info("hello world");

        Thread.sleep(5000);

        KeelInstance.Keel.close();
    }
}
