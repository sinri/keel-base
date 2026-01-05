package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verticle运行状态测试。
 * <p>
 * 测试 Verticle 在不同运行阶段的状态转换。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
public class VerticleStatusTest {

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testSuccessfulDeploymentAndUndeploy(VertxTestContext testContext) throws Throwable {
        var v1 = new V1(KeelSampleImpl.Keel, false);

        // 初始状态应该是 BEFORE_RUNNING
        testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, v1.getRunningState()));

        v1.deployMe(new DeploymentOptions())
          .onSuccess(id -> {
              // 部署成功后应该是 RUNNING
              testContext.verify(() -> {
                  assertEquals(KeelVerticleRunningStateEnum.RUNNING, v1.getRunningState());
                  assertNotNull(id);
              });
          })
          .onFailure(testContext::failNow);

        // 等待自动解除部署
        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        // 解除部署后应该是 AFTER_RUNNING
        testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, v1.getRunningState()));
        testContext.completeNow();
    }

    @Test
    void testFailedDeployment(VertxTestContext testContext) throws Throwable {
        var v1 = new V1(KeelSampleImpl.Keel, true);

        // 初始状态应该是 BEFORE_RUNNING
        testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, v1.getRunningState()));
        
        v1.deployMe(new DeploymentOptions())
          .onSuccess(id -> {
              testContext.failNow(new AssertionError("Should have failed"));
          })
          .onFailure(e -> {
              // 启动失败后应该是 RUNNING_FAILED
              testContext.verify(() -> {
                  assertEquals(KeelVerticleRunningStateEnum.RUNNING_FAILED, v1.getRunningState());
                  assertEquals("Start failure", e.getMessage());
              });
              testContext.completeNow();
          });

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    @Test
    void testStateTransitionTiming(VertxTestContext testContext) {
        var v1 = new V1(KeelSampleImpl.Keel, false);

        // 1. 初始状态
        testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, v1.getRunningState()));

        v1.deployMe(new DeploymentOptions())
          .compose(id -> {
              // 3. 部署成功后的状态
              testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.RUNNING, v1.getRunningState()));
              // 等待 2 秒让 verticle 自动解除部署
              return KeelSampleImpl.Keel.asyncSleep(2500);
          })
          .onComplete(ar -> {
              // 4. 自动解除部署后的状态
              testContext.verify(() -> assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, v1.getRunningState()));
              testContext.completeNow();
          });
    }

    @Test
    void testCannotDeployTwice(VertxTestContext testContext) {
        var v1 = new V1(KeelSampleImpl.Keel, false);

        v1.deployMe(new DeploymentOptions())
          .compose(id -> {
              // 尝试再次部署应该抛出异常
              testContext.verify(() -> {
                  assertThrows(KeelVerticle.UnexpectedVerticleRunningState.class, () -> {
                      v1.deployMe(new DeploymentOptions());
                  });
              });
              return Future.succeededFuture();
          })
          .compose(v -> KeelSampleImpl.Keel.asyncSleep(2500))
          .onComplete(testContext.succeedingThenComplete());
    }

    private static class V1 extends AbstractKeelVerticle {
        private final boolean startWithError;

        public V1(Keel keel, boolean startWithError) {
            super(keel);
            this.startWithError = startWithError;
        }

        @Override
        protected Future<Void> startVerticle() {
            return getKeel()
                    .asyncSleep(1000)
                    .compose(v -> {
                        if (startWithError) {
                            return Future.failedFuture(new RuntimeException("Start failure"));
                        } else {
                            getVertx().setTimer(1000, id -> undeployMe());
                            return Future.succeededFuture();
                        }
                    });
        }
    }
}
