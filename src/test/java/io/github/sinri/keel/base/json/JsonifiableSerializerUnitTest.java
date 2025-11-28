package io.github.sinri.keel.base.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonifiableSerializer单元测试。
 *
 * @since 5.0.0
 */
public class JsonifiableSerializerUnitTest {

    @BeforeEach
    void setUp() {
        // Register the serializer before tests
        JsonifiableSerializer.register();
    }

    @Test
    void testRegister() {
        // Registration should not throw exception
        assertDoesNotThrow(() -> JsonifiableSerializer.register());
    }

    @Test
    void testSerializeJsonifiableDataUnit() throws Exception {
        JsonObject json = new JsonObject()
                .put("name", "TestObject")
                .put("value", 123);
        JsonifiableDataUnitImpl dataUnit = new JsonifiableDataUnitImpl(json);

        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(dataUnit);

        assertNotNull(serialized);
        assertTrue(serialized.contains("TestObject"));
        assertTrue(serialized.contains("123"));
    }

    @Test
    void testSerializeUnmodifiableJsonifiableEntity() throws Exception {
        JsonObject json = new JsonObject()
                .put("name", "TestEntity")
                .put("value", 456);
        UnmodifiableJsonifiableEntityImpl entity = new UnmodifiableJsonifiableEntityImpl(json);

        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(entity);
        System.out.println(serialized);

        assertNotNull(serialized);
        assertTrue(serialized.contains("TestEntity"));
        assertTrue(serialized.contains("456"));
    }

    @Test
    void testSerializeComplexObject() throws Exception {
        JsonObject json = new JsonObject()
                .put("string", "test")
                .put("integer", 42)
                .put("boolean", true)
                .put("array", new io.vertx.core.json.JsonArray().add("item1").add("item2"));
        JsonifiableDataUnitImpl dataUnit = new JsonifiableDataUnitImpl(json);

        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(dataUnit);

        assertNotNull(serialized);
        assertTrue(serialized.contains("test"));
        assertTrue(serialized.contains("42"));
        assertTrue(serialized.contains("true"));
    }

    @Test
    void testSerializeEmptyObject() throws Exception {
        JsonObject json = new JsonObject();
        JsonifiableDataUnitImpl dataUnit = new JsonifiableDataUnitImpl(json);

        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(dataUnit);

        assertNotNull(serialized);
        assertEquals("{}", serialized);
    }

    @Test
    void aTest() {
        var e1 = new E1(new JsonObject());
        e1.ensureEntry("a", "b");
        System.out.println(new JsonObject().put("e1", e1));
    }

    static class E1 extends JsonifiableDataUnitImpl {
        public E1(JsonObject json) {
            super(json);
        }
    }
}

