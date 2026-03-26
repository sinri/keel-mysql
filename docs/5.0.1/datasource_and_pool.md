# 数据源与连接池（`KeelMySQLDataSourceProvider` / `NamedMySQLDataSource`）

## `KeelMySQLDataSourceProvider`

包：`io.github.sinri.keel.integration.mysql.provider`。

静态工厂与加载入口：

| API                                                                                                        | 作用                                                                    |
|------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `defaultMySQLDataSourceName()`                                                                             | 读取 `mysql.default_data_source_name`，缺省为 `"default"`                   |
| `getMySQLConfiguration(String dataSourceName)`                                                             | 从 `ConfigElement.root()` 解析 `mysql.<name>` → `KeelMySQLConfiguration` |
| `getDefaultMySQLConfiguration()`                                                                           | 上述默认名                                                                 |
| `load(Vertx, String name, Function<SqlConnection,C> wrapper, Function<SqlConnection,Future<Void>> setup?)` | 构造 `NamedMySQLDataSource<C>` 并做**首次连通性等待**                            |
| `loadDynamic(Vertx, name)`                                                                                 | `wrapper` 为 `DynamicNamedMySQLConnection`                             |
| `loadDefault(Vertx)`                                                                                       | `loadDynamic` + 默认数据源名                                                |

**首次加载**：`waitForLoading` 用 **`PoolOptions` 的连接超时** 注册定时器；若在超时前 **`withConnection` 探测
**成功则完成，否则失败（提示检查配置）。因此 **`poolConnectionTimeout`** 同时影响 Vert.x 取连行为与这里的「启动探测」窗口。

**`connectionSetUpFunction`**：每个**新物理连接**建立时调用（例如 `SET NAMES`、会话变量），然后连接会关闭并进入池（由
`NamedMySQLDataSource` 的 connect handler 管理）。

## `NamedMySQLDataSource<C>`

包：`io.github.sinri.keel.integration.mysql.datasource`。

底层为 Vert.x **`Pool`**（`MySQLBuilder.pool()`），对外暴露：

### 连接生命周期

| 方法                                             | 说明                                                                                 |
|------------------------------------------------|------------------------------------------------------------------------------------|
| `withConnection(Function<C, Future<T>>)`       | 借连 → 执行 → **`close`**；异常包装为 **`KeelMySQLException`**                               |
| `withTransaction(Function<C, Future<T>>)`      | `begin` → 业务 → 成功则 `commit`，失败分支处理 `rollback` 与 **`TransactionRollbackException`** |
| `executeInConnection` / `executeInTransaction` | 返回 **`ValueBox<T>`**，标记为 **Technical Preview**                                     |

类型参数 **`C`** 须实现 **`NamedMySQLConnection`**，通常 **`DynamicNamedMySQLConnection`
** 或自定义子类（见 [connection_and_action.md](./connection_and_action.md)）。

### 事务语义（摘要）

- 成功路径：`function.apply` 完成后 **`transaction.commit()`**。
- 失败路径：区分是否已为 **`TransactionRollbackException`**（视为已回滚）与其它异常（尝试 **`rollback`** 后再失败包装 *
  *`KeelMySQLException`**）。
- 开始事务失败会包装为 **`KeelMySQLConnectionException`**。

具体分支以实现代码为准。

### 虚拟线程（5.0.1）

| API                                    | 说明                                           |
|----------------------------------------|----------------------------------------------|
| `fetchConnectionInVirtualThread()`     | 仅在**虚拟线程**中调用；内部 **`Future.await()`**；递增活跃计数 |
| `returnConnectionFromVirtualThread(C)` | 关闭连接并递减计数；**必须与上面成对使用**                      |

**勿在 Vert.x 事件循环线程**调用 `fetchConnectionInVirtualThread()`。未启用虚拟线程时抛 **`UnsupportedOperationException`
**。

**注意**：直接 **`asyncClose()`** / **`close()`** 会释放连接，但**不会**修正本类维护的借出计数；虚拟线程路径务必走 *
*`returnConnectionFromVirtualThread`**。

### 观测与版本

| API                                 | 说明                                            |
|-------------------------------------|-----------------------------------------------|
| `getCurrentPoolSize()`              | 委托 **`Pool#size()`**（近似）                      |
| `getCurrentIdleConnectionCount()`   | `max(0, poolSize - borrowed)`                 |
| `getCurrentActiveConnectionCount()` | 当前借出未还                                        |
| `getFullVersion()`                  | 首次连接初始化时 **`SELECT VERSION()`** 缓存，可能为 `null` |

**已弃用**：`getCurrentInitializedConnectionCount()` → 请用 **`getCurrentPoolSize()`**。

### 关闭

- **`close()`** / **`close(Completable)`**：关闭底层 **`pool`**。

## 场景对照

| 场景       | 做法                                                                                                            |
|----------|---------------------------------------------------------------------------------------------------------------|
| 应用启动加载主库 | `provider.loadDefault(vertx)` 或 `loadDynamic(vertx, "billing")`                                               |
| 多租户多配置   | 多个 `mysql.<name>` + 多次 `load`                                                                                 |
| 全局事务边界   | `dataSource.withTransaction(conn -> ...)`                                                                     |
| 只读批量     | `withConnection` 内使用 `select` / `StreamableStatement`（见 [execution_and_stream.md](./execution_and_stream.md)） |
