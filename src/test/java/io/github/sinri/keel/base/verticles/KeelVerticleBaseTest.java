package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeelVerticleBase单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
class KeelVerticleBaseTest {

    @BeforeEach
    void setUp(Vertx vertx) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
    }

    @AfterEach
    void tearDown() {
        // 清理操作
    }

    /**
     * 测试基本的部署和解除部署功能。
     * <p>
     * 验证 verticle 可以成功部署和解除部署。
     */
    @Test
    void testDeployAndUndeploy(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestVerticle verticle = new TestVerticle();

        assertEquals(KeelVerticleRunningStateEnum.BEFORE_RUNNING, verticle.getRunningState(),
                "Initial state should be BEFORE_RUNNING");

        verticle.deployMe(vertx, new DeploymentOptions())
                .compose(deploymentId -> {
                    assertNotNull(deploymentId, "Deployment ID should not be null");
                    assertEquals(KeelVerticleRunningStateEnum.RUNNING, verticle.getRunningState(),
                            "State should be RUNNING after deployment");
                    assertTrue(verticle.isStarted(), "Verticle should be started");
                    return verticle.undeployMe();
                })
                .onComplete(testContext.succeeding(v -> {
                    assertEquals(KeelVerticleRunningStateEnum.AFTER_RUNNING, verticle.getRunningState(),
                            "State should be AFTER_RUNNING after undeploy");
                    assertTrue(verticle.isStopped(), "Verticle should be stopped");
                    testContext.completeNow();
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试部署失败时的状态转换。
     * <p>
     * 验证当 startVerticle 返回失败时，状态正确转换为 DEPLOY_FAILED。
     */
    @Test
    void testDeployFailure(Vertx vertx, VertxTestContext testContext) throws Throwable {
        FailingVerticle verticle = new FailingVerticle();

        verticle.deployMe(vertx, new DeploymentOptions())
                .onComplete(ar -> {
                    assertTrue(ar.failed(), "Deployment should fail");
                    assertEquals(KeelVerticleRunningStateEnum.DEPLOY_FAILED, verticle.getRunningState(),
                            "State should be DEPLOY_FAILED on failure");
                    testContext.completeNow();
                });

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试重复部署的异常处理。
     * <p>
     * 验证不能对已部署的 verticle 再次部署。
     */
    @Test
    void testDoubleDeployment(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestVerticle verticle = new TestVerticle();

        verticle.deployMe(vertx, new DeploymentOptions())
                .compose(deploymentId -> {
                    return verticle.deployMe(vertx, new DeploymentOptions());
                })
                .onComplete(ar -> {
                    assertTrue(ar.failed(), "Second deployment should fail");
                    assertInstanceOf(IllegalStateException.class, ar.cause(), "Should throw IllegalStateException");
                    testContext.completeNow();
                });

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试 wrap 静态工厂方法（带start和stop函数）。
     * <p>
     * 验证可以通过 wrap 方法创建匿名 verticle 实例。
     */
    @Test
    void testWrapWithStartAndStop(Vertx vertx, VertxTestContext testContext) throws Throwable {
        AtomicBoolean started = new AtomicBoolean(false);
        AtomicBoolean stopped = new AtomicBoolean(false);

        KeelVerticleBase verticle = KeelVerticleBase.wrap(
                v -> {
                    started.set(true);
                    return Future.succeededFuture();
                },
                v -> {
                    stopped.set(true);
                    return Future.succeededFuture();
                }
        );

        verticle.deployMe(vertx, new DeploymentOptions())
                .compose(deploymentId -> {
                    assertTrue(started.get(), "Start function should be called");
                    assertFalse(stopped.get(), "Stop function should not be called yet");
                    return verticle.undeployMe();
                })
                .onComplete(testContext.succeeding(v -> {
                    assertTrue(stopped.get(), "Stop function should be called");
                    testContext.completeNow();
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试 wrap 静态工厂方法（仅start函数）。
     * <p>
     * 验证可以通过 wrap 方法创建只需要 start 逻辑的 verticle。
     */
    @Test
    void testWrapWithStartOnly(Vertx vertx, VertxTestContext testContext) throws Throwable {
        AtomicBoolean started = new AtomicBoolean(false);

        KeelVerticleBase verticle = KeelVerticleBase.wrap(
                v -> {
                    started.set(true);
                    return Future.succeededFuture();
                }
        );

        verticle.deployMe(vertx, new DeploymentOptions())
                .compose(deploymentId -> {
                    assertTrue(started.get(), "Start function should be called");
                    return verticle.undeployMe();
                })
                .onComplete(testContext.succeeding(v -> {
                    testContext.completeNow();
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试 getVerticleInfo 方法。
     * <p>
     * 验证能够正确获取 verticle 的信息。
     */
    @Test
    void testGetVerticleInfo(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestVerticle verticle = new TestVerticle();

        verticle.deployMe(vertx, new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .onComplete(testContext.succeeding(deploymentId -> {
                    JsonObject info = verticle.getVerticleInfo();

                    assertNotNull(info, "Verticle info should not be null");
                    assertTrue(info.containsKey("identity"), "Info should contain identity");
                    assertTrue(info.containsKey("class"), "Info should contain class");
                    assertTrue(info.containsKey("config"), "Info should contain config");
                    assertTrue(info.containsKey("deployment_id"), "Info should contain deployment_id");
                    assertTrue(info.containsKey("thread_model"), "Info should contain thread_model");

                    assertEquals(TestVerticle.class.getName(), info.getString("class"),
                            "Class name should match");
                    assertEquals(deploymentId, info.getString("deployment_id"),
                            "Deployment ID should match");
                    assertEquals("WORKER", info.getString("thread_model"),
                            "Thread model should be WORKER");

                    verticle.undeployMe().onComplete(ar -> testContext.completeNow());
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试 getVerticleIdentity 方法。
     * <p>
     * 验证能够获取 verticle 的身份标识。
     */
    @Test
    void testGetVerticleIdentity(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestVerticle verticle = new TestVerticle();

        verticle.deployMe(vertx, new DeploymentOptions())
                .onComplete(testContext.succeeding(deploymentId -> {
                    String identity = verticle.getVerticleInstanceIdentity();

                    assertNotNull(identity, "Identity should not be null");
                    assertTrue(identity.contains(TestVerticle.class.getName()),
                            "Identity should contain class name");
                    assertTrue(identity.contains(deploymentId),
                            "Identity should contain deployment ID");
                    assertTrue(identity.contains("@"),
                            "Identity should contain @ separator");

                    verticle.undeployMe().onComplete(ar -> testContext.completeNow());
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试自定义 verticle 身份标识。
     * <p>
     * 验证可以重载 getVerticleIdentity 方法来自定义身份。
     */
    @Test
    void testCustomVerticleIdentity(Vertx vertx, VertxTestContext testContext) throws Throwable {
        String customIdentity = "custom-verticle";
        CustomIdentityVerticle verticle = new CustomIdentityVerticle(customIdentity);

        verticle.deployMe(vertx, new DeploymentOptions())
                .onComplete(testContext.succeeding(deploymentId -> {
                    String identity = verticle.getVerticleInstanceIdentity();

                    assertTrue(identity.startsWith(customIdentity),
                            "Identity should start with custom identity");

                    JsonObject info = verticle.getVerticleInfo();
                    assertEquals(customIdentity, info.getString("identity"),
                            "Info should contain custom identity");

                    verticle.undeployMe().onComplete(ar -> testContext.completeNow());
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试 getKeel 方法。
     * <p>
     * 验证能够正确获取 Keel 实例。
     */
    @Test
    void testGetKeel(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestVerticle verticle = new TestVerticle();

        verticle.deployMe(vertx, new DeploymentOptions())
                .onComplete(testContext.succeeding(deploymentId -> {
                    assertNotNull(verticle.getKeelInstance(), "Keel instance should not be null");
                    assertEquals(vertx, verticle.getKeelInstance().getVertx(),
                            "Keel should have the same Vertx instance");

                    verticle.undeployMe().onComplete(ar -> testContext.completeNow());
                }));

        testContext.awaitCompletion(3, TimeUnit.SECONDS);
    }

    /**
     * 测试在未部署时访问 Vertx 实例。
     * <p>
     * 验证在 verticle 未部署时访问 Vertx 会抛出异常。
     */
    @Test
    void testGetVertxBeforeDeployment() {
        TestVerticle verticle = new TestVerticle();

        assertThrows(IllegalStateException.class, verticle::getVertxInstance,
                "Getting Vertx before deployment should throw IllegalStateException");
    }

    /**
     * 测试在未部署时获取 verticle 信息。
     * <p>
     * 验证在 verticle 未部署时获取信息会抛出异常。
     */
    @Test
    void testGetVerticleInfoBeforeDeployment() {
        TestVerticle verticle = new TestVerticle();

        assertThrows(IllegalStateException.class, verticle::getVerticleInfo,
                "Getting verticle info before deployment should throw IllegalStateException");
    }

    /**
     * 测试多次连续部署和解除部署。
     * <p>
     * 验证可以多次部署和解除部署不同的 verticle 实例。
     */
    @Test
    void testMultipleDeployments(Vertx vertx, VertxTestContext testContext) throws Throwable {
        AtomicInteger completedCount = new AtomicInteger(0);
        int totalVerticles = 3;

        for (int i = 0; i < totalVerticles; i++) {
            TestVerticle verticle = new TestVerticle();
            verticle.deployMe(vertx, new DeploymentOptions())
                    .compose(id -> verticle.undeployMe())
                    .onComplete(ar -> {
                        assertTrue(ar.succeeded(), "Deployment and undeploy should succeed");
                        if (completedCount.incrementAndGet() == totalVerticles) {
                            testContext.completeNow();
                        }
                    });
        }

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试用的基础 verticle 实现。
     * <p>
     * 提供基本的启动和停止功能。
     */
    private static class TestVerticle extends KeelVerticleBase {
        private boolean started = false;
        private boolean stopped = false;

        @Override
        protected Future<?> startVerticle() {
            started = true;
            return Future.succeededFuture();
        }

        @Override
        protected Future<?> stopVerticle() {
            stopped = true;
            return Future.succeededFuture();
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isStopped() {
            return stopped;
        }

        public Vertx getVertxInstance() {
            return getVertx();
        }

        public io.github.sinri.keel.base.Keel getKeelInstance() {
            return getKeel();
        }
    }

    /**
     * 测试用的失败 verticle。
     * <p>
     * 启动时总是返回失败。
     */
    private static class FailingVerticle extends KeelVerticleBase {
        @Override
        protected Future<?> startVerticle() {
            return Future.failedFuture(new RuntimeException("Intentional failure"));
        }
    }

    /**
     * 测试用的自定义身份 verticle。
     * <p>
     * 使用自定义的身份标识。
     */
    private static class CustomIdentityVerticle extends KeelVerticleBase {
        private final String customIdentity;

        public CustomIdentityVerticle(String customIdentity) {
            this.customIdentity = customIdentity;
        }

        @Override
        protected String getVerticleIdentity() {
            return customIdentity;
        }

        @Override
        protected Future<?> startVerticle() {
            return Future.succeededFuture();
        }
    }
}

