package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigProperty单元测试。
 * <p>
 * 测试ConfigProperty类的各项功能。
 *
 * @since 5.0.0
 */
class ConfigPropertyUnitTest {
    private ConfigProperty configProperty;

    @BeforeEach
    void setUp() {
        configProperty = new ConfigProperty();
    }

    @Test
    void testDefaultConstructor() {
        ConfigProperty property = new ConfigProperty();
        assertNotNull(property);
        assertEquals("", property.getPropertyName());
        assertEquals("", property.getPropertyValue());
    }

    @Test
    void testSetKeychain() {
        List<String> keychain = List.of("app", "name");
        configProperty.setKeychain(keychain);

        assertEquals("app.name", configProperty.getPropertyName());
    }

    @Test
    void testSetKeychainWithMultipleLevels() {
        List<String> keychain = List.of("database", "pool", "size");
        configProperty.setKeychain(keychain);

        assertEquals("database.pool.size", configProperty.getPropertyName());
    }

    @Test
    void testSetKeychainWithSingleKey() {
        List<String> keychain = List.of("singleKey");
        configProperty.setKeychain(keychain);

        assertEquals("singleKey", configProperty.getPropertyName());
    }

    @Test
    void testSetKeychainWithEmptyList() {
        List<String> keychain = List.of();
        configProperty.setKeychain(keychain);

        assertEquals("", configProperty.getPropertyName());
    }

    @Test
    void testAddToKeychain() {
        configProperty.addToKeychain("app");
        assertEquals("app", configProperty.getPropertyName());

        configProperty.addToKeychain("name");
        assertEquals("app.name", configProperty.getPropertyName());

        configProperty.addToKeychain("full");
        assertEquals("app.name.full", configProperty.getPropertyName());
    }

    @Test
    void testSetValue() {
        configProperty.setValue("testValue");
        assertEquals("testValue", configProperty.getPropertyValue());
    }

    @Test
    void testSetValueNull() {
        configProperty.setValue(null);
        assertEquals("", configProperty.getPropertyValue());
    }

    @Test
    void testSetValueEmpty() {
        configProperty.setValue("");
        assertEquals("", configProperty.getPropertyValue());
    }

    @Test
    void testSetValueWithSpecialCharacters() {
        configProperty.setValue("value with spaces");
        assertEquals("value with spaces", configProperty.getPropertyValue());

        configProperty.setValue("value:with:colons");
        assertEquals("value:with:colons", configProperty.getPropertyValue());

        configProperty.setValue("value=with=equals");
        assertEquals("value=with=equals", configProperty.getPropertyValue());
    }

    @Test
    void testGetPropertyName() {
        configProperty.setKeychain(List.of("app", "name"));
        assertEquals("app.name", configProperty.getPropertyName());
    }

    @Test
    void testGetPropertyValue() {
        configProperty.setValue("testValue");
        assertEquals("testValue", configProperty.getPropertyValue());
    }

    @Test
    void testToString() {
        configProperty.setKeychain(List.of("app", "name"));
        configProperty.setValue("TestApp");

        assertEquals("app.name=TestApp", configProperty.toString());
    }

    @Test
    void testToStringWithEmptyKeychain() {
        configProperty.setValue("value");
        assertEquals("=value", configProperty.toString());
    }

    @Test
    void testToStringWithEmptyValue() {
        configProperty.setKeychain(List.of("app", "name"));
        assertEquals("app.name=", configProperty.toString());
    }

    @Test
    void testToStringWithBothEmpty() {
        assertEquals("=", configProperty.toString());
    }

    @Test
    void testChainedOperations() {
        ConfigProperty result = configProperty
                .setKeychain(List.of("app", "name"))
                .setValue("TestApp");

        assertSame(configProperty, result);
        assertEquals("app.name=TestApp", configProperty.toString());
    }

    @Test
    void testChainedWithAddToKeychain() {
        ConfigProperty result = configProperty
                .addToKeychain("app")
                .addToKeychain("name")
                .setValue("TestApp");

        assertSame(configProperty, result);
        assertEquals("app.name=TestApp", configProperty.toString());
    }

    @Test
    void testMixedKeychainOperations() {
        configProperty.setKeychain(List.of("app"));
        configProperty.addToKeychain("config");
        configProperty.addToKeychain("name");

        assertEquals("app.config.name", configProperty.getPropertyName());
    }

    @Test
    void testComplexPropertyName() {
        configProperty
                .addToKeychain("level1")
                .addToKeychain("level2")
                .addToKeychain("level3")
                .addToKeychain("level4")
                .addToKeychain("level5")
                .setValue("deepValue");

        assertEquals("level1.level2.level3.level4.level5", configProperty.getPropertyName());
        assertEquals("level1.level2.level3.level4.level5=deepValue", configProperty.toString());
    }

    @Test
    void testPropertyWithNumericValue() {
        configProperty.setKeychain(List.of("server", "port"));
        configProperty.setValue("8080");

        assertEquals("server.port=8080", configProperty.toString());
    }

    @Test
    void testPropertyWithBooleanValue() {
        configProperty.setKeychain(List.of("feature", "enabled"));
        configProperty.setValue("true");

        assertEquals("feature.enabled=true", configProperty.toString());
    }

    @Test
    void testPropertyWithLongValue() {
        configProperty.setKeychain(List.of("data", "size"));
        configProperty.setValue("123456789012345");

        assertEquals("data.size=123456789012345", configProperty.toString());
    }

    @Test
    void testPropertyWithFloatValue() {
        configProperty.setKeychain(List.of("value", "pi"));
        configProperty.setValue("3.14159");

        assertEquals("value.pi=3.14159", configProperty.toString());
    }

    @Test
    void testPropertyWithPathValue() {
        configProperty.setKeychain(List.of("app", "path"));
        configProperty.setValue("/usr/local/app");

        assertEquals("app.path=/usr/local/app", configProperty.toString());
    }

    @Test
    void testPropertyWithWindowsPathValue() {
        configProperty.setKeychain(List.of("app", "path"));
        configProperty.setValue("C:\\Users\\Test\\Application");

        assertEquals("app.path=C:\\Users\\Test\\Application", configProperty.toString());
    }

    @Test
    void testPropertyWithUrlValue() {
        configProperty.setKeychain(List.of("database", "url"));
        configProperty.setValue("jdbc:mysql://localhost:3306/testdb");

        assertEquals("database.url=jdbc:mysql://localhost:3306/testdb", configProperty.toString());
    }

    @Test
    void testPropertyWithMultilineValue() {
        configProperty.setKeychain(List.of("app", "description"));
        configProperty.setValue("Line 1\nLine 2\nLine 3");

        assertTrue(configProperty.getPropertyValue().contains("\n"));
        assertEquals("app.description=Line 1\nLine 2\nLine 3", configProperty.toString());
    }

    @Test
    void testPropertyWithUnicodeValue() {
        configProperty.setKeychain(List.of("app", "name"));
        configProperty.setValue("测试应用");

        assertEquals("app.name=测试应用", configProperty.toString());
    }

    @Test
    void testSetKeychainMultipleTimes() {
        configProperty.setKeychain(List.of("first", "keychain"));
        assertEquals("first.keychain", configProperty.getPropertyName());

        // Setting keychain again should append
        configProperty.setKeychain(List.of("second", "keychain"));
        assertEquals("first.keychain.second.keychain", configProperty.getPropertyName());
    }

    @Test
    void testSetValueMultipleTimes() {
        configProperty.setValue("firstValue");
        assertEquals("firstValue", configProperty.getPropertyValue());

        configProperty.setValue("secondValue");
        assertEquals("secondValue", configProperty.getPropertyValue());

        configProperty.setValue(null);
        assertEquals("", configProperty.getPropertyValue());
    }

    @Test
    void testCompletePropertyConfiguration() {
        ConfigProperty property = new ConfigProperty()
                .setKeychain(List.of("database", "primary", "connection"))
                .addToKeychain("pool")
                .addToKeychain("size")
                .setValue("50");

        assertEquals("database.primary.connection.pool.size", property.getPropertyName());
        assertEquals("50", property.getPropertyValue());
        assertEquals("database.primary.connection.pool.size=50", property.toString());
    }
}

