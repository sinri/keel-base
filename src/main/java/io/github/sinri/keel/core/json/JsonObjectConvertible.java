package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for those entities could be serialized as a JSON Object.
 *
 * @since 4.1.1
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
