# Keel-MySQL 5.0.1 文档导航

本目录为库使用者准备的分主题说明。总览性内容见 [使用说明.md](./使用说明.md)。

## 按模块阅读

| 文档                                 | 包 / 主题                                                          | 适合读者           |
|------------------------------------|-----------------------------------------------------------------|----------------|
| [配置与即时查询.md](./配置与即时查询.md)         | `KeelMySQLConfiguration`、无池的一次性查询 / 游标流                         | 运维配置、脚本型访问     |
| [数据源与连接池.md](./数据源与连接池.md)         | `KeelMySQLDataSourceProvider`、`NamedMySQLDataSource`            | 应用启动、池与事务      |
| [命名连接与动作模式.md](./命名连接与动作模式.md)     | `NamedMySQLConnection`、`action.single` / `action.mix`           | 领域服务、分层架构      |
| [语句执行与流式读取.md](./语句执行与流式读取.md)     | `connection.target`（`RunnableStatement*`、`StreamableStatement`） | 执行路径、游标批量读     |
| [DSL条件与DDL.md](./DSL条件与DDL.md)     | `statement.impl`、`statement.component`、`condition`              | 类型安全拼 SQL      |
| [原始SQL与模板SQL.md](./原始SQL与模板SQL.md) | `RawStatement`、`statement.templated`                            | 手写 SQL、外部模板文件  |
| [结果集分页与映射.md](./结果集分页与映射.md)       | `result`、`result.matrix`、`result.pagination`、`result.stream`    | 查询结果消费         |
| [开发工具代码生成.md](./开发工具代码生成.md)       | `dev`（`opens` 包）                                                | 从 schema 生成行类型 |
| [异常与SQL审计.md](./异常与SQL审计.md)       | `exception`、`StatementAuditorHolder`                            | 排错、对接日志系统      |
| [场景实践汇编.md](./场景实践汇编.md)           | 跨模块常见用法串联                                                       | 落地实现时对照        |

## 按场景速查

| 场景                         | 建议阅读                                                           |
|----------------------------|----------------------------------------------------------------|
| 首次接入：Maven/Gradle、JDK、JPMS | [使用说明.md](./使用说明.md)                                           |
| 多数据源、连接池参数                 | [配置与即时查询.md](./配置与即时查询.md)、[数据源与连接池.md](./数据源与连接池.md)          |
| 在 `withConnection` 里写业务    | [命名连接与动作模式.md](./命名连接与动作模式.md)、[语句执行与流式读取.md](./语句执行与流式读取.md)  |
| 需要 `BEGIN`/`COMMIT`        | [数据源与连接池.md](./数据源与连接池.md)                                     |
| 虚拟线程里阻塞拿连接                 | [数据源与连接池.md](./数据源与连接池.md) § 虚拟线程                              |
| 列表分页（总数 + 当前页）             | [结果集分页与映射.md](./结果集分页与映射.md)                                   |
| 大结果集、避免一次性加载               | [语句执行与流式读取.md](./语句执行与流式读取.md)、[配置与即时查询.md](./配置与即时查询.md) § 流式 |
| 外部 `.sql` 模板               | [原始SQL与模板SQL.md](./原始SQL与模板SQL.md)                             |
| 监控池、接审计日志                  | [异常与SQL审计.md](./异常与SQL审计.md)、[数据源与连接池.md](./数据源与连接池.md) § 观测   |
| 从表结构生成 Java 行类型            | [开发工具代码生成.md](./开发工具代码生成.md)                                   |

版本与坐标以仓库 `gradle.properties` 为准；发布版请将依赖版本改为实际工件号。
