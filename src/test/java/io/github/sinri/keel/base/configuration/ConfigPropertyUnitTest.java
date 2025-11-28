package io.github.sinri.keel.base.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ConfigProperty单元测试。
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
    void testSetKeychain() {
        List<String> keychain = List.of("app", "name");
        configProperty.setKeychain(keychain);

        assertEquals("app.name", configProperty.getPropertyName());
    }

    @Test
    void testAddToKeychain() {
        configProperty.addToKeychain("app");
        configProperty.addToKeychain("name");

        assertEquals("app.name", configProperty.getPropertyName());
    }

    @Test
    void testSetValue() {
        configProperty.setValue("TestApp");
        assertEquals("TestApp", configProperty.getPropertyValue());
    }

    @Test
    void testSetNullValue() {
        configProperty.setValue(null);
        assertEquals("", configProperty.getPropertyValue());
    }

    @Test
    void testGetPropertyName() {
        configProperty.setKeychain(List.of("server", "port"));
        assertEquals("server.port", configProperty.getPropertyName());
    }

    @Test
    void testGetPropertyNameEmptyKeychain() {
        assertEquals("", configProperty.getPropertyName());
    }

    @Test
    void testGetPropertyValue() {
        configProperty.setValue("8080");
        assertEquals("8080", configProperty.getPropertyValue());
    }

    @Test
    void testToString() {
        configProperty.setKeychain(List.of("app", "name"));
        configProperty.setValue("TestApp");

        String str = configProperty.toString();
        assertEquals("app.name=TestApp", str);
    }

    @Test
    void testToStringEmptyValue() {
        configProperty.setKeychain(List.of("app", "name"));
        configProperty.setValue("");

        String str = configProperty.toString();
        assertEquals("app.name=", str);
    }

    @Test
    void testChaining() {
        ConfigProperty property = new ConfigProperty()
                .setKeychain(List.of("app", "name"))
                .setValue("TestApp");

        assertEquals("app.name", property.getPropertyName());
        assertEquals("TestApp", property.getPropertyValue());
    }

    @Test
    void testMultipleAddToKeychain() {
        configProperty.addToKeychain("level1");
        configProperty.addToKeychain("level2");
        configProperty.addToKeychain("level3");

        assertEquals("level1.level2.level3", configProperty.getPropertyName());
    }
}

