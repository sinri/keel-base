package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 4.1.1
 */
public class JsonifiableDataUnitImpl implements JsonifiableDataUnit {
    @Nonnull
    private JsonObject jsonObject;

    public JsonifiableDataUnitImpl(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JsonifiableDataUnitImpl() {
        this.jsonObject = new JsonObject();
    }

    @Nonnull
    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    @Override
    public void reloadData(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public String toJsonExpression() {
        return jsonObject.encode();
    }

    @Override
    public String toFormattedJsonExpression() {
        return jsonObject.encodePrettily();
    }

    /**
     * As of 4.1.5, provide default implementation.
     *
     * @return a JSON expression of this data unit, following {@link #toJsonExpression()}.
     */
    @Override
    public String toString() {
        return toJsonExpression();
    }
}
