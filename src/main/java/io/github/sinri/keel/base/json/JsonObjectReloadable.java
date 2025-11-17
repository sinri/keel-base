package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * 本接口定义了一类可通过 JSON 对象重载数据的实体。
 *
 * @since 4.1.1
 */
public interface JsonObjectReloadable {
    /**
     * @param jsonObject the JSON object with which this class should be reloaded
     */
    void reloadData(@NotNull JsonObject jsonObject);

}
