package io.github.sinri.keel.base.configuration.lab;

import io.github.sinri.keel.base.configuration.ConfigProperty;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 配置节点
 * <p>
 * 基于{@link JsonifiableDataUnit}实现。
 *
 * @since 5.0.0
 */
interface ConfigElement extends JsonifiableDataUnit {
    /**
     * 获取配置节点的名称
     *
     * @return 当前配置节点的配置子项名称
     */
    @NotNull String getElementName();

    /**
     * 获取配置节点的值
     *
     * @return 当前配置节点的配置子项值，可以为 null。
     */
    @Nullable String getElementValue();

    /**
     * 设置配置节点的值
     *
     * @param value 设置当前配置节点的配置子项值
     */
    void setElementValue(@Nullable String value);

    /**
     * 获取配置节点的子项名称集合
     *
     * @return 当前配置节点的配置子项名称集合
     */
    @NotNull Set<String> getChildNames();

    /**
     * 获取指定名称对应的配置节点的子项，如果不存在则创建。
     * <p>
     * 本方法确保当前配置节点在方法执行后拥有指定名称的子节点。
     *
     * @param childName 子节点的名称
     * @return 子节点，可能为新创建的或已存在的，不为 null
     */
    @NotNull ConfigElement ensureChild(@NotNull String childName);

    /**
     * 创建指定名称对应的配置节点的子项
     *
     * @param child 子节点
     */
    void addChild(@NotNull ConfigElement child);

    /**
     * 移除指定名称对应的配置节点的子项
     *
     * @param childName 子节点的名称
     */
    void removeChild(@NotNull String childName);

    /**
     * 获取指定名称对应的配置节点的子项
     *
     * @param childName 子节点的名称
     * @return 子节点，如果不存在就返回 null
     */
    @Nullable ConfigElement getChild(@NotNull String childName);

    /**
     * 从 Properties 对象中重新加载配置数据以完全更新所有子项。
     *
     * @param properties Properties 对象，包含配置数据
     */
    void reloadData(@NotNull Properties properties);

    /**
     * 根据指定的键链，层层抽取出以配置子项为根的配置节点。
     *
     * @param keychain 键链，表示要提取的子项路径
     * @return 提取的子项，如果不存在则返回 null
     */
    @Nullable ConfigElement extract(@NotNull String... keychain);

    /**
     * 根据指定的键链，层层抽取出以配置子项为根的配置节点。
     *
     * @param keychain 键链，表示要提取的子项路径
     * @return 提取的子项，如果不存在则返回 null
     */
    @Nullable ConfigElement extract(@NotNull List<@NotNull String> keychain);

    /**
     * 将当前配置节点及其子节点转换为配置属性列表。
     *
     * @return 配置属性列表
     */
    @NotNull List<@NotNull ConfigProperty> transformChildrenToPropertyList();
}
