package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for those entities could be reloaded with a JSON Object.
 *
 * @since 4.1.1
 */
public interface JsonObjectReloadable {
    /**
     * @param jsonObject the JSON object with which this class should be reloaded
     */
    void reloadData(@NotNull JsonObject jsonObject);

}
