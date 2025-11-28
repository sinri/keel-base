package io.github.sinri.keel.base.configuration;

import io.github.sinri.keel.base.KeelInstance;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigPropertiesBuilder单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class ConfigPropertiesBuilderUnitTest {
    private ConfigPropertiesBuilder builder;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        KeelInstance.Keel.initializeVertx(vertx);
        builder = new ConfigPropertiesBuilder();
        testContext.completeNow();
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
    void testWriteToFile(Vertx vertx, VertxTestContext testContext) {
        builder.add("app.name", "TestApp");
        String testFile = "/tmp/test-config-" + System.currentTimeMillis() + ".properties";

        builder.writeToFile(testFile)
               .onComplete(ar -> {
                   if (ar.succeeded()) {
                       vertx.fileSystem().readFile(testFile)
                            .onComplete(readAr -> {
                                if (readAr.succeeded()) {
                                    String content = readAr.result().toString();
                                    assertTrue(content.contains("app.name=TestApp"));
                                    // Clean up
                                    vertx.fileSystem().delete(testFile)
                                         .onComplete(deleteAr -> testContext.completeNow());
                                } else {
                                    testContext.failNow(readAr.cause());
                                }
                            });
                   } else {
                       testContext.failNow(ar.cause());
                   }
               });
    }

    @Test
    void testAppendToFile(Vertx vertx, VertxTestContext testContext) {
        builder.add("app.name", "TestApp");
        String testFile = "/tmp/test-config-append-" + System.currentTimeMillis() + ".properties";

        // First write
        builder.writeToFile(testFile)
               .compose(v -> {
                   // Then append
                   ConfigPropertiesBuilder builder2 = new ConfigPropertiesBuilder();
                   builder2.add("app.version", "1.0.0");
                   return builder2.appendToFile(testFile);
               })
               .onComplete(ar -> {
                   if (ar.succeeded()) {
                       vertx.fileSystem().readFile(testFile)
                            .onComplete(readAr -> {
                                if (readAr.succeeded()) {
                                    String content = readAr.result().toString();
                                    assertTrue(content.contains("app.name=TestApp"));
                                    assertTrue(content.contains("app.version=1.0.0"));
                                    // Clean up
                                    vertx.fileSystem().delete(testFile)
                                         .onComplete(deleteAr -> testContext.completeNow());
                                } else {
                                    testContext.failNow(readAr.cause());
                                }
                            });
                   } else {
                       testContext.failNow(ar.cause());
                   }
               });
    }
}

