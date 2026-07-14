# Keel-MySQL 5.0.4 文档导航

本目录以当前 5.0.4 源码为准，覆盖安装配置、连接生命周期、SQL 执行、批处理、结果读取及升级注意事项。

## 按主题阅读

| 文档 | 主题 |
|---|---|
| [快速开始](./getting_started.md) | 环境、依赖、配置、数据源与首个参数化查询 |
| [原始 SQL 与模板 SQL](./raw_and_templated_sql.md) | `RawStatement`、`TemplatedStatement`、参数绑定选型 |
| [语句执行与流式读取](./execution_and_stream.md) | prepare/query、顺序多参数、写批处理与流式读取 |
| [配置与即时查询](./configuration_and_query.md) | 连接池、SSL/TLS、`instantQuery` / `instantQueryForStream` |
| [迁移与发布说明](./release_notes.md) | 5.0.4 新增功能、行为变化、废弃 API 与升级清单 |

## 5.0.4 相对 5.0.1 的要点

- `RunnableStatement` 执行层新增 `executeThroughPrepare(Tuple)`，支持 `?` 占位符参数绑定。
- `execute()` 默认走无参数预编译路径；若 SQL 包含 `?`，必须显式传入 `Tuple`。
- 普通查询协议由 `executeThroughQuery()` 显式选择，不支持 `Tuple` 参数绑定。
- 语句对象不再保存 `toPrepareStatement` 状态；旧 API 已恢复签名、标记废弃，并在调用时抛出异常。
- `KeelMySQLConfiguration` 的即时查询和流式即时查询新增 `Tuple` 参数绑定重载。
- 写语句新增基于 Vert.x `executeBatch` 的显式批处理入口，批结果支持完整汇总。
- 支持 SSL/TLS、池空闲超时、会话字符集/`sql_mode` 感知转义和 JSON 列便利读取。
- 连接或事务回调同步抛错时也会释放连接；虚拟线程阻塞取连会校验调用线程。

其余未变更的 DSL、DDL、代码生成、分页和映射主题可继续参考 [5.0.1 文档](../5.0.1/index.md)。凡涉及配置、转义、执行协议、批处理、`rawForDirectQuery`、`RawStatement(String, boolean)` 或 `?` 参数绑定的说明，均以本目录为准。
