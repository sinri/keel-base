package io.github.sinri.keel.base.json;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * 本接口对应一种可以与 JsonObject 进行互换的 Bean 实体。
 *
 * @since 5.0.2
 */
@NullMarked
@TechnicalPreview(since = "5.0.2")
public interface JsonObjectMappedBean extends JsonObjectConvertible, JsonObjectReloadable, java.io.Serializable {
    /**
     * 基于本 Bean 类中的 getter 方法，将获取到的内容转化为对象 JsonObject 的键值对。
     * <p>
     * 支持的 getter 方法格式：
     * <ul>
     *   <li>getXxx() - 适用于非 boolean 类型</li>
     *   <li>isXxx() - 适用于 boolean 类型（推荐）</li>
     * </ul>
     * <p>
     * 键的定义为：取方法名去掉前缀 get/is 后，对后面部分运用下划线命名法生成。
     *
     * @return 当前 Bean 类的对应 JsonObject 对象
     */
    @Override
    default JsonObject toJsonObject() {
        var j = new JsonObject();
        for (var method : this.getClass().getMethods()) {
            String methodName = method.getName();
            String prefix = null;

            // 检查是否为 get 前缀的方法（非 boolean 类型）
            if (methodName.startsWith("get") && method.getParameterCount() == 0 && !methodName.equals("getClass")) {
                prefix = "get";
            }
            // 检查是否为 is 前缀的方法（boolean 类型）
            else if (methodName.startsWith("is") && method.getParameterCount() == 0 && methodName.length() > 2) {
                // 确保第三个字符是大写字母（避免处理 isA、isB 这样的方法）
                if (Character.isUpperCase(methodName.charAt(2))) {
                    prefix = "is";
                }
            }

            if (prefix != null) {
                var name = methodName.substring(prefix.length());
                if (!name.isEmpty()) {
                    StringBuilder snakeName = new StringBuilder();
                    for (int i = 0; i < name.length(); i++) {
                        char c = name.charAt(i);
                        char nextChar = (i < name.length() - 1) ? name.charAt(i + 1) : '\0';
                        // 当前是大写字母，且满足以下条件之一时添加下划线：
                        // 1. 不是第一个字符
                        // 2. 后一个字符是小写字母（表示连续大写的缩写词结束）
                        if (Character.isUpperCase(c)) {
                            if (i > 0 && (Character.isLowerCase(nextChar) || !Character.isUpperCase(name.charAt(i - 1)))) {
                                snakeName.append('_');
                            }
                            snakeName.append(Character.toLowerCase(c));
                        } else {
                            snakeName.append(c);
                        }
                    }
                    try {
                        j.put(snakeName.toString(), method.invoke(this));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return j;
    }

    /**
     * 从入参的 JsonObject 中提取键值对，根据键的命名寻找相应的 setter 方法，并以相应的值进行赋值。
     * <p>
     * JsonObject 中的键的命名支持驼峰命名法和下划线命名法。
     * <p>
     * 值为 {@link JsonObject} 类型时，要求相应 setter 方法接收
     * {@link JsonObject} 类型、
     * {@link JsonObjectReloadable} 兼容类型、
     * {@link UnmodifiableJsonifiableEntity} 兼容类型的参数以接收数据。
     * <p>
     * 值为 {@link io.vertx.core.json.JsonArray} 类型时，要求相应 setter 方法接收
     * {@link io.vertx.core.json.JsonArray} 类型、
     * 以 {@link JsonObjectReloadable} 兼容类型
     * 或{@link UnmodifiableJsonifiableEntity} 兼容类型为元素的{@link java.util.List}类型参数以接收数据。
     *
     * @param jsonObject 用于重载数据的 JSON 对象，通常由 {@link JsonObjectConvertible#toJsonObject()} 方法生成
     */
    @Override
    default void reloadData(JsonObject jsonObject) {
        for (String key : jsonObject.getMap().keySet()) {
            Object value = jsonObject.getValue(key);
            if (value == null) {
                continue;
            }

            // 将下划线命名法转换为驼峰命名法
            String camelKey = snakeToCamel(key);
            String setterName = "set" + camelKey;

            // 查找匹配的 setter 方法
            for (var method : this.getClass().getMethods()) {
                if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object convertedValue = convertValue(value, paramType);
                    if (convertedValue != null || paramType == Object.class) {
                        try {
                            method.invoke(this, convertedValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("Failed to invoke setter: " + setterName, e);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 将下划线命名法转换为驼峰命名法
     *
     * @param snakeCase 下划线命名法字符串
     * @return 驼峰命名法字符串
     */
    private String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                // 首字母大写
                if (i == 0) {
                    result.append(Character.toUpperCase(c));
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    /**
     * 将值转换为指定的类型
     *
     * @param value      原始值
     * @param targetType 目标类型
     * @return 转换后的值，如果无法转换则返回 null
     */
    private <T> @Nullable Object convertValue(@Nullable Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        // 如果类型匹配或可以自动转换
        if (targetType.isInstance(value)) {
            return value;
        }

        // 处理 JsonObject 类型
        if (value instanceof JsonObject jsonValue) {
            // 目标类型是 JsonObject
            if (targetType == JsonObject.class) {
                return jsonValue;
            }
            // 目标类型是 JsonObjectReloadable 的子类
            if (JsonObjectReloadable.class.isAssignableFrom(targetType)) {
                try {
                    var instance = (JsonObjectReloadable) targetType.getDeclaredConstructor().newInstance();
                    instance.reloadData(jsonValue);
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create JsonObjectReloadable instance: " + targetType, e);
                }
            }
            // 目标类型是 UnmodifiableJsonifiableEntity 的子类
            if (UnmodifiableJsonifiableEntity.class.isAssignableFrom(targetType)) {
                try {
                    return UnmodifiableJsonifiableEntity.wrap(jsonValue, targetType.asSubclass(UnmodifiableJsonifiableEntity.class));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create UnmodifiableJsonifiableEntity instance: " + targetType, e);
                }
            }
        }

        // 处理 JsonArray 类型
        if (value instanceof io.vertx.core.json.JsonArray jsonArray) {
            // 目标类型是 JsonArray
            if (targetType == io.vertx.core.json.JsonArray.class) {
                return jsonArray;
            }
            // 目标类型是 List 或其子类
            if (java.util.List.class.isAssignableFrom(targetType)) {
                try {
                    java.util.List<Object> list = (java.util.List<Object>) (targetType == java.util.List.class
                            ? new java.util.ArrayList<>()
                            : targetType.getDeclaredConstructor().newInstance());
                    for (Object item : jsonArray) {
                        //                        if (item instanceof JsonObject itemJson) {
                        //                            // 尝试推断元素类型，默认使用 JsonObject
                        //                            list.add(itemJson);
                        //                        } else {
                        //                            list.add(item);
                        //                        }
                        list.add(item);
                    }
                    return list;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create List instance: " + targetType, e);
                }
            }
        }

        // 处理基本类型转换
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        if (targetType == Float.class || targetType == float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
        }

        // 无法转换
        throw new IllegalArgumentException("Unsupported target type " + targetType + " for value " + value);
    }

    @Override
    default String toJsonExpression() {
        return toJsonObject().encode();
    }

    @Override
    default String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }
}
