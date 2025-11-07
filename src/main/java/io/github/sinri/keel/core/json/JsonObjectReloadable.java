package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * An interface for those entities could be reloaded with a JSON Object.
 *
 * @since 4.1.1
 */
public interface JsonObjectReloadable {
    /**
     * @param jsonObject the JSON object with which this class should be reloaded
     */
    void reloadData(@Nonnull JsonObject jsonObject);

}
