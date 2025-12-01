package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelInstance;
import io.vertx.core.DeploymentOptions;
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
 * KeelVerticle接口单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public class KeelVerticleUnitTest {
    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        KeelInstance.Keel.initializeVertx(vertx);
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
                    assertThrows(IllegalStateException.class, () -> {
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

        assertThrows(IllegalStateException.class, () -> {
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
                    assertTrue(identity.startsWith("my-verticle@"));
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
                    assertTrue(identity.contains(TestKeelVerticle.class.getName()));
                    assertTrue(identity.contains("@"));
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

    /**
     * 测试用的KeelVerticle实现。
     */
    private static class TestKeelVerticle extends AbstractKeelVerticle {
        @Override
        protected io.vertx.core.@NotNull Future<Void> startVerticle() {
            return io.vertx.core.Future.succeededFuture();
        }
    }
}

