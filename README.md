# Keel-MySQL

Keel-MySQL 是 [Keel](https://github.com/sinri/keel) 的 MySQL 集成库，基于 Vert.x MySQL Client，提供命名数据源、连接池与事务管理、SQL DSL、参数化执行、流式读取及结果映射。

当前候选版本为 **5.0.4-rc.1**。完整说明见 [5.0.4 文档](docs/5.0.4/index.md)。

## 环境与依赖

- JDK 17+
- MySQL 5.7+（JSON 便利读取要求 MySQL 5.7+）
- 运行时依赖由 Maven 工件传递引入

Gradle Kotlin DSL：

```kotlin
dependencies {
    implementation("io.github.sinri:keel-mysql:5.0.4-rc.1")
}
```

Maven：

```xml
<dependency>
  <groupId>io.github.sinri</groupId>
  <artifactId>keel-mysql</artifactId>
  <version>5.0.4-rc.1</version>
</dependency>
```

正式版发布后，可将版本替换为 `5.0.4`。

## 最小配置

配置位于 `mysql.<数据源名>` 下：

```properties
mysql.default_data_source_name=default
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

不要把真实密码写入示例或版本控制。可通过 `KeelMySQLConfiguration.generateSamplePropertiesForConfig("default")` 生成占位配置。

## 快速使用

```java
var provider = new KeelMySQLDataSourceProvider();

provider.loadDefault(vertx).compose(dataSource ->
    dataSource.withConnection(connection ->
        connection.rawForPreparedQuery(
                "SELECT * FROM user WHERE id = ?"
        ).executeThroughPrepare(Tuple.of(123))
    ).eventually(dataSource::close)
);
```

使用 `withConnection` / `withTransaction` 时，数据源负责归还连接。手工从虚拟线程取连时，必须与 `returnConnectionFromVirtualThread` 配对。

## 5.0.4 重点

- `Tuple` 参数绑定覆盖连接执行、即时查询和流式即时查询。
- 写语句提供显式 `executeBatch` 批处理入口，并正确汇总批结果链。
- 新增 SSL/TLS 连接配置和 `poolIdleTimeout`。
- 新 `statement.quoter.Quoter` 根据会话字符集及 `sql_mode` 转义字符串；旧 `integration.mysql.Quoter` 已废弃。
- `ResultRow.readJson` 提供 JSON 列便利读取。
- 加强连接/事务失败路径的资源释放，并限制虚拟线程阻塞入口。

升级前请阅读 [5.0.4 迁移与发布说明](docs/5.0.4/release_notes.md)，尤其是 5.0.4 中会直接抛出 `UnsupportedOperationException` 的废弃 API。

## 构建

```shell
./gradlew test
./gradlew javadoc
```

许可证：[GPL-3.0](LICENSE)。
