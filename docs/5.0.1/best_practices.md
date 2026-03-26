# 场景实践汇编

本文按常见业务场景串联 API，细节仍以各模块专文为准（见 [index.md](index.md)）。

## 1. 应用启动：加载默认数据源并查询一行

1. 确保 Keel 根配置已加载，且存在 **`mysql.<name>.*`**（见 [配置与即时查询](./configuration_and_query.md)）。
2. **`new KeelMySQLDataSourceProvider().loadDefault(vertx)`** 得到 *
   *`Future<NamedMySQLDataSource<DynamicNamedMySQLConnection>>`**。
3. **
   `dataSource.withConnection(conn -> conn.select(s -> s.from("users").where(w -> w.expressionEqualsNumericValue("id", id))).executeForOneRow(MyUserRow.class))`
   **。

事务边界由 **`withConnection`** 管理单语句；多语句原子性见场景 2。

## 2. 同一请求内多语句事务

**`dataSource.withTransaction(conn ->`**

**
`Future.succeededFuture().compose(v -> conn.update(...).executeForAffectedRows()).compose(afx -> conn.insert(...).executeForLastInsertedID())`
**

**`)`**

失败时库内会尝试 **rollback** 并抛出 **`KeelMySQLException`**（语义见 [数据源与连接池](./datasource_and_pool.md)）。**不要在
** **`NamedActionInterface`** 实现里再 **`beginTransaction`**，以免双重事务边界。

## 3. 分页列表（总数 + 当前页数据）

**`conn.select(s -> { ... 排序相同 ... }).executeForPagination(pageNo, pageSize)`**

返回 **`PaginationResult`**，可 **`toJsonObject()`
** 给 HTTP 层（见 [结果集分页与映射](./result_pagination_and_mapping.md)）。

## 4. 大表导出：游标 + 批量

在 **`withConnection`** 内：

**
`new SelectStatement()...attachToConnectionForStream(keel, conn.getSqlConnection()).streamRead(row -> { ... process ... }, 500)`
**

需已有 **`Keel`** 实例；**`ResultStreamReader`** 实现 **`read(Row)`
**（见 [语句执行与流式读取](./execution_and_stream.md)）。

## 5. 虚拟线程中的同步风格（5.0.1）

**`C c = dataSource.fetchConnectionInVirtualThread();`**

**`try { ... c.select(...).execute(); ... } finally { dataSource.returnConnectionFromVirtualThread(c); }`**

在虚拟线程中若需阻塞等待 **`Future`**，请使用你项目统一的阻塞适配方式（例如 Vert.x 提供的 **`await()`** 或 *
*`toCompletionStage().join()`** 等）。**禁止**在事件循环线程调用 **`fetchConnectionInVirtualThread`**。

## 6. 资源目录中的 SQL 模板

**`conn.templatedRead("/path/report.sql", m -> m.put("start", ...).put("end", ...)).executeForResultMatrix()`**

占位符为 **`{name}`** 文本替换，用户参与部分须先 **`Quoter`** 或校验（见 [原始SQL与模板SQL](./raw_and_templated_sql.md)）。

## 7. 接入 SQL 审计

应用日志工厂就绪后：

**`StatementAuditorHolder.getInstance().reloadSqlAuditLogger(appLoggerFactory);`**

之后通过 **`RunnableStatement.execute()`** 的语句会写审计事件（见 [异常与SQL审计](./exception_and_audit.md)）。

## 8. 一次性运维脚本（无长期池）

**`config.instantQuery(vertx, "SELECT ...")`** 或 **`config.instantQueryForStream(keel, sql, batch, handler)`
**（见 [配置与即时查询](./configuration_and_query.md)）。注意 **`instantQuery`** 为 Technical Preview，且不适合高并发短连接场景。
