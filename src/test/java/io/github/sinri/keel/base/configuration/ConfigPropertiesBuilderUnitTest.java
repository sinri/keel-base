package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    void testDefaultConstructor() {
        ConfigPropertiesBuilder newBuilder = new ConfigPropertiesBuilder();
        assertNotNull(newBuilder);
        assertEquals("", newBuilder.writeToString());
    }

    @Test
    void testSetPrefix() {
        ConfigPropertiesBuilder result = builder.setPrefix(List.of("app", "config"));
        assertSame(builder, result);
    }

    @Test
    void testSetPrefixVarargs() {
        ConfigPropertiesBuilder result = builder.setPrefix("app", "config");
        assertSame(builder, result);
    }

    @Test
    void testSetPrefixNull() {
        ConfigPropertiesBuilder result = builder.setPrefix((List<String>) null);
        assertSame(builder, result);
    }

    @Test
    void testAddWithKeychain() {
        builder.add(List.of("app", "name"), "TestApp");
        String result = builder.writeToString();
        assertEquals("app.name=TestApp", result);
    }

    @Test
    void testAddWithSingleKey() {
        builder.add("singleKey", "singleValue");
        String result = builder.writeToString();
        assertEquals("singleKey=singleValue", result);
    }

    @Test
    void testAddWithPrefix() {
        builder.setPrefix("database", "primary");
        builder.add("host", "localhost");
        builder.add("port", "3306");

        String result = builder.writeToString();
        assertTrue(result.contains("database.primary.host=localhost"));
        assertTrue(result.contains("database.primary.port=3306"));
    }

    @Test
    void testAddWithPrefixAndKeychain() {
        builder.setPrefix("database");
        builder.add(List.of("primary", "host"), "localhost");
        builder.add(List.of("secondary", "host"), "localhost2");

        String result = builder.writeToString();
        assertTrue(result.contains("database.primary.host=localhost"));
        assertTrue(result.contains("database.secondary.host=localhost2"));
    }

    @Test
    void testAddWithNullValue() {
        builder.add("key", null);
        String result = builder.writeToString();
        assertEquals("key=", result);
    }

    @Test
    void testAddWithEmptyValue() {
        builder.add("key", "");
        String result = builder.writeToString();
        assertEquals("key=", result);
    }

    @Test
    void testAddMultipleProperties() {
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.host", "localhost");
        builder.add("server.port", "8080");

        String result = builder.writeToString();
        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("app.version=1.0.0"));
        assertTrue(result.contains("server.host=localhost"));
        assertTrue(result.contains("server.port=8080"));
    }

    @Test
    void testWriteToStringEmpty() {
        String result = builder.writeToString();
        assertEquals("", result);
    }

    @Test
    void testWriteToStringWithOneProperty() {
        builder.add("key", "value");
        String result = builder.writeToString();
        assertEquals("key=value", result);
    }

    @Test
    void testWriteToStringWithMultipleProperties() {
        builder.add("key1", "value1");
        builder.add("key2", "value2");
        builder.add("key3", "value3");

        String result = builder.writeToString();
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertEquals("key1=value1", lines[0]);
        assertEquals("key2=value2", lines[1]);
        assertEquals("key3=value3", lines[2]);
    }

    @Test
    void testWriteToFile() throws IOException {
        Path filePath = tempDir.resolve("test.properties");

        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.host", "localhost");

        builder.writeToFile(filePath.toString());

        assertTrue(Files.exists(filePath));

        String content = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertTrue(content.contains("app.name=TestApp"));
        assertTrue(content.contains("app.version=1.0.0"));
        assertTrue(content.contains("server.host=localhost"));
    }

    @Test
    void testWriteToFileOverwritesExisting() throws IOException {
        Path filePath = tempDir.resolve("overwrite.properties");

        // Write first time
        builder.add("key1", "value1");
        builder.writeToFile(filePath.toString());

        String firstContent = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertTrue(firstContent.contains("key1=value1"));

        // Write second time with different content
        ConfigPropertiesBuilder newBuilder = new ConfigPropertiesBuilder();
        newBuilder.add("key2", "value2");
        newBuilder.writeToFile(filePath.toString());

        String secondContent = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertFalse(secondContent.contains("key1=value1"));
        assertTrue(secondContent.contains("key2=value2"));
    }

    @Test
    void testAppendToFile() throws IOException {
        Path filePath = tempDir.resolve("append.properties");

        // Write initial content
        Files.writeString(filePath, "initial.key=initial.value\n", StandardCharsets.US_ASCII);

        // Append new content
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.appendToFile(filePath.toString());

        String content = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertTrue(content.contains("initial.key=initial.value"));
        assertTrue(content.contains("app.name=TestApp"));
        assertTrue(content.contains("app.version=1.0.0"));
    }

    @Test
    void testAppendToFileCreatesIfNotExists() throws IOException {
        Path filePath = tempDir.resolve("newfile.properties");

        builder.add("key", "value");
        builder.appendToFile(filePath.toString());

        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertTrue(content.contains("key=value"));
    }

    @Test
    void testAppendToFileMultipleTimes() throws IOException {
        Path filePath = tempDir.resolve("multiappend.properties");

        // First append
        builder.add("key1", "value1");
        builder.appendToFile(filePath.toString());

        // Second append
        ConfigPropertiesBuilder builder2 = new ConfigPropertiesBuilder();
        builder2.add("key2", "value2");
        builder2.appendToFile(filePath.toString());

        // Third append
        ConfigPropertiesBuilder builder3 = new ConfigPropertiesBuilder();
        builder3.add("key3", "value3");
        builder3.appendToFile(filePath.toString());

        String content = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertTrue(content.contains("key1=value1"));
        assertTrue(content.contains("key2=value2"));
        assertTrue(content.contains("key3=value3"));
    }

    @Test
    void testSetConfigPropertyList() {
        ConfigProperty prop1 = new ConfigProperty()
                .setKeychain(List.of("app", "name"))
                .setValue("TestApp");
        ConfigProperty prop2 = new ConfigProperty()
                .setKeychain(List.of("app", "version"))
                .setValue("1.0.0");

        List<ConfigProperty> list = List.of(prop1, prop2);

        ConfigPropertiesBuilder result = builder.setConfigPropertyList(list);
        assertSame(builder, result);

        String output = builder.writeToString();
        assertTrue(output.contains("app.name=TestApp"));
        assertTrue(output.contains("app.version=1.0.0"));
    }

    @Test
    void testSetConfigPropertyListReplacesExisting() {
        builder.add("old.key", "old.value");

        ConfigProperty newProp = new ConfigProperty()
                .setKeychain(List.of("new", "key"))
                .setValue("new.value");

        builder.setConfigPropertyList(List.of(newProp));

        String output = builder.writeToString();
        assertFalse(output.contains("old.key=old.value"));
        assertTrue(output.contains("new.key=new.value"));
    }

    @Test
    void testChainedOperations() {
        String result = builder
                .setPrefix("database", "primary")
                .add("host", "localhost")
                .add("port", "3306")
                .writeToString();

        assertTrue(result.contains("database.primary.host=localhost"));
        assertTrue(result.contains("database.primary.port=3306"));
    }

    @Test
    void testComplexConfiguration() {
        builder.setPrefix("app");
        builder.add("name", "TestApp");
        builder.add("version", "1.0.0");

        builder.setPrefix("server");
        builder.add("host", "localhost");
        builder.add("port", "8080");

        builder.setPrefix("database");
        builder.add(List.of("primary", "host"), "db1.example.com");
        builder.add(List.of("primary", "port"), "3306");
        builder.add(List.of("secondary", "host"), "db2.example.com");
        builder.add(List.of("secondary", "port"), "3307");

        String result = builder.writeToString();

        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("app.version=1.0.0"));
        assertTrue(result.contains("server.host=localhost"));
        assertTrue(result.contains("server.port=8080"));
        assertTrue(result.contains("database.primary.host=db1.example.com"));
        assertTrue(result.contains("database.primary.port=3306"));
        assertTrue(result.contains("database.secondary.host=db2.example.com"));
        assertTrue(result.contains("database.secondary.port=3307"));
    }

    @Test
    void testPropertyWithSpecialCharacters() {
        builder.add("path", "C:\\Users\\Test\\Application");
        builder.add("url", "jdbc:mysql://localhost:3306/testdb");
        builder.add("description", "This is a test: with special chars");

        String result = builder.writeToString();
        assertTrue(result.contains("path=C:\\Users\\Test\\Application"));
        assertTrue(result.contains("url=jdbc:mysql://localhost:3306/testdb"));
        assertTrue(result.contains("description=This is a test: with special chars"));
    }

    @Test
    void testWriteEmptyListToFile() throws IOException {
        Path filePath = tempDir.resolve("empty.properties");
        builder.setConfigPropertyList(List.of());
        builder.writeToFile(filePath.toString());

        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath, StandardCharsets.US_ASCII);
        assertEquals("", content);
    }

    @Test
    void testPrefixDoesNotAffectPreviouslyAddedProperties() {
        builder.add("key1", "value1");
        builder.setPrefix("prefix");
        builder.add("key2", "value2");

        String result = builder.writeToString();
        assertTrue(result.contains("key1=value1"));
        assertTrue(result.contains("prefix.key2=value2"));
    }

    @Test
    void testChangePrefixMultipleTimes() {
        builder.setPrefix("prefix1");
        builder.add("key1", "value1");

        builder.setPrefix("prefix2");
        builder.add("key2", "value2");

        builder.setPrefix((List<String>) null);
        builder.add("key3", "value3");

        String result = builder.writeToString();
        assertTrue(result.contains("prefix1.key1=value1"));
        assertTrue(result.contains("prefix2.key2=value2"));
        assertTrue(result.contains("key3=value3"));
    }

    @Test
    void testDeepNestedProperties() {
        builder.add(List.of("level1", "level2", "level3", "level4", "level5"), "deepValue");

        String result = builder.writeToString();
        assertTrue(result.contains("level1.level2.level3.level4.level5=deepValue"));
    }

    @Test
    void testNumericValues() {
        builder.add("int.value", "42");
        builder.add("long.value", "123456789012345");
        builder.add("float.value", "3.14");
        builder.add("double.value", "2.718281828");

        String result = builder.writeToString();
        assertTrue(result.contains("int.value=42"));
        assertTrue(result.contains("long.value=123456789012345"));
        assertTrue(result.contains("float.value=3.14"));
        assertTrue(result.contains("double.value=2.718281828"));
    }

    @Test
    void testBooleanValues() {
        builder.add("feature.enabled", "true");
        builder.add("feature.disabled", "false");
        builder.add("feature.yes", "YES");
        builder.add("feature.no", "NO");

        String result = builder.writeToString();
        assertTrue(result.contains("feature.enabled=true"));
        assertTrue(result.contains("feature.disabled=false"));
        assertTrue(result.contains("feature.yes=YES"));
        assertTrue(result.contains("feature.no=NO"));
    }

    @Test
    void testUnicodeValues() {
        builder.add("app.name", "测试应用");
        builder.add("app.description", "これはテストです");

        String result = builder.writeToString();
        assertTrue(result.contains("app.name=测试应用"));
        assertTrue(result.contains("app.description=これはテストです"));
    }

    @Test
    void testLargePropertyList() {
        for (int i = 0; i < 1000; i++) {
            builder.add("key" + i, "value" + i);
        }

        String result = builder.writeToString();
        String[] lines = result.split("\n");
        assertEquals(1000, lines.length);

        assertTrue(result.contains("key0=value0"));
        assertTrue(result.contains("key500=value500"));
        assertTrue(result.contains("key999=value999"));
    }

    @Test
    void testRoundTripWithConfigTree() throws IOException, NotConfiguredException {
        // Build properties
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.host", "localhost");
        builder.add("server.port", "8080");

        // Write to file
        Path filePath = tempDir.resolve("roundtrip.properties");
        builder.writeToFile(filePath.toString());

        // Load back into ConfigTree
        ConfigNode rootNode = ConfigNode.create("root");
        ConfigTree tree = ConfigTree.wrap(rootNode);
        tree.loadPropertiesFile(filePath.toString());

        // Verify values
        assertEquals("TestApp", tree.readString(List.of("app", "name")));
        assertEquals("1.0.0", tree.readString(List.of("app", "version")));
        assertEquals("localhost", tree.readString(List.of("server", "host")));
        assertEquals(8080, tree.readInteger(List.of("server", "port")));
    }
}

