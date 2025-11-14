package io.github.sinri.keel.base.configuration;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * Represents a configuration element in the Keel configuration system. This
 * class allows for
 * hierarchical configuration structures, where each element can have a name, a
 * value, and child elements.
 * It supports reading values of various types (String, Integer, Long, Float,
 * Double, Boolean) from
 * the configuration, and provides methods to navigate through the hierarchy
 * using keychains.
 */
public class KeelConfigElement {
    @NotNull
    private final String name;
    @NotNull
    private final Map<String, KeelConfigElement> children;
    @Nullable
    private String value;

    /**
     * Constructs a new KeelConfigElement with the specified name.
     *
     * @param name the name of the configuration element, must not be null
     */
    public KeelConfigElement(@NotNull String name) {
        this.name = name;
        this.value = null;
        this.children = new ConcurrentHashMap<>();
    }

    /**
     * Constructs a new KeelConfigElement by copying the properties from another
     * KeelConfigElement.
     *
     * @param another the KeelConfigElement to copy from, must not be null
     */
    public KeelConfigElement(@NotNull KeelConfigElement another) {
        this.name = another.getName();
        this.children = another.getChildren();
        this.value = another.getValueAsString();
    }

    /**
     * Creates a new {@code KeelConfigElement} instance from the provided JSON
     * object.
     *
     * @param jsonObject the JSON object to convert, must not be null
     * @return a new {@code KeelConfigElement} instance with properties set based on
     *         the JSON object
     * @throws IllegalArgumentException if any of the children in the JSON object is
     *                                  not a JSON object
     */
    public static KeelConfigElement fromJsonObject(@NotNull JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        KeelConfigElement keelConfigElement = new KeelConfigElement(name);
        if (jsonObject.containsKey("value")) {
            keelConfigElement.value = jsonObject.getString("value");
        }
        JsonArray children = jsonObject.getJsonArray("children");
        children.forEach(child -> {
            if (child instanceof JsonObject) {
                keelConfigElement.addChild(fromJsonObject((JsonObject) child));
            } else {
                throw new IllegalArgumentException();
            }
        });
        return keelConfigElement;
    }

    /**
     * Retrieves the configuration follow the ability of Vert.x Config, and returns
     * it as a {@code KeelConfigElement}
     * instance.
     * Btw, VertxConfig is designed as an async config fetch way, it is not matched
     * with this class.
     *
     * @param configRetrieverOptions the options for the configuration retriever,
     *                               must not be null
     * @return a future that completes with a {@code KeelConfigElement} instance
     *         representing the retrieved
     *         configuration
     * @see <a href="https://vertx.io/docs/vertx-config/java/">Vert.x Config</a>
     * @since 3.2.10
     */
    public static Future<KeelConfigElement> retrieve(@NotNull ConfigRetrieverOptions configRetrieverOptions) {
        ConfigRetriever configRetriever = ConfigRetriever.create(Keel.getVertx(), configRetrieverOptions);
        return configRetriever.getConfig()
                              .compose(jsonObject -> {
                                  KeelConfigElement element = fromJsonObject(jsonObject);
                                  return Future.succeededFuture(element);
                              })
                              .andThen(ar -> configRetriever.close());
    }

    /**
     * Performs a depth-first traversal of the configuration tree, starting from the given node,
     * and collects all leaf properties into the provided output list.
     * <p>
     * For each node with a non-null value, a {@link KeelConfigProperty} is created with the current path as its
     * keychain.
     * <p>
     * Child nodes are traversed in lexicographical order to ensure stable output.
     *
     * @param node the current configuration element being traversed
     * @param path the keychain representing the path from the root to the current node
     * @param out  the list to collect resulting {@link KeelConfigProperty} objects
     * @since 4.1.1
     */
    private static void dfsTransform(@NotNull KeelConfigElement node,
                                     @NotNull List<String> path,
                                     @NotNull List<KeelConfigProperty> out) {
        // 当前节点若有值，则输出一条属性
        if (node.value != null) {
            out.add(new KeelConfigProperty()
                    .setKeychain(path)
                    .setValue(node.value));
        }
        // 继续遍历子节点
        if (!node.children.isEmpty()) {
            List<String> keys = new ArrayList<>(node.children.keySet());
            Collections.sort(keys);
            for (String k : keys) {
                KeelConfigElement child = node.children.get(k);
                if (child != null) {
                    List<String> nextPath = new ArrayList<>(path);
                    nextPath.add(k);
                    dfsTransform(child, nextPath, out);
                }
            }
        }
    }

    /**
     * Returns the name of the configuration element.
     *
     * @return the name of the configuration element, which is guaranteed to be
     *         non-null
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Retrieves the value of the configuration element as a string.
     *
     * @return the value of the configuration element, or null if no value is set
     */
    @Nullable
    public String getValueAsString() {
        return value;
    }

    /**
     * Retrieves the value of the configuration element as a string. If the value is
     * not set, it returns the provided
     * default value.
     *
     * @param def the default value to return if the value of the configuration
     *            element is not set
     * @return the value of the configuration element as a string, or the provided
     *         default value if the value is not set
     */
    @Nullable
    public String getValueAsStringElse(@Nullable String def) {
        return Objects.requireNonNullElse(value, def);
    }

    /**
     * Reads the string value of the configuration element.
     * If the keychain is not provided, it attempts to read the value from the
     * current element.
     *
     * @return the string value of the configuration element, or null if the value
     *         is not set or the keychain does not
     *         match any child
     */
    @Nullable
    public String readString() {
        return readString(List.of());
    }

    /**
     * Reads the string value of the configuration element based on the provided
     * keychain.
     *
     * @param keychain the list of keys to traverse in the configuration hierarchy,
     *                 must not be null
     * @return the string value of the configuration element, or null if the value
     *         is not set or the keychain does not
     *         match any child
     */
    @Nullable
    public String readString(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsString();
    }

    /**
     * Reads the string value of the configuration element based on the provided
     * keychain.
     *
     * @param keychain the list of keys to traverse in the configuration hierarchy,
     *                 must not be null
     * @param def      the default value to return if the value of the configuration
     *                 element is not set
     * @return the value of the configuration element as a string, or the provided
     *         default value if the value is not set
     *         or the keychain does not match any child
     */
    @Nullable
    public String readString(@NotNull List<String> keychain, @Nullable String def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsStringElse(def);
    }

    /**
     * Reads the string value of the configuration element based on the provided
     * keychain.
     *
     * @param keychain the single key to traverse in the configuration hierarchy,
     *                 must not be null
     * @param def      the default value to return if the value of the configuration
     *                 element is not set
     * @return the value of the configuration element as a string, or the provided
     *         default value if the value is not set
     *         or the keychain does not match any child
     */
    @Nullable
    public String readString(@NotNull String keychain, @Nullable String def) {
        return readString(List.of(keychain), def);
    }

    /**
     * Returns the value as an Integer if it can be parsed, otherwise returns null.
     *
     * @return the integer representation of the value if it can be parsed, or null
     *         if the value is null or cannot be
     *         parsed
     */
    @Nullable
    public Integer getValueAsInteger() {
        if (value == null)
            return null;
        return Integer.parseInt(value);
    }

    /**
     * Retrieves the value of the configuration element as an integer.
     * If the value is not set or cannot be parsed as an integer, it returns the
     * provided default value.
     *
     * @param def the default value to return if the value of the configuration
     *            element is not set or cannot be parsed
     * @return the value of the configuration element as an integer, or the provided
     *         default value if the value is not
     *         set or cannot be parsed
     */
    public int getValueAsIntegerElse(int def) {
        if (value == null)
            return def;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads the integer value of the configuration element.
     * If no keychain is provided, it attempts to read the value from the current
     * element.
     *
     * @return the integer value of the configuration element, or null if the value
     *         is not set or cannot be parsed as an
     *         integer
     */
    @Nullable
    public Integer readInteger() {
        return readInteger(List.of());
    }

    /**
     * Reads the integer value of the configuration element based on the provided
     * keychain.
     *
     * @param keychain the list of keys to traverse in the configuration hierarchy,
     *                 must not be null
     * @return the integer value of the configuration element, or null if the value
     *         is not set or cannot be parsed as an
     *         integer
     */
    @Nullable
    public Integer readInteger(@NotNull List<String> keychain) {
        var x = this.extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsInteger();
    }

    /**
     * Reads the integer value of the configuration element based on the provided
     * keychain.
     * If the value is not set or cannot be parsed as an integer, it returns the
     * provided default value.
     *
     * @param keychain the list of keys to traverse in the configuration hierarchy,
     *                 must not be null
     * @param def      the default value to return if the value of the configuration
     *                 element is not set or cannot be
     *                 parsed
     * @return the value of the configuration element as an integer, or the provided
     *         default value if the value is not
     *         set or cannot be parsed
     */
    public int readInteger(@NotNull List<String> keychain, int def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsIntegerElse(def);
    }

    /**
     * Reads the integer value of the configuration element based on the provided
     * keychain.
     * If the value is not set or cannot be parsed as an integer, it returns the
     * provided default value.
     *
     * @param keychain the single key to traverse in the configuration hierarchy,
     *                 must not be null
     * @param def      the default value to return if the value of the configuration
     *                 element is not set or cannot be
     *                 parsed
     * @return the value of the configuration element as an integer, or the provided
     *         default value if the value is not
     *         set or cannot be parsed
     */
    public int readInteger(@NotNull String keychain, int def) {
        return readInteger(List.of(keychain), def);
    }

    /**
     * Converts the value to a Long if possible.
     *
     * @return the long representation of the value, or null if the value is null or
     *         cannot be parsed as a Long
     */
    @Nullable
    public Long getValueAsLong() {
        if (value == null)
            return null;
        return Long.parseLong(value);
    }

    /**
     * Retrieves the value as a long, or returns a default value if the conversion
     * fails or the value is null.
     *
     * @param def the default long value to return if the value cannot be converted
     *            to a long or is null
     * @return the long value of the current object, or the specified default value
     *         if conversion is not possible
     */
    public long getValueAsLongElse(long def) {
        if (value == null)
            return def;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads a long value from the input source.
     *
     * @return the long value read, or null if the value cannot be read or is not
     *         available
     */
    @Nullable
    public Long readLong() {
        return readLong(List.of());
    }

    /**
     * Retrieves a long value from the configuration based on the provided keychain.
     *
     * @param keychain the list of keys to navigate through the configuration
     *                 hierarchy
     * @return the long value if found, or null if the keychain does not resolve to
     *         a valid long value
     */
    @Nullable
    public Long readLong(@NotNull List<String> keychain) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted != null) {
            return extracted.getValueAsLong();
        } else {
            return null;
        }
    }

    /**
     * Reads a long value from the configuration using the provided keychain.
     *
     * @param keychain the list of keys to traverse in the configuration to find the
     *                 desired value
     * @param def      the default value to return if the keychain does not resolve
     *                 to a valid long or is not found
     * @return the long value associated with the given keychain, or the default
     *         value if the keychain is invalid or the
     *         value is not a long
     */
    public long readLong(@NotNull List<String> keychain, long def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsLongElse(def);
    }

    /**
     * Reads a long value from the configuration using the provided keychain.
     *
     * @param keychain the keychain to use for locating the value in the
     *                 configuration
     * @param def      the default value to return if the keychain does not exist or
     *                 the value is not a valid long
     * @return the long value found at the specified keychain, or the default value
     *         if not found or invalid
     */
    public long readLong(@NotNull String keychain, long def) {
        return readLong(List.of(keychain), def);
    }

    /**
     * Converts the stored value to a Float if possible.
     *
     * @return the value as a Float, or null if the value is null or cannot be
     *         parsed as a Float
     */
    @Nullable
    public Float getValueAsFloat() {
        if (value == null)
            return null;
        return Float.parseFloat(value);
    }

    /**
     * Attempts to parse the value as a float. If the value is null or cannot be
     * parsed, returns the provided default
     * value.
     *
     * @param def the default float value to return if the value is null or cannot
     *            be parsed
     * @return the parsed float value, or the default value if parsing fails or the
     *         value is null
     */
    public float getValueAsFloatElse(float def) {
        if (value == null)
            return def;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads a floating-point number from the input source.
     *
     * @return The floating-point number read, or null if no valid number is found.
     */
    @Nullable
    public Float readFloat() {
        return readFloat(List.of());
    }

    /**
     * Reads a float value from the configuration based on the provided keychain.
     *
     * @param keychain a non-null list of strings representing the path to the
     *                 desired configuration value
     * @return the float value if found and successfully converted, or null if the
     *         value is not found or cannot be
     *         converted to a float
     */
    @Nullable
    public Float readFloat(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsFloat();
    }

    /**
     * Reads a float value from the configuration based on the provided keychain.
     *
     * @param keychain the list of keys to traverse the configuration tree
     * @param def      the default float value to return if the keychain does not
     *                 resolve to a valid float
     * @return the float value found at the end of the keychain, or the default
     *         value if not found or invalid
     */
    public float readFloat(@NotNull List<String> keychain, float def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsFloatElse(def);
    }

    /**
     * Reads a float value from the configuration using the provided keychain.
     *
     * @param keychain the keychain to use for locating the value in the
     *                 configuration
     * @param def      the default value to return if the keychain does not exist or
     *                 the value is not a valid float
     * @return the float value found at the specified keychain, or the default value
     *         if not found or invalid
     */
    public float readFloat(@NotNull String keychain, float def) {
        return readFloat(List.of(keychain), def);
    }

    /**
     * Converts the value to a Double if it is not null.
     *
     * @return the value as a Double, or null if the original value is null
     */
    @Nullable
    public Double getValueAsDouble() {
        if (value == null)
            return null;
        return Double.parseDouble(value);
    }

    /**
     * Attempts to parse the value as a double. If the value is null or cannot be
     * parsed as a double,
     * returns the provided default value.
     *
     * @param def the default value to return if the value is null or cannot be
     *            parsed as a double
     * @return the parsed double value, or the default value if parsing fails or the
     *         value is null
     */
    public double getValueAsDoubleElse(double def) {
        if (value == null)
            return def;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Reads a double value from the input.
     *
     * @return the double value read, or null if the input cannot be converted to a
     *         double
     */
    @Nullable
    public Double readDouble() {
        return readDouble(List.of());
    }

    /**
     * Reads a double value from the data structure using the provided keychain.
     *
     * @param keychain a non-null list of strings representing the path to the
     *                 desired value
     * @return the double value if found, or null if the value is not present or
     *         cannot be converted to a double
     */
    @Nullable
    public Double readDouble(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsDouble();
    }

    /**
     * Reads a double value from the configuration using the provided keychain.
     *
     * @param keychain the list of keys to navigate through the configuration
     * @param def      the default value to return if the keychain does not resolve
     *                 to a valid double
     * @return the double value found at the keychain, or the default value if the
     *         keychain is invalid or the value is
     *         not a double
     */
    public double readDouble(@NotNull List<String> keychain, double def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsDoubleElse(def);
    }

    /**
     * Reads a double value from the configuration using the provided keychain.
     * If the keychain does not exist or the value cannot be parsed as a double, the
     * default value is returned.
     *
     * @param keychain the keychain to read the double value from
     * @param def      the default value to return if the keychain is not found or
     *                 the value is not a valid double
     * @return the double value associated with the keychain, or the default value
     *         if the keychain is not found or the
     *         value is not a valid double
     */
    public double readDouble(@NotNull String keychain, double def) {
        return readDouble(List.of(keychain), def);
    }

    /**
     * Converts the stored value to a Boolean.
     * If the value is "YES" or "TRUE" (case-insensitive), it returns true.
     * If the value is null, it returns null.
     * Otherwise, it returns false.
     *
     * @return the Boolean representation of the value, or null if the value is null
     */
    @Nullable
    public Boolean getValueAsBoolean() {
        if (value == null)
            return null;
        return "YES".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value);
    }

    /**
     * Converts the value to a boolean, using a set of predefined true string
     * values.
     * If the value is null, it returns the provided default value.
     *
     * @param def the default boolean value to return if the current value is null
     * @return true if the value (case-insensitive) is one of "YES", "TRUE", "ON",
     *         or "1"; otherwise, returns the
     *         default value
     */
    public boolean getValueAsBooleanElse(boolean def) {
        if (value == null)
            return def;
        return "YES".equalsIgnoreCase(value)
                || "TRUE".equalsIgnoreCase(value)
                || "ON".equalsIgnoreCase(value)
                || "1".equalsIgnoreCase(value);
    }

    /**
     * Reads a boolean value from the input source.
     *
     * @return The boolean value read, or null if the value could not be read or is
     *         not available.
     */
    @Nullable
    public Boolean readBoolean() {
        return readBoolean(List.of());
    }

    /**
     * Reads a boolean value from the configuration based on the provided keychain.
     *
     * @param keychain the list of keys representing the path to the desired boolean
     *                 value
     * @return the boolean value if found, or null if the value is not present or
     *         cannot be converted to a boolean
     */
    @Nullable
    public Boolean readBoolean(@NotNull List<String> keychain) {
        var x = extract(keychain);
        if (x == null)
            return null;
        return x.getValueAsBoolean();
    }

    /**
     * Reads a boolean value from the configuration based on the provided keychain.
     *
     * @param keychain the list of keys representing the path to the desired
     *                 configuration element
     * @param def      the default boolean value to return if the configuration
     *                 element is not found or cannot be
     *                 converted to a boolean
     * @return the boolean value of the configuration element, or the default value
     *         if the element is not found or
     *         conversion fails
     */
    public boolean readBoolean(@NotNull List<String> keychain, boolean def) {
        KeelConfigElement extracted = this.extract(keychain);
        if (extracted == null)
            return def;
        return extracted.getValueAsBooleanElse(def);
    }

    /**
     * Reads a boolean value from the configuration based on the provided keychain.
     *
     * @param keychain the keychain to locate the boolean value
     * @param def      the default boolean value to return if the keychain is not
     *                 found or the value is not a valid
     *                 boolean
     * @return the boolean value associated with the keychain, or the default value
     *         if not found or invalid
     */
    public boolean readBoolean(@NotNull String keychain, boolean def) {
        return readBoolean(List.of(keychain), def);
    }

    /**
     * Ensures that a child with the specified name exists in the current
     * configuration element.
     * If the child does not exist, it is created and added to the children map.
     *
     * @param childName the name of the child element to ensure
     * @return the KeelConfigElement corresponding to the child
     */
    public KeelConfigElement ensureChild(@NotNull String childName) {
        return this.children.computeIfAbsent(childName, x -> new KeelConfigElement(childName));
    }

    /**
     * Adds a child element to the current KeelConfigElement.
     *
     * @param child the KeelConfigElement to be added as a child. It must not be
     *              null.
     * @return the current KeelConfigElement instance, allowing for method chaining.
     */
    public KeelConfigElement addChild(@NotNull KeelConfigElement child) {
        this.children.put(child.getName(), child);
        return this;
    }

    /**
     * Removes a child element from the current configuration element.
     *
     * @param child the child element to be removed, must not be null
     * @return the current KeelConfigElement instance after the removal
     */
    public KeelConfigElement removeChild(@NotNull KeelConfigElement child) {
        this.children.remove(child.getName());
        return this;
    }

    /**
     * Removes a child element with the specified name from this KeelConfigElement.
     *
     * @param childName the name of the child element to be removed, must not be
     *                  null
     * @return the current KeelConfigElement instance after the removal operation
     */
    public KeelConfigElement removeChild(@NotNull String childName) {
        this.children.remove(childName);
        return this;
    }

    /**
     * Sets the value of the KeelConfigElement.
     *
     * @param value the new value to be set for the KeelConfigElement, must not be
     *              null
     * @return the current instance of KeelConfigElement with the updated value
     */
    public KeelConfigElement setValue(@NotNull String value) {
        this.value = value;
        return this;
    }

    /**
     * Returns a map of child elements associated with this configuration element.
     * The keys in the map are the names of the child elements, and the values are
     * the corresponding
     * {@link KeelConfigElement} instances.
     *
     * @return a non-null map of child elements, where the key is the name of the
     *         child and the value is the
     *         {@link KeelConfigElement} instance
     */
    @NotNull
    public Map<String, KeelConfigElement> getChildren() {
        return children;
    }

    /**
     * Retrieves a child element by its name.
     *
     * @param childName the name of the child element to retrieve, must not be null
     * @return the child element if found, or null if no such child exists
     */
    @Nullable
    public KeelConfigElement getChild(@NotNull String childName) {
        return children.get(childName);
    }

    /**
     * Converts the current object into a JSON representation.
     *
     * @return a JsonObject that represents the current object, including its name,
     *         children, and value if present.
     */
    public JsonObject toJsonObject() {
        JsonArray childArray = new JsonArray();
        children.forEach((cName, c) -> childArray.add(c.toJsonObject()));
        var x = new JsonObject()
                .put("name", name)
                .put("children", childArray);
        if (value != null) {
            x.put("value", value);
        }
        return x;
    }

    /**
     * Extracts a nested KeelConfigElement based on the provided list of keys.
     *
     * @param split the list of keys representing the path to the desired
     *              KeelConfigElement.
     *              If the list is empty, the current instance is returned.
     * @return the KeelConfigElement located at the specified path, or null if any
     *         key in the path does not exist.
     */
    public @Nullable KeelConfigElement extract(@NotNull List<String> split) {
        if (split.isEmpty())
            return this;
        if (split.size() == 1)
            return this.children.get(split.get(0));
        KeelConfigElement keelConfigElement = this.children.get(split.get(0));
        if (keelConfigElement == null) {
            return null;
        }
        for (int i = 1; i < split.size(); i++) {
            keelConfigElement = keelConfigElement.getChild(split.get(i));
            if (keelConfigElement == null) {
                return null;
            }
        }
        return keelConfigElement;
    }

    /**
     * Extracts a configuration element based on the provided split criteria.
     *
     * @param split an array of strings representing the split criteria to locate
     *              the desired configuration element
     * @return the extracted KeelConfigElement if found, or null if no matching
     *         element is found
     */
    public @Nullable KeelConfigElement extract(@NotNull String... split) {
        List<String> list = Arrays.asList(split);
        return this.extract(list);
    }

    /**
     * Loads the provided properties into the configuration, creating a nested
     * structure of KeelConfigElement
     * based on the keys which can be dot-separated for hierarchical representation.
     *
     * @param properties the properties to load. Must not be null.
     * @return the current KeelConfigElement instance, allowing method chaining.
     */
    public KeelConfigElement loadProperties(@NotNull Properties properties) {
        properties.forEach((k, v) -> {
            String fullKey = k.toString();
            String[] keyArray = fullKey.split("\\.");
            if (keyArray.length > 0) {
                KeelConfigElement keelConfigElement = children.computeIfAbsent(
                        keyArray[0],
                        x -> new KeelConfigElement(keyArray[0]));
                if (keyArray.length == 1) {
                    keelConfigElement.setValue(v.toString());
                } else {
                    for (int i = 1; i < keyArray.length; i++) {
                        String key = keyArray[i];
                        keelConfigElement = keelConfigElement.ensureChild(key);
                        if (i == keyArray.length - 1) {
                            keelConfigElement.setValue(v.toString());
                        }
                    }
                }
            }
        });
        return this;
    }

    /**
     * Loads a properties file and returns a configuration element.
     *
     * @param propertiesFileName the name of the properties file to load, must not
     *                           be null
     * @return a non-null KeelConfigElement representing the loaded properties
     * @since 3.0.1
     */
    public @NotNull KeelConfigElement loadPropertiesFile(@NotNull String propertiesFileName) throws IOException {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    /**
     * Loads a properties file and converts it into a KeelConfigElement.
     *
     * @param propertiesFileName the name of the properties file to load. This file
     *                           should be placed alongside the JAR.
     * @param charset            the character set used to read the properties file.
     * @return a non-null KeelConfigElement containing the configuration data from
     *         the loaded properties file.
     */
    public @NotNull KeelConfigElement loadPropertiesFile(@NotNull String propertiesFileName, @NotNull Charset charset)
            throws IOException {
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(propertiesFileName, charset));
        } catch (IOException e) {
            // Keel.getLogger().debug("Cannot read the file config.properties, use the embedded one.");
            System.err.println("[WARNING] Cannot read the file " + propertiesFileName + ". Use the embedded one.");
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName);
            if (resourceAsStream == null) {
                throw new IOException("The embedding properties file is not found.");
            }
            properties.load(resourceAsStream);
            // if the embedded file is not found, throw an IOException
        }

        return loadProperties(properties);
    }

    /**
     * Loads the content of a properties file from the provided string.
     *
     * @param content the string content representing the properties file
     * @return a KeelConfigElement object containing the loaded properties
     */ /*
     * @since 3.0.6
     */
    public @NotNull KeelConfigElement loadPropertiesFileContent(@NotNull String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return loadProperties(properties);
    }

    /**
     * /**
     * Converts all child elements of this configuration element into a list of {@link KeelConfigProperty} objects.
     * <p>
     * Each property in the list represents a leaf node in the configuration tree, with its keychain reflecting the path
     * from this element to the leaf.
     * <p>
     * The properties are collected using a depth-first traversal, and sibling nodes are processed in lexicographical
     * order to ensure stable output.
     *
     * @return a list of {@link KeelConfigProperty} representing all leaf properties of the child elements
     * @since 4.1.1
     */
    public List<KeelConfigProperty> transformChildrenToPropertyList() {
        List<KeelConfigProperty> properties = new ArrayList<>();
        // 为了输出稳定，按字典序遍历同级子节点
        List<String> keys = new ArrayList<>(this.children.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            KeelConfigElement child = this.children.get(key);
            if (child != null) {
                dfsTransform(child, new ArrayList<>(List.of(key)), properties);
            }
        }
        return properties;
    }

}
