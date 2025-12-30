package io.github.sinri.keel.base.configuration.lab;

import io.github.sinri.keel.base.configuration.ConfigProperty;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigElement单元测试。
 * <p>
 * 测试ConfigElement接口及其实现类ConfigElementImpl的各项功能。
 *
 * @since 5.0.0
 */
class ConfigElementUnitTest {
    private ConfigElement configElement;

    @BeforeEach
    void setUp() {
        configElement = new ConfigElementImpl("root");
    }

    @Test
    void testDefaultConstructor() {
        ConfigElement element = new ConfigElementImpl();
        assertNotNull(element);
    }

    @Test
    void testConstructorWithName() {
        ConfigElement element = new ConfigElementImpl("testNode");
        assertNotNull(element);
        assertEquals("testNode", element.getElementName());
    }

    @Test
    void testConstructorWithJsonObject() {
        JsonObject jsonObject = new JsonObject()
                .put("name", "testNode")
                .put("value", "testValue");
        ConfigElement element = new ConfigElementImpl(jsonObject);
        assertNotNull(element);
        assertEquals("testNode", element.getElementName());
        assertEquals("testValue", element.getElementValue());
    }

    @Test
    void testGetElementName() {
        assertEquals("root", configElement.getElementName());
    }

    @Test
    void testGetElementValueNull() {
        assertNull(configElement.getElementValue());
    }

    @Test
    void testSetElementValue() {
        configElement.setElementValue("testValue");
        assertEquals("testValue", configElement.getElementValue());
    }

    @Test
    void testSetElementValueNull() {
        configElement.setElementValue("initialValue");
        configElement.setElementValue(null);
        assertNull(configElement.getElementValue());
    }

    @Test
    void testSetElementValueEmpty() {
        configElement.setElementValue("");
        assertEquals("", configElement.getElementValue());
    }

    @Test
    void testSetElementValueWithSpecialCharacters() {
        configElement.setElementValue("value with spaces");
        assertEquals("value with spaces", configElement.getElementValue());

        configElement.setElementValue("value:with:colons");
        assertEquals("value:with:colons", configElement.getElementValue());

        configElement.setElementValue("value=with=equals");
        assertEquals("value=with=equals", configElement.getElementValue());
    }

    @Test
    void testGetChildNamesEmpty() {
        Set<String> childNames = configElement.getChildNames();
        assertNotNull(childNames);
        assertTrue(childNames.isEmpty());
    }

    @Test
    void testEnsureChild() {
        ConfigElement child = configElement.ensureChild("childNode");
        assertNotNull(child);
        assertEquals("childNode", child.getElementName());
    }

    @Test
    void testEnsureChildMultipleTimes() {
        ConfigElement child1 = configElement.ensureChild("childNode");
        ConfigElement child2 = configElement.ensureChild("childNode");

        assertNotNull(child1);
        assertNotNull(child2);
        assertEquals("childNode", child1.getElementName());
        assertEquals("childNode", child2.getElementName());

        // 确保只有一个子节点
        Set<String> childNames = configElement.getChildNames();
        assertEquals(1, childNames.size());
        assertTrue(childNames.contains("childNode"));
    }

    @Test
    void testEnsureMultipleChildren() {
        configElement.ensureChild("child1");
        configElement.ensureChild("child2");
        configElement.ensureChild("child3");

        Set<String> childNames = configElement.getChildNames();
        assertEquals(3, childNames.size());
        assertTrue(childNames.contains("child1"));
        assertTrue(childNames.contains("child2"));
        assertTrue(childNames.contains("child3"));
    }

    @Test
    void testAddChild() {
        ConfigElement child = new ConfigElementImpl("newChild");
        child.setElementValue("childValue");

        configElement.addChild(child);

        Set<String> childNames = configElement.getChildNames();
        assertTrue(childNames.contains("newChild"));

        ConfigElement retrievedChild = configElement.getChild("newChild");
        assertNotNull(retrievedChild);
        assertEquals("newChild", retrievedChild.getElementName());
        assertEquals("childValue", retrievedChild.getElementValue());
    }

    @Test
    void testAddMultipleChildren() {
        ConfigElement child1 = new ConfigElementImpl("child1");
        ConfigElement child2 = new ConfigElementImpl("child2");

        configElement.addChild(child1);
        configElement.addChild(child2);

        Set<String> childNames = configElement.getChildNames();
        assertEquals(2, childNames.size());
    }

    @Test
    void testGetChild() {
        ConfigElement child = configElement.ensureChild("testChild");
        child.setElementValue("testValue");

        ConfigElement retrievedChild = configElement.getChild("testChild");
        assertNotNull(retrievedChild);
        assertEquals("testChild", retrievedChild.getElementName());
        assertEquals("testValue", retrievedChild.getElementValue());
    }

    @Test
    void testGetChildNotExists() {
        ConfigElement child = configElement.getChild("nonExistent");
        assertNull(child);
    }

    @Test
    void testRemoveChild() {
        configElement.ensureChild("childToRemove");
        assertTrue(configElement.getChildNames().contains("childToRemove"));

        configElement.removeChild("childToRemove");
        assertFalse(configElement.getChildNames().contains("childToRemove"));
    }

    @Test
    void testRemoveChildNotExists() {
        // 应该不会抛出异常
        assertDoesNotThrow(() -> configElement.removeChild("nonExistent"));
    }

    @Test
    void testRemoveMultipleChildren() {
        configElement.ensureChild("child1");
        configElement.ensureChild("child2");
        configElement.ensureChild("child3");

        configElement.removeChild("child1");
        configElement.removeChild("child3");

        Set<String> childNames = configElement.getChildNames();
        assertEquals(1, childNames.size());
        assertTrue(childNames.contains("child2"));
    }

    @Test
    void testReloadDataFromProperties() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("database.host", "localhost");
        properties.setProperty("database.port", "3306");

        configElement.reloadData(properties);

        Set<String> childNames = configElement.getChildNames();
        assertTrue(childNames.contains("app"));
        assertTrue(childNames.contains("database"));

        ConfigElement appNode = configElement.getChild("app");
        assertNotNull(appNode);
        ConfigElement nameNode = appNode.getChild("name");
        assertNotNull(nameNode);
        assertEquals("TestApp", nameNode.getElementValue());

        ConfigElement dbNode = configElement.getChild("database");
        assertNotNull(dbNode);
        ConfigElement portNode = dbNode.getChild("port");
        assertNotNull(portNode);
        assertEquals("3306", portNode.getElementValue());
    }

    @Test
    void testReloadDataWithSingleLevelProperty() {
        Properties properties = new Properties();
        properties.setProperty("singleKey", "singleValue");

        configElement.reloadData(properties);

        ConfigElement child = configElement.getChild("singleKey");
        assertNotNull(child);
        assertEquals("singleValue", child.getElementValue());
    }

    @Test
    void testReloadDataWithDeepNestedProperty() {
        Properties properties = new Properties();
        properties.setProperty("level1.level2.level3.level4.key", "deepValue");

        configElement.reloadData(properties);

        ConfigElement level1 = configElement.getChild("level1");
        assertNotNull(level1);
        ConfigElement level2 = level1.getChild("level2");
        assertNotNull(level2);
        ConfigElement level3 = level2.getChild("level3");
        assertNotNull(level3);
        ConfigElement level4 = level3.getChild("level4");
        assertNotNull(level4);
        ConfigElement key = level4.getChild("key");
        assertNotNull(key);
        assertEquals("deepValue", key.getElementValue());
    }

    @Test
    void testReloadDataClearsExistingChildren() {
        configElement.ensureChild("existingChild");
        assertTrue(configElement.getChildNames().contains("existingChild"));

        Properties properties = new Properties();
        properties.setProperty("newChild", "newValue");

        configElement.reloadData(properties);

        assertFalse(configElement.getChildNames().contains("existingChild"));
        assertTrue(configElement.getChildNames().contains("newChild"));
    }

    @Test
    void testReloadDataWithEmptyProperties() {
        configElement.ensureChild("existingChild");

        Properties properties = new Properties();
        configElement.reloadData(properties);

        assertTrue(configElement.getChildNames().isEmpty());
    }

    @Test
    void testExtractWithVarargs() {
        Properties properties = new Properties();
        properties.setProperty("app.config.database.host", "localhost");
        configElement.reloadData(properties);

        ConfigElement extracted = configElement.extract("app", "config", "database", "host");
        assertNotNull(extracted);
        assertEquals("localhost", extracted.getElementValue());
    }

    @Test
    void testExtractWithList() {
        Properties properties = new Properties();
        properties.setProperty("app.config.database.host", "localhost");
        configElement.reloadData(properties);

        List<String> keychain = List.of("app", "config", "database", "host");
        ConfigElement extracted = configElement.extract(keychain);
        assertNotNull(extracted);
        assertEquals("localhost", extracted.getElementValue());
    }

    @Test
    void testExtractNonExistentPath() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        configElement.reloadData(properties);

        ConfigElement extracted = configElement.extract("app", "nonexistent", "path");
        assertNull(extracted);
    }

    @Test
    void testExtractEmptyPath() {
        ConfigElement extracted = configElement.extract(List.of());
        assertNotNull(extracted);
        assertEquals("root", extracted.getElementName());
    }

    @Test
    void testExtractSingleLevel() {
        configElement.ensureChild("child1").setElementValue("value1");

        ConfigElement extracted = configElement.extract("child1");
        assertNotNull(extracted);
        assertEquals("value1", extracted.getElementValue());
    }

    @Test
    void testTransformChildrenToPropertyList() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("database.host", "localhost");
        properties.setProperty("database.port", "3306");
        configElement.reloadData(properties);

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertNotNull(propertyList);
        assertEquals(4, propertyList.size());

        // 验证属性按字典序排列
        assertEquals("app.name", propertyList.get(0).getPropertyName());
        assertEquals("TestApp", propertyList.get(0).getPropertyValue());
        assertEquals("app.version", propertyList.get(1).getPropertyName());
        assertEquals("1.0.0", propertyList.get(1).getPropertyValue());
        assertEquals("database.host", propertyList.get(2).getPropertyName());
        assertEquals("localhost", propertyList.get(2).getPropertyValue());
        assertEquals("database.port", propertyList.get(3).getPropertyName());
        assertEquals("3306", propertyList.get(3).getPropertyValue());
    }

    @Test
    void testTransformChildrenToPropertyListEmpty() {
        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertNotNull(propertyList);
        assertTrue(propertyList.isEmpty());
    }

    @Test
    void testTransformChildrenToPropertyListWithNestedStructure() {
        Properties properties = new Properties();
        properties.setProperty("level1.level2.key", "value");
        configElement.reloadData(properties);

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertNotNull(propertyList);
        assertEquals(1, propertyList.size());
        assertEquals("level1.level2.key", propertyList.get(0).getPropertyName());
        assertEquals("value", propertyList.get(0).getPropertyValue());
    }

    @Test
    void testTransformChildrenToPropertyListIgnoresNodesWithoutValue() {
        ConfigElement child1 = configElement.ensureChild("child1");
        child1.ensureChild("subchild").setElementValue("value");

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertNotNull(propertyList);
        // 只有有值的节点才会被转换
        assertEquals(1, propertyList.size());
        assertEquals("child1.subchild", propertyList.get(0).getPropertyName());
        assertEquals("value", propertyList.get(0).getPropertyValue());
    }

    @Test
    void testTransformChildrenToPropertyListWithMultipleLevels() {
        ConfigElement level1 = configElement.ensureChild("level1");
        level1.setElementValue("value1");
        ConfigElement level2 = level1.ensureChild("level2");
        level2.setElementValue("value2");

        List<ConfigProperty> propertyList = configElement.transformChildrenToPropertyList();
        assertNotNull(propertyList);
        assertEquals(2, propertyList.size());
        assertEquals("level1", propertyList.get(0).getPropertyName());
        assertEquals("value1", propertyList.get(0).getPropertyValue());
        assertEquals("level1.level2", propertyList.get(1).getPropertyName());
        assertEquals("value2", propertyList.get(1).getPropertyValue());
    }

    @Test
    void testToJsonObject() {
        configElement.setElementValue("rootValue");
        ConfigElement child = configElement.ensureChild("child");
        child.setElementValue("childValue");

        JsonObject jsonObject = configElement.toJsonObject();
        assertNotNull(jsonObject);
        assertEquals("root", jsonObject.getString("name"));
        assertEquals("rootValue", jsonObject.getString("value"));
        assertTrue(jsonObject.containsKey("children"));
    }

    @Test
    void testComplexTreeStructure() {
        // 构建复杂的树形结构
        ConfigElement app = configElement.ensureChild("app");
        app.setElementValue("MyApp");

        ConfigElement config = app.ensureChild("config");
        ConfigElement database = config.ensureChild("database");
        database.ensureChild("host").setElementValue("localhost");
        database.ensureChild("port").setElementValue("3306");
        database.ensureChild("name").setElementValue("testdb");

        ConfigElement cache = config.ensureChild("cache");
        cache.ensureChild("enabled").setElementValue("true");
        cache.ensureChild("ttl").setElementValue("3600");

        // 验证结构
        ConfigElement extractedHost = configElement.extract("app", "config", "database", "host");
        assertNotNull(extractedHost);
        assertEquals("localhost", extractedHost.getElementValue());

        ConfigElement extractedTtl = configElement.extract("app", "config", "cache", "ttl");
        assertNotNull(extractedTtl);
        assertEquals("3600", extractedTtl.getElementValue());

        // 验证转换为属性列表
        ConfigElement appNode = configElement.getChild("app");
        List<ConfigProperty> properties = appNode.transformChildrenToPropertyList();
        assertFalse(properties.isEmpty());
    }

    @Test
    void testChildNamesSortedInTransform() {
        ConfigElement child3 = configElement.ensureChild("child3");
        child3.setElementValue("value3");
        ConfigElement child1 = configElement.ensureChild("child1");
        child1.setElementValue("value1");
        ConfigElement child2 = configElement.ensureChild("child2");
        child2.setElementValue("value2");

        List<ConfigProperty> properties = configElement.transformChildrenToPropertyList();
        assertEquals(3, properties.size());
        assertEquals("child1", properties.get(0).getPropertyName());
        assertEquals("child2", properties.get(1).getPropertyName());
        assertEquals("child3", properties.get(2).getPropertyName());
    }

    @Test
    void testReloadDataWithSpecialCharactersInValue() {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://localhost:3306/db?useSSL=true");
        properties.setProperty("path", "C:\\Users\\Test\\Application");
        properties.setProperty("message", "Hello, World!");

        configElement.reloadData(properties);

        assertEquals("jdbc:mysql://localhost:3306/db?useSSL=true",
                configElement.getChild("url").getElementValue());
        assertEquals("C:\\Users\\Test\\Application",
                configElement.getChild("path").getElementValue());
        assertEquals("Hello, World!",
                configElement.getChild("message").getElementValue());
    }

    @Test
    void testReloadDataWithUnicodeValue() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "测试应用");
        properties.setProperty("app.description", "これはテストです");

        configElement.reloadData(properties);

        ConfigElement name = configElement.extract("app", "name");
        assertNotNull(name);
        assertEquals("测试应用", name.getElementValue());

        ConfigElement desc = configElement.extract("app", "description");
        assertNotNull(desc);
        assertEquals("これはテストです", desc.getElementValue());
    }

    @Test
    void testGetChildNamesIsUnmodifiable() {
        configElement.ensureChild("child1");
        Set<String> childNames = configElement.getChildNames();

        assertThrows(UnsupportedOperationException.class, () -> {
            childNames.add("child2");
        });
    }

    @Test
    void testMultipleOperationsOnSameNode() {
        ConfigElement child = configElement.ensureChild("testChild");
        child.setElementValue("value1");
        assertEquals("value1", child.getElementValue());

        child.setElementValue("value2");
        assertEquals("value2", child.getElementValue());

        ConfigElement subChild = child.ensureChild("subChild");
        subChild.setElementValue("subValue");

        ConfigElement retrievedChild = configElement.getChild("testChild");
        assertNotNull(retrievedChild);
        assertEquals("value2", retrievedChild.getElementValue());

        ConfigElement retrievedSubChild = retrievedChild.getChild("subChild");
        assertNotNull(retrievedSubChild);
        assertEquals("subValue", retrievedSubChild.getElementValue());
    }
}

