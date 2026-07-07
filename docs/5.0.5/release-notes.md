---
layout: default
title: keel-base 5.0.5 版本说明
---

# keel-base 5.0.5 版本说明

本文档面向使用 `keel-base` 库的开发者，说明 5.0.5 相对 5.0.2 文档基线引入的依赖升级、API 调整与行为变更。

## 依赖升级

| 依赖              | 5.0.2  | 5.0.5  |
|-----------------|--------|--------|
| Vert.x          | 5.0.8  | 5.1.3  |
| Jackson         | 2.18.6 | 2.21.4 |
| keel-logger-api | 5.0.1  | 5.0.3  |
| JSpecify        | 1.0.0  | 1.0.0  |

> 当前分支版本号为 `5.0.5-SNAPSHOT`。发布正式版时应将 `gradle.properties` 中的 `version` 切换为 `5.0.5`。

---

## 新特性

### 1. `ConfigElement.readPropertyRequired`

**包路径**: `io.github.sinri.keel.base.configuration.ConfigElement`

5.0.5 为点号分隔的 properties 键名新增必填读取方法，减少调用方手动拆分键链的样板代码。

```java
ConfigElement root = ConfigElement.root();
root.

loadPropertiesFile("config.properties");

String host = root.readPropertyRequired("database.host");
```

当目标配置不存在或值为 `null` 时，该方法会抛出 `NotConfiguredException`，语义与 `readString(List<String>)` 一致。

同时新增带 formatter 的重载：

```java
int port = root.readPropertyRequired(
        "database.port",
        value -> value == null ? 3306 : Integer.parseInt(value)
);
```

该重载会在配置缺失时向 formatter 传入 `null`，适合集中处理默认值或自定义类型转换。

### 2. `QueuedLogWriterAdapter.restMs()`

**包路径**: `io.github.sinri.keel.base.logger.adapter.QueuedLogWriterAdapter`

队列化日志写入器新增可覆写的空闲等待间隔方法：

```java

@Override
private long restMs() {
    return 50L;
}
```

默认值仍为 `100L`。当日志队列暂时没有待处理记录时，循环会按该间隔休眠；已有实现不覆写该方法时行为不变。

### 3. 发布流程清理任务

构建脚本新增 `cleanMavenCentralStaging` 任务，并让 `publishMavenJavaPublicationToMavenRepository` 依赖它。发布前会清理：

- `build/staging-deploy`
- `build/jreleaser/deploy`

这样可以避免旧 staging 产物混入新的发布流程。

---

## 行为变更与弃用

### 1. 阻塞桥接 API 标记为废弃

以下 API 从 5.0.5 起标记为 `@Deprecated(since = "5.0.5", forRemoval = true)`：

- `KeelAsyncMixinBlock.asyncTransformRawFuture(java.util.concurrent.Future<R>)`
- `KeelAsyncMixinBlock.asyncTransformRawFuture(java.util.concurrent.Future<R>, long)`
- `KeelAsyncMixinBlock.blockAwait(Future<T>)`

推荐替代方式：

```java
// 包装阻塞调用，保持 Vert.x worker pool 调度语义
Future<String> future = keel.executeBlocking(() -> rawFuture.get());

// 阻塞等待 Vert.x Future 时，优先使用 Vert.x 5 原生 await API
String result = someFuture.await();
```

`asyncTransformRawFuture(rawFuture, sleepTime)` 中的 `sleepTime` 实际表示轮询间隔而不是超时时间，语义容易误解。如需超时，应由调用方显式使用
`rawFuture.get(timeout, unit)` 并通过 `executeBlocking` 包装。

### 2. `JsonObjectMappedBean` List 转换边界收敛

`JsonObjectMappedBean` 内部将 JSON array 转换为 `List` 时，统一通过
`createListInstance` 创建目标列表实例，并把 unchecked cast 收敛到单一边界。该调整不改变公开 API，但减少编译期 unchecked 转换噪音，也让异常信息更明确。

### 3. `QueuedLogWriterAdapter` 空闲休眠可配置

日志队列处理循环不再硬编码 `100L` 毫秒休眠，而是调用 `restMs()`。默认行为保持一致；如果子类覆写该方法，空闲轮询频率会随之变化。

---

## 迁移指南

升级到 5.0.5 时，普通调用方通常只需要更新依赖版本。建议检查以下事项：

1. **阻塞桥接调用**：如果代码仍使用 `asyncTransformRawFuture` 或 `blockAwait`，迁移到 `executeBlocking` 或 Vert.x 5
   `Future.await()`。
2. **配置读取**：新增代码优先使用 `readPropertyRequired("a.b.c")` 表达必填配置；保留 `readProperty("a.b.c")` 用于可选配置。
3. **日志队列调优**：对日志写入延迟敏感的实现可覆写 `QueuedLogWriterAdapter.restMs()`；吞吐优先场景可保留默认值。
4. **依赖兼容性**：Vert.x 升至 5.1.3、Jackson 升至 2.21.4，请确认下游项目不存在传递依赖冲突。
