package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;


/**
 * 接口 {@link UnmodifiableJsonifiableEntity} 的实现。
 * <p>
 * 自定义数据读取类可通过继承本类快速实现。
 *
 * @since 5.0.0
 */
public class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @NotNull JsonObject jsonObject;

    public UnmodifiableJsonifiableEntityImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = purify(jsonObject);
    }

    /**
     * @param raw the raw JsonObject.
     * @return the JsonObject that be purified, such as create a copy, remove some fields, and so on.
     */
    @NotNull
    protected JsonObject purify(@NotNull JsonObject raw) {
        return raw;
    }

    @Override
    public final @NotNull String toJsonExpression() {
        return jsonObject.encode();
    }

    @Override
    public @NotNull String toFormattedJsonExpression() {
        return jsonObject.encodePrettily();
    }

    @NotNull
    @Override
    public final String toString() {
        return toJsonExpression();
    }

    @Override
    public @Nullable <T> T read(@NotNull Function<JsonPointer, Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(jsonObject);
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return jsonObject.iterator();
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    /**
     * Creates and returns a deep copy of the current instance.
     *
     * @return A new {@link UnmodifiableJsonifiableEntityImpl} instance that is a deep copy of this object.
     */
    @Override
    @NotNull
    public UnmodifiableJsonifiableEntityImpl copy() {
        return new UnmodifiableJsonifiableEntityImpl(cloneAsJsonObject());
    }
}
