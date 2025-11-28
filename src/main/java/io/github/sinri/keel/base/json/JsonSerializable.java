package io.github.sinri.keel.base.json;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

/**
 * 本接口定义了一类可转写为 JSON 字符串表达的对象。
 *
 * @since 5.0.0
 */
@JsonSerialize(using = JsonifiableSerializer.class)
public interface JsonSerializable {
    @NotNull
    String toJsonExpression();

    @NotNull
    String toFormattedJsonExpression();
}
