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
     * Converts the current state of this entity into a {@link JsonObject};
     * If the class is a wrapper of one {@link JsonObject} instance, it may return the wrapped instance.
     * <p>
     * Commonly, this method should not rely on {@link JsonSerializable#toJsonExpression()},
     * {@link JsonSerializable#toFormattedJsonExpression()}, nor {@code toString()}.
     *
     * @return a non-null {@link JsonObject} representing the current state of the
     *         entity.
     */
    @NotNull
    JsonObject toJsonObject();
}
