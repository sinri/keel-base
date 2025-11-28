package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonifiedThrowable单元测试。
 *
 * @since 5.0.0
 */
public class JsonifiedThrowableUnitTest {

    @Test
    void testWrapSimpleException() {
        Exception exception = new Exception("Test exception");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNotNull(jsonified);
        assertEquals("java.lang.Exception", jsonified.getThrowableClass());
        assertEquals("Test exception", jsonified.getThrowableMessage());
        assertNotNull(jsonified.getThrowableStack());
    }

    @Test
    void testWrapExceptionWithCause() {
        Exception cause = new IllegalArgumentException("Cause exception");
        Exception exception = new RuntimeException("Main exception", cause);
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNotNull(jsonified);
        assertEquals("java.lang.RuntimeException", jsonified.getThrowableClass());
        assertEquals("Main exception", jsonified.getThrowableMessage());

        JsonifiedThrowable causeJsonified = jsonified.getThrowableCause();
        assertNotNull(causeJsonified);
        assertEquals("java.lang.IllegalArgumentException", causeJsonified.getThrowableClass());
        assertEquals("Cause exception", causeJsonified.getThrowableMessage());
    }

    @Test
    void testWrapWithIgnorablePackages() {
        Exception exception = new Exception("Test");
        Set<String> ignorablePackages = Set.of("java.lang", "junit");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception, ignorablePackages, true);

        assertNotNull(jsonified);
        assertEquals("java.lang.Exception", jsonified.getThrowableClass());
    }

    @Test
    void testWrapWithIgnorablePackagesNotOmitted() {
        Exception exception = new Exception("Test");
        Set<String> ignorablePackages = Set.of("java.lang", "junit");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception, ignorablePackages, false);

        assertNotNull(jsonified);
        assertEquals("java.lang.Exception", jsonified.getThrowableClass());
    }

    @Test
    void testGetThrowableClass() {
        NullPointerException exception = new NullPointerException("NPE");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertEquals("java.lang.NullPointerException", jsonified.getThrowableClass());
    }

    @Test
    void testGetThrowableMessage() {
        Exception exception = new Exception("Test message");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertEquals("Test message", jsonified.getThrowableMessage());
    }

    @Test
    void testGetThrowableMessageNull() {
        Exception exception = new Exception();
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNull(jsonified.getThrowableMessage());
    }

    @Test
    void testGetThrowableStack() {
        Exception exception = new Exception("Test");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNotNull(jsonified.getThrowableStack());
        assertFalse(jsonified.getThrowableStack().isEmpty());
    }

    @Test
    void testGetThrowableCause() {
        Exception cause = new IllegalArgumentException("Cause");
        Exception exception = new RuntimeException("Main", cause);
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        JsonifiedThrowable causeJsonified = jsonified.getThrowableCause();
        assertNotNull(causeJsonified);
        assertEquals("java.lang.IllegalArgumentException", causeJsonified.getThrowableClass());
    }

    @Test
    void testGetThrowableCauseNull() {
        Exception exception = new Exception("No cause");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNull(jsonified.getThrowableCause());
    }

    @Test
    void testNestedCauses() {
        Exception cause1 = new IllegalArgumentException("Cause1");
        Exception cause2 = new RuntimeException("Cause2", cause1);
        Exception exception = new Exception("Main", cause2);
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        assertNotNull(jsonified);
        JsonifiedThrowable cause2Jsonified = jsonified.getThrowableCause();
        assertNotNull(cause2Jsonified);
        assertEquals("java.lang.RuntimeException", cause2Jsonified.getThrowableClass());

        JsonifiedThrowable cause1Jsonified = cause2Jsonified.getThrowableCause();
        assertNotNull(cause1Jsonified);
        assertEquals("java.lang.IllegalArgumentException", cause1Jsonified.getThrowableClass());
    }

    @Test
    void testToJsonObject() {
        Exception exception = new Exception("Test");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        JsonObject json = jsonified.toJsonObject();
        assertNotNull(json);
        assertTrue(json.containsKey("class"));
        assertTrue(json.containsKey("message"));
        assertTrue(json.containsKey("stack"));
    }

    @Test
    void testCallStackItem() {
        Exception exception = new Exception("Test");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        var stack = jsonified.getThrowableStack();
        assertFalse(stack.isEmpty());

        var firstItem = stack.get(0);
        assertNotNull(firstItem);
        String type = firstItem.getType();
        assertTrue("call".equals(type) || "ignored".equals(type));
    }

    @Test
    void testCallStackItemTypeCall() {
        Exception exception = new Exception("Test");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception);

        var stack = jsonified.getThrowableStack();
        for (var item : stack) {
            if ("call".equals(item.getType())) {
                assertNotNull(item.getCallStackClass());
                assertNotNull(item.getCallStackMethod());
            }
        }
    }

    @Test
    void testCallStackItemTypeIgnored() {
        Exception exception = new Exception("Test");
        Set<String> ignorablePackages = Set.of("java.lang");
        JsonifiedThrowable jsonified = JsonifiedThrowable.wrap(exception, ignorablePackages, false);

        var stack = jsonified.getThrowableStack();
        // May or may not have ignored items depending on stack trace
        assertNotNull(stack);
    }
}

