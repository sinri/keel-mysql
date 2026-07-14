# 5.0.4 迁移与发布说明

本文按当前 `dev-5.0.4` 源码整理。当前候选版为 `5.0.4-rc.1`；正式发布后使用 `5.0.4`。

## 新增与完善

- 参数化执行：`RunnableStatement.executeThroughPrepare(Tuple)`，以及即时查询和流式即时查询的 `Tuple` 重载。
- 写批处理：`RunnableStatementForWrite.execute(Keel, List<Tuple>)` 使用 Vert.x `executeBatch`。
- 批结果：`StatementExecuteResult.getRowSets()` 暴露完整结果链，总影响/读取行数对全部结果求和。
- SSL/TLS：增加 `sslMode`、PEM 客户端证书/CA、JKS/PFX 信任库等配置。
- 连接池：增加 `poolIdleTimeout`（秒）。
- SQL 字面量：新增 `statement.quoter.Quoter` 与 `MySQLEscapeContext`，根据服务端会话的 `character_set_connection` 和 `sql_mode` 选择转义策略。
- 结果读取：`ResultRow.readJson(String)` 可返回 `JsonObject`、`JsonArray`、标量或 `null`。
- 条件 DSL：`AmongstCondition.amongstReadStatement` 支持 `IN (subquery)`。
- 可靠性：连接/事务回调同步抛错时仍释放连接；虚拟线程阻塞取连会验证当前线程；服务端会话信息初始化支持并发复用与失败重试。

## 不兼容行为与废弃 API

5.0.4 将执行协议选择从语句对象移到执行对象。以下签名仅为迁移期保留，调用时会直接抛出 `UnsupportedOperationException`：

| 旧 API | 替代方式 |
|---|---|
| `AnyStatement#setToPrepareStatement` / `isToPrepareStatement` | 在 `RunnableStatement` 上选择 `executeThroughPrepare...` 或 `executeThroughQuery()` |
| `new RawStatement(sql, boolean)` | `new RawStatement(sql)`，执行时选择协议 |
| `rawForDirectQuery(sql)` | `rawForPreparedQuery(sql).executeThroughQuery()` |

这些不是“仍可工作但不推荐”的普通废弃方法；升级时必须先替换调用点。

旧的 `io.github.sinri.keel.integration.mysql.Quoter` 也已废弃。请迁移到：

```java
import io.github.sinri.keel.integration.mysql.statement.quoter.Quoter;
```

优先使用 `Tuple` 绑定业务值。`Quoter` 适用于必须生成 SQL 字面量的 DSL/模板场景，不负责引用表名、列名等标识符。

## 批处理选型

| API | 语义 | 返回值 |
|---|---|---|
| `executeThroughPrepare(Keel, List<Tuple>)` | 同一 prepared statement 上顺序逐组执行 | 每组一个 `StatementExecuteResult` |
| `RunnableStatementForWrite.execute(Keel, List<Tuple>)` | Vert.x `executeBatch` | 一个包含完整结果链的 `StatementExecuteResult` |

两者目前均标记为 Technical Preview。批处理中途失败时 Future 失败；调用方不应假设未提交事务会自动部分提交，原子性需求必须放入 `withTransaction`。

## 升级检查单

1. 全局搜索上述三个会抛错的旧 API 并替换。
2. 将拼接用户值的 SQL 改为 `?` + `Tuple`；模板 `{name}` 仍是文本替换，不是参数绑定。
3. 若使用字符串字面量转义，迁移到新 `Quoter`，并避免不安全的多字节连接字符集与反斜杠转义组合。
4. 核对 SSL 模式、证书路径、池连接超时和新增的空闲超时。
5. 检查虚拟线程取连与归还是否严格配对；普通异步代码优先使用 `withConnection` / `withTransaction`。
6. 批处理调用按所需语义选择 API，并使用总计方法或 `getRowSets()` 读取完整结果。
7. 运行 `./gradlew test` 和 `./gradlew javadoc`。

内部审查记录见 [PROJECT_REVIEW_2026-07-13.md](./PROJECT_REVIEW_2026-07-13.md)，该文件是审查快照，不作为用户 API 文档。
