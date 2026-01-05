package io.github.sinri.keel.base.logger.factory;

import io.github.sinri.keel.base.logger.adapter.StdoutLogWriter;
import io.github.sinri.keel.logger.api.log.Log;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StdoutLoggerFactory单元测试。
 *
 * @since 5.0.0
 */
class StdoutLoggerFactoryUnitTest {

    @Test
    void testGetInstance() {
        StdoutLoggerFactory instance1 = StdoutLoggerFactory.getInstance();
        StdoutLoggerFactory instance2 = StdoutLoggerFactory.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testSharedAdapter() {
        StdoutLoggerFactory factory = StdoutLoggerFactory.getInstance();
        assertNotNull(factory.sharedAdapter());
        assertInstanceOf(StdoutLogWriter.class, factory.sharedAdapter());
    }

    @Test
    void testCreateLogger() {
        StdoutLoggerFactory factory = StdoutLoggerFactory.getInstance();
        Logger logger = factory.createLogger("test-topic");

        assertNotNull(logger);
    }

    @Test
    void testCreateLoggerWithDifferentTopics() {
        StdoutLoggerFactory factory = StdoutLoggerFactory.getInstance();
        Logger logger1 = factory.createLogger("topic1");
        Logger logger2 = factory.createLogger("topic2");

        assertNotNull(logger1);
        assertNotNull(logger2);
    }

    @Test
    void testCreateSpecificLogger() {
        StdoutLoggerFactory factory = StdoutLoggerFactory.getInstance();
        SpecificLogger<Log> logger = factory.createLogger("test-topic", Log::new);

        assertNotNull(logger);
    }
}

