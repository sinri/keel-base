package io.github.sinri.keel.base.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 配置树。
 * <p>
 * 配置树是配置节点的根节点，提供了便捷的读取方法，支持通过键链（keychain）读取不同类型的配置值。
 * <p>
 * 如果配置不存在或无法解析，将抛出 {@link NotConfiguredException} 异常。
 *
 * @since 5.0.0
 */
class ConfigTreeImpl implements ConfigTree {
    private final @NotNull ConfigNode rootNode;

    /**
     * 构造一个空的配置树。
     */
    public ConfigTreeImpl(@NotNull ConfigNode rootNode) {
        super();
        this.rootNode = rootNode;
    }

    public final @NotNull ConfigNode getRootNode() {
        return rootNode;
    }

    @Override
    public @NotNull ConfigTree loadProperties(@NotNull Properties properties) {
        this.getRootNode().reloadData(properties);
        return this;
    }

    @Override
    public @Nullable ConfigTreeImpl extract(@NotNull List<@NotNull String> path) {
        if (path.isEmpty())
            return this;
        if (path.size() == 1) {
            ConfigNode configNode = this.getRootNode().getChildren().get(path.get(0));
            return new ConfigTreeImpl(configNode);
        }
        ConfigNode configElement = this.getRootNode().getChildren().get(path.get(0));
        if (configElement == null) {
            return null;
        }
        for (int i = 1; i < path.size(); i++) {
            configElement = configElement.getChild(path.get(i));
            if (configElement == null) {
                return null;
            }
        }
        return new ConfigTreeImpl(configElement);
    }

    /**
     * 通过键链读取字符串配置值。
     *
     * @param keychain 配置项的键链，从根节点到目标节点的路径
     * @return 配置项的字符串值
     * @throws NotConfiguredException 如果配置不存在或值为 null
     */
    @Override
    public @NotNull String readString(@NotNull List<@NotNull String> keychain) throws NotConfiguredException {
        @Nullable ConfigTreeImpl x = extract(keychain);
        if (x == null) {
            throw new NotConfiguredException(keychain);
        }
        String valueAsString = x.getRootNode().getValue();
        if (valueAsString == null) {
            throw new NotConfiguredException(keychain);
        }
        return valueAsString;
    }

    /**
     * 将本配置节点的各有效配置项转化为配置项列表。
     *
     * @return 配置项 {@link ConfigProperty} 列表
     */
    public @NotNull List<@NotNull ConfigProperty> transformChildrenToPropertyList() {
        List<@NotNull ConfigProperty> properties = new ArrayList<>();
        // 为了输出稳定，按字典序遍历同级子节点
        List<String> keys = new ArrayList<>(this.getRootNode().getChildren().keySet());
        Collections.sort(keys);
        for (String key : keys) {
            ConfigNode child = this.getRootNode().getChild(key);
            if (child != null) {
                dfsTransform(child, new ArrayList<>(List.of(key)), properties);
            }
        }
        return properties;
    }

    /**
     * 以给定的配置节点为根节点，执行深度优先搜索，对具有非空值的配置项构建清单，均以字典序编列。
     *
     * @param node 给定的配置节点
     * @param path 给定的配置节点在完整的配置树中的路径，自根节点到当前节点
     * @param out  通过遍历收集到的配置项将加入到这个 {@link ConfigProperty} 列表中
     */
    private void dfsTransform(@NotNull ConfigNode node,
                              @NotNull List<String> path,
                              @NotNull List<@NotNull ConfigProperty> out) {
        // 当前节点若有值，则输出一条属性
        if (node.getValue() != null) {
            out.add(new ConfigProperty()
                    .setKeychain(path)
                    .setValue(node.getValue()));
        }
        // 继续遍历子节点
        if (!node.getChildren().isEmpty()) {
            List<String> keys = new ArrayList<>(node.getChildren().keySet());
            Collections.sort(keys);
            for (String k : keys) {
                ConfigNode child = node.getChildren().get(k);
                if (child != null) {
                    List<String> nextPath = new ArrayList<>(path);
                    nextPath.add(k);
                    dfsTransform(child, nextPath, out);
                }
            }
        }
    }
}
