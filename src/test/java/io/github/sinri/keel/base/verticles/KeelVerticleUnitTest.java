package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
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
 * KeelVerticle接口单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
public class KeelVerticleUnitTest {
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testDeployMe(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(deploymentId);
                    assertEquals(deploymentId, verticle.deploymentID());
                    return verticle.undeployMe();
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
    void testDeployMeAlreadyDeployed(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    // Try to deploy again - should throw exception
                    assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, () -> {
                        verticle.deployMe(new DeploymentOptions());
                    });
                    return verticle.undeployMe();
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
    void testUndeployMe(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> verticle.undeployMe())
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @Test
    void testUndeployMeNotDeployed() {
        TestKeelVerticle verticle = new TestKeelVerticle();

        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, () -> {
            verticle.undeployMe();
        });
    }

    @Test
    void testGetVerticleInfo(VertxTestContext testContext) {
        JsonObject config = new JsonObject().put("testKey", "testValue");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(options)
                .compose(deploymentId -> {
                    JsonObject info = verticle.getVerticleInfo();
                    assertNotNull(info);
                    assertEquals(TestKeelVerticle.class.getName(), info.getString("class"));
                    assertNotNull(info.getJsonObject("config"));
                    assertEquals("testValue", info.getJsonObject("config").getString("testKey"));
                    assertEquals(deploymentId, info.getString("deployment_id"));
                    assertNotNull(info.getString("thread_model"));
                    return verticle.undeployMe();
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
    void testVerticleIdentityWithConfig(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
                .put(KeelVerticle.CONFIG_KEY_OF_VERTICLE_IDENTITY, "my-verticle");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(options)
                .compose(deploymentId -> {
                    String identity = verticle.verticleIdentity();
                    assertNotNull(identity);
                    // 格式应该是: my-verticle@{deploymentId}:{uuid}
                    assertTrue(identity.contains("my-verticle@"));
                    assertTrue(identity.contains(":"));
                    String[] parts = identity.split(":");
                    assertEquals(2, parts.length);
                    assertTrue(parts[0].startsWith("my-verticle@"));
                    assertFalse(parts[1].isEmpty());
                    return verticle.undeployMe();
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

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    String identity = verticle.verticleIdentity();
                    assertNotNull(identity);
                    // 格式应该是: {className}@{deploymentId}:{uuid}
                    assertTrue(identity.contains(TestKeelVerticle.class.getName()));
                    assertTrue(identity.contains("@"));
                    assertTrue(identity.contains(":"));
                    String[] parts = identity.split(":");
                    assertEquals(2, parts.length);
                    assertFalse(parts[1].isEmpty());
                    return verticle.undeployMe();
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
    void testGetVerticleInfoBeforeDeployment() {
        TestKeelVerticle verticle = new TestKeelVerticle();
        assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, verticle::getVerticleInfo);
    }

    @Test
    void testUndeployMeAfterAlreadyUndeployed(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> verticle.undeployMe())
                .compose(v -> {
                    // 尝试再次解除部署应该失败
                    return verticle.undeployMe();
                })
                .onComplete(ar -> {
                    if (ar.failed()) {
                        // 预期失败，因为已经解除部署
                        testContext.completeNow();
                    } else {
                        testContext.failNow(new AssertionError("Should have failed when undeploying already undeployed verticle"));
                    }
                });
    }

    @Test
    void testDeployMeWithCustomThreadingModel(VertxTestContext testContext) {
        // 如果当前 JDK 版本小于 21，则跳过测试
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

        DeploymentOptions options = new DeploymentOptions()
                .setThreadingModel(io.vertx.core.ThreadingModel.VIRTUAL_THREAD);

        TestKeelVerticle verticle = new TestKeelVerticle();

        verticle.deployMe(options)
                .compose(deploymentId -> {
                    assertEquals(io.vertx.core.ThreadingModel.VIRTUAL_THREAD, verticle.contextThreadModel());
                    return verticle.undeployMe();
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
    void testGetKeel(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        // 在部署前后都应该能获取 Keel 实例
        assertNotNull(verticle.getKeel());
        assertEquals(KeelSampleImpl.Keel, verticle.getKeel());

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(verticle.getKeel());
                    assertEquals(KeelSampleImpl.Keel, verticle.getKeel());
                    return verticle.undeployMe();
                })
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        assertNotNull(verticle.getKeel());
                        assertEquals(KeelSampleImpl.Keel, verticle.getKeel());
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @Test
    void testGetRunningState(VertxTestContext testContext) {
        TestKeelVerticle verticle = new TestKeelVerticle();

        // 初始状态
        assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, verticle.getRunningState());

        verticle.deployMe(new DeploymentOptions())
                .compose(deploymentId -> {
                    // 运行状态
                    assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticle.getRunningState());
                    return verticle.undeployMe();
                })
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        // 停止后状态
                        assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, verticle.getRunningState());
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
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
        protected io.vertx.core.Future<Void> startVerticle() {
            return io.vertx.core.Future.succeededFuture();
        }
    }
}

