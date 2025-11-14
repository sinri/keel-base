package io.github.sinri.keel.base.json;

/**
 * An interface for those entities could be serialized as a string of JSON Expression.
 *
 * @since 4.1.1
 */
public interface JsonSerializable {
    /**
     * @return the JSON Object expression of this instance.
     */
    String toJsonExpression();

    /**
     * @return the formatted JSON Object expression of this instance.
     */
    String toFormattedJsonExpression();
}
