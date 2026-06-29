# Keel-MySQL 5.0.4 文档导航

本目录记录 5.0.4 起的最新执行语义，重点是原始 SQL、模板 SQL 与即时查询的真实 `Tuple` 参数绑定支持。

## 按主题阅读

| 文档 | 主题 |
|---|---|
| [原始 SQL 与模板 SQL](./raw_and_templated_sql.md) | `RawStatement`、`TemplatedStatement`、参数绑定选型 |
| [语句执行与流式读取](./execution_and_stream.md) | `RunnableStatement` 的 prepare/query 执行入口 |
| [配置与即时查询](./configuration_and_query.md) | `instantQuery` / `instantQueryForStream` 的参数绑定入口 |

## 5.0.4 相对 5.0.1 的要点

- `RunnableStatement` 执行层新增 `executeThroughPrepare(Tuple)`，支持 `?` 占位符参数绑定。
- `execute()` 默认走无参数预编译路径；若 SQL 包含 `?`，必须显式传入 `Tuple`。
- 普通查询协议由 `executeThroughQuery()` 显式选择，不支持 `Tuple` 参数绑定。
- 语句对象不再保存 `toPrepareStatement` 状态；旧 API 已恢复签名、标记废弃，并在调用时抛出异常。
- `KeelMySQLConfiguration` 的即时查询和流式即时查询新增 `Tuple` 参数绑定重载。

其余未变更主题可继续参考 5.0.1 文档，但凡涉及预编译、`rawForDirectQuery`、`RawStatement(String, boolean)` 或“不支持 `?` 绑定”的说明，均以本目录为准。
