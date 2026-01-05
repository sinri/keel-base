package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InstantKeelVerticle单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
@SuppressWarnings("NullAway")
public class InstantKeelVerticleUnitTest {
    private Vertx vertx; // Initialized by @BeforeEach

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testInstantVerticleCreation(VertxTestContext testContext) {
        AtomicBoolean executed = new AtomicBoolean(false);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            executed.set(true);
            return Future.succeededFuture();
        });

        assertNotNull(verticle);
        assertInstanceOf(InstantKeelVerticle.class, verticle);

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertTrue(executed.get());
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
    void testInstantVerticleWithFailure(VertxTestContext testContext) {
        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance ->
                Future.failedFuture(new RuntimeException("Test failure")));

        vertx.deployVerticle(verticle)
             .onComplete(ar -> {
                 if (ar.failed()) {
                     assertInstanceOf(RuntimeException.class, ar.cause());
                     assertEquals("Test failure", ar.cause().getMessage());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(new AssertionError("Should have failed"));
                 }
             });
    }

    @Test
    void testInstantVerticleAutoUndeploy(VertxTestContext testContext) {
        AtomicBoolean executed = new AtomicBoolean(false);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            executed.set(true);
            // Auto undeploy after completion
            verticleInstance.getVertx().setTimer(100L, id -> verticleInstance.undeployMe());
            return Future.succeededFuture();
        });

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 // Wait a bit for auto-undeploy
                 Promise<Void> promise = Promise.promise();
                 vertx.setTimer(100, id -> promise.complete());
                 return promise.future();
             })
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     assertTrue(executed.get());
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testInstantVerticleAccessToSelf(VertxTestContext testContext) {
        final KeelVerticle[] verticleRef = new KeelVerticle[1];
        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            assertNotNull(verticleInstance);
            assertNotNull(verticleInstance.deploymentID());
            assertEquals(verticleInstance, verticleRef[0]);
            return Future.succeededFuture();
        });
        verticleRef[0] = verticle;

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> vertx.undeploy(deploymentId))
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testInstantVerticleWithConfig(VertxTestContext testContext) {
        JsonObject config = new JsonObject()
                .put("testKey", "testValue")
                .put(KeelVerticle.CONFIG_KEY_OF_VERTICLE_IDENTITY, "instant-verticle");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            JsonObject verticleConfig = verticleInstance.config();
            assertNotNull(verticleConfig);
            assertEquals("testValue", verticleConfig.getString("testKey"));

            String identity = verticleInstance.verticleIdentity();
            assertTrue(identity.contains("instant-verticle@"));

            return Future.succeededFuture();
        });

        vertx.deployVerticle(verticle, options)
             .compose(deploymentId -> vertx.undeploy(deploymentId))
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testInstantVerticleLifecycle(VertxTestContext testContext) {
        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticleInstance.getRunningState());
            return Future.succeededFuture();
        });

        assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, verticle.getRunningState());

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticle.getRunningState());
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
    void testInstantVerticleWithAsyncOperation(VertxTestContext testContext) {
        AtomicBoolean asyncCompleted = new AtomicBoolean(false);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance ->
                verticleInstance.getKeel()
                                .asyncSleep(100)
                                .compose(v -> {
                                    asyncCompleted.set(true);
                                    return Future.succeededFuture();
                                }));

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertTrue(asyncCompleted.get());
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
    void testInstantVerticleCanAccessVertx(VertxTestContext testContext) {
        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            Vertx verticleVertx = verticleInstance.getVertx();
            assertNotNull(verticleVertx);
            return Future.succeededFuture();
        });

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> vertx.undeploy(deploymentId))
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testInstantVerticleMultipleOperations(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance ->
                Future.succeededFuture()
                      .compose(v -> {
                          counter.incrementAndGet();
                          return verticleInstance.getKeel().asyncSleep(50);
                      })
                      .compose(v -> {
                          counter.incrementAndGet();
                          return verticleInstance.getKeel().asyncSleep(50);
                      })
                      .compose(v -> {
                          counter.incrementAndGet();
                          return Future.succeededFuture();
                      }));

        vertx.deployVerticle(verticle)
             .compose(deploymentId -> {
                 assertEquals(3, counter.get());
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
    void testInstantVerticleGetVerticleInfo(VertxTestContext testContext) {
        JsonObject config = new JsonObject().put("key", "value");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            JsonObject info = verticleInstance.getVerticleInfo();
            assertNotNull(info);
            assertEquals(InstantKeelVerticle.class.getName(), info.getString("class"));
            assertNotNull(info.getJsonObject("config"));
            assertEquals("value", info.getJsonObject("config").getString("key"));
            assertNotNull(info.getString("deployment_id"));
            assertNotNull(info.getString("thread_model"));
            return Future.succeededFuture();
        });

        vertx.deployVerticle(verticle, options)
             .compose(deploymentId -> vertx.undeploy(deploymentId))
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testInstantVerticleDeployMe(VertxTestContext testContext) {
        KeelVerticle verticle = KeelVerticle.instant(KeelSampleImpl.Keel, verticleInstance -> {
            assertNotNull(verticleInstance.deploymentID());
            return Future.succeededFuture();
        });

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
}

