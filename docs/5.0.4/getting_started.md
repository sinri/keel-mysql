# 快速开始（5.0.4）

## 环境与依赖

本项目以 JDK 17 编译。Gradle：

```kotlin
dependencies {
    implementation("io.github.sinri:keel-mysql:5.0.4-rc.1")
}
```

使用 JPMS 时，模块名为 `io.github.sinri.keel.integration.mysql`。

## 配置

Keel-MySQL 从 Keel 根配置的 `mysql.<数据源名>` 节点读取配置。未设置 `mysql.default_data_source_name` 时，默认数据源名为 `default`。

```properties
mysql.default.host=127.0.0.1
mysql.default.port=3306
mysql.default.username=<YOUR_USERNAME>
mysql.default.password=<YOUR_PASSWORD>
mysql.default.schema=<YOUR_DATABASE>
mysql.default.charset=utf8mb4
mysql.default.poolMaxSize=10
mysql.default.poolShared=YES
mysql.default.poolConnectionTimeout=30
mysql.default.poolIdleTimeout=300
```

`poolConnectionTimeout` 和 `poolIdleTimeout` 的单位均为秒。建议通过环境变量或密钥管理注入密码；不要使用已废弃、会输出真实连接信息的 `generatePropertiesForConfig`。

## 加载并使用数据源

```java
var provider = new KeelMySQLDataSourceProvider();

Future<Void> task = provider.loadDefault(vertx).compose(dataSource ->
    dataSource.withConnection(connection ->
        connection.rawForPreparedQuery(
                "SELECT id, name FROM user WHERE id = ?"
        ).executeThroughPrepare(Tuple.of(123))
         .map(result -> {
             ResultMatrix<SimpleResultRow> rows = result.toMatrix();
             // consume rows
             return null;
         })
    ).eventually(dataSource::close)
);
```

`loadDefault` 会进行首次连通性检查。应用内应长期复用同一个数据源，并仅在停机时关闭；上例立即关闭只是展示完整生命周期。

## 事务

```java
dataSource.withTransaction(connection ->
    connection.rawForPreparedQuery(
            "UPDATE account SET balance = balance - ? WHERE id = ?"
    ).executeThroughPrepare(Tuple.of(100, fromId))
     .compose(ignored -> connection.rawForPreparedQuery(
             "UPDATE account SET balance = balance + ? WHERE id = ?"
     ).executeThroughPrepare(Tuple.of(100, toId)))
);
```

回调成功时自动提交，失败时自动回滚，连接最终自动归还。回调在返回 `Future` 前同步抛出异常时，5.0.4 同样会执行清理。

## 下一步

- 执行协议与批处理：[语句执行与流式读取](./execution_and_stream.md)
- SSL/TLS 与一次性查询：[配置与即时查询](./configuration_and_query.md)
- 从旧版本迁移：[迁移与发布说明](./release_notes.md)
