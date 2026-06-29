package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.ConfigPropertiesBuilder;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Keel MySQL配置类，用于管理MySQL连接和连接池的配置信息。
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelMySQLConfiguration extends ConfigElement {
    public KeelMySQLConfiguration(ConfigElement base) {
        super(base);
    }

    /**
     * 生成 MySQL 配置的样本属性字符串，所有值为占位符，可作为配置模板使用。
     * <p>
     * 生成的内容不包含任何真实的连接信息或密码，可安全地写入文档、日志或版本控制。
     *
     * @param dataSourceName 数据源名称
     * @return 包含占位符值的样本配置属性字符串
     */
    public static String generateSamplePropertiesForConfig(String dataSourceName) {
        var builder = new ConfigPropertiesBuilder();
        List<String> prefix = List.of("mysql", dataSourceName);

        builder.add(prefix, "host", "127.0.0.1");
        builder.add(prefix, "port", "3306");
        builder.add(prefix, "username", "<YOUR_USERNAME>");
        builder.add(prefix, "password", "<YOUR_PASSWORD>");
        builder.add(prefix, "schema", "<YOUR_DATABASE>");
        builder.add(prefix, "charset", "utf8mb4");
        builder.add(prefix, "ssl", "NO");
        builder.add(prefix, "sslMode", "DISABLED");
        builder.add(prefix, "sslCa", "<PATH_TO_CA_CERT_PEM>");
        builder.add(prefix, "sslCert", "<PATH_TO_CLIENT_CERT_PEM>");
        builder.add(prefix, "sslKey", "<PATH_TO_CLIENT_KEY_PEM>");
        builder.add(prefix, "poolMaxSize", "10");
        builder.add(prefix, "poolShared", "YES");
        builder.add(prefix, "poolConnectionTimeout", "30");
        builder.add(prefix, "poolIdleTimeout", "300");

        return builder.writeToString();
    }

    /**
     * 获取MySQL连接选项
     *
     * @return MySQL连接选项
     */

    public MySQLConnectOptions getConnectOptions() {
        // mysql.XXX.connect::database,host,password,port,user,charset,useAffectedRows,connectionTimeout
        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions()
                .setUseAffectedRows(true);
        mySQLConnectOptions.setHost(getHost())
                           .setPort(getPort())
                           .setUser(getUsername())
                           .setPassword(getPassword());
        String charset = getCharset();
        if (charset != null) mySQLConnectOptions.setCharset(charset);
        String schema = getSchema();
        if (schema != null) {
            mySQLConnectOptions.setDatabase(schema);
        }
        SslMode sslMode = getSslMode();
        if (sslMode != null) {
            mySQLConnectOptions.setSslMode(sslMode);
        }
        ClientSSLOptions sslOptions = getSslOptions();
        if (sslOptions != null) {
            mySQLConnectOptions.setSslOptions(sslOptions);
        }

        return mySQLConnectOptions;
    }

    /**
     * 获取连接池选项
     *
     * @return 连接池选项
     */

    public PoolOptions getPoolOptions() {
        // mysql.XXX.pool::poolConnectionTimeout
        PoolOptions poolOptions = new PoolOptions();
        Integer poolMaxSize = getPoolMaxSize();
        if (poolMaxSize != null) {
            poolOptions.setMaxSize(poolMaxSize);
        }
        Integer poolConnectionTimeout = getPoolConnectionTimeout();
        if (poolConnectionTimeout != null) {
            poolOptions.setConnectionTimeout(poolConnectionTimeout);
            poolOptions.setConnectionTimeoutUnit(TimeUnit.SECONDS);
        }
        Integer poolIdleTimeout = getPoolIdleTimeout();
        if (poolIdleTimeout != null) {
            poolOptions.setIdleTimeout(poolIdleTimeout);
            poolOptions.setIdleTimeoutUnit(TimeUnit.SECONDS);
        }
        poolOptions.setShared(getPoolShared());
        poolOptions.setName("Keel-MySQL-Pool-" + this.getDataSourceName());
        return poolOptions;
    }

    /**
     * 获取MySQL主机地址
     *
     * @return 主机地址
     */

    public String getHost() {
        try {
            return readString(List.of("host"));
        } catch (NotConfiguredException e) {
            return "127.0.0.1";
        }
    }

    /**
     * 获取MySQL端口号
     *
     * @return 端口号，默认3306
     */
    public int getPort() {
        try {
            return readInteger(List.of("port"));
        } catch (NotConfiguredException e) {
            return 3306;
        }
    }

    /**
     * 获取MySQL密码
     *
     * @return 密码
     */
    @Nullable
    public String getPassword() {
        try {
            return readString(List.of("password"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取MySQL用户名
     *
     * @return 用户名
     */
    @Nullable
    public String getUsername() {
        try {
            return readString(List.of("username"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取MySQL数据库名
     *
     * @return 数据库名
     */
    @Nullable
    public String getSchema() {
        try {
            return readString(List.of("schema"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取MySQL字符集
     *
     * @return 字符集
     */
    @Nullable
    public String getCharset() {
        try {
            return readString(List.of("charset"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取 MySQL SSL 模式。
     * <p>
     * `sslMode` 可取 `DISABLED`、`PREFERRED`、`REQUIRED`、`VERIFY_CA`、`VERIFY_IDENTITY`；
     * 未配置 `sslMode` 时，`ssl=true` 会映射为 `REQUIRED`，`ssl=false` 会映射为 `DISABLED`。
     *
     * @return SSL 模式
     */
    @Nullable
    public SslMode getSslMode() {
        try {
            return parseSslMode(readString(List.of("sslMode")));
        } catch (NotConfiguredException e) {
            try {
                return readBoolean(List.of("ssl")) ? SslMode.REQUIRED : SslMode.DISABLED;
            } catch (NotConfiguredException ignored) {
                return null;
            }
        }
    }

    /**
     * 获取 MySQL 客户端 SSL 选项。
     * <p>
     * 当前支持 `sslTrustAll`、`sslHostnameVerificationAlgorithm`、`sslCa`/`sslTrustCertPath`/`sslPemTrustCertPath`、
     * `sslCert`/`sslKey` 客户端 PEM 证书、
     * `sslJksTrustStorePath`/`sslJksTrustStorePassword` 以及
     * `sslPfxTrustStorePath`/`sslPfxTrustStorePassword`。
     *
     * @return SSL 选项
     */
    @Nullable
    public ClientSSLOptions getSslOptions() {
        ClientSSLOptions sslOptions = new ClientSSLOptions();
        boolean configured = false;

        Boolean sslTrustAll = getSslTrustAll();
        if (sslTrustAll != null) {
            sslOptions.setTrustAll(sslTrustAll);
            configured = true;
        }

        String hostnameVerificationAlgorithm = getSslHostnameVerificationAlgorithm();
        if (hostnameVerificationAlgorithm != null) {
            sslOptions.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
            configured = true;
        }

        KeyCertOptions keyCertOptions = getSslKeyCertOptions();
        if (keyCertOptions != null) {
            sslOptions.setKeyCertOptions(keyCertOptions);
            configured = true;
        }

        TrustOptions trustOptions = getSslTrustOptions();
        if (trustOptions != null) {
            sslOptions.setTrustOptions(trustOptions);
            configured = true;
        }

        return configured ? sslOptions : null;
    }

    @Nullable
    public Boolean getSslTrustAll() {
        try {
            return readBoolean(List.of("sslTrustAll"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    @Nullable
    public String getSslHostnameVerificationAlgorithm() {
        try {
            return readString(List.of("sslHostnameVerificationAlgorithm"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    @Nullable
    public KeyCertOptions getSslKeyCertOptions() {
        String keyPath = getFirstConfiguredString("sslKey", "sslClientKey", "sslPemKeyCertKeyPath");
        if (keyPath == null) {
            return null;
        }

        String certPath = getFirstConfiguredString("sslClientCert", "sslPemKeyCertCertPath", "sslCert");
        if (certPath == null) {
            return null;
        }

        return new PemKeyCertOptions()
                .addCertPath(certPath)
                .addKeyPath(keyPath);
    }

    @Nullable
    public TrustOptions getSslTrustOptions() {
        String pemTrustCertPath = getFirstConfiguredString("sslCa", "sslTrustCertPath", "sslPemTrustCertPath");
        if (pemTrustCertPath == null && getFirstConfiguredString("sslKey") == null) {
            // 兼容 issue 中提出的 sslCert 命名；若同时存在 sslKey，则 sslCert 用作客户端证书。
            pemTrustCertPath = getFirstConfiguredString("sslCert");
        }
        if (pemTrustCertPath != null) {
            return new PemTrustOptions().addCertPath(pemTrustCertPath);
        }

        String jksTrustStorePath = getFirstConfiguredString("sslJksTrustStorePath");
        if (jksTrustStorePath != null) {
            JksOptions jksOptions = new JksOptions().setPath(jksTrustStorePath);
            String password = getFirstConfiguredString("sslJksTrustStorePassword");
            if (password != null) {
                jksOptions.setPassword(password);
            }
            return jksOptions;
        }

        String pfxTrustStorePath = getFirstConfiguredString("sslPfxTrustStorePath");
        if (pfxTrustStorePath != null) {
            PfxOptions pfxOptions = new PfxOptions().setPath(pfxTrustStorePath);
            String password = getFirstConfiguredString("sslPfxTrustStorePassword");
            if (password != null) {
                pfxOptions.setPassword(password);
            }
            return pfxOptions;
        }

        return null;
    }

    /**
     * 获取连接池最大大小
     *
     * @return 连接池最大大小
     */
    @Nullable
    public Integer getPoolMaxSize() {
        try {
            return readInteger(List.of("poolMaxSize"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取数据源名称，用于MySQL客户端池名称
     * 为实际不同的数据源使用不同的名称；
     * 如果想创建临时数据源执行即时查询，UUID是很好的组件
     *
     * @return 数据源名称
     */

    public String getDataSourceName() {
        return getElementName();
    }


    /**
     * 设置客户端等待从池中获取连接的时间
     * 如果超过时间没有可用连接，将抛出异常
     * 时间单位通过`setConnectionTimeoutUnit`设置
     *
     * @return 连接超时时间（秒）
     * @see <a
     *         href="https://vertx.io/docs/apidocs/io/vertx/sqlclient/PoolOptions.html#setConnectionTimeout-int-">Vertx
     *         PoolOptions</a>
     */
    @Nullable
    public Integer getPoolConnectionTimeout() {
        try {
            return readInteger(List.of("poolConnectionTimeout"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取池内连接空闲超时时间。
     * <p>
     * 若配置该值，连接池会主动关闭空闲超过该时长的连接；建议设置为小于 MySQL {@code wait_timeout}
     * 或中间代理的空闲连接超时。
     *
     * @return 空闲超时时间（秒）
     * @see <a
     *         href="https://vertx.io/docs/apidocs/io/vertx/sqlclient/PoolOptions.html#setIdleTimeout-int-">Vertx
     *         PoolOptions</a>
     */
    @Nullable
    public Integer getPoolIdleTimeout() {
        try {
            return readInteger(List.of("poolIdleTimeout"));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    /**
     * 获取连接池是否共享
     * 可以在多个verticle或同一verticle的多个实例之间共享池
     * 这样的池应该在verticle外部创建，否则在创建它的verticle取消部署时会被关闭
     *
     * @return 是否共享连接池
     */
    public boolean getPoolShared() {
        try {
            return readBoolean(List.of("poolShared"));
        } catch (NotConfiguredException e) {
            return true;
        }
    }

    private SslMode parseSslMode(String rawValue) {
        String normalized = rawValue.trim()
                                    .replace('-', '_')
                                    .toUpperCase(Locale.ROOT);
        return SslMode.valueOf(normalized);
    }

    @Nullable
    private String getFirstConfiguredString(String... keys) {
        for (String key : keys) {
            try {
                return readString(List.of(key));
            } catch (NotConfiguredException ignored) {
            }
        }
        return null;
    }


    /**
     * 使用客户端对目标 MySQL 数据库执行一次性 SQL 查询。
     * <p>
     * 客户端将被创建，然后在 SQL 查询后很快关闭。为了安全使用此方法，
     * 请记住启用池共享并为池设置唯一名称。
     * <p>
     * 若 SQL 中包含 {@code ?} 占位符，请使用
     * {@link #instantQuery(Vertx, String, Tuple)} 传入绑定参数。
     *
     * @param sql 不需要参数绑定的完整 SQL 语句
     */
    @TechnicalPreview(since = "5.0.0")
    public Future<ResultMatrix<SimpleResultRow>> instantQuery(Vertx vertx, String sql) {
        return instantQuery(vertx, sql, null);
    }

    /**
     * 使用客户端对目标 MySQL 数据库执行一次性参数化 SQL 查询。
     * <p>
     * 客户端将被创建，然后在 SQL 查询后很快关闭。为了安全使用此方法，
     * 请记住启用池共享并为池设置唯一名称。
     * <p>
     * 当 {@code parameters} 为 {@code null} 时，等价于 {@link #instantQuery(Vertx, String)}。
     *
     * @param sql        包含 {@code ?} 占位符的 SQL 语句，或不需要绑定参数的完整 SQL 语句
     * @param parameters 绑定到 {@code ?} 占位符的参数；不需要参数时可为 {@code null}
     */
    @TechnicalPreview(since = "5.0.4")
    public Future<ResultMatrix<SimpleResultRow>> instantQuery(Vertx vertx, String sql, @Nullable Tuple parameters) {
        var sqlClient = MySQLBuilder.client()
                                    .with(this.getPoolOptions())
                                    .connectingTo(this.getConnectOptions())
                                    .using(vertx)
                                    .build();
        return Future.succeededFuture()
                     .compose(v -> {
                         var query = sqlClient.preparedQuery(sql);
                         if (parameters == null) {
                             return query.execute();
                         }
                         return query.execute(parameters);
                     })
                     .compose(rows -> Future.succeededFuture(ResultMatrix.createSimple(rows)))
                     .andThen(ar -> sqlClient.close());
    }

    /**
     * Handle every batch of rows read, or throw any exceptions in rows handler to stop the process.
     * All dynamic resources would be closed inside this function.
     * <p>
     * 若 SQL 中包含 {@code ?} 占位符，请使用
     * {@link #instantQueryForStream(Keel, String, Tuple, int, Function)} 传入绑定参数。
     *
     * @param sql                SQL without parameter binding
     * @param readWindowSize     how many rows read once
     * @param readWindowFunction the async handler of the read rows
     */

    public Future<Void> instantQueryForStream(Keel keel,
                                              String sql,
                                              int readWindowSize,
                                              Function<RowSet<Row>, Future<Void>> readWindowFunction
    ) {
        return instantQueryForStream(keel, sql, null, readWindowSize, readWindowFunction);
    }

    /**
     * Handle every batch of rows read from a parameterized query, or throw any exceptions in rows handler to stop the process.
     * All dynamic resources would be closed inside this function.
     * <p>
     * When {@code parameters} is {@code null}, it is equivalent to
     * {@link #instantQueryForStream(Keel, String, int, Function)}.
     *
     * @param sql                SQL with {@code ?} placeholders, or a complete SQL without parameters
     * @param parameters         parameters bound to {@code ?} placeholders; nullable when no parameter is required
     * @param readWindowSize     how many rows read once
     * @param readWindowFunction the async handler of the read rows
     */
    public Future<Void> instantQueryForStream(Keel keel,
                                              String sql,
                                              @Nullable Tuple parameters,
                                              int readWindowSize,
                                              Function<RowSet<Row>, Future<Void>> readWindowFunction
    ) {
        return Future.succeededFuture()
                     .compose(v -> {
                         Pool pool = MySQLBuilder.pool()
                                                 .with(this.getPoolOptions())
                                                 .connectingTo(this.getConnectOptions())
                                                 .using(keel)
                                                 .build();
                         return Future.succeededFuture(pool);
                     })
                     .compose(pool -> pool
                             .getConnection()
                             .compose(sqlConnection -> sqlConnection
                                     .prepare(sql)
                                     .compose(preparedStatement -> {
                                         Cursor cursor = parameters == null
                                                 ? preparedStatement.cursor()
                                                 : preparedStatement.cursor(parameters);

                                         return keel.asyncCallRepeatedly(routineResult -> cursor
                                                            .read(readWindowSize)
                                                            .compose(readWindowFunction)
                                                            .compose(v -> {
                                                                if (!cursor.hasMore()) {
                                                                    routineResult.stop();
                                                                    return Future.succeededFuture();
                                                                }
                                                                return Future.succeededFuture();
                                                            }))
                                                    .eventually(cursor::close);
                                     })
                                     .eventually(sqlConnection::close))
                             .eventually(pool::close));
    }
}
