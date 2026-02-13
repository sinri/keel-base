package io.github.sinri.keel.base.json;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.InvocationTargetException;

@NullMarked
@TechnicalPreview(since = "5.0.2")
public interface JsonObjectConvertibleBean extends JsonObjectConvertible, java.io.Serializable {
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

}
