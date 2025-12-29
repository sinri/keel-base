package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AbstractKeelVerticle单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public class AbstractKeelVerticleUnitTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testVerticleLifecycle(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticle.getRunningState());
                 assertNotNull(verticle.deploymentID());
                 assertEquals(deploymentId, verticle.deploymentID());
                 return vertx.undeploy(deploymentId);
             })
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, verticle.getRunningState());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testVertxMethod(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        // Before deployment, should return Keel's vertx
        assertEquals(vertx, verticle.getVertx());

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 // After deployment, should return context's vertx
                 assertEquals(vertx, verticle.getVertx());
                 return vertx.undeploy(deploymentId);
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
    void testContextThreadModel(VertxTestContext testContext) {
        // 如果当前 JDK 版本小于 21，则直接 return
        String version = System.getProperty("java.version");
        int majorVersion;
        if (version.startsWith("1.")) {
            majorVersion = Integer.parseInt(version.substring(2, 3));
        } else {
            int dotPos = version.indexOf(".");
            majorVersion = dotPos != -1 ? Integer.parseInt(version.substring(0, dotPos)) : Integer.parseInt(version);
        }
        if (majorVersion < 21) {
            testContext.completeNow();
            return;
        }


        TestKeelVerticle verticle = new TestKeelVerticle();

        // Before deployment, should return null
        assertNull(verticle.contextThreadModel());

        DeploymentOptions options = new DeploymentOptions()
                .setThreadingModel(io.vertx.core.ThreadingModel.VIRTUAL_THREAD);

        vertx.deployVerticle(verticle, options)
             .compose(deploymentId -> {
                 assertNotNull(verticle.contextThreadModel());
                 assertEquals(io.vertx.core.ThreadingModel.VIRTUAL_THREAD, verticle.contextThreadModel());
                 return vertx.undeploy(deploymentId);
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
    void testConfig(VertxTestContext testContext) {
        JsonObject config = new JsonObject().put("testKey", "testValue");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        TestKeelVerticle verticle = new TestKeelVerticle();

        // Before deployment, should return null
        assertNull(verticle.config());

        vertx.deployVerticle(verticle, options)
             .compose(deploymentId -> {
                 assertNotNull(verticle.config());
                 assertEquals("testValue", verticle.config().getString("testKey"));
                 return vertx.undeploy(deploymentId);
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
    void testVerticleIdentity(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
                .put(KeelVerticle.CONFIG_KEY_OF_VERTICLE_IDENTITY, "custom-identity");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        TestKeelVerticle verticle = new TestKeelVerticle();

        vertx.deployVerticle(verticle, options)
             .compose(deploymentId -> {
                 String identity = verticle.verticleIdentity();
                 assertNotNull(identity);
                 assertTrue(identity.contains("custom-identity"));
                 assertTrue(identity.contains(":"));
                 return vertx.undeploy(deploymentId);
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
    void testVerticleIdentityWithoutConfig(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 String identity = verticle.verticleIdentity();
                 assertNotNull(identity);
                 assertTrue(identity.contains(TestKeelVerticle.class.getName()));
                 assertTrue(identity.contains(":"));
                 return vertx.undeploy(deploymentId);
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
    void testStartFailure(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle() {
            @Override
            protected @NotNull Future<Void> startVerticle() {
                return Future.failedFuture(new RuntimeException("Start failure"));
            }
        };

        vertx.deployVerticle(verticle)
             .onComplete(ar -> {
                 if (ar.failed()) {
                     assertInstanceOf(RuntimeException.class, ar.cause());
                     assertEquals("Start failure", ar.cause().getMessage());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(new AssertionError("Should have failed"));
                 }
             });
    }

    @Test
    void testInitialState() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, verticle.getRunningState());
    }

    /**
     * 测试用的KeelVerticle实现。
     */
    private static class TestKeelVerticle extends AbstractKeelVerticle {
        TestKeelVerticle() {
            super(KeelSampleImpl.Keel);
        }

        @Override
        protected @NotNull Future<Void> startVerticle() {
            return Future.succeededFuture();
        }
    }
}

