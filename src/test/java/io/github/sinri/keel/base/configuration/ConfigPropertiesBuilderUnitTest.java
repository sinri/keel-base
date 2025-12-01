package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigPropertiesBuilder单元测试。
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
    void testAddWithList() {
        builder.add(List.of("app", "name"), "TestApp");
        String result = builder.writeToString();

        assertTrue(result.contains("app.name=TestApp"));
    }

    @Test
    void testAddWithString() {
        builder.add("app.name", "TestApp");
        String result = builder.writeToString();

        assertTrue(result.contains("app.name=TestApp"));
    }

    @Test
    void testSetPrefix() {
        builder.setPrefix("prefix");
        builder.add("key", "value");
        String result = builder.writeToString();

        assertTrue(result.contains("prefix.key=value"));
    }

    @Test
    void testSetPrefixWithList() {
        builder.setPrefix(List.of("level1", "level2"));
        builder.add("key", "value");
        String result = builder.writeToString();

        assertTrue(result.contains("level1.level2.key=value"));
    }

    @Test
    void testSetPrefixWithVarArgs() {
        builder.setPrefix("level1", "level2");
        builder.add("key", "value");
        String result = builder.writeToString();

        assertTrue(result.contains("level1.level2.key=value"));
    }

    @Test
    void testWriteToString() {
        builder.add("app.name", "TestApp");
        builder.add("app.version", "1.0.0");
        builder.add("server.port", "8080");

        String result = builder.writeToString();
        assertNotNull(result);
        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("app.version=1.0.0"));
        assertTrue(result.contains("server.port=8080"));
    }

    @Test
    void testWriteToStringEmpty() {
        String result = builder.writeToString();
        assertEquals("", result);
    }

    @Test
    void testSetConfigPropertyList() {
        ConfigProperty prop1 = new ConfigProperty()
                .setKeychain(List.of("app", "name"))
                .setValue("TestApp");
        ConfigProperty prop2 = new ConfigProperty()
                .setKeychain(List.of("app", "version"))
                .setValue("1.0.0");

        builder.setConfigPropertyList(List.of(prop1, prop2));
        String result = builder.writeToString();

        assertTrue(result.contains("app.name=TestApp"));
        assertTrue(result.contains("app.version=1.0.0"));
    }

    @Test
    void testWriteToFile() throws IOException {
        builder.add("app.name", "TestApp");
        String testFile = "/tmp/test-config-" + System.currentTimeMillis() + ".properties";

        try {
            builder.writeToFile(testFile);
            Path path = Paths.get(testFile);
            String content = Files.readString(path, StandardCharsets.US_ASCII);
            assertTrue(content.contains("app.name=TestApp"));
        } finally {
            // Clean up
            Path path = Paths.get(testFile);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }

    @Test
    void testAppendToFile() throws IOException {
        builder.add("app.name", "TestApp");
        String testFile = "/tmp/test-config-append-" + System.currentTimeMillis() + ".properties";

        try {
            // First write
            builder.writeToFile(testFile);
            // Then append
            ConfigPropertiesBuilder builder2 = new ConfigPropertiesBuilder();
            builder2.add("app.version", "1.0.0");
            builder2.appendToFile(testFile);

            Path path = Paths.get(testFile);
            String content = Files.readString(path, StandardCharsets.US_ASCII);
            assertTrue(content.contains("app.name=TestApp"));
            assertTrue(content.contains("app.version=1.0.0"));
        } finally {
            // Clean up
            Path path = Paths.get(testFile);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        }
    }
}

