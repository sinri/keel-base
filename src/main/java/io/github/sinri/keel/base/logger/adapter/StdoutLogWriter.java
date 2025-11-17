package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.internal.StackKit;
import io.github.sinri.keel.logger.api.adapter.BaseLogWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Keel 体系下的面向标准输出的主题化日志记录即时处理器。
 *
 * @since 5.0.0
 */
public final class StdoutLogWriter extends BaseLogWriter {
    private static final StdoutLogWriter instance = new StdoutLogWriter();

    private StdoutLogWriter() {
    }

    public static StdoutLogWriter getInstance() {
        return instance;
    }

    @Override
    public String renderThrowable(@NotNull Throwable throwable) {
        return StackKit.renderThrowableChain(throwable);
    }

    @Override
    public String renderClassification(@NotNull List<String> classification) {
        return new JsonArray(classification).encode();
    }

    @Override
    public String renderContext(@NotNull Map<String, Object> context) {
        return new JsonObject(context).encodePrettily();
    }
}
