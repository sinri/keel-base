package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigPropertiesBuilder单元测试。
 * <p>
 * 测试ConfigPropertiesBuilder类的各项功能。
 *
 * @since 5.0.0
 */
class ConfigPropertiesBuilderUnitTest {

    private ConfigPropertiesBuilder builder;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        builder = new ConfigPropertiesBuilder();
    }

    /**
     * 测试添加带前缀和键链的配置项
     */
    @Test
    void testAddWithPrefixAndKeychain() {
        builder.add(List.of("app", "server"), List.of("host"), "localhost");
        String result = builder.writeToString();
        assertEquals("app.server.host=localhost", result);
    }

    /**
     * 测试添加不带前缀的配置项（使用键链列表）
     */
    @Test
    void testAddWithKeychainOnly() {
        builder.add(List.of("database", "url"), "jdbc:mysql://localhost:3306/test");
        String result = builder.writeToString();
        assertEquals("database.url=jdbc:mysql://localhost:3306/test", result);
    }

    /**
     * 测试添加带前缀的配置项（使用单字符串键）
     */
    @Test
    void testAddWithPrefixAndSingleStringKey() {
        builder.add(List.of("app"), "name", "TestApp");
        String result = builder.writeToString();
        assertEquals("app.name=TestApp", result);
    }

    /**
     * 测试添加不带前缀的配置项（使用单字符串键）
     */
    @Test
    void testAddWithSingleStringKeyOnly() {
        builder.add("version", "1.0.0");
        String result = builder.writeToString();
        assertEquals("version=1.0.0", result);
    }

    /**
     * 测试添加多个配置项
     */
    @Test
    void testAddMultipleProperties() {
        builder.add("app.name", "MyApp")
               .add("app.version", "2.0.0")
               .add(List.of("server", "port"), "8080")
               .add(List.of("database"), "host", "localhost");

        String result = builder.writeToString();
        String expected = "app.name=MyApp\n" +
                "app.version=2.0.0\n" +
                "server.port=8080\n" +
                "database.host=localhost";
        assertEquals(expected, result);
    }

    /**
     * 测试空配置项列表
     */
    @Test
    void testEmptyConfigPropertyList() {
        String result = builder.writeToString();
        assertEquals("", result);
    }

    /**
     * 测试设置配置项列表
     */
    @Test
    void testSetConfigPropertyList() {
        List<ConfigProperty> propertyList = new ArrayList<>();
        propertyList.add(new ConfigProperty().setKeychain(List.of("test", "key1")).setValue("value1"));
        propertyList.add(new ConfigProperty().setKeychain(List.of("test", "key2")).setValue("value2"));

        builder.setConfigPropertyList(propertyList);
        String result = builder.writeToString();

        String expected = "test.key1=value1\n" +
                "test.key2=value2";
        assertEquals(expected, result);
    }

    /**
     * 测试链式调用
     */
    @Test
    void testMethodChaining() {
        ConfigPropertiesBuilder result = builder.add("key1", "value1");
        assertSame(builder, result, "add方法应返回当前实例以支持链式调用");

        result = builder.add(List.of("key2"), "value2");
        assertSame(builder, result, "add方法应返回当前实例以支持链式调用");

        result = builder.add(List.of("prefix"), "key3", "value3");
        assertSame(builder, result, "add方法应返回当前实例以支持链式调用");

        result = builder.add(List.of("p1"), List.of("k4"), "value4");
        assertSame(builder, result, "add方法应返回当前实例以支持链式调用");
    }

    /**
     * 测试写入文件
     */
    @Test
    void testWriteToFile() throws IOException {
        builder.add("app.name", "TestApp")
               .add("app.version", "1.0.0")
               .add(List.of("server", "port"), "8080");

        Path testFile = tempDir.resolve("test-config.properties");
        builder.writeToFile(testFile.toString());

        assertTrue(Files.exists(testFile), "文件应该被创建");

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        String expected = "app.name=TestApp\n" +
                "app.version=1.0.0\n" +
                "server.port=8080";
        assertEquals(expected, content);
    }

    /**
     * 测试追加到文件
     */
    @Test
    void testAppendToFile() throws IOException {
        // 先创建一个文件并写入初始内容
        Path testFile = tempDir.resolve("append-test.properties");
        Files.writeString(testFile, "existing.key=existingValue\n", StandardCharsets.US_ASCII);

        // 追加新内容
        builder.add("new.key", "newValue")
               .add("another.key", "anotherValue");
        builder.appendToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        String expected = "existing.key=existingValue\n" +
                "new.key=newValue\n" +
                "another.key=anotherValue";
        assertEquals(expected, content);
    }

    /**
     * 测试追加到不存在的文件（应该创建文件）
     */
    @Test
    void testAppendToNonExistentFile() throws IOException {
        Path testFile = tempDir.resolve("new-file.properties");
        assertFalse(Files.exists(testFile), "文件开始时不应存在");

        builder.add("test.key", "testValue");
        builder.appendToFile(testFile.toString());

        assertTrue(Files.exists(testFile), "文件应该被创建");
        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("test.key=testValue", content);
    }

    /**
     * 测试带有特殊字符的值
     */
    @Test
    void testSpecialCharactersInValues() {
        builder.add("path", "/usr/local/bin")
               .add("url", "http://example.com:8080/path?query=value")
               .add("message", "Hello, World!");

        String result = builder.writeToString();
        String expected = "path=/usr/local/bin\n" +
                "url=http://example.com:8080/path?query=value\n" +
                "message=Hello, World!";
        assertEquals(expected, result);
    }

    /**
     * 测试空值
     */
    @Test
    void testEmptyValue() {
        builder.add("empty.key", "");
        String result = builder.writeToString();
        assertEquals("empty.key=", result);
    }

    /**
     * 测试复杂的嵌套键链
     */
    @Test
    void testComplexNestedKeychain() {
        builder.add(List.of("app", "database", "connection", "pool"), List.of("max", "size"), "100")
               .add(List.of("app", "database", "connection", "pool"), List.of("min", "size"), "10");

        String result = builder.writeToString();
        String expected = "app.database.connection.pool.max.size=100\n" +
                "app.database.connection.pool.min.size=10";
        assertEquals(expected, result);
    }

    /**
     * 测试设置配置项列表后的链式调用
     */
    @Test
    void testSetConfigPropertyListChaining() {
        List<ConfigProperty> propertyList = new ArrayList<>();
        propertyList.add(new ConfigProperty().setKeychain(List.of("initial", "key")).setValue("initialValue"));

        ConfigPropertiesBuilder result = builder.setConfigPropertyList(propertyList);
        assertSame(builder, result, "setConfigPropertyList方法应返回当前实例以支持链式调用");

        result.add("additional.key", "additionalValue");
        String output = result.writeToString();

        String expected = "initial.key=initialValue\n" +
                "additional.key=additionalValue";
        assertEquals(expected, output);
    }

    /**
     * 测试写入文件时覆盖已存在的文件
     */
    @Test
    void testWriteToFileOverwritesExistingFile() throws IOException {
        Path testFile = tempDir.resolve("overwrite-test.properties");

        // 先写入初始内容
        Files.writeString(testFile, "old.key=oldValue\n", StandardCharsets.US_ASCII);

        // 使用builder覆盖写入
        builder.add("new.key", "newValue");
        builder.writeToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("new.key=newValue", content, "文件内容应该被完全覆盖");
        assertFalse(content.contains("old.key"), "旧内容不应存在");
    }
}
