package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * An interface for those entities could be read as an JSON object.
 *
 * @since 4.1.1
 */
public interface JsonObjectReadable extends Iterable<Map.Entry<String, Object>> {

    @Nullable
    <T> T read(@Nonnull Function<JsonPointer, Class<T>> func);

    /**
     * Reads a string value from a JSON structure using the specified JSON Pointer arguments.
     * This method constructs the JSON Pointer dynamically using the provided arguments
     * and attempts to retrieve the string value at the resulting location.
     *
     * @param args The sequence of string arguments used to build the JSON Pointer.
     *             Each argument represents a step in the JSON path to the desired value.
     * @return The string value found at the JSON Pointer's location, or null if the value is not present
     *         or cannot be read as a string.
     */
    default @Nullable String readString(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return String.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readString(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull String readStringRequired(String... args) {
        var r = readString(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * Reads a number value from a JSON structure using the specified JSON Pointer arguments.
     * This method constructs the JSON Pointer dynamically using the provided arguments
     * and attempts to retrieve the number value at the resulting location.
     *
     * @param args The sequence of string arguments used to build the JSON Pointer.
     *             Each argument represents a step in the JSON path to the desired value.
     * @return The number value found at the JSON Pointer's location, or null if the value is not present
     *         or cannot be read as a number.
     */
    default @Nullable Number readNumber(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Number.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readNumber(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Number readNumberRequired(String... args) {
        var r = readNumber(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Long readLong(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.longValue();
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readLong(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Long readLongRequired(String... args) {
        var r = readLong(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Integer readInteger(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.intValue();
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readInteger(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Integer readIntegerRequired(String... args) {
        var r = readInteger(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Float readFloat(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.floatValue();
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readFloat(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Float readFloatRequired(String... args) {
        var r = readFloat(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Double readDouble(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.doubleValue();
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readDouble(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Double readDoubleRequired(String... args) {
        var r = readDouble(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Boolean readBoolean(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Boolean.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readBoolean(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Boolean readBooleanRequired(String... args) {
        var r = readBoolean(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable JsonObject readJsonObject(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonObject.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readJsonObject(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull JsonObject readJsonObjectRequired(String... args) {
        var r = readJsonObject(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable JsonArray readJsonArray(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readJsonArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull JsonArray readJsonArrayRequired(String... args) {
        var r = readJsonArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<JsonObject> readJsonObjectArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<JsonObject> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else if (x instanceof JsonObject) {
                list.add((JsonObject) x);
            } else {
                throw new RuntimeException("NOT JSON OBJECT");
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readJsonObjectArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<JsonObject> readJsonObjectArrayRequired(String... args) {
        var r = readJsonObjectArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<String> readStringArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<String> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else {
                list.add(x.toString());
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readStringArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<String> readStringArrayRequired(String... args) {
        var r = readStringArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<Integer> readIntegerArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Integer> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).intValue());
                } else {
                    throw new RuntimeException("Not Integer");
                }
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readIntegerArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<Integer> readIntegerArrayRequired(String... args) {
        var r = readIntegerArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<Long> readLongArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Long> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0L);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).longValue());
                } else {
                    throw new RuntimeException("Not Long");
                }
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readLongArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<Long> readLongArrayRequired(String... args) {
        var r = readLongArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<Float> readFloatArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Float> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0.0f);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).floatValue());
                } else {
                    throw new RuntimeException("Not Float");
                }
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readFloatArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<Float> readFloatArrayRequired(String... args) {
        var r = readFloatArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable List<Double> readDoubleArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Double> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0.0);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).doubleValue());
                } else {
                    throw new RuntimeException("Not Double");
                }
            }
        });
        return list;
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readDoubleArray(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull List<Double> readDoubleArrayRequired(String... args) {
        var r = readDoubleArray(args);
        Objects.requireNonNull(r);
        return r;
    }

    default @Nullable Object readValue(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Object.class;
        });
    }

    /**
     * This method is a shortcut for {@link UnmodifiableJsonifiableEntity#readValue(String...)} with
     * {@link Objects#requireNonNull(Object)}.
     *
     * @param args The JSON Pointer arguments.
     * @throws NullPointerException if the value is null.
     */
    default @Nonnull Object readValueRequired(String... args) {
        var r = readValue(args);
        Objects.requireNonNull(r);
        return r;
    }

    /**
     * Read an entity from a JSON Object with Jackson
     * {@link JsonObject#mapTo(Class)}.
     *
     * @param cClass The class of the entity to be read.
     * @param args   The arguments used to form a JSON pointer for locating the JSON
     *               object within a larger structure.
     * @param <C>    The type of the entity to be read.
     * @return The entity read from the JSON Object.
     * @since 4.0.13
     */
    default @Nullable <C> C readEntity(@Nonnull Class<C> cClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null) {
            return null;
        }
        try {
            return jsonObject.mapTo(cClass);
        } catch (Throwable t) {
            return null;
        }
    }


    boolean isEmpty();

    /**
     * @since 3.0.0
     */
    @Override
    @Nonnull
    Iterator<Map.Entry<String, Object>> iterator();
}