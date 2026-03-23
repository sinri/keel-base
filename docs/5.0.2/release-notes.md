---
layout: default
title: keel-base 5.0.2 版本说明
---

# keel-base 5.0.2 版本说明

本文档面向使用 keel-base 库的开发者，说明 5.0.2 版本中引入的新特性、行为变更与缺陷修复。

## 依赖升级

| 依赖              | 5.0.1  | 5.0.2  |
|-----------------|--------|--------|
| Vert.x          | 5.0.7  | 5.0.8  |
| Jackson         | 2.18.2 | 2.18.6 |
| keel-logger-api | 5.0.0  | 5.0.1  |

---

## 新特性

### 1. `JsonObjectMappedBean` — Bean 与 JsonObject 双向映射

**包路径**: `io.github.sinri.keel.base.json.JsonObjectMappedBean`

> 标注为 `@TechnicalPreview(since = "5.0.2")`，API 可能在后续版本调整。

提供基于反射的 Java Bean 与 Vert.x
`JsonObject` 之间的自动双向映射能力。实现该接口的 Bean 类只需遵循标准 getter/setter 命名规范，即可获得序列化与反序列化能力。

#### 命名规则

- **Bean → JSON**：getter 方法名去掉 `get`/`is` 前缀后，转换为 snake_case 作为 JSON 键名。
    - `getUserName()` → `"user_name"`
    - `isActive()` → `"active"`
    - `getHTTPResponse()` → `"http_response"`
- **JSON → Bean**：JSON 键名（snake_case 或 camelCase）转换为 `setXxx()` setter 方法名进行赋值。

#### 基本用法

```java
// 定义 Bean
public class UserBean implements JsonObjectMappedBean {
    private String userName;
    private int userId;
    private boolean active;

    // 标准 getter/setter ...
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

// Bean → JsonObject
UserBean bean = new UserBean();
bean.

setUserName("Alice");
bean.

setUserId(42);
bean.

setActive(true);

JsonObject json = bean.toJsonObject();
// {"user_name": "Alice", "user_id": 42, "active": true}

// JsonObject → Bean
UserBean restored = new UserBean();
restored.

reloadData(json);
// restored.getUserName() == "Alice"
```

#### 嵌套对象支持

当 JSON 值为 `JsonObject` 时，`reloadData` 会根据 setter 参数类型自动处理：

| setter 参数类型                            | 行为                                           |
|----------------------------------------|----------------------------------------------|
| `JsonObject`                           | 直接赋值                                         |
| 实现 `JsonObjectReloadable` 的类型          | 通过无参构造函数实例化后调用 `reloadData()`                |
| 继承 `UnmodifiableJsonifiableEntity` 的类型 | 通过 `UnmodifiableJsonifiableEntity.wrap()` 创建 |

当 JSON 值为 `JsonArray` 时：

| setter 参数类型 | 行为         |
|-------------|------------|
| `JsonArray` | 直接赋值       |
| `List` 或其子类 | 创建列表并逐元素拷贝 |

#### 支持的基本类型转换

`reloadData` 自动处理以下类型转换：`String`、`int`/`Integer`、`long`/`Long`、`double`/`Double`、`float`/`Float`、`boolean`/
`Boolean`。JSON 中的 `null` 值会被跳过（保留 Bean 中的原有值）。

---

### 2. `asyncCallExclusively` 锁获取失败处理

**包路径**: `io.github.sinri.keel.base.async.KeelAsyncMixinLock`（通过 `Keel` 接口暴露）

新增一个四参数重载，允许在获取锁失败时自定义恢复逻辑，而非直接返回失败的 Future。同时引入
`LockAcquireFailedException` 类型化异常，方便区分锁超时与其他错误。

#### API 概览

```java
// 新增：带失败处理器的完整版本
<T> Future<T> asyncCallExclusively(
        String lockName,
        long waitTimeForLock,
        Supplier<Future<T>> exclusiveSupplier,
        Function<LockAcquireFailedException, Future<T>> lockAcquireFailedHandleSupplier
);

// 已有：锁失败时直接返回 failedFuture（行为不变）
<T> Future<T> asyncCallExclusively(String lockName, long waitTimeForLock, Supplier<Future<T>> exclusiveSupplier);

// 已有：默认 1 秒超时（行为不变）
<T> Future<T> asyncCallExclusively(String lockName, Supplier<Future<T>> exclusiveSupplier);
```

#### 用法示例

```java
// 锁获取失败时返回默认值，而非抛出异常
keel.asyncCallExclusively(
    "resource-lock",
            500L,
            () ->

loadResource(),

lockFailure ->{
        logger.

warning("Lock acquire failed: "+lockFailure.getMessage());
        return Future.

succeededFuture(defaultValue);
    }
            );
```

> 原有的两参数和三参数重载行为完全不变，现有代码无需修改。

---

## 缺陷修复

### `KeelImpl.shared()` 错误信息修正

调用 `Keel.shared()` 时若全局 Keel 尚未初始化，现在会抛出含正确描述的 `IllegalStateException`：

- **修复前**: `"Shared Keel has been initialized"`（错误，与实际情况相反）
- **修复后**: `"Shared Keel has not been initialized yet"`

### `KeelImpl.ensureShared()` 线程安全修复

`ensureShared(Supplier)` 在并发场景下存在竞态条件，多个线程可能同时创建实例。现已使用
`AtomicReference.compareAndSet` 保证原子性，确保全局只有一个 Keel 实例被创建和使用。

### `ConfigElement.loadLocalPropertiesFile()` 资源泄漏修复

`FileReader` 和 fallback 路径中的 `InputStream` 现在均使用 try-with-resources 确保正确关闭，消除了文件句柄泄漏风险。

### `ConfigElement` 拷贝构造函数改为防御性拷贝

`new ConfigElement(anotherElement)` 现在会深拷贝 `parentKeyChain` 和 `children`，修改副本不再影响原对象。

**升级注意**：如果现有代码依赖拷贝构造函数后两个对象共享同一子节点映射的行为，需要调整。

### `KeelVerticleBase.stop()` 不再导致 `undeployed()` 永久挂起

当 `stopVerticle()` 返回失败的 Future 时，
`undeployed()` 返回的 Future 现在会正确地以失败态完成，而非永远处于 pending 状态。同时 `runningState` 在任何情况下都会转为
`AFTER_RUNNING`。

### `KeelVerticleBase.runningState` 增加 `volatile`

该字段现在声明为 `volatile`，保证跨线程读写的可见性。

### `QueuedLogWriterAdapter` 移除调试输出

`startVerticle()` 中残留的三处 `System.out.println()` 调试语句已移除。

### `KeelAsyncMixinCore.asyncSleep` 防止双重完成异常

当使用 interrupter 提前中断 sleep 时，定时器回调不再因重复完成 Promise 而抛出异常（`complete()` 改为 `tryComplete()`）。

### `ConfigElement.tryToExtract` 性能优化

`tryToExtract(List<String>)` 不再通过捕获 `NotConfiguredException` 来返回 `null`，而是直接遍历子节点链，在频繁查询场景下避免异常创建的开销。

---

## 迁移指南

本版本对现有 API 保持向后兼容。升级到 5.0.2 无需修改现有代码，但请注意以下事项：

1. **`ConfigElement` 拷贝构造函数**：如果依赖于拷贝后共享子节点引用的行为（通常不应该），需要调整为显式共享。
2. **`asyncCallExclusively` 三参数重载**：行为不变，但内部实现已变为委托给新的四参数重载。如果通过反射调用或做方法引用缓存，请确认无影响。
3. **依赖版本**：Vert.x 升至 5.0.8、Jackson 升至 2.18.6，请确认下游项目不存在版本冲突。
