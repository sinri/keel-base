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
     * 在此类包装的 JSON 对象中创建或替换键值对。
     * <p>
     * 如果指定的键已存在，则替换其值；如果不存在，则创建新的键值对。
     *
     * @param key   要创建或替换的键
     * @param value 要设置的值，可以为 null
     */
    void ensureEntry(@NotNull String key, @Nullable Object value);

    /**
     * 从此类包装的 JSON 对象中移除指定的键值对。
     *
     * @param key 要移除的键
     */
    void removeEntry(@NotNull String key);

    /**
     * 从实体的 JSON 表示中检索与指定键关联的 JSON 对象。
     * <p>
     * 如果给定键不存在 JSON 对象，则创建一个新的空 JSON 对象，将其与键关联，
     * 并添加到实体的 JSON 表示中。这确保该键始终映射到一个有效的 JSON 对象。
     *
     * @param key 要检索或创建 JSON 对象的键
     * @return 与指定键关联的现有或新创建的 JSON 对象
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
     * 从实体的 JSON 表示中检索与指定键关联的 JSON 数组。
     * <p>
     * 如果给定键不存在 JSON 数组，则创建一个新的空 JSON 数组，将其与键关联，
     * 并添加到实体的 JSON 表示中。这确保该键始终映射到一个有效的 JSON 数组。
     *
     * @param key 要检索或创建 JSON 数组的键
     * @return 与指定键关联的现有或新创建的 JSON 数组
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
