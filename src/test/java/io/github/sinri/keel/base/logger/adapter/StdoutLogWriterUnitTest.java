package io.github.sinri.keel.base.logger.adapter;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StdoutLogWriter单元测试。
 *
 * @since 5.0.0
 */
class StdoutLogWriterUnitTest {

    @Test
    void testGetInstance() {
        StdoutLogWriter instance1 = StdoutLogWriter.getInstance();
        StdoutLogWriter instance2 = StdoutLogWriter.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testRenderClassification() {
        StdoutLogWriter writer = StdoutLogWriter.getInstance();
        List<String> classification = Arrays.asList("ERROR", "DATABASE", "CONNECTION");

        String rendered = writer.renderClassification(classification);
        assertNotNull(rendered);

        JsonArray array = new JsonArray(rendered);
        assertEquals(3, array.size());
        assertEquals("ERROR", array.getString(0));
        assertEquals("DATABASE", array.getString(1));
        assertEquals("CONNECTION", array.getString(2));
    }

    @Test
    void testRenderClassificationEmpty() {
        StdoutLogWriter writer = StdoutLogWriter.getInstance();
        List<String> classification = List.of();

        String rendered = writer.renderClassification(classification);
        assertNotNull(rendered);

        JsonArray array = new JsonArray(rendered);
        assertEquals(0, array.size());
    }

    @Test
    void testRenderContext() {
        StdoutLogWriter writer = StdoutLogWriter.getInstance();
        Map<String, Object> context = new HashMap<>();
        context.put("userId", "12345");
        context.put("action", "login");
        context.put("timestamp", 1234567890L);

        String rendered = writer.renderContext(context);
        assertNotNull(rendered);

        JsonObject json = new JsonObject(rendered);
        assertEquals("12345", json.getString("userId"));
        assertEquals("login", json.getString("action"));
        assertEquals(1234567890L, json.getLong("timestamp"));
    }

    @Test
    void testRenderContextEmpty() {
        StdoutLogWriter writer = StdoutLogWriter.getInstance();
        Map<String, Object> context = new HashMap<>();

        String rendered = writer.renderContext(context);
        assertNotNull(rendered);

        JsonObject json = new JsonObject(rendered);
        assertTrue(json.isEmpty());
    }
}

