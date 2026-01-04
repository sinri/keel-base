package io.github.sinri.keel.base;

import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.util.Properties;

/**
 * 测试辅助类，提供测试数据生成和断言辅助方法。
 *
 * @since 5.0.0
 */
@NullMarked
public class TestHelper {
    /**
     * 创建测试用的Properties对象。
     *
     * @return 包含测试数据的Properties对象
     */
    public static Properties createTestProperties() {
        Properties props = new Properties();
        props.setProperty("app.name", "TestApp");
        props.setProperty("app.version", "1.0.0");
        props.setProperty("server.host", "localhost");
        props.setProperty("server.port", "8080");
        props.setProperty("database.url", "jdbc:mysql://localhost:3306/test");
        props.setProperty("database.username", "testuser");
        props.setProperty("database.password", "testpass");
        return props;
    }

    /**
     * 创建测试用的JsonObject对象。
     *
     * @return 包含测试数据的JsonObject对象
     */
    public static JsonObject createTestJsonObject() {
        return new JsonObject()
                .put("name", "TestObject")
                .put("value", 123)
                .put("active", true)
                .put("nested", new JsonObject()
                        .put("key1", "value1")
                        .put("key2", "value2"));
    }

    /**
     * 创建测试用的复杂JsonObject对象。
     *
     * @return 包含复杂测试数据的JsonObject对象
     */
    public static JsonObject createComplexTestJsonObject() {
        return new JsonObject()
                .put("string", "test")
                .put("integer", 42)
                .put("long", 123456789L)
                .put("float", 3.14f)
                .put("double", 2.71828)
                .put("boolean", true)
                .put("array", new io.vertx.core.json.JsonArray()
                        .add("item1")
                        .add("item2")
                        .add("item3"))
                .put("object", new JsonObject()
                        .put("nested", "value"));
    }
}

