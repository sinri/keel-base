package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.logger.api.adapter.BaseLogWriter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

/**
 * 面向标准输出的即时性日志写入适配器。。
 *
 * @since 5.0.0
 */
@NullMarked
public final class StdoutLogWriter extends BaseLogWriter {
    private static final StdoutLogWriter instance = new StdoutLogWriter();

    private StdoutLogWriter() {
    }

    public static StdoutLogWriter getInstance() {
        return instance;
    }

    @Override
    public String renderClassification(List<String> classification) {
        return new JsonArray(classification).encode();
    }

    @Override
    public String renderContext(Map<String, Object> context) {
        return new JsonObject(context).encodePrettily();
    }
}
