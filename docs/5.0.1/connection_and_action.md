# 命名连接与动作模式（`connection` / `action`）

## 核心类型

### `NamedMySQLConnection`

包：`io.github.sinri.keel.integration.mysql.connection`。

组合能力：

- **`RunnableStatementFactory`**：在此连接上构建 `select` / `update` /
  `rawForPreparedQuery` 等（见 [execution_and_stream.md](./execution_and_stream.md)）。
- **`MySQLServerVersionMixin`**：访问服务端版本相关能力（若数据源已注入版本字符串）。
- **`Closeable`**：默认委托 **`asyncClose()` → getSqlConnection().close()`**。

常用默认方法：**`isForTransaction()`**、**`beginTransaction` / `commitTransaction` / `rollbackTransaction`**（基于 Vert.x *
*`SqlConnection`** 上的事务对象）。

**实现方式**：

- 库内置 **`DynamicNamedMySQLConnection`**：仅持有数据源名字符串，适合 `loadDynamic`。
- 业务可 **`extends AbstractNamedMySQLConnection`**：传入 **`SqlConnection`**，可附加领域方法；**`getMysqlVersion()`
  ** 由数据源在出池时注入。

### `SqlConnectionHolder`

仅暴露 **`getSqlConnection()`**，供语句在 **attach** 阶段绑定连接。

## 动作模式（`action.single` / `action.mix`）

用于把「**一条业务用例**」与**同一命名连接**绑定，并**复用**语句工厂，便于分层与测试。

### `NamedActionInterface<C extends NamedMySQLConnection>`

- **`extends RunnableStatementFactory`**，且 **`getSqlConnection()`** 默认转发到 *
  *`getNamedSqlConnection().getSqlConnection()`**。
- 文档约定：**不要在 Action 内部自行开启/提交事务**，应由上层（如 `NamedMySQLDataSource.withTransaction`）统一管理。

### `AbstractNamedAction<C>`

构造函数注入 **`C namedSqlConnection`**，实现 **`getNamedSqlConnection()`**。

### Mixin 变体（`action.mix`）

- **`NamedActionMixinInterface<C, W> extends SelfInterface<W>, RunnableStatementFactory`**：适合「链式 / 流式 API」返回 *
  *`this`** 或包装类型 **`W`** 的风格。
- **`AbstractNamedMixinAction<C, W>`**：同样持有 **`C`**。

**典型用法**：在 `withConnection` 或 `withTransaction` 回调里 `new OrderAction(conn).select(...).execute()`，或将
`conn` 传入仓储构造函数。

## 与语句构建的关系

- **推荐**：`namedConnection.select(s -> s.from("t").where(...)).execute()`（连接已由工厂绑定）。
- **等价**：`new SelectStatement()....attachToConnection(namedConnection.getSqlConnection()).execute()`。

二者最终都落到 **`connection.target`** 包中的可执行类型（见下一篇文档）。
