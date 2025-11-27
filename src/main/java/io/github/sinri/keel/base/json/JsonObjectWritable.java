package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 本接口定义了一类可作为 JSON 对象写入的实体。
 *
 * @since 5.0.0
 */
public interface JsonObjectWritable extends JsonObjectReadable {
    /**
     * Create or replace the Key-Value pair in this class wrapped JSON Object.
     */
    void ensureEntry(@NotNull String key, @Nullable Object value);

    void removeEntry(@NotNull String key);

    /**
     * Retrieves the JSON object associated with the specified key from the entity's
     * JSON representation.
     * If no JSON object exists for the given key, a new empty JSON object is
     * created, associated with the key,
     * and added to the entity's JSON representation. This ensures that the key
     * always maps to a valid JSON object.
     *
     * @param key the key for which the JSON object is to be retrieved or created
     * @return the existing or newly created JSON object associated with the
     *         specified key
     */
    default JsonObject ensureJsonObject(@NotNull String key) {
        JsonObject x = this.readJsonObject(key);
        if (x == null) {
            x = new JsonObject();
            ensureEntry(key, x);
        }
        return x;
    }

    /**
     * Retrieves the JSON array associated with the specified key from the entity's
     * JSON representation.
     * If no JSON array exists for the given key, a new empty JSON array is created,
     * associated with the key,
     * and added to the entity's JSON representation. This ensures that the key
     * always maps to a valid JSON array.
     *
     * @param key the key for which the JSON array is to be retrieved or created
     * @return the existing or newly created JSON array associated with the
     *         specified key
     */
    default JsonArray ensureJsonArray(@NotNull String key) {
        JsonArray x = this.readJsonArray(key);
        if (x == null) {
            x = new JsonArray();
            ensureEntry(key, x);
        }
        return x;
    }
}
