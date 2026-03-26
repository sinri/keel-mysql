# 语句执行与流式读取（`connection.target`）

包：`io.github.sinri.keel.integration.mysql.connection.target`。

本包将 **`AnyStatement` + `SqlConnection`** 组合为**可执行的 Future 操作**，并区分读 / 写 / 分页 / 流式。

## 类层次（概念）

- **`AnyStatementWithSqlConnection`**：持有语句与连接、审计 UUID、绑定 **`StatementAuditorHolder`
  **（见 [exception_and_audit.md](./exception_and_audit.md)）。
- **`RunnableStatement`**：**`execute()` → `StatementExecuteResult`**；根据语句的 **`isToPrepareStatement()`** 选择 *
  *`query(sql)`** 或 **`preparedQuery(sql)`**（无参数绑定）。
- **`RunnableStatementForRead`**：在 `execute()` 之上提供 *
  *`executeForOneRow` / `executeForRowList` / `executeForResultMatrix` / `executeForCategorizedMap` /
  `executeForUniqueKeyBoundMap`** 等。
- **`RunnableStatementForReadAndPagination`**：**`executeForPagination(pageNo, pageSize)`
  **（见 [result_pagination_and_mapping.md](./result_pagination_and_mapping.md)）。
- **`RunnableStatementForModify`**：**`executeForAffectedRows()`**。
- **`RunnableStatementForWrite`**（extends Modify）：**`executeForLastInsertedID()`**（依赖 MySQL 协议属性 *
  *`LAST_INSERTED_ID`**）。
- **`StreamableStatement`**：需要 **`Keel`** 实例；**`streamRead(ResultStreamReader, batch)`** 使用 **`prepare` + `cursor`
  ** 批量拉取。

## 如何得到这些类型

| 来源                                                                        | 返回类型                                                                |
|---------------------------------------------------------------------------|---------------------------------------------------------------------|
| **`RunnableStatementFactory#select`**                                     | `RunnableStatementForReadAndPagination`（与 `pagination(...)` 相同附着类型） |
| **`union` / `templatedRead`**                                             | `RunnableStatementForRead`                                          |
| **`update` / `delete` / `templatedModify` / 多数 DDL**                      | `RunnableStatementForModify`                                        |
| **`insert` / `replace`**                                                  | `RunnableStatementForWrite`                                         |
| **`call` / `truncateTable` / 等**                                          | `RunnableStatement` 或 Modify，以实现为准                                  |
| **`ReadStatementMixin#attachToConnectionForStream(keel, sqlConnection)`** | `StreamableStatement`                                               |

`SelectStatement` 等实现 **`ReadStatementMixin`** 时，可 **`attachToConnectionForStream`** 做游标流。

## `StreamableStatement` 与 `ResultStreamReader`

- **`streamRead(reader)`** 默认 batch 为 1；**`streamRead(reader, batch)`** 可加大窗口。
- 内部 **`asyncCallRepeatedly`**：无更多行时 **`stop()`**；否则 **`cursor.read(batch)`** 后对行集 **`asyncCallIteratively`
  ** 调用 **`ResultStreamReader#read(Row)`**。
- **`ResultStreamReader`** 还提供静态 **`mapRowToEntity` / `mapRowToResultRow`
  **（见 [result_pagination_and_mapping.md](./result_pagination_and_mapping.md)）。

## 与 `instantQueryForStream` 的对比

| 方式                                             | 连接 / 池                                            |
|------------------------------------------------|---------------------------------------------------|
| `KeelMySQLConfiguration#instantQueryForStream` | 自建 **pool**，方法内关闭                                 |
| `StreamableStatement`                          | 使用**已有** `SqlConnection`（通常来自池内 `withConnection`） |

大结果集在应用内优先：**池化连接 + `StreamableStatement`**；工具脚本可考虑 **`instantQueryForStream`**。

## 预编译与原始协议

**`RunnableStatement.execute()`** 完全由语句的 **`isToPrepareStatement()`** 决定走 **`preparedQuery` 还是 `query`**。

- **`RawStatement(..., true)`** 与 **`rawForPreparedQuery`**：预编译路径，但 **SQL 中不得含 `?`**（当前实现无绑定参数）。
- **`rawForDirectQuery`**：普通查询协议。

详见 [raw_and_templated_sql.md](./raw_and_templated_sql.md)。
