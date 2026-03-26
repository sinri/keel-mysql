# 配置与即时查询（`KeelMySQLConfiguration`）

## 模块角色

`io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration` 继承 Keel 的 **`ConfigElement`**，表示**某一个命名数据源
**在配置树中的片段。它负责：

- 从配置读取主机、端口、账号、库名、字符集、连接池选项；
- 生成 Vert.x 的 **`MySQLConnectOptions`** 与 **`PoolOptions`**；
- 提供**不经过** `NamedMySQLDataSource` 的轻量入口：**一次性查询**与**配置模板**。

通常通过 **`KeelMySQLDataSourceProvider.getMySQLConfiguration(name)`** 得到实例；也可在测试中手工
`new KeelMySQLConfiguration(ConfigElement.root().extract("mysql", "myds"))`（需保证 Keel 根配置已加载）。

## 配置键（相对 `mysql.<数据源名>`）

| 方法 / 含义 | 配置键                            | 默认或未配置      |
|---------|--------------------------------|-------------|
| 主机      | `host`                         | `127.0.0.1` |
| 端口      | `port`                         | `3306`      |
| 用户 / 密码 | `username`、`password`          | 密码可为 null   |
| 数据库     | `schema`                       | 可选          |
| 字符集     | `charset`                      | 可选          |
| 池最大连接数  | `poolMaxSize`                  | 交 Vert.x 默认 |
| 是否共享池   | `poolShared`（YES/NO）           | `true`      |
| 取连超时    | `poolConnectionTimeout`（**秒**） | 不设置则本类不写入该项 |

根级可选：**`mysql.default_data_source_name`**（默认逻辑名 `default`）。

连接上 **`useAffectedRows`** 固定为 **`true`**。

## 配置模板（5.0.1 推荐）

- **`generateSamplePropertiesForConfig(String dataSourceName)`**：生成**仅占位符**的样本属性串，可放进文档或模板仓库，**不含
  **真实密码。
- **`generatePropertiesForConfig(...)`**（**已弃用**）：会把密码明文写入字符串，**请勿**用于生产或提交到版本库。

## 即时查询：`instantQuery` / `instantQueryForStream`

二者均挂在 **`KeelMySQLConfiguration`** 上，适合**临时客户端**或工具代码，而不是长期持有的业务池。

### `instantQuery(Vertx vertx, String sql)`

- 使用 **`MySQLBuilder.client()`** 创建客户端，执行后关闭。
- 返回 **`Future<ResultMatrix<SimpleResultRow>>`**。
- 标注为 **Technical Preview**：若频繁调用，需注意 Vert.x 与文档中关于**池共享、唯一池名**的说明，避免资源或行为不符合预期。

###

`instantQueryForStream(Keel keel, String sql, int readWindowSize, Function<RowSet<Row>, Future<Void>> readWindowFunction)`

- 使用 **`MySQLBuilder.pool()`**，在连接上 **`prepare` + `cursor`**，按 **`readWindowSize`** 批量调用 *
  *`readWindowFunction`**。
- 在 **`keel.asyncCallRepeatedly`** 中循环直到无更多行；实现中在链路末尾 **`eventually`** 关闭 **pool** 等，适合**大结果集
  **且不想一次性装入内存的场景。
- SQL 仍须由调用方保证合法与安全（防注入）。

## 与 `NamedMySQLDataSource` 的选择

| 需求                    | 建议                                                                                                                   |
|-----------------------|----------------------------------------------------------------------------------------------------------------------|
| 应用内长期复用、事务、连接计数       | **`NamedMySQLDataSource`**（见 [datasource_and_pool.md](./datasource_and_pool.md)）                                     |
| 单次脚本、极短生命周期查询         | 可考虑 **`instantQuery`**（注意 preview 语义）                                                                                |
| 大结果集流式处理且自管 pool 生命周期 | **`instantQueryForStream`** 或自建池 + **`StreamableStatement`**（见 [execution_and_stream.md](./execution_and_stream.md)） |
