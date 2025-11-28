package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.TestHelper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigElement单元测试。
 *
 * @since 5.0.0
 */
class ConfigElementUnitTest {
    private ConfigElement root;

    @BeforeEach
    void setUp() {
        root = new ConfigElement("root");
    }

    @Test
    void testCreateConfigElement() {
        ConfigElement element = new ConfigElement("test");
        assertEquals("test", element.getName());
        assertNull(element.getValueAsString());
        assertTrue(element.getChildren().isEmpty());
    }

    @Test
    void testCopyConfigElement() {
        ConfigElement original = new ConfigElement("original");
        original.setValue("value1");
        ConfigElement child1 = new ConfigElement("child1");
        child1.setValue("childValue1");
        original.addChild(child1);

        ConfigElement copy = new ConfigElement(original);
        assertEquals("original", copy.getName());
        assertEquals("value1", copy.getValueAsString());
        assertNotNull(copy.getChild("child1"));
        assertEquals("childValue1", copy.getChild("child1").getValueAsString());
    }

    @Test
    void testSetAndGetValue() {
        root.setValue("testValue");
        assertEquals("testValue", root.getValueAsString());
    }

    @Test
    void testAddChild() {
        ConfigElement child = new ConfigElement("child");
        child.setValue("childValue");
        root.addChild(child);

        ConfigElement retrieved = root.getChild("child");
        assertNotNull(retrieved);
        assertEquals("childValue", retrieved.getValueAsString());
    }

    @Test
    void testRemoveChild() {
        ConfigElement child = new ConfigElement("child");
        root.addChild(child);
        assertNotNull(root.getChild("child"));

        root.removeChild("child");
        assertNull(root.getChild("child"));
    }

    @Test
    void testRemoveChildByElement() {
        ConfigElement child = new ConfigElement("child");
        root.addChild(child);
        assertNotNull(root.getChild("child"));

        root.removeChild(child);
        assertNull(root.getChild("child"));
    }

    @Test
    void testEnsureChild() {
        ConfigElement child1 = root.ensureChild("child1");
        assertNotNull(child1);
        assertEquals("child1", child1.getName());

        ConfigElement child2 = root.ensureChild("child1");
        assertSame(child1, child2);
    }

    @Test
    void testExtractWithList() {
        ConfigElement level1 = new ConfigElement("level1");
        ConfigElement level2 = new ConfigElement("level2");
        level2.setValue("value");
        level1.addChild(level2);
        root.addChild(level1);

        ConfigElement extracted = root.extract(List.of("level1", "level2"));
        assertNotNull(extracted);
        assertEquals("value", extracted.getValueAsString());
    }

    @Test
    void testExtractWithVarArgs() {
        ConfigElement level1 = new ConfigElement("level1");
        ConfigElement level2 = new ConfigElement("level2");
        level2.setValue("value");
        level1.addChild(level2);
        root.addChild(level1);

        ConfigElement extracted = root.extract("level1", "level2");
        assertNotNull(extracted);
        assertEquals("value", extracted.getValueAsString());
    }

    @Test
    void testExtractEmptyPath() {
        ConfigElement extracted = root.extract(List.of());
        assertSame(root, extracted);
    }

    @Test
    void testExtractNonExistentPath() {
        assertNull(root.extract(List.of("non", "existent")));
    }

    @Test
    void testLoadProperties() {
        Properties props = TestHelper.createTestProperties();
        root.loadProperties(props);

        assertEquals("TestApp", root.extract("app", "name").getValueAsString());
        assertEquals("8080", root.extract("server", "port").getValueAsString());
        assertEquals("testpass", root.extract("database", "password").getValueAsString());
    }

    @Test
    void testLoadPropertiesFileContent() {
        String content = "app.name=TestApp\napp.version=1.0.0\nserver.port=8080";
        root.loadPropertiesFileContent(content);

        assertEquals("TestApp", root.extract("app", "name").getValueAsString());
        assertEquals("1.0.0", root.extract("app", "version").getValueAsString());
        assertEquals("8080", root.extract("server", "port").getValueAsString());
    }

    @Test
    void testLoadPropertiesFile() throws IOException {
        root.loadPropertiesFile("test-config.properties", StandardCharsets.UTF_8);

        assertEquals("TestApp", root.extract("app", "name").getValueAsString());
        assertEquals("1.0.0", root.extract("app", "version").getValueAsString());
        assertEquals("8080", root.extract("server", "port").getValueAsString());
    }

    @Test
    void testToJsonObject() {
        root.setValue("rootValue");
        ConfigElement child = new ConfigElement("child");
        child.setValue("childValue");
        root.addChild(child);

        JsonObject json = root.toJsonObject();
        assertEquals("root", json.getString("name"));
        assertEquals("rootValue", json.getString("value"));
        assertTrue(json.containsKey("children"));
        JsonArray children = json.getJsonArray("children");
        assertEquals(1, children.size());
    }

    @Test
    void testToJsonObjectWithoutValue() {
        ConfigElement child = new ConfigElement("child");
        child.setValue("childValue");
        root.addChild(child);

        JsonObject json = root.toJsonObject();
        assertEquals("root", json.getString("name"));
        assertFalse(json.containsKey("value"));
        assertTrue(json.containsKey("children"));
    }

    @Test
    void testReloadData() {
        ConfigElement original = new ConfigElement("original");
        original.setValue("originalValue");
        ConfigElement child = new ConfigElement("child");
        child.setValue("childValue");
        original.addChild(child);

        JsonObject json = original.toJsonObject();

        ConfigElement reloaded = new ConfigElement("reloaded");
        reloaded.reloadData(json);

        assertEquals("original", reloaded.getName());
        assertEquals("originalValue", reloaded.getValueAsString());
        assertNotNull(reloaded.getChild("child"));
        assertEquals("childValue", reloaded.getChild("child").getValueAsString());
    }

    @Test
    void testTransformChildrenToPropertyList() {
        ConfigElement app = new ConfigElement("app");
        app.ensureChild("name").setValue("TestApp");
        app.ensureChild("version").setValue("1.0.0");
        root.addChild(app);

        ConfigElement server = new ConfigElement("server");
        server.ensureChild("port").setValue("8080");
        root.addChild(server);

        List<ConfigProperty> properties = root.transformChildrenToPropertyList();
        assertFalse(properties.isEmpty());

        boolean foundAppName = properties.stream()
                                         .anyMatch(p -> p.getPropertyName().equals("app.name") && p.getPropertyValue()
                                                                                                   .equals("TestApp"));
        assertTrue(foundAppName);

        boolean foundServerPort = properties.stream()
                                            .anyMatch(p -> p.getPropertyName()
                                                            .equals("server.port") && p.getPropertyValue()
                                                                                       .equals("8080"));
        assertTrue(foundServerPort);
    }

    @Test
    void testToJsonExpression() {
        root.setValue("test");
        String json = root.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("test"));
    }

    @Test
    void testToFormattedJsonExpression() {
        root.setValue("test");
        String json = root.toFormattedJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("test"));
    }
}

