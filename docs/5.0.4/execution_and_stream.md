# 语句执行与流式读取（`connection.target`）

包：`io.github.sinri.keel.integration.mysql.connection.target`。

本包将 `AnyStatement` 与 `SqlConnection` 组合为可执行对象，并在执行层决定查询协议与参数绑定。

## `RunnableStatement`

`RunnableStatement` 是通用执行入口：

- `execute()`：默认调用 `executeThroughPrepare()`，即无参数预编译执行。
- `executeThroughPrepare()`：走预编译路径，不绑定参数。
- `executeThroughPrepare(Tuple)`：走预编译路径，并将 `Tuple` 绑定到 SQL 中的 `?` 占位符。
- `executeThroughPrepare(Keel, List<Tuple>)`：复用同一个预编译语句，顺序执行多组参数。
- `executeThroughQuery()`：走普通查询协议，不支持 `Tuple` 参数绑定。

5.0.4 起，语句对象不再持有 `toPrepareStatement` 状态。旧方法 `AnyStatement#setToPrepareStatement` 与 `AnyStatement#isToPrepareStatement` 仅保留废弃签名，调用会抛出 `UnsupportedOperationException`。

## 参数绑定示例

```java
RunnableStatement statement = connection.rawForPreparedQuery(
        "UPDATE user SET name = ? WHERE id = ?"
);

statement.executeThroughPrepare(Tuple.of("Alice", 123));
```

批量参数执行：

```java
statement.executeThroughPrepare(keel, List.of(
        Tuple.of("Alice", 123),
        Tuple.of("Bob", 456)
));
```

## 普通查询协议

当确实需要走普通查询协议时，使用 `executeThroughQuery()`：

```java
connection.rawForPreparedQuery("ANALYZE TABLE user")
          .executeThroughQuery();
```

该路径直接执行完整 SQL 字符串，不能绑定 `Tuple`。若 SQL 含用户输入，调用方必须自行完成校验或改用 `executeThroughPrepare(Tuple)`。

## 流式读取

原有 `StreamableStatement` 仍用于已有连接上的游标读取。配置层的一次性流式查询自 5.0.4 起也提供 `Tuple` 参数绑定重载，详见 [配置与即时查询](./configuration_and_query.md)。
