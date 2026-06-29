# 配置与即时查询（`KeelMySQLConfiguration`）

`io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration` 负责从配置生成 Vert.x MySQL 连接选项和连接池选项，并提供适合工具代码的一次性查询入口。

## 即时查询

`instantQuery(Vertx vertx, String sql)` 会创建临时客户端，执行后关闭，返回 `Future<ResultMatrix<SimpleResultRow>>`。

5.0.4 起新增参数绑定重载：

```java
configuration.instantQuery(
        vertx,
        "SELECT * FROM user WHERE id = ?",
        Tuple.of(123)
);
```

当第三个参数为 `null` 时，行为等价于无参数版本。若 SQL 中包含 `?` 占位符，应传入匹配数量和顺序的 `Tuple`。

## 即时流式查询

`instantQueryForStream(Keel keel, String sql, int readWindowSize, Function<RowSet<Row>, Future<Void>> readWindowFunction)` 使用临时 pool、连接、预编译语句和 cursor 按窗口读取数据，并在方法链路结束时关闭资源。

5.0.4 起新增参数绑定重载：

```java
configuration.instantQueryForStream(
        keel,
        "SELECT * FROM user WHERE status = ?",
        Tuple.of("ACTIVE"),
        100,
        rows -> {
            // handle rows
            return Future.succeededFuture();
        }
);
```

## 使用建议

- 应用内长期复用、事务和连接计数优先使用 `NamedMySQLDataSource`。
- 单次脚本或工具代码可使用 `instantQuery`。
- 大结果集处理可使用 `instantQueryForStream` 或已有连接上的 `StreamableStatement`。
- 涉及用户输入时优先使用 `Tuple` 参数绑定，不要把值拼接进 SQL 字符串。
