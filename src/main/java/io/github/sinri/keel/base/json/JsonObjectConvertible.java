package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * 本接口定义了一类可组装或转换为 JSON 对象的实体。
 *
 * @since 5.0.0
 */
public interface JsonObjectConvertible extends JsonSerializable {
    /**
     * 将当前实体的状态转换为 {@link JsonObject}。
     * <p>
     * 如果该类是对某个 {@link JsonObject} 实例的包装，则可能直接返回被包装的实例。
     * <p>
     * 通常，此方法不应依赖于 {@link JsonSerializable#toJsonExpression()}、
     * {@link JsonSerializable#toFormattedJsonExpression()} 或 {@code toString()} 方法。
     *
     * @return 表示当前实体状态的非空 {@link JsonObject}
     */
    @NotNull JsonObject toJsonObject();
}
