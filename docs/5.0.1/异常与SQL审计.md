# 异常与 SQL 审计（`exception` / `StatementAuditorHolder`）

## 异常类型

包：`io.github.sinri.keel.integration.mysql.exception`。

| 类型                                 | 典型场景                                                       |
|------------------------------------|------------------------------------------------------------|
| **`KeelMySQLConnectionException`** | 从池取连失败、事务 **`begin` 失败等**                                  |
| **`KeelMySQLException`**           | 在 **`withConnection` / `withTransaction`** 业务链路中失败，或回滚后的包装 |
| **`KeelSQLGenerateError`**         | SQL 生成阶段错误                                                 |
| **`KeelSQLResultRowIndexError`**   | 结果矩阵按索引/首行读取时无数据或越界                                        |

捕获时建议记录 **cause**（Vert.x / MySQL 驱动异常链），并根据是否可重试区分处理。

## SQL 审计：`StatementAuditorHolder`

类：`io.github.sinri.keel.integration.mysql.statement.StatementAuditorHolder`。

单例 **`getInstance()`**：

- 默认使用 **`SilentLoggerFactory`**，审计事件不产生输出。
- **`reloadSqlAuditLogger(LoggerFactory)`**：切换为实际日志工厂后，**`RunnableStatement.execute()`** 会在**执行前
  **记录 query/prepare 信息，**成功后**记录影响行数/获取行数，**失败**记录错误（具体字段见 **`MySQLAuditSpecificLog`**）。

集成步骤概要：

1. 在应用启动时（日志系统就绪后）调用 **`StatementAuditorHolder.getInstance().reloadSqlAuditLogger(yourLoggerFactory)`**。
2. 确保 **`SpecificLogger<MySQLAuditSpecificLog>`** 在所用 **`LoggerFactory`** 中能正确创建。

审计与业务日志分离，便于合规与排障；注意日志中可能含 **SQL 文本**，敏感数据需在上层脱敏。

## 与 DSL 安全说明的关系

条件、原始 SQL、模板替换均可能将**敏感或恶意内容
**写入 SQL；异常堆栈与审计日志可能暴露 SQL。生产环境应限制日志级别与字段脱敏（见 [DSL条件与DDL.md](./DSL条件与DDL.md)、[原始SQL与模板SQL.md](./原始SQL与模板SQL.md)）。
