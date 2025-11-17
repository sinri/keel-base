package io.github.sinri.keel.base.json;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * 接口 {@link JsonifiableDataUnit} 的基本实现。
 * <p>
 * 自定义数据类可通过继承本类快速实现。
 *
 * @since 5.0.0
 */
public class JsonifiableDataUnitImpl implements JsonifiableDataUnit {
    @NotNull
    private JsonObject jsonObject;

    public JsonifiableDataUnitImpl(@NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JsonifiableDataUnitImpl() {
        this.jsonObject = new JsonObject();
    }

    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    @Override
    public void reloadData(@NotNull JsonObject jsonObject) {
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
