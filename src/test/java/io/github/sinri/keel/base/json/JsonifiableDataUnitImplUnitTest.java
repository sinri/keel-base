package io.github.sinri.keel.base.json;

import io.github.sinri.keel.base.TestHelper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonifiableDataUnitImpl单元测试。
 *
 * @since 5.0.0
 */
public class JsonifiableDataUnitImplUnitTest {
    private JsonifiableDataUnitImpl dataUnit;

    @BeforeEach
    void setUp() {
        dataUnit = new JsonifiableDataUnitImpl(TestHelper.createTestJsonObject());
    }

    @Test
    void testCreateWithJsonObject() {
        JsonObject json = new JsonObject().put("key", "value");
        JsonifiableDataUnitImpl unit = new JsonifiableDataUnitImpl(json);
        assertEquals("value", unit.toJsonObject().getString("key"));
    }

    @Test
    void testCreateEmpty() {
        JsonifiableDataUnitImpl unit = new JsonifiableDataUnitImpl();
        assertNotNull(unit.toJsonObject());
        assertTrue(unit.toJsonObject().isEmpty());
    }

    @Test
    void testToJsonObject() {
        JsonObject json = dataUnit.toJsonObject();
        assertNotNull(json);
        assertEquals("TestObject", json.getString("name"));
        assertEquals(123, json.getInteger("value"));
    }

    @Test
    void testReloadData() {
        JsonObject newJson = new JsonObject().put("newKey", "newValue");
        dataUnit.reloadData(newJson);

        JsonObject json = dataUnit.toJsonObject();
        assertEquals("newValue", json.getString("newKey"));
        assertFalse(json.containsKey("name"));
    }

    @Test
    void testToJsonExpression() {
        String json = dataUnit.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("TestObject"));
    }

    @Test
    void testToFormattedJsonExpression() {
        String json = dataUnit.toFormattedJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("TestObject"));
    }

    @Test
    void testToString() {
        String str = dataUnit.toString();
        assertNotNull(str);
        assertTrue(str.contains("TestObject"));
    }

    @Test
    void testEnsureEntry() {
        dataUnit.ensureEntry("newKey", "newValue");
        assertEquals("newValue", dataUnit.toJsonObject().getString("newKey"));
    }

    @Test
    void testRemoveEntry() {
        dataUnit.ensureEntry("tempKey", "tempValue");
        assertTrue(dataUnit.toJsonObject().containsKey("tempKey"));

        dataUnit.removeEntry("tempKey");
        assertFalse(dataUnit.toJsonObject().containsKey("tempKey"));
    }

    @Test
    void testIsEmpty() {
        JsonifiableDataUnitImpl empty = new JsonifiableDataUnitImpl();
        assertTrue(empty.isEmpty());

        assertFalse(dataUnit.isEmpty());
    }

    @Test
    void testIterator() {
        int count = 0;
        for (var entry : dataUnit) {
            count++;
            assertNotNull(entry.getKey());
        }
        assertTrue(count > 0);
    }

    @Test
    void testWriteToBuffer() {
        Buffer buffer = Buffer.buffer();
        dataUnit.writeToBuffer(buffer);

        assertTrue(buffer.length() > 0);
        JsonObject readBack = new JsonObject();
        readBack.readFromBuffer(0, buffer);
        assertEquals("TestObject", readBack.getString("name"));
    }

    @Test
    void testReadFromBuffer() {
        JsonObject original = new JsonObject().put("key", "value");
        JsonifiableDataUnitImpl originalUnit = new JsonifiableDataUnitImpl(original);

        Buffer buffer = Buffer.buffer();
        originalUnit.writeToBuffer(buffer);

        JsonifiableDataUnitImpl newUnit = new JsonifiableDataUnitImpl();
        int pos = newUnit.readFromBuffer(0, buffer);

        assertEquals("value", newUnit.toJsonObject().getString("key"));
        assertTrue(pos > 0);
    }

    @Test
    void testReadMethod() {
        String value = dataUnit.read(jsonPointer -> {
            jsonPointer.append("name");
            return String.class;
        });
        assertEquals("TestObject", value);
    }

    @Test
    void testReadMethodWithNonExistentKey() {
        String value = dataUnit.read(jsonPointer -> {
            jsonPointer.append("nonExistent");
            return String.class;
        });
        assertNull(value);
    }
}

