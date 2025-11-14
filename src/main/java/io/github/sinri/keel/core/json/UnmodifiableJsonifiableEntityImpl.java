package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;


/**
 * An implementation of the UnmodifiableJsonifiableEntity interface that provides a read-only
 * wrapper around a JsonObject. This class ensures that the underlying JSON object cannot be
 * modified, providing a safe and immutable representation.
 *
 * <p>The class supports converting the JSON object to a string, reading values from the JSON
 * object using a provided function, converting the JSON object to a buffer, and iterating over
 * the entries in the JSON object. It also provides a method to create a copy of the current
 * instance.
 *
 * <p>As of 3.2.15, it is public.</p>
 *
 * @since 3.1.10
 */
public class UnmodifiableJsonifiableEntityImpl implements UnmodifiableJsonifiableEntity {
    private final @NotNull JsonObject jsonObject;

    public UnmodifiableJsonifiableEntityImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = purify(jsonObject);
    }

    /**
     * @param raw the raw JsonObject.
     * @return the JsonObject that be purified, such as create a copy, remove some fields, and so on.
     * @since 4.1.0
     */
    protected JsonObject purify(JsonObject raw) {
        return raw;
    }

    @Override
    public final String toJsonExpression() {
        return jsonObject.encode();
    }

    @Override
    public String toFormattedJsonExpression() {
        return jsonObject.encodePrettily();
    }

    /**
     * As of 4.1.0, it is final.
     *
     * @since 3.2.17
     */
    @Override
    public final String toString() {
        return toJsonExpression();
    }

    /**
     * @since 2.7
     * @since 2.8 If java.lang.ClassCastException occurred, return null instead.
     * @since 3.1.10 make it abstract.
     */
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
     * @since 4.0.0
     */
    @Override
    public UnmodifiableJsonifiableEntityImpl copy() {
        return new UnmodifiableJsonifiableEntityImpl(cloneAsJsonObject());
    }
}
