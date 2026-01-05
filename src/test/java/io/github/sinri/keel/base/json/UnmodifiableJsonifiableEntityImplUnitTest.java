package io.github.sinri.keel.base.json;

import io.github.sinri.keel.base.TestHelper;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UnmodifiableJsonifiableEntityImpl单元测试。
 *
 * @since 5.0.0
 */
@NullMarked
public class UnmodifiableJsonifiableEntityImplUnitTest {
    private UnmodifiableJsonifiableEntityImpl entity;

    @BeforeEach
    void setUp() {
        entity = new UnmodifiableJsonifiableEntityImpl(TestHelper.createTestJsonObject());
    }

    @Test
    void testCreateWithJsonObject() {
        JsonObject json = new JsonObject().put("key", "value");
        UnmodifiableJsonifiableEntityImpl entity = new UnmodifiableJsonifiableEntityImpl(json);
        assertEquals("value", entity.readString("key"));
    }

    @Test
    void testToJsonExpression() {
        String json = entity.toJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("TestObject"));
    }

    @Test
    void testToFormattedJsonExpression() {
        String json = entity.toFormattedJsonExpression();
        assertNotNull(json);
        assertTrue(json.contains("TestObject"));
    }

    @Test
    void testToString() {
        String str = entity.toString();
        assertNotNull(str);
        assertTrue(str.contains("TestObject"));
    }

    @Test
    void testReadMethod() {
        String value = entity.read(jsonPointer -> {
            jsonPointer.append("name");
            return String.class;
        });
        assertEquals("TestObject", value);
    }

    @Test
    void testReadMethodWithNonExistentKey() {
        String value = entity.read(jsonPointer -> {
            jsonPointer.append("nonExistent");
            return String.class;
        });
        assertNull(value);
    }

    @Test
    void testReadMethodWithWrongType() {
        Integer value = entity.read(jsonPointer -> {
            jsonPointer.append("name");
            return Integer.class;
        });
        assertNull(value);
    }

    @Test
    void testIterator() {
        int count = 0;
        for (Map.Entry<String, Object> entry : entity) {
            count++;
            assertNotNull(entry.getKey());
        }
        assertTrue(count > 0);
    }

    @Test
    void testIsEmpty() {
        UnmodifiableJsonifiableEntityImpl empty = new UnmodifiableJsonifiableEntityImpl(new JsonObject());
        assertTrue(empty.isEmpty());

        assertFalse(entity.isEmpty());
    }

    @Test
    void testCopy() {
        UnmodifiableJsonifiableEntityImpl copy = entity.copy();
        assertNotNull(copy);
        assertEquals("TestObject", copy.readString("name"));

        // Verify it's a deep copy - modifying original shouldn't affect copy
        JsonObject originalJson = entity.cloneAsJsonObject();
        originalJson.put("newKey", "newValue");
        // Copy should not have the new key
        assertNull(copy.readString("newKey"));
    }

    @Test
    void testCloneAsJsonObject() {
        JsonObject cloned = entity.cloneAsJsonObject();
        assertNotNull(cloned);
        assertEquals("TestObject", cloned.getString("name"));

        // Verify it's a copy
        cloned.put("newKey", "newValue");
        assertNull(entity.readString("newKey"));
    }

    @Test
    void testCloneAsJsonObjectCreatesCopy() {
        JsonObject cloned = entity.cloneAsJsonObject();
        assertNotNull(cloned);
        assertEquals("TestObject", cloned.getString("name"));
    }

    @Test
    void testPurifyMethod() {
        JsonObject json = new JsonObject().put("key", "value");
        UnmodifiableJsonifiableEntityImpl entity = new UnmodifiableJsonifiableEntityImpl(json) {
            @Override
            protected JsonObject purify(JsonObject raw) {
                JsonObject purified = raw.copy();
                purified.remove("key");
                purified.put("purified", true);
                return purified;
            }
        };

        assertNull(entity.readString("key"));
        assertEquals(Boolean.TRUE, entity.readBoolean("purified"));
    }

    @Test
    void testReadString() {
        String value = entity.readString("name");
        assertEquals("TestObject", value);
    }

    @Test
    void testReadNestedString() {
        String value = entity.readString("nested", "key1");
        assertEquals("value1", value);
    }

    @Test
    void testReadNumber() {
        Number value = entity.readNumber("value");
        assertNotNull(value);
        assertEquals(123, value.intValue());
    }

    @Test
    void testReadBoolean() {
        Boolean value = entity.readBoolean("active");
        assertEquals(Boolean.TRUE, value);
    }

    @Test
    void testReadJsonObject() {
        JsonObject nested = entity.readJsonObject("nested");
        assertNotNull(nested);
        assertEquals("value1", nested.getString("key1"));
    }
}

