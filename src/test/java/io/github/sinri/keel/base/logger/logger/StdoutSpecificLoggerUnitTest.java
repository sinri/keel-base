package io.github.sinri.keel.base.logger.logger;

import io.github.sinri.keel.logger.api.log.Log;
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
        StdoutSpecificLogger<Log> logger = new StdoutSpecificLogger<>("test-topic", Log::new);
        assertNotNull(logger);
    }

    @Test
    void testCreateLoggerWithDifferentTopics() {
        StdoutSpecificLogger<Log> logger1 = new StdoutSpecificLogger<>("topic1", Log::new);
        StdoutSpecificLogger<Log> logger2 = new StdoutSpecificLogger<>("topic2", Log::new);

        assertNotNull(logger1);
        assertNotNull(logger2);
    }
}

