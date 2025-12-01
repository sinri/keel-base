package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelInstance;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InstantKeelVerticle单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public class InstantKeelVerticleUnitTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelInstance.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testInstantVerticleCreation(VertxTestContext testContext) {
        AtomicBoolean executed = new AtomicBoolean(false);

        KeelVerticle verticle = KeelVerticle.instant(KeelInstance.Keel, verticleInstance -> {
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
        KeelVerticle verticle = KeelVerticle.instant(KeelInstance.Keel, verticleInstance -> {
            return Future.failedFuture(new RuntimeException("Test failure"));
        });

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

        KeelVerticle verticle = KeelVerticle.instant(KeelInstance.Keel, verticleInstance -> {
            executed.set(true);
            // Auto undeploy after completion
            vertx.setTimer(100L, id -> verticleInstance.undeployMe());
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
        KeelVerticle verticle = KeelVerticle.instant(KeelInstance.Keel, verticleInstance -> {
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
}

