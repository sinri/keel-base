# JavaDoc 术语表与写作约定

## 目标

- 本文件用于统一本仓库中 JavaDoc 的术语写法与表达口径，避免同一概念出现多种表述。
- JavaDoc **使用中文**，专有名词（如 `Vert.x`、`Verticle`、`Future`、`Promise`）保留英文写法；如需解释，建议在首次出现时用中文括注说明。
- 使用 `<p>` 分段，但**不要书写** `</p>`。
- `@since` **仅允许出现在类型级别**（class/interface/enum/record）的 JavaDoc 中；方法、字段、`package-info.java` 不写
  `@since`。

## 术语规范

- **Keel**
    - 推荐：**Keel 体系**
    - 说明：用于描述 Keel 相关能力与组件集合，例如“Keel 体系下的标准 Verticle 基类”。

- **Vert.x**
    - 写法：`Vert.x`
    - 说明：指 Vert.x 平台整体时使用 `Vert.x`；指具体类型时使用其类名（例如 `Vertx`、`Context`、`DeploymentOptions`）。

- **Verticle**
    - 写法：`Verticle`
    - 可选（首次出现）：`Verticle（部署单元）`

- **部署 ID**
    - 写法：**部署 ID**
    - 说明：避免 “部署ID/Deployment ID” 等混用；如确需英文，可在首次出现时写“部署 ID（deployment ID）”。

- **配置 / configuration / config**
    - `configuration`：统一译为“**配置管理**”（谈“机制/体系/模块”时）
    - `config`：统一译为“**配置**”（谈“配置对象/配置内容/配置项”时）

- **Future / Promise**
    - 写法：保留 `Future`、`Promise`
    - 可选（首次出现）：`Future（异步结果）`、`Promise（异步承诺）`

