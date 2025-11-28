package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigTree单元测试。
 *
 * @since 5.0.0
 */
class ConfigTreeUnitTest {
    private ConfigTree configTree;

    @BeforeEach
    void setUp() {
        configTree = new ConfigTree();
        ConfigElement app = configTree.ensureChild("app");
        app.ensureChild("name").setValue("TestApp");
        app.ensureChild("version").setValue("1.0.0");
        app.ensureChild("enabled").setValue("true");

        ConfigElement server = configTree.ensureChild("server");
        server.ensureChild("port").setValue("8080");
        server.ensureChild("host").setValue("localhost");

        ConfigElement numbers = configTree.ensureChild("numbers");
        numbers.ensureChild("int").setValue("42");
        numbers.ensureChild("long").setValue("123456789");
        numbers.ensureChild("float").setValue("3.14");
        numbers.ensureChild("double").setValue("2.71828");
    }

    @Test
    void testReadString() throws ConfigTree.NotConfiguredException {
        String appName = configTree.readString(List.of("app", "name"));
        assertEquals("TestApp", appName);

        String serverHost = configTree.readString(List.of("server", "host"));
        assertEquals("localhost", serverHost);
    }

    @Test
    void testReadStringThrowsException() {
        assertThrows(ConfigTree.NotConfiguredException.class, () -> {
            configTree.readString(List.of("non", "existent"));
        });
    }

    @Test
    void testReadBoolean() throws ConfigTree.NotConfiguredException {
        Boolean enabled = configTree.readBoolean(List.of("app", "enabled"));
        assertTrue(enabled);

        // Test with "YES"
        configTree.ensureChild("test").ensureChild("yes").setValue("YES");
        assertTrue(configTree.readBoolean(List.of("test", "yes")));

        // Test with "TRUE"
        configTree.ensureChild("test").ensureChild("true").setValue("TRUE");
        assertTrue(configTree.readBoolean(List.of("test", "true")));

        // Test with "false"
        configTree.ensureChild("test").ensureChild("false").setValue("false");
        assertFalse(configTree.readBoolean(List.of("test", "false")));
    }

    @Test
    void testReadInteger() throws ConfigTree.NotConfiguredException {
        Integer port = configTree.readInteger(List.of("server", "port"));
        assertEquals(8080, port);

        Integer intValue = configTree.readInteger(List.of("numbers", "int"));
        assertEquals(42, intValue);
    }

    @Test
    void testReadIntegerThrowsException() {
        assertThrows(ConfigTree.NotConfiguredException.class, () -> {
            configTree.readInteger(List.of("non", "existent"));
        });
    }

    @Test
    void testReadIntegerInvalidFormat() {
        configTree.ensureChild("invalid").ensureChild("number").setValue("not-a-number");
        assertThrows(NumberFormatException.class, () -> {
            configTree.readInteger(List.of("invalid", "number"));
        });
    }

    @Test
    void testReadLong() throws ConfigTree.NotConfiguredException {
        Long longValue = configTree.readLong(List.of("numbers", "long"));
        assertEquals(123456789L, longValue);
    }

    @Test
    void testReadLongThrowsException() {
        assertThrows(ConfigTree.NotConfiguredException.class, () -> {
            configTree.readLong(List.of("non", "existent"));
        });
    }

    @Test
    void testReadFloat() throws ConfigTree.NotConfiguredException {
        Float floatValue = configTree.readFloat(List.of("numbers", "float"));
        assertEquals(3.14f, floatValue, 0.001f);
    }

    @Test
    void testReadFloatThrowsException() {
        assertThrows(ConfigTree.NotConfiguredException.class, () -> {
            configTree.readFloat(List.of("non", "existent"));
        });
    }

    @Test
    void testReadDouble() throws ConfigTree.NotConfiguredException {
        Double doubleValue = configTree.readDouble(List.of("numbers", "double"));
        assertEquals(2.71828, doubleValue, 0.00001);
    }

    @Test
    void testReadDoubleThrowsException() {
        assertThrows(ConfigTree.NotConfiguredException.class, () -> {
            configTree.readDouble(List.of("non", "existent"));
        });
    }

    @Test
    void testNotConfiguredException() {
        ConfigTree.NotConfiguredException exception = new ConfigTree.NotConfiguredException(List.of("app", "name"));
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("app.name"));
    }

    @Test
    void testCreateFromConfigElement() {
        ConfigElement element = new ConfigElement("root");
        element.ensureChild("test").setValue("value");
        ConfigTree tree = new ConfigTree(element);

        try {
            String value = tree.readString(List.of("test"));
            assertEquals("value", value);
        } catch (ConfigTree.NotConfiguredException e) {
            fail("Should not throw exception");
        }
    }
}

