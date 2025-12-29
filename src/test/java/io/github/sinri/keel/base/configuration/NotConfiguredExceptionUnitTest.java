package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NotConfiguredException单元测试。
 * <p>
 * 测试NotConfiguredException异常类的各项功能。
 *
 * @since 5.0.0
 */
class NotConfiguredExceptionUnitTest {

    @Test
    void testConstructorWithSingleKey() {
        List<String> keychain = List.of("singleKey");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertNotNull(exception);
        assertEquals("Provided Keychain Not Configured: singleKey", exception.getMessage());
    }

    @Test
    void testConstructorWithMultipleKeys() {
        List<String> keychain = List.of("app", "name");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertNotNull(exception);
        assertEquals("Provided Keychain Not Configured: app.name", exception.getMessage());
    }

    @Test
    void testConstructorWithDeepKeychain() {
        List<String> keychain = List.of("database", "primary", "connection", "pool", "size");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertNotNull(exception);
        assertEquals("Provided Keychain Not Configured: database.primary.connection.pool.size",
                exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyKeychain() {
        List<String> keychain = List.of();
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertNotNull(exception);
        assertEquals("Provided Keychain Not Configured: ", exception.getMessage());
    }

    @Test
    void testExceptionIsThrowable() {
        NotConfiguredException exception = new NotConfiguredException(List.of("test"));
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(Throwable.class, exception);
    }

    @Test
    void testThrowException() {
        assertThrows(NotConfiguredException.class, () -> {
            throw new NotConfiguredException(List.of("missing", "key"));
        });
    }

    @Test
    void testCatchException() {
        try {
            throw new NotConfiguredException(List.of("test", "key"));
        } catch (NotConfiguredException e) {
            assertEquals("Provided Keychain Not Configured: test.key", e.getMessage());
        }
    }

    @Test
    void testExceptionInConfigTreeReadString() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readString(List.of("nonexistent", "key"))
        );

        assertEquals("Provided Keychain Not Configured: nonexistent.key", exception.getMessage());
    }

    @Test
    void testExceptionInConfigTreeReadInteger() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readInteger(List.of("missing", "port"))
        );

        assertEquals("Provided Keychain Not Configured: missing.port", exception.getMessage());
    }

    @Test
    void testExceptionInConfigTreeReadBoolean() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readBoolean(List.of("missing", "flag"))
        );

        assertEquals("Provided Keychain Not Configured: missing.flag", exception.getMessage());
    }

    @Test
    void testExceptionInConfigTreeReadLong() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readLong(List.of("missing", "value"))
        );

        assertEquals("Provided Keychain Not Configured: missing.value", exception.getMessage());
    }

    @Test
    void testExceptionInConfigTreeReadFloat() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readFloat(List.of("missing", "value"))
        );

        assertEquals("Provided Keychain Not Configured: missing.value", exception.getMessage());
    }

    @Test
    void testExceptionInConfigTreeReadDouble() {
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readDouble(List.of("missing", "value"))
        );

        assertEquals("Provided Keychain Not Configured: missing.value", exception.getMessage());
    }

    @Test
    void testExceptionWithNullValueInConfigTree() {
        ConfigNode rootNode = ConfigNode.create("root");
        rootNode.ensureChild("app").ensureChild("name").setValue(null);
        ConfigTree configTree = ConfigTree.wrap(rootNode);

        NotConfiguredException exception = assertThrows(NotConfiguredException.class, () ->
                configTree.readString(List.of("app", "name"))
        );

        assertEquals("Provided Keychain Not Configured: app.name", exception.getMessage());
    }

    @Test
    void testExceptionMessageFormatting() {
        List<String> keychain = List.of("level1", "level2", "level3");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        String message = exception.getMessage();
        assertTrue(message.startsWith("Provided Keychain Not Configured: "));
        assertTrue(message.contains("level1.level2.level3"));
    }

    @Test
    void testExceptionWithSingleCharacterKeys() {
        List<String> keychain = List.of("a", "b", "c", "d");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertEquals("Provided Keychain Not Configured: a.b.c.d", exception.getMessage());
    }

    @Test
    void testExceptionWithNumericKeys() {
        List<String> keychain = List.of("array", "0", "value");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertEquals("Provided Keychain Not Configured: array.0.value", exception.getMessage());
    }

    @Test
    void testExceptionWithSpecialCharacters() {
        List<String> keychain = List.of("app-name", "config_value");
        NotConfiguredException exception = new NotConfiguredException(keychain);

        assertEquals("Provided Keychain Not Configured: app-name.config_value", exception.getMessage());
    }

    @Test
    void testMultipleExceptionInstances() {
        NotConfiguredException exception1 = new NotConfiguredException(List.of("key1"));
        NotConfiguredException exception2 = new NotConfiguredException(List.of("key2"));

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Provided Keychain Not Configured: key1", exception1.getMessage());
        assertEquals("Provided Keychain Not Configured: key2", exception2.getMessage());
    }

    @Test
    void testExceptionStackTrace() {
        NotConfiguredException exception = new NotConfiguredException(List.of("test", "key"));

        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    void testExceptionCause() {
        NotConfiguredException exception = new NotConfiguredException(List.of("test", "key"));

        // NotConfiguredException doesn't have a cause by default
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionInTryCatchFinally() {
        boolean exceptionCaught = false;
        boolean finallyExecuted = false;

        try {
            ConfigNode rootNode = ConfigNode.create("root");
            ConfigTree configTree = ConfigTree.wrap(rootNode);
            configTree.readString(List.of("missing", "key"));
        } catch (NotConfiguredException e) {
            exceptionCaught = true;
            assertEquals("Provided Keychain Not Configured: missing.key", e.getMessage());
        } finally {
            finallyExecuted = true;
        }

        assertTrue(exceptionCaught);
        assertTrue(finallyExecuted);
    }

    @Test
    void testExceptionPropagation() {
        assertThrows(NotConfiguredException.class, () -> {
            helperMethodThatThrows();
        });
    }

    private void helperMethodThatThrows() throws NotConfiguredException {
        throw new NotConfiguredException(List.of("propagated", "exception"));
    }

    @Test
    void testExceptionWithVeryLongKeychain() {
        List<String> longKeychain = List.of(
                "level1", "level2", "level3", "level4", "level5",
                "level6", "level7", "level8", "level9", "level10"
        );
        NotConfiguredException exception = new NotConfiguredException(longKeychain);

        String expectedMessage = "Provided Keychain Not Configured: " +
                "level1.level2.level3.level4.level5.level6.level7.level8.level9.level10";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testExceptionToString() {
        NotConfiguredException exception = new NotConfiguredException(List.of("test", "key"));
        String toString = exception.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("NotConfiguredException"));
        assertTrue(toString.contains("Provided Keychain Not Configured: test.key"));
    }
}

