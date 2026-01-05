package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
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
@NullMarked
@SuppressWarnings("NullAway")
public class AbstractKeelVerticleUnitTest {
    private Vertx vertx; // Initialized by @BeforeEach

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
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::getVertx);

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

        // Before deployment, should throw exception
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::contextThreadModel);

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

        // Before deployment, should throw exception
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::config);

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
                 // 格式应该是: custom-identity@{deploymentId}:{uuid}
                 assertTrue(identity.contains("custom-identity@"));
                 assertTrue(identity.contains(":"));
                 // 验证包含两个部分：标记@部署ID 和 UUID
                 String[] parts = identity.split(":");
                 assertEquals(2, parts.length);
                 assertTrue(parts[0].startsWith("custom-identity@"));
                 assertFalse(parts[1].isEmpty()); // UUID 部分
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
                 // 格式应该是: {className}@{deploymentId}:{uuid}
                 assertTrue(identity.contains(TestKeelVerticle.class.getName()));
                 assertTrue(identity.contains("@"));
                 assertTrue(identity.contains(":"));
                 // 验证包含两个部分：类名@部署ID 和 UUID
                 String[] parts = identity.split(":");
                 assertEquals(2, parts.length);
                 assertFalse(parts[1].isEmpty()); // UUID 部分
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
            protected Future<Void> startVerticle() {
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

    @Test
    void testStopVerticle(VertxTestContext testContext) {
        TestKeelVerticleWithStop verticle = new TestKeelVerticleWithStop();

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticle.getRunningState());
                 assertFalse(verticle.isStopCalled());
                 return vertx.undeploy(deploymentId);
             })
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, verticle.getRunningState());
                     assertTrue(verticle.isStopCalled());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testStopVerticleWithFailure(VertxTestContext testContext) {
        TestKeelVerticleWithStopFailure verticle = new TestKeelVerticleWithStopFailure();

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> vertx.undeploy(deploymentId))
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     // 即使 stopVerticle 失败，undeploy 仍然会完成
                     assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, verticle.getRunningState());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testGetVertxBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::getVertx);
    }

    @Test
    void testContextThreadModelBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::contextThreadModel);
    }

    @Test
    void testDeploymentIdBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::deploymentID);
    }

    @Test
    void testConfigBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::config);
    }

    @Test
    void testVerticleIdentityBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::verticleIdentity);
    }

    @Test
    void testRunningFailedState(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle() {
            @Override
            protected Future<Void> startVerticle() {
                return Future.failedFuture(new RuntimeException("Start failure"));
            }
        };

        assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, verticle.getRunningState());

        vertx.deployVerticle(verticle)
             .onComplete(ar -> {
                 if (ar.failed()) {
                     assertEquals(KeelVerticleRunningStateEnum.RUNNING_FAILED, verticle.getRunningState());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(new AssertionError("Should have failed"));
                 }
             });
    }

    /**
     * 测试用的KeelVerticle实现。
     */
    private static class TestKeelVerticle extends AbstractKeelVerticle {
        TestKeelVerticle() {
            super(KeelSampleImpl.Keel);
        }

        @Override
        protected Future<Void> startVerticle() {
            return Future.succeededFuture();
        }
    }

    /**
     * 测试 stopVerticle 方法的 Verticle 实现。
     */
    private static class TestKeelVerticleWithStop extends AbstractKeelVerticle {
        private boolean stopCalled = false;

        TestKeelVerticleWithStop() {
            super(KeelSampleImpl.Keel);
        }

        @Override
        protected Future<Void> startVerticle() {
            return Future.succeededFuture();
        }

        @Override
        protected Future<Void> stopVerticle() {
            stopCalled = true;
            return Future.succeededFuture();
        }

        public boolean isStopCalled() {
            return stopCalled;
        }
    }

    /**
     * 测试 stopVerticle 失败的 Verticle 实现。
     */
    private static class TestKeelVerticleWithStopFailure extends AbstractKeelVerticle {
        TestKeelVerticleWithStopFailure() {
            super(KeelSampleImpl.Keel);
        }

        @Override
        protected Future<Void> startVerticle() {
            return Future.succeededFuture();
        }

        @Override
        protected Future<Void> stopVerticle() {
            return Future.failedFuture(new RuntimeException("Stop failure"));
        }
    }
}

