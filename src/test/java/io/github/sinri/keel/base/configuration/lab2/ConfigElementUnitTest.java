package io.github.sinri.keel.base.configuration.lab2;

import io.github.sinri.keel.base.configuration.ConfigProperty;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigElement单元测试。
 * <p>
 * 测试ConfigElement类的各项功能。
 *
 * @since 5.0.0
 */
class ConfigElementUnitTest {
    private ConfigElement configElement;

    @BeforeEach
    void setUp() {
        configElement = new ConfigElement("root");
    }

    @Test
    void testConstructorWithName() {
        ConfigElement element = new ConfigElement("testNode");
        assertNotNull(element);
        assertEquals("testNode", element.getElementName());
    }

    @Test
    void testGetElementName() {
        assertEquals("root", configElement.getElementName());
    }

    @Test
    void testSetAndGetElementValue() throws NotConfiguredException {
        configElement.setElementValue("testValue");
        assertEquals("testValue", configElement.getElementValue());
    }

    @Test
    void testGetElementValueOnNonLeafNode() {
        configElement.ensureChild("child1");
        assertThrows(NotConfiguredException.class, () -> configElement.getElementValue());
    }

    @Test
    void testGetChildNames() {
        assertTrue(configElement.getChildNames().isEmpty());

        configElement.ensureChild("child1");
        configElement.ensureChild("child2");

        Set<String> childNames = configElement.getChildNames();
        assertEquals(2, childNames.size());
        assertTrue(childNames.contains("child1"));
        assertTrue(childNames.contains("child2"));
    }

    @Test
    void testGetChildNamesReturnsUnmodifiableSet() {
        Set<String> childNames = configElement.getChildNames();
        assertThrows(UnsupportedOperationException.class, () -> childNames.add("newChild"));
    }

    @Test
    void testEnsureChildCreatesNewChild() {
        ConfigElement child = configElement.ensureChild("newChild");
        assertNotNull(child);
        assertEquals("newChild", child.getElementName());
        assertTrue(configElement.getChildNames().contains("newChild"));
    }

    @Test
    void testEnsureChildReturnsExistingChild() {
        ConfigElement child1 = configElement.ensureChild("child");
        child1.setElementValue("value1");

        ConfigElement child2 = configElement.ensureChild("child");
        assertSame(child1, child2);
    }

    @Test
    void testAddChild() {
        ConfigElement child = new ConfigElement("childNode");
        child.setElementValue("childValue");

        configElement.addChild(child);

        assertTrue(configElement.getChildNames().contains("childNode"));
        assertEquals(child, configElement.getChild("childNode"));
    }

    @Test
    void testRemoveChild() {
        configElement.ensureChild("child1");
        configElement.ensureChild("child2");

        configElement.removeChild("child1");

        assertFalse(configElement.getChildNames().contains("child1"));
        assertTrue(configElement.getChildNames().contains("child2"));
    }

    @Test
    void testGetChild() {
        ConfigElement child = configElement.ensureChild("child");
        ConfigElement retrieved = configElement.getChild("child");

        assertNotNull(retrieved);
        assertSame(child, retrieved);
    }

    @Test
    void testGetChildReturnsNullForNonExistent() {
        ConfigElement child = configElement.getChild("nonExistent");
        assertNull(child);
    }

    @Test
    void testReloadDataWithSimpleProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");

        configElement.reloadData(properties);

        assertEquals("value1", configElement.getChild("key1").getElementValue());
        assertEquals("value2", configElement.getChild("key2").getElementValue());
    }

    @Test
    void testReloadDataWithNestedProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("database.url", "jdbc:mysql://localhost");

        configElement.reloadData(properties);

        ConfigElement server = configElement.getChild("server");
        assertNotNull(server);
        assertEquals("localhost", server.getChild("host").getElementValue());
        assertEquals("8080", server.getChild("port").getElementValue());

        ConfigElement database = configElement.getChild("database");
        assertNotNull(database);
        assertEquals("jdbc:mysql://localhost", database.getChild("url").getElementValue());
    }

    @Test
    void testReloadDataWithDeepNestedProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("a.b.c.d", "deepValue");

        configElement.reloadData(properties);

        ConfigElement a = configElement.getChild("a");
        assertNotNull(a);
        ConfigElement b = a.getChild("b");
        assertNotNull(b);
        ConfigElement c = b.getChild("c");
        assertNotNull(c);
        ConfigElement d = c.getChild("d");
        assertNotNull(d);
        assertEquals("deepValue", d.getElementValue());
    }


    @Test
    void testExtractWithVarargs() {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");

        configElement.reloadData(properties);

        ConfigElement extracted = configElement.extract("server", "host");
        assertNotNull(extracted);
        assertEquals("host", extracted.getElementName());
    }

    @Test
    void testExtractWithList() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");

        configElement.reloadData(properties);

        ConfigElement extracted = configElement.extract(Arrays.asList("server", "host"));
        assertNotNull(extracted);
        assertEquals("localhost", extracted.getElementValue());
    }

    @Test
    void testExtractReturnsNullForNonExistent() {
        ConfigElement extracted = configElement.extract("nonExistent", "path");
        assertNull(extracted);
    }

    @Test
    void testExtractWithEmptyKeychain() {
        ConfigElement extracted = configElement.extract(List.of());
        assertSame(configElement, extracted);
    }

    @Test
    void testTransformChildrenToPropertyList() {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("database.url", "jdbc:mysql://localhost");

        configElement.reloadData(properties);

        System.out.println(configElement.debugToString(0));

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();

        assertEquals(3, propertyList.size());

        // 验证属性按字典序排列
        ConfigProperty first = propertyList.get(0);
        assertEquals("jdbc:mysql://localhost", first.getPropertyValue());
        assertEquals("database.url", first.getPropertyName());

        ConfigProperty second = propertyList.get(1);
        assertEquals("localhost", second.getPropertyValue());
        assertEquals("server.host", second.getPropertyName());

        ConfigProperty third = propertyList.get(2);
        assertEquals("8080", third.getPropertyValue());
        assertEquals("server.port", third.getPropertyName());
    }

    @Test
    void testTransformChildrenToPropertyListWithEmptyNode() {
        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertTrue(propertyList.isEmpty());
    }

    @Test
    void testReadStringWithList() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "MyApp");

        configElement.reloadData(properties);

        String value = configElement.readString(Arrays.asList("app", "name"));
        assertEquals("MyApp", value);
    }

    @Test
    void testReadStringWithListThrowsNotConfiguredException() {
        assertThrows(NotConfiguredException.class,
                () -> configElement.readString(Arrays.asList("nonExistent", "key")));
    }

    @Test
    void testReadStringWithNonLeafNodeThrowsException() {
        configElement.ensureChild("parent").ensureChild("child");
        assertThrows(NotConfiguredException.class,
                () -> configElement.readString(List.of("parent")));
    }

    @Test
    void testReadStringWithDotJoinedKeyChain() {
        Properties properties = new Properties();
        properties.setProperty("app.version", "1.0.0");

        configElement.reloadData(properties);

        String value = configElement.readString("app.version");
        assertEquals("1.0.0", value);
    }

    @Test
    void testReadStringWithDotJoinedKeyChainReturnsNullForNonExistent() {
        String value = configElement.readString("nonExistent.key");
        assertNull(value);
    }

    @Test
    void testReadBoolean() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("flag.yes", "YES");
        properties.setProperty("flag.true", "TRUE");
        properties.setProperty("flag.True", "True");
        properties.setProperty("flag.false", "FALSE");
        properties.setProperty("flag.no", "NO");
        properties.setProperty("flag.other", "anything");

        configElement.reloadData(properties);

        assertTrue(configElement.readBoolean(Arrays.asList("flag", "yes")));
        assertTrue(configElement.readBoolean(Arrays.asList("flag", "true")));
        assertTrue(configElement.readBoolean(Arrays.asList("flag", "True")));
        assertFalse(configElement.readBoolean(Arrays.asList("flag", "false")));
        assertFalse(configElement.readBoolean(Arrays.asList("flag", "no")));
        assertFalse(configElement.readBoolean(Arrays.asList("flag", "other")));
    }

    @Test
    void testReadInteger() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.int", "42");
        properties.setProperty("number.negative", "-100");

        configElement.reloadData(properties);

        assertEquals(42, configElement.readInteger(Arrays.asList("number", "int")));
        assertEquals(-100, configElement.readInteger(Arrays.asList("number", "negative")));
    }

    @Test
    void testReadIntegerThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-number");

        configElement.reloadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readInteger(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadLong() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.long", "9223372036854775807");
        properties.setProperty("number.negative", "-9223372036854775808");

        configElement.reloadData(properties);

        assertEquals(9223372036854775807L, configElement.readLong(Arrays.asList("number", "long")));
        assertEquals(-9223372036854775808L, configElement.readLong(Arrays.asList("number", "negative")));
    }

    @Test
    void testReadLongThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-long");

        configElement.reloadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readLong(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadFloat() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.float", "3.14");
        properties.setProperty("number.negative", "-2.5");

        configElement.reloadData(properties);

        assertEquals(3.14f, configElement.readFloat(Arrays.asList("number", "float")), 0.001f);
        assertEquals(-2.5f, configElement.readFloat(Arrays.asList("number", "negative")), 0.001f);
    }

    @Test
    void testReadFloatThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-float");

        configElement.reloadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readFloat(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadDouble() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.double", "3.141592653589793");
        properties.setProperty("number.negative", "-2.718281828459045");

        configElement.reloadData(properties);

        assertEquals(3.141592653589793, configElement.readDouble(Arrays.asList("number", "double")), 0.000001);
        assertEquals(-2.718281828459045, configElement.readDouble(Arrays.asList("number", "negative")), 0.000001);
    }

    @Test
    void testReadDoubleThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-double");

        configElement.reloadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readDouble(Arrays.asList("number", "invalid")));
    }

    @Test
    void testComplexConfigurationScenario() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("server.ssl.enabled", "true");
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/test");
        properties.setProperty("database.username", "root");
        properties.setProperty("database.connection.timeout", "30");

        configElement.reloadData(properties);

        // 验证基本读取
        assertEquals("TestApp", configElement.readString(Arrays.asList("app", "name")));
        assertEquals("1.0.0", configElement.readString(Arrays.asList("app", "version")));

        // 验证嵌套读取
        assertEquals("localhost", configElement.readString(Arrays.asList("server", "host")));
        assertEquals(8080, configElement.readInteger(Arrays.asList("server", "port")));
        assertTrue(configElement.readBoolean(Arrays.asList("server", "ssl", "enabled")));

        // 验证提取子树
        ConfigElement serverNode = configElement.extract("server");
        assertNotNull(serverNode);
        assertEquals(3, serverNode.getChildNames().size());

        // 验证转换为属性列表
        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertEquals(8, propertyList.size());
    }

    @Test
    void testConcurrentAccess() {
        // 测试线程安全性（ConcurrentHashMap）
        assertDoesNotThrow(() -> {
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    configElement.ensureChild("child" + i);
                }
            });

            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    configElement.getChildNames();
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();
        });
    }
}

