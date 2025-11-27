package io.github.sinri.keel.base.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.shareddata.ClusterSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * 本接口定义了一类基于 JSON 对象的数据实体，可读写、转换、重载。
 *
 * @since 5.0.0
 */
public interface JsonifiableDataUnit
        extends JsonObjectConvertible, JsonObjectReloadable, JsonObjectWritable,
        UnmodifiableJsonifiableEntity, ClusterSerializable {
    @Nullable
    default <T> T read(@NotNull Function<JsonPointer, Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(toJsonObject());
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }

    @Override
    default void ensureEntry(@NotNull String key, Object value) {
        toJsonObject().put(key, value);
    }

    @Override
    default void removeEntry(@NotNull String key) {
        this.toJsonObject().remove(key);
    }

    @Override
    default boolean isEmpty() {
        return toJsonObject().isEmpty();
    }

    @Override
    @NotNull
    default Iterator<Map.Entry<String, Object>> iterator() {
        return toJsonObject().iterator();
    }

    @Override
    default void writeToBuffer(Buffer buffer) {
        this.toJsonObject().writeToBuffer(buffer);
    }

    /**
     * Following {@link JsonObject#readFromBuffer(int, Buffer)}
     *
     * @param pos    the position where to start reading the {@code buffer}
     * @param buffer the {@link Buffer} where the serialized bytes must be read from
     * @return the position where the next byte to be read would be
     */
    @Override
    default int readFromBuffer(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int start = pos + 4;
        Buffer buf = buffer.getBuffer(start, start + length);
        this.reloadData(new JsonObject(buf));
        return pos + length + 4;
    }

    @NotNull
    @Override
    default String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }

    @NotNull
    @Override
    default String toJsonExpression() {
        return toJsonObject().encode();
    }
}
