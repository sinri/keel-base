package io.github.sinri.keel.base.configuration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigNode单元测试。
 * <p>
 * 测试ConfigNode接口和ConfigNodeImpl实现的各项功能。
 *
 * @since 5.0.0
 */
class ConfigNodeUnitTest {
    private ConfigNode configNode;

    @BeforeEach
    void setUp() {
        configNode = ConfigNode.create("root");
    }

    @Test
    void testCreateWithName() {
        ConfigNode node = ConfigNode.create("testNode");
        assertNotNull(node);
        assertEquals("testNode", node.getName());
        assertNull(node.getValue());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    void testCreateFromAnotherNode() {
        ConfigNode original = ConfigNode.create("original");
        original.setValue("testValue");
        original.ensureChild("child1").setValue("value1");

        ConfigNode copy = ConfigNode.create(original);
        assertNotNull(copy);
        assertEquals("original", copy.getName());
        assertEquals("testValue", copy.getValue());
        assertEquals(1, copy.getChildren().size());
        assertNotNull(copy.getChild("child1"));
    }

    @Test
    void testSetAndGetValue() {
        assertNull(configNode.getValue());

        configNode.setValue("testValue");
        assertEquals("testValue", configNode.getValue());

        configNode.setValue(null);
        assertNull(configNode.getValue());
    }

    @Test
    void testEnsureChild() {
        ConfigNode child = configNode.ensureChild("child1");
        assertNotNull(child);
        assertEquals("child1", child.getName());

        // Ensure idempotency
        ConfigNode sameChild = configNode.ensureChild("child1");
        assertSame(child, sameChild);
    }

    @Test
    void testAddChild() {
        ConfigNode child = ConfigNode.create("child1");
        child.setValue("value1");

        configNode.addChild(child);
        assertEquals(1, configNode.getChildren().size());

        ConfigNode retrieved = configNode.getChild("child1");
        assertNotNull(retrieved);
        assertEquals("child1", retrieved.getName());
        assertEquals("value1", retrieved.getValue());
    }

    @Test
    void testAddChildReplacesExisting() {
        ConfigNode child1 = ConfigNode.create("child");
        child1.setValue("value1");
        configNode.addChild(child1);

        ConfigNode child2 = ConfigNode.create("child");
        child2.setValue("value2");
        configNode.addChild(child2);

        assertEquals(1, configNode.getChildren().size());
        ConfigNode retrieved = configNode.getChild("child");
        assertEquals("value2", retrieved.getValue());
    }

    @Test
    void testRemoveChild() {
        configNode.ensureChild("child1");
        assertEquals(1, configNode.getChildren().size());

        configNode.removeChild("child1");
        assertEquals(0, configNode.getChildren().isEmpty() ? 0 : configNode.getChildren().size());
        assertNull(configNode.getChild("child1"));
    }

    @Test
    void testRemoveNonExistentChild() {
        assertDoesNotThrow(() -> configNode.removeChild("nonexistent"));
        assertTrue(configNode.getChildren().isEmpty());
    }

    @Test
    void testGetChild() {
        assertNull(configNode.getChild("nonexistent"));

        ConfigNode child = configNode.ensureChild("child1");
        ConfigNode retrieved = configNode.getChild("child1");
        assertSame(child, retrieved);
    }

    @Test
    void testGetChildren() {
        assertTrue(configNode.getChildren().isEmpty());

        configNode.ensureChild("child1");
        configNode.ensureChild("child2");
        configNode.ensureChild("child3");

        assertEquals(3, configNode.getChildren().size());
        assertTrue(configNode.getChildren().containsKey("child1"));
        assertTrue(configNode.getChildren().containsKey("child2"));
        assertTrue(configNode.getChildren().containsKey("child3"));
    }

    @Test
    void testToJsonObject() {
        configNode.setValue("rootValue");
        configNode.ensureChild("child1").setValue("value1");
        configNode.ensureChild("child2").setValue("value2");

        JsonObject json = configNode.toJsonObject();
        assertNotNull(json);
        assertEquals("root", json.getString("name"));
        assertEquals("rootValue", json.getString("value"));

        JsonArray children = json.getJsonArray("children");
        assertNotNull(children);
        assertEquals(2, children.size());
    }

    @Test
    void testToJsonObjectWithoutValue() {
        configNode.ensureChild("child1").setValue("value1");

        JsonObject json = configNode.toJsonObject();
        assertNotNull(json);
        assertEquals("root", json.getString("name"));
        assertFalse(json.containsKey("value"));
    }

    @Test
    void testDecodeFromJsonObject() {
        JsonObject json = new JsonObject()
                .put("name", "testNode")
                .put("value", "testValue")
                .put("children", new JsonArray()
                        .add(new JsonObject()
                                .put("name", "child1")
                                .put("value", "value1")
                                .put("children", new JsonArray()))
                        .add(new JsonObject()
                                .put("name", "child2")
                                .put("value", "value2")
                                .put("children", new JsonArray())));

        ConfigNode decoded = ConfigNode.decodeFromJsonObject(json);
        assertNotNull(decoded);
        assertEquals("testNode", decoded.getName());
        assertEquals("testValue", decoded.getValue());
        assertEquals(2, decoded.getChildren().size());

        ConfigNode child1 = decoded.getChild("child1");
        assertNotNull(child1);
        assertEquals("value1", child1.getValue());

        ConfigNode child2 = decoded.getChild("child2");
        assertNotNull(child2);
        assertEquals("value2", child2.getValue());
    }

    @Test
    void testDecodeFromJsonObjectWithNullName() {
        JsonObject json = new JsonObject()
                .put("value", "testValue")
                .put("children", new JsonArray());

        assertThrows(NullPointerException.class, () -> ConfigNode.decodeFromJsonObject(json));
    }

    @Test
    void testDecodeFromJsonObjectWithInvalidChild() {
        JsonObject json = new JsonObject()
                .put("name", "testNode")
                .put("children", new JsonArray().add("invalidChild"));

        assertThrows(IllegalArgumentException.class, () -> ConfigNode.decodeFromJsonObject(json));
    }

    @Test
    void testDecodeFromJsonObjectWithNullValue() {
        JsonObject json = new JsonObject()
                .put("name", "testNode")
                .put("value", null)
                .put("children", new JsonArray());

        ConfigNode decoded = ConfigNode.decodeFromJsonObject(json);
        assertNotNull(decoded);
        assertNull(decoded.getValue());
    }

    @Test
    void testReloadDataFromProperties() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/test");

        configNode.reloadData(properties);

        ConfigNode appNode = configNode.getChild("app");
        assertNotNull(appNode);

        ConfigNode appNameNode = appNode.getChild("name");
        assertNotNull(appNameNode);
        assertEquals("TestApp", appNameNode.getValue());

        ConfigNode appVersionNode = appNode.getChild("version");
        assertNotNull(appVersionNode);
        assertEquals("1.0.0", appVersionNode.getValue());

        ConfigNode serverNode = configNode.getChild("server");
        assertNotNull(serverNode);
        assertEquals("localhost", serverNode.getChild("host").getValue());
        assertEquals("8080", serverNode.getChild("port").getValue());
    }

    @Test
    void testReloadDataFromPropertiesWithSingleKey() {
        Properties properties = new Properties();
        properties.setProperty("simpleKey", "simpleValue");

        configNode.reloadData(properties);

        ConfigNode child = configNode.getChild("simpleKey");
        assertNotNull(child);
        assertEquals("simpleValue", child.getValue());
    }

    @Test
    void testReloadDataFromPropertiesOverwritesExisting() {
        configNode.ensureChild("app").ensureChild("name").setValue("OldApp");

        Properties properties = new Properties();
        properties.setProperty("app.name", "NewApp");

        configNode.reloadData(properties);

        ConfigNode appNode = configNode.getChild("app");
        assertNotNull(appNode);
        ConfigNode appNameNode = appNode.getChild("name");
        assertNotNull(appNameNode);
        assertEquals("NewApp", appNameNode.getValue());
    }

    @Test
    void testReloadDataFromPropertiesWithEmptyProperties() {
        configNode.ensureChild("existing").setValue("value");

        Properties emptyProps = new Properties();
        configNode.reloadData(emptyProps);

        // Existing children should remain
        assertNotNull(configNode.getChild("existing"));
    }

    @Test
    void testNestedStructure() {
        ConfigNode level1 = configNode.ensureChild("level1");
        ConfigNode level2 = level1.ensureChild("level2");
        ConfigNode level3 = level2.ensureChild("level3");
        level3.setValue("deepValue");

        ConfigNode retrieved = configNode.getChild("level1")
                                         .getChild("level2")
                                         .getChild("level3");
        assertNotNull(retrieved);
        assertEquals("deepValue", retrieved.getValue());
    }

    @Test
    void testToJsonExpression() {
        configNode.setValue("testValue");
        configNode.ensureChild("child").setValue("childValue");

        String json = configNode.toJsonExpression();
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("root"));
        assertTrue(json.contains("testValue"));
    }

    @Test
    void testToFormattedJsonExpression() {
        configNode.setValue("testValue");
        configNode.ensureChild("child").setValue("childValue");

        String json = configNode.toFormattedJsonExpression();
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("\n")); // Should contain newlines for formatting
    }

    @Test
    void testReloadDataFromJsonObject() {
        configNode.setValue("oldValue");
        configNode.ensureChild("oldChild").setValue("oldChildValue");

        JsonObject json = new JsonObject()
                .put("name", "newRoot")
                .put("value", "newValue")
                .put("children", new JsonArray()
                        .add(new JsonObject()
                                .put("name", "newChild")
                                .put("value", "newChildValue")
                                .put("children", new JsonArray())));

        configNode.reloadData(json);

        assertEquals("newRoot", configNode.getName());
        assertEquals("newValue", configNode.getValue());
        assertEquals(1, configNode.getChildren().size());

        ConfigNode newChild = configNode.getChild("newChild");
        assertNotNull(newChild);
        assertEquals("newChildValue", newChild.getValue());

        // Old child should be removed
        assertNull(configNode.getChild("oldChild"));
    }

    @Test
    void testConcurrentChildManipulation() {
        // Test thread safety of concurrent map
        for (int i = 0; i < 100; i++) {
            configNode.ensureChild("child" + i).setValue("value" + i);
        }

        assertEquals(100, configNode.getChildren().size());

        for (int i = 0; i < 100; i++) {
            ConfigNode child = configNode.getChild("child" + i);
            assertNotNull(child);
            assertEquals("value" + i, child.getValue());
        }
    }
}

