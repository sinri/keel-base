# keel-base 5.0.2 使用指南

本文档面向使用 `keel-base` 库的开发者，系统介绍 5.0.2 版本的模块设计、核心 API 及典型使用方式。

---

## 目录

- [概述与引入](#概述与引入)
- [模块总览](#模块总览)
- [快速开始](#快速开始)
- [Keel 核心与异步能力](#keel-核心与异步能力)
    - [创建与共享 Keel 实例](#创建与共享-keel-实例)
    - [异步睡眠](#异步睡眠)
    - [异步循环与迭代](#异步循环与迭代)
    - [并行执行](#并行执行)
    - [独占锁](#独占锁)
    - [阻塞转异步](#阻塞转异步)
- [JSON 数据体系](#json-数据体系)
    - [接口层次](#接口层次)
    - [只读实体 UnmodifiableJsonifiableEntity](#只读实体-unmodifiablejsonifiableentity)
    - [可读写实体 JsonifiableDataUnit](#可读写实体-jsonifiabledataunit)
    - [Bean 映射 JsonObjectMappedBean（技术预览）](#bean-映射-jsonobjectmappedbean技术预览)
    - [异常 JSON 化 JsonifiedThrowable](#异常-json-化-jsonifiedthrowable)
    - [Jackson 序列化集成](#jackson-序列化集成)
- [配置管理](#配置管理)
    - [加载 properties 文件](#加载-properties-文件)
    - [配置树操作](#配置树操作)
    - [生成 properties 文件](#生成-properties-文件)
- [日志体系](#日志体系)
    - [初始化 Vert.x 日志](#初始化-vertx-日志)
    - [标准输出日志](#标准输出日志)
    - [文件日志](#文件日志)
    - [指标记录器](#指标记录器)
- [Verticle 体系](#verticle-体系)
    - [KeelVerticleBase 基类](#keelverticlebase-基类)
    - [Verticle 生命周期](#verticle-生命周期)
    - [快捷包装 Verticle](#快捷包装-verticle)
- [注解](#注解)
- [附录：依赖版本](#附录依赖版本)

---

## 概述与引入

`keel-base` 是 Keel 体系的基础模块，基于 Vert.x 5 构建，提供异步能力封装、JSON 数据实体、分层配置管理、日志体系和 Verticle 基类等基础设施。

### Maven / Gradle 引入

**Gradle (Kotlin DSL)：**

```kotlin
dependencies {
    implementation("io.github.sinri:keel-base:5.0.2")
}
```

**Maven：**

```xml

<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>keel-base</artifactId>
    <version>5.0.2</version>
</dependency>
```

### 环境要求

- **JDK**：17+（虚拟线程相关能力需要 JDK 21+）
- **Vert.x**：5.0.8
- **Jackson**：2.18.6

### Java 模块系统

`keel-base` 使用 JPMS，模块名为 `io.github.sinri.keel.base`。在 `module-info.java` 中添加：

```java
requires io.github.sinri.keel.base;
```

---

## 模块总览

| 包                                          | 说明                                      |
|--------------------------------------------|-----------------------------------------|
| `io.github.sinri.keel.base.async`          | Keel 核心接口与异步能力（循环、并行、锁、阻塞转异步）           |
| `io.github.sinri.keel.base.json`           | JSON 数据实体读写、转换、序列化                      |
| `io.github.sinri.keel.base.configuration`  | 分层配置管理（`.properties` 加载、配置树、类型读取）       |
| `io.github.sinri.keel.base.logger.logger`  | 日志记录器（标准输出、特定日志）                        |
| `io.github.sinri.keel.base.logger.factory` | 日志工厂（标准输出工厂、文件日志工厂）                     |
| `io.github.sinri.keel.base.logger.adapter` | 日志写入适配器（即时输出、队列化文件写入）                   |
| `io.github.sinri.keel.base.logger.metric`  | 定量指标记录器                                 |
| `io.github.sinri.keel.base.verticles`      | Verticle 标准基类与生命周期管理                    |
| `io.github.sinri.keel.base.annotations`    | 注解（`@TechnicalPreview`、`SelfInterface`） |

> `io.github.sinri.keel.base.internal` 为内部实现包，不对外暴露，请勿直接使用。

---

## 快速开始

以下示例演示了最基本的 Keel 初始化和异步调用：

```java
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.logger.factory.VertxLoggerDelegateFactoryWorker;
import io.vertx.core.Vertx;

public class QuickStart {
    static void main(String[] args) {
        // 1. 确保 Vert.x 日志提供者正确配置（必须在加载任何 Vert.x 类之前）
        VertxLoggerDelegateFactoryWorker.ensureProperty();

        // 2. 创建 Keel 实例并设为全局共享
        Vertx vertx = Vertx.vertx();
        Keel keel = Keel.create(vertx);
        Keel.share(keel);

        // 3. 使用异步能力
        keel.asyncSleep(1000)
            .onSuccess(v -> System.out.println("1 秒后执行"))
            .onFailure(Throwable::printStackTrace);
    }
}
```

---

## Keel 核心与异步能力

`Keel` 接口是本库的异步能力入口，它继承了 Vert.x 的 `Vertx` 接口，同时混入了一组异步 Mixin 能力。

### 创建与共享 Keel 实例

```java
// 从 Vertx 实例创建
Keel keel = Keel.create(vertx);

// 设为全局共享（后续可通过 Keel.shared() 获取）
Keel.

share(keel);

// 也可直接从 Vertx 实例共享
Keel.

share(vertx);

// 获取全局共享实例（未初始化时抛出 IllegalStateException）
Keel shared = Keel.shared();

// 懒初始化：若尚未共享则创建，否则返回现有实例（线程安全）
Keel safe = Keel.ensureShared(() -> Keel.create(Vertx.vertx()));
```

> `ensureShared` 使用 `AtomicReference.compareAndSet` 保证并发安全，多线程同时调用也只会创建一个实例。

### 异步睡眠

非阻塞地等待一段时间，不会阻塞线程：

```java
// 睡眠 500 毫秒
keel.asyncSleep(500).

onSuccess(v ->{ /* 500ms 后执行 */ });

// 带可中断的睡眠
Promise<Void> interrupter = Promise.promise();
keel.

asyncSleep(5000,interrupter)
    .

onSuccess(v ->{ /* 定时到达或被中断后执行 */ });

        // 在其他地方提前中断睡眠
        interrupter.

complete();
```

### 异步循环与迭代

#### 异步重复调用

`asyncCallRepeatedly` 是所有循环类 API 的基础。它持续执行一个异步逻辑，直到显式调用 `stop()`：

```java
keel.asyncCallRepeatedly(task ->{
        return

doSomethingAsync()
        .

compose(result ->{
        if(

shouldStop(result)){
        task.

stop();
            }
                    return Future.

succeededFuture();
        });
                });
```

#### 异步迭代

对集合中的元素逐个执行异步操作（串行，非并行）：

```java
List<String> urls = List.of("url1", "url2", "url3");

// 逐个处理
keel.

asyncCallIteratively(urls, url ->

fetchAsync(url));

        // 逐个处理并支持提前终止
        keel.

asyncCallIteratively(urls, (url, task) ->{
        if("stop".

equals(url)){
        task.

stop();
        return Future.

succeededFuture();
    }
            return

fetchAsync(url);
});

        // 批量处理（每次取 10 个元素）
        keel.

asyncCallIteratively(urls.iterator(),batch ->

processBatch(batch), 10);
```

#### 异步步进循环

类似 `for (long i = start; i < end; i += step)` 的异步版本：

```java
// 从 0 到 100，步长 10
keel.asyncCallStepwise(0,100,10,(i, task) ->{
        return

processPage(i);
});

        // 简化：执行 5 次（i = 0, 1, 2, 3, 4）
        keel.

asyncCallStepwise(5,i ->

processItem(i));
```

#### 无限循环

持续执行异步逻辑，即使循环体抛出异常也不停止：

```java
keel.asyncCallEndlessly(() ->

pollAndProcess());
```

### 并行执行

对集合中的所有元素同时触发异步操作：

```java
List<String> tasks = List.of("a", "b", "c");

// 全部成功才算成功
keel.

parallelForAllSuccess(tasks, t ->

processAsync(t));

        // 任一成功即算成功
        keel.

parallelForAnySuccess(tasks, t ->

processAsync(t));

        // 等待全部完成（不论成功失败）
        keel.

parallelForAllComplete(tasks, t ->

processAsync(t));
```

### 独占锁

基于 Vert.x 的 SharedData 锁机制，在分布式或并发场景下保证某段逻辑独占执行：

```java
// 基本用法：默认 1 秒超时
keel.asyncCallExclusively("my-lock",() ->

criticalOperation());

        // 自定义超时
        keel.

asyncCallExclusively("my-lock",5000L,() ->

criticalOperation());

        // 5.0.2 新增：锁获取失败时自定义处理（而非直接抛异常）
        keel.

asyncCallExclusively(
    "my-lock",
            500L,
            () ->

criticalOperation(),

lockFailure ->{
        // 锁获取失败时的降级逻辑
        return Future.

succeededFuture(defaultValue);
    }
            );
```

> `LockAcquireFailedException` 是 5.0.2 新增的类型化异常，方便在 `catch` 或 `onFailure` 中区分锁超时与其他错误。

### 阻塞转异步

#### CompletableFuture 转 Vert.x Future

```java
CompletableFuture<String> cf = someExternalApi();
Future<String> future = keel.asyncTransformCompletableFuture(cf);
```

#### java.util.concurrent.Future 转 Vert.x Future

```java
java.util.concurrent.Future<String> rawFuture = executor.submit(() -> "result");

// 方式一：直接转换（在 EventLoop 中会自动使用 executeBlocking）
Future<String> f1 = keel.asyncTransformRawFuture(rawFuture);

// 方式二：轮询方式转换（指定轮询间隔）
Future<String> f2 = keel.asyncTransformRawFuture(rawFuture, 100L);
```

#### 在虚拟线程中运行（JDK 21+）

```java
keel.runInVerticleOnVirtualThread(() ->{
        // 此处运行在虚拟线程中
        return

longRunningComputation();
});
```

#### 阻塞等待异步结果

> **警告**：严禁在 EventLoop 线程中调用。仅适用于 Worker 线程、虚拟线程或普通线程。

```java
String result = keel.blockAwait(someAsyncOperation());
```

---

## JSON 数据体系

`io.github.sinri.keel.base.json` 包提供了一套围绕 Vert.x `JsonObject` 的数据实体接口层次，支持类型安全的读取、写入、序列化和双向映射。

### 接口层次

```
JsonSerializable                 -- toJsonExpression() / toFormattedJsonExpression()
├── JsonObjectConvertible        -- toJsonObject()
│   └── JsonObjectMappedBean     -- Bean ↔ JsonObject 双向映射（5.0.2 新增，技术预览）
│
JsonObjectReadable               -- read() 系列，基于 JSON Pointer 的类型安全读取
├── JsonObjectWritable           -- ensureEntry() / removeEntry()
│   └── JsonifiableDataUnit      -- 可读写+可序列化+可重载的完整数据实体
│       └── JsonifiableDataUnitImpl  -- 默认实现（推荐继承）
│
├── UnmodifiableJsonifiableEntity    -- 只读 JSON 实体
│   └── UnmodifiableJsonifiableEntityImpl  -- 默认实现（推荐继承）
│
JsonObjectReloadable             -- reloadData(JsonObject)
```

### 只读实体 UnmodifiableJsonifiableEntity

适合对外部传入的 JSON 数据做只读封装，保证数据不被意外修改：

```java
public class UserProfile extends UnmodifiableJsonifiableEntityImpl {
    public UserProfile(JsonObject jsonObject) {
        super(jsonObject);
    }

    // 可选：在构造时过滤敏感字段
    @Override
    protected JsonObject purify(JsonObject raw) {
        raw.remove("password");
        return raw;
    }

    public String getName() {
        return readStringRequired("name");
    }

    public Integer getAge() {
        return readInteger("age");
    }

    public List<String> getTags() {
        return readStringArrayRequired("tags");
    }
}

// 使用
JsonObject raw = new JsonObject().put("name", "Alice").put("age", 30);
UserProfile profile = new UserProfile(raw);
String name = profile.getName();        // "Alice"
Integer age = profile.getAge();         // 30

// 也可通过静态方法快速包装
UnmodifiableJsonifiableEntity entity = UnmodifiableJsonifiableEntity.wrap(raw);
String nameValue = entity.readString("name");
```

#### JSON Pointer 读取

所有 `readXxx` 方法支持多级路径参数，等价于 JSON Pointer：

```java
// 对应 JSON: {"user": {"address": {"city": "Shanghai"}}}
String city = entity.readString("user", "address", "city");
```

`readXxx` 返回 `@Nullable`，`readXxxRequired` 在值为 null 时抛出 `NullPointerException`。

支持的类型：`String`、`Number`、`Integer`、`Long`、`Float`、`Double`、`Boolean`、`JsonObject`、`JsonArray`，以及数组变体如
`readStringArray`、`readJsonObjectArray` 等。

### 可读写实体 JsonifiableDataUnit

在只读能力之上增加写入、重载和集群序列化支持。适合需要动态构建或修改的数据对象：

```java
public class OrderInfo extends JsonifiableDataUnitImpl {
    public OrderInfo() {
        super();
    }

    public OrderInfo(JsonObject jsonObject) {
        super(jsonObject);
    }

    public String getOrderId() {
        return readStringRequired("order_id");
    }

    public OrderInfo setOrderId(String orderId) {
        ensureEntry("order_id", orderId);
        return this;
    }

    public double getAmount() {
        return readDoubleRequired("amount");
    }

    public OrderInfo setAmount(double amount) {
        ensureEntry("amount", amount);
        return this;
    }
}

// 使用
OrderInfo order = new OrderInfo();
order.

setOrderId("ORD-001").

setAmount(99.5);

JsonObject json = order.toJsonObject();      // {"order_id":"ORD-001","amount":99.5}
String encoded = order.toJsonExpression();    // 紧凑 JSON 字符串

// 从 JSON 重载
OrderInfo restored = new OrderInfo();
restored.

reloadData(json);
```

`JsonifiableDataUnit` 同时实现了 `ClusterSerializable`，可直接在 Vert.x 集群 SharedData 中传输。

### Bean 映射 JsonObjectMappedBean（技术预览）

> 标注为 `@TechnicalPreview(since = "5.0.2")`，API 可能在后续版本调整。

提供基于反射的 Java Bean 与 `JsonObject` 之间的自动双向映射。实现该接口的 Bean 类只需遵循标准 getter/setter 命名规范：

```java
public class UserBean implements JsonObjectMappedBean {
    private String userName;
    private int userId;
    private boolean active;

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
```

#### Bean → JsonObject

getter 方法名去掉 `get`/`is` 前缀后，转换为 snake_case 作为 JSON 键名：

| getter 方法           | JSON 键          |
|---------------------|-----------------|
| `getUserName()`     | `user_name`     |
| `isActive()`        | `active`        |
| `getHTTPResponse()` | `http_response` |

```java
UserBean bean = new UserBean();
bean.

setUserName("Alice");
bean.

setUserId(42);
bean.

setActive(true);

JsonObject json = bean.toJsonObject();
// {"user_name":"Alice","user_id":42,"active":true}
```

#### JsonObject → Bean

JSON 键名（snake_case 或 camelCase）被转换为对应的 `setXxx()` setter 方法进行赋值：

```java
UserBean restored = new UserBean();
restored.

reloadData(json);
// restored.getUserName() == "Alice"
```

#### 嵌套对象支持

当 JSON 值为 `JsonObject` 时，`reloadData` 根据 setter 参数类型自动处理：

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

支持的基本类型转换：`String`、`int`/`Integer`、`long`/`Long`、`double`/`Double`、`float`/`Float`、`boolean`/`Boolean`。JSON 中的
`null` 值会被跳过（保留 Bean 中的原有值）。

### 异常 JSON 化 JsonifiedThrowable

将 Java 异常转换为结构化的 JSON 表示，适合在日志中输出或通过网络传输：

```java
try{
riskyOperation();
}catch(
Exception e){
JsonifiedThrowable jt = JsonifiedThrowable.wrap(e);
    logger.

error("Operation failed",jt.toJsonObject());
        // 输出结构类似：
        // {"class":"java.io.IOException","message":"...","stack":[...],"cause":{...}}
        }
```

`wrap` 方法支持自定义可忽略的堆栈包集合，用于精简输出中的框架层堆栈。

### Jackson 序列化集成

要让 Jackson 正确序列化所有 `JsonSerializable` 的子类（如将
`JsonifiableDataUnitImpl` 嵌入其他 Jackson 序列化的对象中），需要在程序启动时注册序列化器：

```java
JsonifiableSerializer.register();
```

此调用是幂等的，多次调用不会产生副作用。

---

## 配置管理

`io.github.sinri.keel.base.configuration` 包提供基于 `.properties` 文件的分层配置管理。配置项以 `.` 分隔的键名组织为树形结构。

### 加载 properties 文件

```java
// 方式一：使用全局根节点
ConfigElement root = ConfigElement.root();
root.

loadPropertiesFile("config.properties");

// 方式二：创建独立的配置节点
ConfigElement myConfig = new ConfigElement("app");
myConfig.

loadPropertiesFile("app.properties");
```

`loadPropertiesFile` 会先尝试从文件系统读取；若失败，则回退到 ClassPath 资源中查找。

### 配置树操作

假设 `config.properties` 内容：

```properties
database.host=localhost
database.port=3306
database.name=mydb
app.debug=true
app.maxRetries=3
```

加载后可按键链提取和读取：

```java
ConfigElement root = ConfigElement.root();
root.

loadPropertiesFile("config.properties");

// 提取子树
ConfigElement dbConfig = root.extract("database");
String host = dbConfig.readString(List.of("host"));       // "localhost"
int port = dbConfig.readInteger(List.of("port"));          // 3306

// 安全提取（不存在时返回 null 而非抛异常）
ConfigElement optional = root.tryToExtract("nonexistent");  // null

// 直接通过点号分隔的键名读取
String debug = root.readProperty("app.debug");              // "true"

// 类型读取
boolean isDebug = root.extract("app").readBoolean(List.of("debug"));  // true
int retries = root.extract("app").readInteger(List.of("maxRetries")); // 3

// 动态修改配置
ConfigElement appConfig = root.ensureChild("app");
appConfig.

ensureChild("newKey").

setElementValue("newValue");

// 遍历所有配置项
List<ConfigProperty> allProps = root.transformChildrenToPropertyList();
for(
ConfigProperty prop :allProps){
        System.out.

println(prop.getPropertyName() +"="+prop.

getPropertyValue());
        }
```

> 当请求的配置项不存在时，`extract` 和 `readString` 等方法会抛出 `NotConfiguredException`。使用 `tryToExtract` 和
`tryToGetChild` 可避免异常。

### 生成 properties 文件

```java
ConfigPropertiesBuilder builder = new ConfigPropertiesBuilder();
builder.

add(List.of("database", "host"), "localhost")
        .

add(List.of("database", "port"), "3306")
        .

add("app.debug","true");

// 输出为字符串
String content = builder.writeToString();

// 写入文件
builder.

writeToFile("output.properties");

// 追加到已有文件
builder.

appendToFile("output.properties");
```

---

## 日志体系

keel-base 的日志体系建立在 `keel-logger-api` 之上，提供标准输出和文件两种日志输出方式，以及基于队列的异步日志写入机制。

### 初始化 Vert.x 日志

> **重要**：必须在任何 Vert.x 类被加载之前调用，通常放在 `main` 方法的第一行。

```java
VertxLoggerDelegateFactoryWorker.ensureProperty();
```

此方法检查系统属性 `vertx.logger-delegate-factory-class-name`，若未设置则默认使用 JUL，避免 Vert.x 自动探测失败导致初始化异常。

### 标准输出日志

使用 `StdoutLoggerFactory` 创建输出到标准输出的日志记录器：

```java
// 获取工厂单例
StdoutLoggerFactory factory = StdoutLoggerFactory.getInstance();

// 创建普通日志记录器
Logger logger = factory.createLogger("MyComponent");
logger.

info("应用启动完成");
logger.

warning("配置项缺失，使用默认值");
logger.

error("操作失败",throwable);

// 创建特定日志记录器（结构化日志）
SpecificLogger<MyLog> specificLogger = factory.createLogger("Audit", MyLog::new);
specificLogger.

log(log ->log.

setAction("login").

setUserId("u123"));
```

### 文件日志

通过继承 `FileLoggerFactory` 和 `FileLogWriterAdapter` 实现文件日志。`FileLogWriterAdapter` 基于
`QueuedLogWriterAdapter`，提供异步队列化写入，不阻塞业务线程。

```java
public class MyFileLogWriterAdapter extends FileLogWriterAdapter {
    private FileWriter writer;

    @Override
    protected Future<Void> prepareForLoop() {
        try {
            writer = new FileWriter("app.log", true);
            return Future.succeededFuture();
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    protected FileWriter getFileWriterForTopic(String topic) {
        return writer;
    }

    @Override
    public String render(String topic, SpecificLog<?> log) {
        return String.format("[%s] %s: %s", log.timestamp(), topic, log.message());
    }
}

public class MyFileLoggerFactory extends FileLoggerFactory {
    private final MyFileLogWriterAdapter adapter = new MyFileLogWriterAdapter();

    @Override
    public FileLogWriterAdapter sharedAdapter() {
        return adapter;
    }
}
```

由于 `QueuedLogWriterAdapter` 继承了 `KeelVerticleBase`，使用前需要将其作为 Verticle 部署：

```java
MyFileLoggerFactory loggerFactory = new MyFileLoggerFactory();
loggerFactory.

sharedAdapter()
    .

deployMe(keel, new DeploymentOptions())
        .

onSuccess(id ->{
Logger logger = loggerFactory.createLogger("App");
        logger.

info("文件日志已就绪");
    });
```

### 指标记录器

`AbstractMetricRecorder` 提供基于队列的异步指标记录能力，同样需要作为 Verticle 部署：

```java
public class MyMetricRecorder extends AbstractMetricRecorder {
    @Override
    protected Future<Void> prepareForLoop() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer) {
        // 批量写入指标存储
        return writeToStorage(buffer);
    }
}

// 部署并使用
MyMetricRecorder recorder = new MyMetricRecorder();
recorder.

deployMe(keel, new DeploymentOptions())
        .

onSuccess(id ->{
        recorder.

recordMetric(new MetricRecord("api_latency", 42.5));
        });
```

---

## Verticle 体系

### KeelVerticleBase 基类

`KeelVerticleBase` 是 Keel 体系下 Verticle 的标准基类，对 Vert.x 的 `Deployable` 进行了封装，提供：

- 自动将 `Vertx` 包装为 `Keel` 实例（通过 `getKeel()` 获取）
- 运行状态管理（`BEFORE_RUNNING` → `RUNNING` → `AFTER_RUNNING` / `DEPLOY_FAILED`）
- 卸载 Future（`undeployed()`）
- 标准化的生命周期回调

#### 自定义 Verticle

```java
public class MyWorker extends KeelVerticleBase {
    @Override
    protected Future<?> startVerticle() {
        // Verticle 启动逻辑
        getKeel().asyncCallEndlessly(() -> {
            return pollAndProcess();
        });
        return Future.succeededFuture();
    }

    @Override
    protected Future<?> stopVerticle() {
        // 可选：Verticle 停止时的清理逻辑
        return cleanup();
    }
}
```

### Verticle 生命周期

```
                    deployMe()
BEFORE_RUNNING ──────────────► RUNNING
                                 │
                          undeploy / 外部卸载
                                 │
                                 ▼
                           AFTER_RUNNING
                                 
            startVerticle() 失败
BEFORE_RUNNING ──────────────► DEPLOY_FAILED
```

部署和卸载：

```java
MyWorker worker = new MyWorker();

// 部署
worker.

deployMe(keel, new DeploymentOptions())
        .

onSuccess(deploymentId ->{
        System.out.

println("部署成功: "+deploymentId);

// 获取 Verticle 信息
JsonObject info = worker.getVerticleInfo();
    });

            // 卸载
            worker.

undeployMe()
    .

onSuccess(v ->System.out.

println("卸载完成"));

        // 等待卸载完成
        worker.

undeployed()
    .

onSuccess(v ->System.out.

println("Verticle 已完全停止"));
```

### 快捷包装 Verticle

对于简单场景，可通过 `wrap` 静态方法创建匿名 Verticle，无需定义子类：

```java
// 仅定义启动逻辑
KeelVerticleBase.wrap(verticle ->{
        verticle.

getKeel().

asyncCallEndlessly(() ->

doWork());
        return Future.

succeededFuture();
}).

deployMe(keel, new DeploymentOptions());

        // 同时定义启动和停止逻辑
        KeelVerticleBase.

wrap(
        verticle ->

initAndStartLoop(),

verticle ->

cleanupResources()
).

deployMe(keel, new DeploymentOptions());
```

---

## 注解

### @TechnicalPreview

标注处于技术预览阶段的类、方法或字段。API 可能在后续版本中发生变化，在生产环境中使用时需谨慎评估。

```java

@TechnicalPreview(since = "5.0.2", notice = "API 可能变更")
public interface SomeExperimentalApi {
}
```

### SelfInterface

提供链式调用的类型安全辅助接口：

```java
public class Builder implements SelfInterface<Builder> {
    public Builder setName(String name) {
        // ...
        return getImplementation();
    }
}
```

---

## 附录：依赖版本

| 依赖              | 版本             |
|-----------------|----------------|
| Vert.x          | 5.0.8          |
| Jackson         | 2.18.6         |
| keel-logger-api | 5.0.1          |
| JSpecify        | 编译时依赖          |
| JDK             | 17+（虚拟线程需 21+） |
