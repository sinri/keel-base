package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigTree单元测试。
 * <p>
 * 测试ConfigTree接口和ConfigTreeImpl实现的各项功能。
 *
 * @since 5.0.0
 */
class ConfigTreeUnitTest {
    private ConfigTree configTree;
    @TempDir
    Path tempDir;
    private ConfigNode rootNode;

    @BeforeEach
    void setUp() {
        rootNode = ConfigNode.create("root");
        configTree = ConfigTree.wrap(rootNode);
    }

    @Test
    void testWrap() {
        ConfigNode node = ConfigNode.create("testRoot");
        ConfigTree tree = ConfigTree.wrap(node);
        assertNotNull(tree);
    }

    @Test
    void testLoadProperties() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");

        ConfigTree result = configTree.loadProperties(properties);
        assertSame(configTree, result); // Should return this for chaining
    }

    @Test
    void testLoadPropertiesFile() throws IOException, NotConfiguredException {
        // Create a temporary properties file
        File propertiesFile = tempDir.resolve("test.properties").toFile();
        Files.writeString(propertiesFile.toPath(),
                "app.name=TestApp\n" +
                        "app.version=1.0.0\n" +
                        "server.host=localhost\n" +
                        "server.port=8080\n",
                StandardCharsets.UTF_8);

        configTree.loadPropertiesFile(propertiesFile.getAbsolutePath());

        String appName = configTree.readString(List.of("app", "name"));
        assertEquals("TestApp", appName);
    }

    @Test
    void testLoadPropertiesFileWithCharset() throws IOException, NotConfiguredException {
        File propertiesFile = tempDir.resolve("test-charset.properties").toFile();
        Files.writeString(propertiesFile.toPath(),
                "app.name=TestApp\n" +
                        "app.version=2.0.0\n",
                StandardCharsets.UTF_8);

        configTree.loadPropertiesFile(propertiesFile.getAbsolutePath(), StandardCharsets.UTF_8);

        String appVersion = configTree.readString(List.of("app", "version"));
        assertEquals("2.0.0", appVersion);
    }

    @Test
    void testLoadPropertiesFileNotFoundUsesEmbedded() {
        // This test uses embedded resource
        assertThrows(IOException.class, () ->
                configTree.loadPropertiesFile("nonexistent.properties")
        );
    }

    @Test
    void testLoadPropertiesFileContent() throws NotConfiguredException {
        String content = "app.name=TestApp\n" +
                "app.version=1.0.0\n" +
                "server.host=localhost\n" +
                "server.port=8080\n";

        configTree.loadPropertiesFileContent(content);

        String appName = configTree.readString(List.of("app", "name"));
        assertEquals("TestApp", appName);
    }

    @Test
    void testLoadPropertiesFileContentInvalidFormat() {
        String invalidContent = "invalid\nformat\nno equals";

        // Should not throw, but properties will be empty or malformed
        assertDoesNotThrow(() -> configTree.loadPropertiesFileContent(invalidContent));
    }

    @Test
    void testExtractWithListPath() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("server.host", "localhost");
        configTree.loadProperties(properties);

        ConfigTree extracted = configTree.extract(List.of("app"));
        assertNotNull(extracted);

        String name = extracted.readString(List.of("name"));
        assertEquals("TestApp", name);
    }

    @Test
    void testExtractWithVarargsPath() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("server.host", "localhost");
        configTree.loadProperties(properties);

        ConfigTree extracted = configTree.extract("app");
        assertNotNull(extracted);

        String name = extracted.readString(List.of("name"));
        assertEquals("TestApp", name);
    }

    @Test
    void testExtractWithEmptyPath() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        configTree.loadProperties(properties);

        ConfigTree extracted = configTree.extract(List.of());
        assertNotNull(extracted);
        assertSame(configTree, extracted);
    }

    @Test
    void testExtractWithNonExistentPath() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        configTree.loadProperties(properties);

        // When path has more than one element and first element doesn't exist
        ConfigTree extracted = configTree.extract(List.of("nonexistent", "child"));
        assertNull(extracted);
    }

    @Test
    void testExtractWithDeepPath() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("a.b.c.d.e", "deepValue");
        configTree.loadProperties(properties);

        ConfigTree extracted = configTree.extract(List.of("a", "b", "c"));
        assertNotNull(extracted);

        String value = extracted.readString(List.of("d", "e"));
        assertEquals("deepValue", value);
    }

    @Test
    void testReadString() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("server.host", "localhost");
        configTree.loadProperties(properties);

        String appName = configTree.readString(List.of("app", "name"));
        assertEquals("TestApp", appName);

        String serverHost = configTree.readString(List.of("server", "host"));
        assertEquals("localhost", serverHost);
    }

    @Test
    void testReadStringNotConfigured() {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        configTree.loadProperties(properties);

        assertThrows(NotConfiguredException.class, () ->
                configTree.readString(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testReadStringNullValue() {
        rootNode.ensureChild("app").ensureChild("name").setValue(null);

        assertThrows(NotConfiguredException.class, () ->
                configTree.readString(List.of("app", "name"))
        );
    }

    @Test
    void testReadBoolean() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("feature.enabled", "true");
        properties.setProperty("feature.disabled", "false");
        properties.setProperty("feature.yes", "YES");
        properties.setProperty("feature.no", "NO");
        properties.setProperty("feature.mixed", "TrUe");
        configTree.loadProperties(properties);

        assertTrue(configTree.readBoolean(List.of("feature", "enabled")));
        assertFalse(configTree.readBoolean(List.of("feature", "disabled")));
        assertTrue(configTree.readBoolean(List.of("feature", "yes")));
        assertFalse(configTree.readBoolean(List.of("feature", "no")));
        assertTrue(configTree.readBoolean(List.of("feature", "mixed")));
    }

    @Test
    void testReadBooleanNotConfigured() {
        assertThrows(NotConfiguredException.class, () ->
                configTree.readBoolean(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testReadInteger() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("server.port", "8080");
        properties.setProperty("server.timeout", "5000");
        properties.setProperty("server.negative", "-100");
        configTree.loadProperties(properties);

        assertEquals(8080, configTree.readInteger(List.of("server", "port")));
        assertEquals(5000, configTree.readInteger(List.of("server", "timeout")));
        assertEquals(-100, configTree.readInteger(List.of("server", "negative")));
    }

    @Test
    void testReadIntegerInvalidFormat() {
        Properties properties = new Properties();
        properties.setProperty("invalid.number", "notAnInteger");
        configTree.loadProperties(properties);

        assertThrows(NumberFormatException.class, () ->
                configTree.readInteger(List.of("invalid", "number"))
        );
    }

    @Test
    void testReadIntegerNotConfigured() {
        assertThrows(NotConfiguredException.class, () ->
                configTree.readInteger(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testReadLong() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("data.size", "123456789012345");
        properties.setProperty("data.small", "100");
        configTree.loadProperties(properties);

        assertEquals(123456789012345L, configTree.readLong(List.of("data", "size")));
        assertEquals(100L, configTree.readLong(List.of("data", "small")));
    }

    @Test
    void testReadLongInvalidFormat() {
        Properties properties = new Properties();
        properties.setProperty("invalid.long", "notALong");
        configTree.loadProperties(properties);

        assertThrows(NumberFormatException.class, () ->
                configTree.readLong(List.of("invalid", "long"))
        );
    }

    @Test
    void testReadLongNotConfigured() {
        assertThrows(NotConfiguredException.class, () ->
                configTree.readLong(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testReadFloat() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("value.pi", "3.14159");
        properties.setProperty("value.e", "2.71828");
        properties.setProperty("value.negative", "-1.5");
        configTree.loadProperties(properties);

        assertEquals(3.14159f, configTree.readFloat(List.of("value", "pi")), 0.00001f);
        assertEquals(2.71828f, configTree.readFloat(List.of("value", "e")), 0.00001f);
        assertEquals(-1.5f, configTree.readFloat(List.of("value", "negative")), 0.00001f);
    }

    @Test
    void testReadFloatInvalidFormat() {
        Properties properties = new Properties();
        properties.setProperty("invalid.float", "notAFloat");
        configTree.loadProperties(properties);

        assertThrows(NumberFormatException.class, () ->
                configTree.readFloat(List.of("invalid", "float"))
        );
    }

    @Test
    void testReadFloatNotConfigured() {
        assertThrows(NotConfiguredException.class, () ->
                configTree.readFloat(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testReadDouble() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("value.pi", "3.141592653589793");
        properties.setProperty("value.e", "2.718281828459045");
        properties.setProperty("value.negative", "-1.5");
        configTree.loadProperties(properties);

        assertEquals(3.141592653589793, configTree.readDouble(List.of("value", "pi")), 0.000000000000001);
        assertEquals(2.718281828459045, configTree.readDouble(List.of("value", "e")), 0.000000000000001);
        assertEquals(-1.5, configTree.readDouble(List.of("value", "negative")), 0.000000000000001);
    }

    @Test
    void testReadDoubleInvalidFormat() {
        Properties properties = new Properties();
        properties.setProperty("invalid.double", "notADouble");
        configTree.loadProperties(properties);

        assertThrows(NumberFormatException.class, () ->
                configTree.readDouble(List.of("invalid", "double"))
        );
    }

    @Test
    void testReadDoubleNotConfigured() {
        assertThrows(NotConfiguredException.class, () ->
                configTree.readDouble(List.of("nonexistent", "key"))
        );
    }

    @Test
    void testComplexConfiguration() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.enabled", "true");
        properties.setProperty("server.host", "localhost");
        properties.setProperty("server.port", "8080");
        properties.setProperty("server.timeout", "5000");
        properties.setProperty("database.url", "jdbc:mysql://localhost:3306/test");
        properties.setProperty("database.pool.size", "10");
        properties.setProperty("database.pool.timeout", "30000");
        configTree.loadProperties(properties);

        // Test string values
        assertEquals("TestApp", configTree.readString(List.of("app", "name")));
        assertEquals("1.0.0", configTree.readString(List.of("app", "version")));
        assertEquals("localhost", configTree.readString(List.of("server", "host")));
        assertEquals("jdbc:mysql://localhost:3306/test", configTree.readString(List.of("database", "url")));

        // Test boolean value
        assertTrue(configTree.readBoolean(List.of("app", "enabled")));

        // Test integer values
        assertEquals(8080, configTree.readInteger(List.of("server", "port")));
        assertEquals(5000, configTree.readInteger(List.of("server", "timeout")));
        assertEquals(10, configTree.readInteger(List.of("database", "pool", "size")));
        assertEquals(30000, configTree.readInteger(List.of("database", "pool", "timeout")));
    }

    @Test
    void testExtractAndRead() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("database.primary.host", "db1.example.com");
        properties.setProperty("database.primary.port", "3306");
        properties.setProperty("database.secondary.host", "db2.example.com");
        properties.setProperty("database.secondary.port", "3307");
        configTree.loadProperties(properties);

        // Extract database subtree
        ConfigTree databaseTree = configTree.extract("database");
        assertNotNull(databaseTree);

        // Extract primary subtree
        ConfigTree primaryTree = databaseTree.extract("primary");
        assertNotNull(primaryTree);

        assertEquals("db1.example.com", primaryTree.readString(List.of("host")));
        assertEquals(3306, primaryTree.readInteger(List.of("port")));

        // Extract secondary subtree
        ConfigTree secondaryTree = databaseTree.extract("secondary");
        assertNotNull(secondaryTree);

        assertEquals("db2.example.com", secondaryTree.readString(List.of("host")));
        assertEquals(3307, secondaryTree.readInteger(List.of("port")));
    }

    @Test
    void testChainedOperations() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "TestApp");
        properties.setProperty("app.version", "1.0.0");

        ConfigTree result = configTree
                .loadProperties(properties);

        assertSame(configTree, result);

        assertEquals("TestApp", configTree.readString(List.of("app", "name")));
        assertEquals("1.0.0", configTree.readString(List.of("app", "version")));
    }

    @Test
    void testEmptyPropertiesFile() throws IOException {
        File propertiesFile = tempDir.resolve("empty.properties").toFile();
        Files.writeString(propertiesFile.toPath(), "", StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> configTree.loadPropertiesFile(propertiesFile.getAbsolutePath()));
    }

    @Test
    void testPropertiesWithComments() throws IOException, NotConfiguredException {
        File propertiesFile = tempDir.resolve("commented.properties").toFile();
        Files.writeString(propertiesFile.toPath(),
                "# This is a comment\n" +
                        "app.name=TestApp\n" +
                        "! This is also a comment\n" +
                        "app.version=1.0.0\n",
                StandardCharsets.UTF_8);

        configTree.loadPropertiesFile(propertiesFile.getAbsolutePath());

        assertEquals("TestApp", configTree.readString(List.of("app", "name")));
        assertEquals("1.0.0", configTree.readString(List.of("app", "version")));
    }

    @Test
    void testPropertiesWithSpecialCharacters() throws NotConfiguredException {
        Properties properties = new Properties();
        properties.setProperty("app.name", "Test App");
        properties.setProperty("app.description", "This is a test: with colon");
        properties.setProperty("app.path", "C:\\Users\\Test");
        configTree.loadProperties(properties);

        assertEquals("Test App", configTree.readString(List.of("app", "name")));
        assertEquals("This is a test: with colon", configTree.readString(List.of("app", "description")));
        assertEquals("C:\\Users\\Test", configTree.readString(List.of("app", "path")));
    }
}

