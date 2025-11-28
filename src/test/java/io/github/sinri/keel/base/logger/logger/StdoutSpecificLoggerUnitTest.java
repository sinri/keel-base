package io.github.sinri.keel.base.logger.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * StdoutSpecificLogger单元测试。
 *
 * @since 5.0.0
 */
class StdoutSpecificLoggerUnitTest {

    @Test
    void testCreateLogger() {
        StdoutSpecificLogger<?> logger = new StdoutSpecificLogger<>("test-topic", () -> null);
        assertNotNull(logger);
    }

    @Test
    void testCreateLoggerWithDifferentTopics() {
        StdoutSpecificLogger<?> logger1 = new StdoutSpecificLogger<>("topic1", () -> null);
        StdoutSpecificLogger<?> logger2 = new StdoutSpecificLogger<>("topic2", () -> null);

        assertNotNull(logger1);
        assertNotNull(logger2);
    }
}

