package io.github.sinri.keel.base.configuration;

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
    void testTryToGetChildNames() {
        assertTrue(configElement.getChildNames().isEmpty());

        configElement.ensureChild("child1");
        configElement.ensureChild("child2");

        Set<String> childNames = configElement.getChildNames();
        assertEquals(2, childNames.size());
        assertTrue(childNames.contains("child1"));
        assertTrue(childNames.contains("child2"));
    }

    @Test
    void testTryToGetChildNamesReturnsUnmodifiableSet() {
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
        assertEquals(child, configElement.tryToGetChild("childNode"));
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
    void testTryToGetChild() {
        ConfigElement child = configElement.ensureChild("child");
        ConfigElement retrieved = configElement.tryToGetChild("child");

        assertNotNull(retrieved);
        assertSame(child, retrieved);
    }

    @Test
    void testTryToGetChildReturnsNullForNonExistent() {
        ConfigElement child = configElement.tryToGetChild("nonExistent");
        assertNull(child);
    }

    @Test
    void testLoadDataWithSimpleProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");

        configElement.loadData(properties);

        assertEquals("value1", configElement.getChild("key1").getElementValue());
        assertEquals("value2", configElement.getChild("key2").getElementValue());
    }

    @Test
    void testLoadDataWithNestedProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("database.url", "jdbc:mysql://localhost");

        configElement.loadData(properties);

        ConfigElement server = configElement.getChild("server");
        assertNotNull(server);
        assertEquals("localhost", server.getChild("host").getElementValue());
        assertEquals("8080", server.getChild("port").getElementValue());

        ConfigElement database = configElement.tryToGetChild("database");
        assertNotNull(database);
        assertEquals("jdbc:mysql://localhost", database.getChild("url").getElementValue());
    }

    @Test
    void testLoadDataWithDeepNestedProperties() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("a.b.c.d", "deepValue");

        configElement.loadData(properties);

        ConfigElement a = configElement.tryToGetChild("a");
        assertNotNull(a);
        ConfigElement b = a.tryToGetChild("b");
        assertNotNull(b);
        ConfigElement c = b.tryToGetChild("c");
        assertNotNull(c);
        ConfigElement d = c.tryToGetChild("d");
        assertNotNull(d);
        assertEquals("deepValue", d.getElementValue());
    }


    @Test
    void testExtractWithVarargs() {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");

        configElement.loadData(properties);

        ConfigElement extracted = configElement.extract("server", "host");
        assertNotNull(extracted);
        assertEquals("host", extracted.getElementName());
    }

    @Test
    void testExtractWithList() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("server.host", "localhost");

        configElement.loadData(properties);

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
        // 一层属性（顶层）
        properties.setProperty("version", "1.0.0");
        properties.setProperty("environment", "production");

        // 二层属性
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("database.url", "jdbc:mysql://localhost");

        // 三层属性
        properties.setProperty("server.ssl.enabled", "true");
        properties.setProperty("server.ssl.port", "8443");
        properties.setProperty("database.connection.timeout", "30");
        properties.setProperty("database.connection.maxPoolSize", "10");

        // 四层属性
        properties.setProperty("logging.handler.file.path", "/var/log/app.log");
        properties.setProperty("logging.handler.file.maxSize", "10MB");
        properties.setProperty("cache.redis.cluster.nodes", "localhost:6379");

        configElement.loadData(properties);

        System.out.println(configElement.debugToString(0));

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();

        assertEquals(12, propertyList.size());

        // 验证属性按字典序排列
        // cache.redis.cluster.nodes
        assertEquals("cache.redis.cluster.nodes", propertyList.get(0).getPropertyName());
        assertEquals("localhost:6379", propertyList.get(0).getPropertyValue());

        // database.connection.maxPoolSize
        assertEquals("database.connection.maxPoolSize", propertyList.get(1).getPropertyName());
        assertEquals("10", propertyList.get(1).getPropertyValue());

        // database.connection.timeout
        assertEquals("database.connection.timeout", propertyList.get(2).getPropertyName());
        assertEquals("30", propertyList.get(2).getPropertyValue());

        // database.url
        assertEquals("database.url", propertyList.get(3).getPropertyName());
        assertEquals("jdbc:mysql://localhost", propertyList.get(3).getPropertyValue());

        // environment
        assertEquals("environment", propertyList.get(4).getPropertyName());
        assertEquals("production", propertyList.get(4).getPropertyValue());

        // logging.handler.file.maxSize
        assertEquals("logging.handler.file.maxSize", propertyList.get(5).getPropertyName());
        assertEquals("10MB", propertyList.get(5).getPropertyValue());

        // logging.handler.file.path
        assertEquals("logging.handler.file.path", propertyList.get(6).getPropertyName());
        assertEquals("/var/log/app.log", propertyList.get(6).getPropertyValue());

        // server.host
        assertEquals("server.host", propertyList.get(7).getPropertyName());
        assertEquals("localhost", propertyList.get(7).getPropertyValue());

        // server.port
        assertEquals("server.port", propertyList.get(8).getPropertyName());
        assertEquals("8080", propertyList.get(8).getPropertyValue());

        // server.ssl.enabled
        assertEquals("server.ssl.enabled", propertyList.get(9).getPropertyName());
        assertEquals("true", propertyList.get(9).getPropertyValue());

        // server.ssl.port
        assertEquals("server.ssl.port", propertyList.get(10).getPropertyName());
        assertEquals("8443", propertyList.get(10).getPropertyValue());

        // version
        assertEquals("version", propertyList.get(11).getPropertyName());
        assertEquals("1.0.0", propertyList.get(11).getPropertyValue());
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

        configElement.loadData(properties);

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

        configElement.loadData(properties);

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

        configElement.loadData(properties);

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

        configElement.loadData(properties);

        assertEquals(42, configElement.readInteger(Arrays.asList("number", "int")));
        assertEquals(-100, configElement.readInteger(Arrays.asList("number", "negative")));
    }

    @Test
    void testReadIntegerThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-number");

        configElement.loadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readInteger(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadLong() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.long", "9223372036854775807");
        properties.setProperty("number.negative", "-9223372036854775808");

        configElement.loadData(properties);

        assertEquals(9223372036854775807L, configElement.readLong(Arrays.asList("number", "long")));
        assertEquals(-9223372036854775808L, configElement.readLong(Arrays.asList("number", "negative")));
    }

    @Test
    void testReadLongThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-long");

        configElement.loadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readLong(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadFloat() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.float", "3.14");
        properties.setProperty("number.negative", "-2.5");

        configElement.loadData(properties);

        assertEquals(3.14f, configElement.readFloat(Arrays.asList("number", "float")), 0.001f);
        assertEquals(-2.5f, configElement.readFloat(Arrays.asList("number", "negative")), 0.001f);
    }

    @Test
    void testReadFloatThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-float");

        configElement.loadData(properties);

        assertThrows(NumberFormatException.class,
                () -> configElement.readFloat(Arrays.asList("number", "invalid")));
    }

    @Test
    void testReadDouble() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("number.double", "3.141592653589793");
        properties.setProperty("number.negative", "-2.718281828459045");

        configElement.loadData(properties);

        assertEquals(3.141592653589793, configElement.readDouble(Arrays.asList("number", "double")), 0.000001);
        assertEquals(-2.718281828459045, configElement.readDouble(Arrays.asList("number", "negative")), 0.000001);
    }

    @Test
    void testReadDoubleThrowsNumberFormatException() {
        Properties properties = new Properties();
        properties.setProperty("number.invalid", "not-a-double");

        configElement.loadData(properties);

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

        configElement.loadData(properties);

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

