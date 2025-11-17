package io.github.sinri.keel.base.json;

/**
 * 本接口定义了一类可转写为 JSON 字符串表达的对象。
 *
 * @since 5.0.0
 */
public interface JsonSerializable {
    String toJsonExpression();

    String toFormattedJsonExpression();
}
