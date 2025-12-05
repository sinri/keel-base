package io.github.sinri.keel.base.verticles;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelInstance;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class VerticleStatusTest {
    public VerticleStatusTest(Vertx vertx) {
        KeelInstance.Keel.initializeVertx(vertx);
    }

    @Test
    void testCommon(VertxTestContext testContext) {
        var v1 = new V1(KeelInstance.Keel, false);
        System.out.println("1: " + v1.getRunningState());
        v1.deployMe(new DeploymentOptions())
          .onSuccess(id -> {
              System.out.println("3: " + v1.getRunningState() + " id=" + id);
          })
          .onFailure(e -> {
              System.out.println("3: " + v1.getRunningState() + " e=" + e.getMessage());
          });
        System.out.println("2: " + v1.getRunningState());
        KeelInstance.Keel.asyncSleep(4000).onComplete(ar -> {
                        System.out.println("4: " + v1.getRunningState());
                    })
                         .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testError(VertxTestContext testContext) {
        var v1 = new V1(KeelInstance.Keel, true);
        System.out.println("1: " + v1.getRunningState());
        v1.deployMe(new DeploymentOptions())
          .onSuccess(id -> {
              System.out.println("3: " + v1.getRunningState() + " id=" + id);
          })
          .onFailure(e -> {
              System.out.println("3: " + v1.getRunningState() + " e=" + e.getMessage());
          });
        System.out.println("2: " + v1.getRunningState());
        KeelInstance.Keel.asyncSleep(4000)
                         .onComplete(ar -> {
                             System.out.println("4: " + v1.getRunningState());
                         })
                         .onComplete(testContext.succeedingThenComplete());
    }

    private static class V1 extends AbstractKeelVerticle {
        private final boolean startWithError;

        public V1(@NotNull Keel keel, boolean startWithError) {
            super(keel);
            this.startWithError = startWithError;
        }

        @Override
        protected @NotNull Future<Void> startVerticle() {
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
