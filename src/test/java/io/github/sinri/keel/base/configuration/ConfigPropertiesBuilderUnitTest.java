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

    @BeforeEach
    void setUp() {
        builder = new ConfigPropertiesBuilder();
    }

    @Test
    void testDefaultConstructor() {
        ConfigPropertiesBuilder newBuilder = new ConfigPropertiesBuilder();
        assertNotNull(newBuilder);
        assertEquals("", newBuilder.writeToString());
    }

    @Test
    void testSetPrefixWithList() {
        List<String> prefix = List.of("app", "config");
        ConfigPropertiesBuilder result = builder.setPrefix(prefix);

        assertSame(builder, result);
    }

    @Test
    void testSetPrefixWithVarargs() {
        ConfigPropertiesBuilder result = builder.setPrefix("app", "config");

        assertSame(builder, result);
    }

    @Test
    void testSetPrefixWithNull() {
        ConfigPropertiesBuilder result = builder.setPrefix((List<String>) null);

        assertSame(builder, result);
    }

    @Test
    void testSetPrefixWithEmptyList() {
        ConfigPropertiesBuilder result = builder.setPrefix(List.of());

        assertSame(builder, result);
    }

    @Test
    void testAddWithListKeychainWithoutPrefix() {
        builder.add(List.of("app", "name"), "TestApp");

        String result = builder.writeToString();
        assertEquals("app.name=TestApp", result);
    }

    @Test
    void testAddWithListKeychainWithPrefix() {
        builder.setPrefix("database", "primary");
        builder.add(List.of("host"), "localhost");

        String result = builder.writeToString();
        assertEquals("database.primary.host=localhost", result);
    }

    @Test
    void testAddWithListKeychainNullValue() {
        builder.add(List.of("app", "name"), null);

        String result = builder.writeToString();
        assertEquals("app.name=", result);
    }

    @Test
    void testAddWithListKeychainEmptyValue() {
        builder.add(List.of("app", "name"), "");

        String result = builder.writeToString();
        assertEquals("app.name=", result);
    }

    @Test
    void testAddWithStringKeychainWithoutPrefix() {
        builder.add("server.port", "8080");

        String result = builder.writeToString();
        assertEquals("server.port=8080", result);
    }

    @Test
    void testAddWithStringKeychainWithPrefix() {
        builder.setPrefix("app");
        builder.add("version", "1.0.0");

        String result = builder.writeToString();
        assertEquals("app.version=1.0.0", result);
    }

    @Test
    void testAddWithStringKeychainNullValue() {
        builder.add("app.name", null);

        String result = builder.writeToString();
        assertEquals("app.name=", result);
    }

    @Test
    void testAddMultipleProperties() {
        builder.add(List.of("app", "name"), "TestApp");
        builder.add(List.of("app", "version"), "1.0.0");
        builder.add(List.of("server", "port"), "8080");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(3, lines.length);
        assertEquals("app.name=TestApp", lines[0]);
        assertEquals("app.version=1.0.0", lines[1]);
        assertEquals("server.port=8080", lines[2]);
    }

    @Test
    void testAddWithPrefixChange() {
        builder.setPrefix("app");
        builder.add("name", "TestApp");

        builder.setPrefix("server");
        builder.add("port", "8080");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(2, lines.length);
        assertEquals("app.name=TestApp", lines[0]);
        assertEquals("server.port=8080", lines[1]);
    }

    @Test
    void testAddWithNullPrefix() {
        builder.setPrefix((List<String>) null);
        builder.add("app.name", "TestApp");

        String result = builder.writeToString();
        assertEquals("app.name=TestApp", result);
    }

    @Test
    void testSetConfigPropertyList() {
        List<ConfigProperty> propertyList = new ArrayList<>();

        ConfigProperty prop1 = new ConfigProperty();
        prop1.addToKeychain("app").addToKeychain("name").setValue("TestApp");

        ConfigProperty prop2 = new ConfigProperty();
        prop2.addToKeychain("app").addToKeychain("version").setValue("1.0.0");

        propertyList.add(prop1);
        propertyList.add(prop2);

        ConfigPropertiesBuilder result = builder.setConfigPropertyList(propertyList);

        assertSame(builder, result);

        String output = builder.writeToString();
        String[] lines = output.split("\n");

        assertEquals(2, lines.length);
        assertEquals("app.name=TestApp", lines[0]);
        assertEquals("app.version=1.0.0", lines[1]);
    }

    @Test
    void testSetConfigPropertyListOverwritesPreviousProperties() {
        builder.add("old.key", "oldValue");

        List<ConfigProperty> propertyList = new ArrayList<>();
        ConfigProperty prop = new ConfigProperty();
        prop.addToKeychain("new").addToKeychain("key").setValue("newValue");
        propertyList.add(prop);

        builder.setConfigPropertyList(propertyList);

        String result = builder.writeToString();
        assertEquals("new.key=newValue", result);
    }

    @Test
    void testWriteToStringWithEmptyList() {
        String result = builder.writeToString();
        assertEquals("", result);
    }

    @Test
    void testWriteToStringWithSingleProperty() {
        builder.add("app.name", "TestApp");

        String result = builder.writeToString();
        assertEquals("app.name=TestApp", result);
    }

    @Test
    void testWriteToStringWithMultipleProperties() {
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.port", "8080");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(3, lines.length);
    }

    @Test
    void testWriteToStringWithSpecialCharactersInValue() {
        builder.add("app.path", "/usr/local/app");
        builder.add("database.url", "jdbc:mysql://localhost:3306/testdb");
        builder.add("message", "Hello World!");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(3, lines.length);
        assertTrue(result.contains("app.path=/usr/local/app"));
        assertTrue(result.contains("database.url=jdbc:mysql://localhost:3306/testdb"));
        assertTrue(result.contains("message=Hello World!"));
    }

    @Test
    void testWriteToStringWithUnicodeCharacters() {
        builder.add("app.name", "测试应用");
        builder.add("app.description", "これはテストです");

        String result = builder.writeToString();
        assertTrue(result.contains("app.name=测试应用"));
        assertTrue(result.contains("app.description=これはテストです"));
    }

    @Test
    void testWriteToFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test-config.properties");

        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.port", "8080");

        builder.writeToFile(testFile.toString());

        assertTrue(Files.exists(testFile));

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        String[] lines = content.split("\n");

        assertEquals(3, lines.length);
        assertEquals("app.name=TestApp", lines[0]);
        assertEquals("app.version=1.0.0", lines[1]);
        assertEquals("server.port=8080", lines[2]);
    }

    @Test
    void testWriteToFileOverwritesExistingFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test-config.properties");

        // 先写入初始内容
        Files.writeString(testFile, "old.key=oldValue", StandardCharsets.US_ASCII);

        // 使用builder写入新内容
        builder.add("new.key", "newValue");
        builder.writeToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("new.key=newValue", content);
        assertFalse(content.contains("old.key"));
    }

    @Test
    void testWriteToFileWithEmptyBuilder(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("empty-config.properties");

        builder.writeToFile(testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("", content);
    }

    @Test
    void testAppendToFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("append-config.properties");

        // 先写入初始内容
        Files.writeString(testFile, "existing.key=existingValue\n", StandardCharsets.US_ASCII);

        // 追加新内容
        builder.add("new.key", "newValue");
        builder.appendToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertTrue(content.contains("existing.key=existingValue"));
        assertTrue(content.contains("new.key=newValue"));
    }

    @Test
    void testAppendToFileCreatesNewFileIfNotExists(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("new-append-config.properties");

        builder.add("app.name", "TestApp");
        builder.appendToFile(testFile.toString());

        assertTrue(Files.exists(testFile));
        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("app.name=TestApp", content);
    }

    @Test
    void testAppendToFileMultipleTimes(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("multi-append-config.properties");

        // 第一次追加
        ConfigPropertiesBuilder builder1 = new ConfigPropertiesBuilder();
        builder1.add("key1", "value1");
        builder1.appendToFile(testFile.toString());

        // 第二次追加
        ConfigPropertiesBuilder builder2 = new ConfigPropertiesBuilder();
        builder2.add("key2", "value2");
        builder2.appendToFile(testFile.toString());

        // 第三次追加
        ConfigPropertiesBuilder builder3 = new ConfigPropertiesBuilder();
        builder3.add("key3", "value3");
        builder3.appendToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertTrue(content.contains("key1=value1"));
        assertTrue(content.contains("key2=value2"));
        assertTrue(content.contains("key3=value3"));
    }

    @Test
    void testAppendToFileWithEmptyBuilder(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("empty-append-config.properties");

        Files.writeString(testFile, "existing.key=existingValue\n", StandardCharsets.US_ASCII);

        builder.appendToFile(testFile.toString());

        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        assertEquals("existing.key=existingValue\n", content);
    }

    @Test
    void testChainedOperations() {
        String result = builder
                .setPrefix("app")
                .add("name", "TestApp")
                .add("version", "1.0.0")
                .setPrefix("server")
                .add("port", "8080")
                .writeToString();

        assertNotNull(result);
        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("app.version=1.0.0"));
        assertTrue(result.contains("server.port=8080"));
    }

    @Test
    void testComplexConfigurationScenario() {
        builder.setPrefix("app");
        builder.add("name", "MyApplication");
        builder.add("version", "1.0.0");

        builder.setPrefix("server");
        builder.add(List.of("host"), "localhost");
        builder.add(List.of("port"), "8080");

        builder.setPrefix("database", "primary");
        builder.add("url", "jdbc:mysql://localhost:3306/testdb");
        builder.add("username", "root");
        builder.add("password", "secret");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(7, lines.length);
        assertTrue(result.contains("app.name=MyApplication"));
        assertTrue(result.contains("app.version=1.0.0"));
        assertTrue(result.contains("server.host=localhost"));
        assertTrue(result.contains("server.port=8080"));
        assertTrue(result.contains("database.primary.url=jdbc:mysql://localhost:3306/testdb"));
        assertTrue(result.contains("database.primary.username=root"));
        assertTrue(result.contains("database.primary.password=secret"));
    }

    @Test
    void testAddWithDeepNestedKeychain() {
        builder.add(List.of("level1", "level2", "level3", "level4", "level5"), "deepValue");

        String result = builder.writeToString();
        assertEquals("level1.level2.level3.level4.level5=deepValue", result);
    }

    @Test
    void testAddWithDeepNestedKeychainAndPrefix() {
        builder.setPrefix("root", "sub");
        builder.add(List.of("level1", "level2", "level3"), "deepValue");

        String result = builder.writeToString();
        assertEquals("root.sub.level1.level2.level3=deepValue", result);
    }

    @Test
    void testMixedKeychainFormats() {
        builder.add(List.of("app", "name"), "TestApp");
        builder.add("server.port", "8080");
        builder.add(List.of("database", "url"), "jdbc:mysql://localhost");

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(3, lines.length);
        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("server.port=8080"));
        assertTrue(result.contains("database.url=jdbc:mysql://localhost"));
    }

    @Test
    void testPropertyWithNumericValue() {
        builder.add("server.port", "8080");
        builder.add("timeout.seconds", "30");
        builder.add("max.connections", "100");

        String result = builder.writeToString();
        assertTrue(result.contains("server.port=8080"));
        assertTrue(result.contains("timeout.seconds=30"));
        assertTrue(result.contains("max.connections=100"));
    }

    @Test
    void testPropertyWithBooleanValue() {
        builder.add("feature.enabled", "true");
        builder.add("debug.mode", "false");
        builder.add("ssl.enabled", "TRUE");

        String result = builder.writeToString();
        assertTrue(result.contains("feature.enabled=true"));
        assertTrue(result.contains("debug.mode=false"));
        assertTrue(result.contains("ssl.enabled=TRUE"));
    }

    @Test
    void testPropertyWithUrlValue() {
        builder.add("database.url", "jdbc:mysql://localhost:3306/testdb");
        builder.add("api.endpoint", "https://api.example.com/v1");
        builder.add("ftp.server", "ftp://ftp.example.com:21");

        String result = builder.writeToString();
        assertTrue(result.contains("database.url=jdbc:mysql://localhost:3306/testdb"));
        assertTrue(result.contains("api.endpoint=https://api.example.com/v1"));
        assertTrue(result.contains("ftp.server=ftp://ftp.example.com:21"));
    }

    @Test
    void testPropertyWithPathValue() {
        builder.add("app.home", "/usr/local/app");
        builder.add("log.path", "/var/log/app.log");
        builder.add("data.dir", "/data/storage");

        String result = builder.writeToString();
        assertTrue(result.contains("app.home=/usr/local/app"));
        assertTrue(result.contains("log.path=/var/log/app.log"));
        assertTrue(result.contains("data.dir=/data/storage"));
    }

    @Test
    void testPropertyWithWindowsPathValue() {
        builder.add("app.home", "C:\\Program Files\\Application");
        builder.add("log.path", "D:\\Logs\\app.log");

        String result = builder.writeToString();
        assertTrue(result.contains("app.home=C:\\Program Files\\Application"));
        assertTrue(result.contains("log.path=D:\\Logs\\app.log"));
    }

    @Test
    void testPropertyWithSpacesInValue() {
        builder.add("app.name", "My Application Name");
        builder.add("app.description", "This is a test application");

        String result = builder.writeToString();
        assertTrue(result.contains("app.name=My Application Name"));
        assertTrue(result.contains("app.description=This is a test application"));
    }

    @Test
    void testPropertyWithSpecialCharactersInValue() {
        builder.add("password", "p@ssw0rd!#$");
        builder.add("regex.pattern", "^[a-zA-Z0-9]+$");
        builder.add("email", "test@example.com");

        String result = builder.writeToString();
        assertTrue(result.contains("password=p@ssw0rd!#$"));
        assertTrue(result.contains("regex.pattern=^[a-zA-Z0-9]+$"));
        assertTrue(result.contains("email=test@example.com"));
    }

    @Test
    void testEmptyKeychainInList() {
        builder.add(List.of(), "value");

        String result = builder.writeToString();
        assertEquals("=value", result);
    }

    @Test
    void testSingleElementKeychain() {
        builder.add(List.of("key"), "value");

        String result = builder.writeToString();
        assertEquals("key=value", result);
    }

    @Test
    void testPrefixResetBehavior() {
        builder.setPrefix("prefix1");
        builder.add("key1", "value1");

        builder.setPrefix("prefix2");
        builder.add("key2", "value2");

        String result = builder.writeToString();
        // Note: setPrefix累加前缀
        assertTrue(result.contains("prefix1.key1=value1"));
        assertTrue(result.contains("prefix2.key2=value2"));
    }

    @Test
    void testWriteAndReadRoundTrip(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("roundtrip-config.properties");

        // 写入
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.port", "8080");
        builder.writeToFile(testFile.toString());

        // 读取验证
        String content = Files.readString(testFile, StandardCharsets.US_ASCII);
        String[] lines = content.split("\n");

        assertEquals(3, lines.length);
        assertEquals("app.name=TestApp", lines[0]);
        assertEquals("app.version=1.0.0", lines[1]);
        assertEquals("server.port=8080", lines[2]);
    }

    @Test
    void testLargeNumberOfProperties() {
        for (int i = 0; i < 1000; i++) {
            builder.add("key" + i, "value" + i);
        }

        String result = builder.writeToString();
        String[] lines = result.split("\n");

        assertEquals(1000, lines.length);
        assertTrue(result.contains("key0=value0"));
        assertTrue(result.contains("key999=value999"));
    }

    @Test
    void testPropertyOverwriteByReassignment() {
        List<ConfigProperty> propertyList = new ArrayList<>();

        ConfigProperty prop1 = new ConfigProperty();
        prop1.addToKeychain("app").addToKeychain("name").setValue("FirstValue");
        propertyList.add(prop1);

        builder.setConfigPropertyList(propertyList);
        assertEquals("app.name=FirstValue", builder.writeToString());

        // 重新设置列表
        List<ConfigProperty> newPropertyList = new ArrayList<>();
        ConfigProperty prop2 = new ConfigProperty();
        prop2.addToKeychain("app").addToKeychain("name").setValue("SecondValue");
        newPropertyList.add(prop2);

        builder.setConfigPropertyList(newPropertyList);
        assertEquals("app.name=SecondValue", builder.writeToString());
    }

    @Test
    void testBuilderReusability() {
        // 第一次使用
        builder.add("key1", "value1");
        String result1 = builder.writeToString();
        assertEquals("key1=value1", result1);

        // 添加更多属性后再次使用
        builder.add("key2", "value2");
        String result2 = builder.writeToString();
        String[] lines = result2.split("\n");
        assertEquals(2, lines.length);
    }
}

