package io.github.sinri.keel.base.logger.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * StdoutLogger单元测试。
 *
 * @since 5.0.0
 */
class StdoutLoggerUnitTest {

    @Test
    void testCreateLogger() {
        StdoutLogger logger = new StdoutLogger("test-topic");
        assertNotNull(logger);
    }

    @Test
    void testCreateLoggerWithDifferentTopics() {
        StdoutLogger logger1 = new StdoutLogger("topic1");
        StdoutLogger logger2 = new StdoutLogger("topic2");

        assertNotNull(logger1);
        assertNotNull(logger2);
    }
}

