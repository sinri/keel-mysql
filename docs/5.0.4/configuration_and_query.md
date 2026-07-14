# 配置与即时查询（`KeelMySQLConfiguration`）

`io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration` 负责从配置生成 Vert.x MySQL 连接选项和连接池选项，并提供适合工具代码的一次性查询入口。

## 连接与连接池配置

| 键 | 说明 | 未配置时 |
|---|---|---|
| `host` / `port` | MySQL 地址与端口 | `127.0.0.1` / `3306` |
| `username` / `password` | 登录凭据 | 用户名必须有效，密码可为空 |
| `schema` / `charset` | 默认库与连接字符集 | 可选 |
| `poolMaxSize` | 最大池大小 | Vert.x 默认值 |
| `poolShared` | 是否共享池 | `true` |
| `poolConnectionTimeout` | 取连超时，单位秒 | Vert.x 默认值 |
| `poolIdleTimeout` | 空闲连接超时，单位秒 | Vert.x 默认值 |

连接选项固定启用 `useAffectedRows=true`。池名为 `Keel-MySQL-Pool-<数据源名>`。

## SSL/TLS（5.0.4）

`sslMode` 支持 `DISABLED`、`PREFERRED`、`REQUIRED`、`VERIFY_CA`、`VERIFY_IDENTITY`。未配置 `sslMode` 时，兼容键 `ssl=YES/NO` 分别映射到 `REQUIRED` / `DISABLED`。

常用 PEM 配置：

```properties
mysql.default.sslMode=VERIFY_IDENTITY
mysql.default.sslCa=/path/to/ca.pem
mysql.default.sslCert=/path/to/client-cert.pem
mysql.default.sslKey=/path/to/client-key.pem
```

还支持 `sslTrustAll`、`sslHostnameVerificationAlgorithm`、JKS（`sslJksTrustStorePath` / `sslJksTrustStorePassword`）和 PFX（`sslPfxTrustStorePath` / `sslPfxTrustStorePassword`）信任库。生产环境不建议启用 `sslTrustAll`；需要校验服务端身份时使用 `VERIFY_IDENTITY` 并正确配置 CA 与主机名。

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

该方法总是使用预编译查询。客户端在完成或失败后关闭；它适合脚本和工具代码，不应替代应用长期复用的数据源。

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

`readWindowSize` 应为正数，处理函数必须返回非空 `Future<Void>`。处理函数失败会停止读取；cursor、prepared statement、连接和 pool 会沿异步链关闭。

## 使用建议

- 应用内长期复用、事务和连接计数优先使用 `NamedMySQLDataSource`。
- 单次脚本或工具代码可使用 `instantQuery`。
- 大结果集处理可使用 `instantQueryForStream` 或已有连接上的 `StreamableStatement`。
- 涉及用户输入时优先使用 `Tuple` 参数绑定，不要把值拼接进 SQL 字符串。
