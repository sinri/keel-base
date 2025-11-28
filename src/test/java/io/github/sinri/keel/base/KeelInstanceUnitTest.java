package io.github.sinri.keel.base;

import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeelInstance单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class KeelInstanceUnitTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        // Initialize Vertx for tests
        KeelInstance.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testSingleton() {
        KeelInstance instance1 = KeelInstance.Keel;
        KeelInstance instance2 = KeelInstance.Keel;

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    void testGetConfiguration() {
        ConfigTree config = KeelInstance.Keel.getConfiguration();
        assertNotNull(config);
    }

    @Test
    void testConfigMethod(VertxTestContext testContext) {
        // Set up configuration
        ConfigTree config = KeelInstance.Keel.getConfiguration();
        config.ensureChild("app").ensureChild("name").setValue("TestApp");

        String value = KeelInstance.Keel.config("app.name");
        assertEquals("TestApp", value);

        String nonExistent = KeelInstance.Keel.config("non.existent");
        assertNull(nonExistent);

        testContext.completeNow();
    }

    @Test
    void testGetLoggerFactory() {
        LoggerFactory factory = KeelInstance.Keel.getLoggerFactory();
        assertNotNull(factory);
    }

    @Test
    void testSetLoggerFactory() {
        LoggerFactory originalFactory = KeelInstance.Keel.getLoggerFactory();
        LoggerFactory newFactory = StdoutLoggerFactory.getInstance();

        KeelInstance.Keel.setLoggerFactory(newFactory);
        assertSame(newFactory, KeelInstance.Keel.getLoggerFactory());

        // Restore original
        KeelInstance.Keel.setLoggerFactory(originalFactory);
    }

    @Test
    void testIsVertxInitialized(Vertx vertx) {
        assertTrue(KeelInstance.Keel.isVertxInitialized());
    }

    @Test
    void testGetVertx(Vertx vertx) {
        Vertx keelVertx = KeelInstance.Keel.getVertx();
        assertNotNull(keelVertx);
        assertEquals(vertx, keelVertx);
    }

    @Test
    void testInitializeVertxStandalone(VertxTestContext testContext) {
        // Close current vertx first
        KeelInstance.Keel.close()
                         .compose(v -> {
                             VertxOptions options = new VertxOptions();
                             KeelInstance.Keel.initializeVertxStandalone(options);
                             assertTrue(KeelInstance.Keel.isVertxInitialized());
                             return KeelInstance.Keel.close();
                         })
                         .onComplete(ar -> {
                             if (ar.succeeded()) {
                                 testContext.completeNow();
                             } else {
                                 testContext.failNow(ar.cause());
                             }
                         });
    }

    @Test
    void testInitializeVertxTwice(VertxTestContext testContext) {
        KeelInstance.Keel.close()
                         .compose(v -> {
                             VertxOptions options = new VertxOptions();
                             return KeelInstance.Keel.initializeVertx(options);
                         })
                         .compose(v -> {
                             // Try to initialize again - should throw exception
                             assertThrows(IllegalStateException.class, () -> {
                                 KeelInstance.Keel.initializeVertx(new VertxOptions());
                             });
                             return KeelInstance.Keel.close();
                         })
                         .onComplete(ar -> {
                             if (ar.succeeded()) {
                                 testContext.completeNow();
                             } else {
                                 testContext.failNow(ar.cause());
                             }
                         });
    }

    @Test
    void testClose(VertxTestContext testContext) {
        // Create a new vertx instance for this test
        Vertx testVertx = Vertx.vertx();
        KeelInstance.Keel.initializeVertx(testVertx);

        KeelInstance.Keel.close()
                         .onComplete(ar -> {
                             if (ar.succeeded()) {
                                 assertFalse(KeelInstance.Keel.isVertxInitialized());
                                 testContext.completeNow();
                             } else {
                                 testContext.failNow(ar.cause());
                             }
                         });
    }

    @Test
    void testGracefullyClose(VertxTestContext testContext) {
        Vertx testVertx = Vertx.vertx();
        KeelInstance.Keel.initializeVertx(testVertx);

        KeelInstance.Keel.gracefullyClose(promise -> {
            promise.complete();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertFalse(KeelInstance.Keel.isVertxInitialized());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testIsRunningInVertxCluster(Vertx vertx) {
        boolean isClustered = KeelInstance.Keel.isRunningInVertxCluster();
        assertFalse(isClustered); // Default vertx is not clustered
    }
}

