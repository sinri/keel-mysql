# 语句执行与流式读取（`connection.target`）

包：`io.github.sinri.keel.integration.mysql.connection.target`。

本包将 `AnyStatement` 与 `SqlConnection` 组合为可执行对象，并在执行层决定查询协议与参数绑定。

## `RunnableStatement`

`RunnableStatement` 是通用执行入口：

- `execute()`：默认调用 `executeThroughPrepare()`，即无参数预编译执行。
- `executeThroughPrepare()`：走预编译路径，不绑定参数。
- `executeThroughPrepare(Tuple)`：走预编译路径，并将 `Tuple` 绑定到 SQL 中的 `?` 占位符。
- `executeThroughPrepare(Keel, List<Tuple>)`：复用同一个预编译语句，按列表顺序逐组执行，返回每组对应的结果列表。该方法不是 Vert.x 批协议。
- `executeThroughQuery()`：走普通查询协议，不支持 `Tuple` 参数绑定。

5.0.4 起，语句对象不再持有 `toPrepareStatement` 状态。旧方法 `AnyStatement#setToPrepareStatement` 与 `AnyStatement#isToPrepareStatement` 仅保留废弃签名，调用会抛出 `UnsupportedOperationException`。

## 参数绑定示例

```java
RunnableStatement statement = connection.rawForPreparedQuery(
        "UPDATE user SET name = ? WHERE id = ?"
);

statement.executeThroughPrepare(Tuple.of("Alice", 123));
```

顺序执行多组参数：

```java
statement.executeThroughPrepare(keel, List.of(
        Tuple.of("Alice", 123),
        Tuple.of("Bob", 456)
));
```

## 写语句批处理

`insert` / `replace` 等写语句返回的 `RunnableStatementForWrite` 提供真正的 Vert.x 批处理入口：

```java
RunnableStatementForWrite statement = connection.insert(insert -> insert
        .intoTable("user")
        .macroWriteOneRow(row -> row
                .putExpression("name", "?")
                .putExpression("status", "?")));

statement.execute(keel, List.of(
        Tuple.of("Alice", "ACTIVE"),
        Tuple.of("Bob", "ACTIVE")
));
```

`RunnableStatementFactory.insert(...)` / `replace(...)` 返回已附着连接的 `RunnableStatementForWrite`。普通 `put(...)` / `addDataRow(...)` 会生成字面量 SQL；参数批处理应像上例一样显式生成 `?`，并保证每组 Tuple 的数量和顺序一致。该入口内部调用 `executeBatch`，返回一个 `StatementExecuteResult`：

- `getRowSets()` 按驱动返回顺序保留完整批结果链；
- `getTotalAffectedRows()` 与 `getTotalFetchedRows()` 对整条结果链求和；
- `getRowSet()`、迭代器、`toMatrix()` 仍只针对首个结果集。

不要把它与返回 `List<StatementExecuteResult>` 的顺序多参数执行混淆。若 SQL 已由 `WriteIntoStatement` 生成多行字面量，可改用 `divide(int)` 显式拆分，不需要再套参数批处理。

## 普通查询协议

当确实需要走普通查询协议时，使用 `executeThroughQuery()`：

```java
connection.rawForPreparedQuery("ANALYZE TABLE user")
          .executeThroughQuery();
```

该路径直接执行完整 SQL 字符串，不能绑定 `Tuple`。若 SQL 含用户输入，调用方必须自行完成校验或改用 `executeThroughPrepare(Tuple)`。

## 流式读取

原有 `StreamableStatement` 仍用于已有连接上的游标读取。配置层的一次性流式查询自 5.0.4 起也提供 `Tuple` 参数绑定重载，详见 [配置与即时查询](./configuration_and_query.md)。
