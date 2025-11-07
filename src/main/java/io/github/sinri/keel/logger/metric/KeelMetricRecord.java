package io.github.sinri.keel.logger.metric;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.core.json.JsonObjectConvertible;

import javax.annotation.Nonnull;
import java.util.Map;

public interface KeelMetricRecord<T> extends SelfInterface<T>, JsonObjectConvertible {
    @Nonnull
    String metricName();

    double value();

    @Nonnull
    Map<String, String> labels();

    T label(String name, String value);
}
