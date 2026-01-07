package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeelAsyncMixinLogic单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class KeelAsyncMixinLogicUnitTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public KeelAsyncMixinLogicUnitTest(Vertx vertx) {
        super(vertx);
    }


    @Test
    void testAsyncCallRepeatedly(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);

        asyncCallRepeatedly(repeatedlyCallTask -> {
            int current = count.incrementAndGet();
            if (current >= 5) {
                repeatedlyCallTask.stop();
            }
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(5, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithIterator(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");
        List<String> processed = new ArrayList<>();

        asyncCallIteratively(items.iterator(), item -> {
            processed.add(item);
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, processed.size());
                assertEquals("a", processed.get(0));
                assertEquals("b", processed.get(1));
                assertEquals("c", processed.get(2));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithIterable(VertxTestContext testContext) {
        List<String> items = Arrays.asList("x", "y", "z");
        List<String> processed = new ArrayList<>();

        asyncCallIteratively(items, item -> {
            processed.add(item);
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, processed.size());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatch(VertxTestContext testContext) {
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
        List<List<Integer>> batches = new ArrayList<>();

        asyncCallIteratively(items.iterator(), (batch, task) -> {
            batches.add(new ArrayList<>(batch));
            return asyncSleep(10);
        }, 2).onComplete(ar -> {
            if (ar.succeeded()) {
                assertTrue(batches.size() >= 2);
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchSizeZero(VertxTestContext testContext) {
        List<Integer> items = Arrays.asList(1, 2, 3);

        assertThrows(IllegalArgumentException.class, () -> {
            asyncCallIteratively(items.iterator(), (batch, task) ->
                    asyncSleep(10), 0);
        });
        testContext.completeNow();
    }

    @Test
    void testAsyncCallStepwise(VertxTestContext testContext) {
        List<Long> values = new ArrayList<>();

        asyncCallStepwise(0, 5, 1, (value, task) -> {
            values.add(value);
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(5, values.size());
                assertEquals(0L, values.get(0));
                assertEquals(4L, values.get(4));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallStepwiseWithTimes(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);

        asyncCallStepwise(3, (value, task) -> {
            count.incrementAndGet();
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallStepwiseWithInvalidStep(VertxTestContext testContext) {
        assertThrows(IllegalArgumentException.class, () -> {
            asyncCallStepwise(0, 10, 0, (value, task) ->
                    asyncSleep(10));
        });
        testContext.completeNow();
    }

    @Test
    void testAsyncCallStepwiseWithInvalidRange(VertxTestContext testContext) {
        assertThrows(IllegalArgumentException.class, () -> {
            asyncCallStepwise(10, 5, 1, (value, task) ->
                    asyncSleep(10));
        });
        testContext.completeNow();
    }

    @Test
    void testAsyncCallStepwiseWithZeroTimes(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);

        asyncCallStepwise(0, (value, task) -> {
            count.incrementAndGet();
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(0, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithStop(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c", "d", "e");
        List<String> processed = new ArrayList<>();

        asyncCallIteratively(items.iterator(), (item, task) -> {
            processed.add(item);
            if (processed.size() >= 3) {
                task.stop();
            }
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, processed.size());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}

