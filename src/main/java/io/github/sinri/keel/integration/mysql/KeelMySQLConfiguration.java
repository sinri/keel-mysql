package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.ConfigPropertiesBuilder;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Keel MySQL配置类，用于管理MySQL连接和连接池的配置信息。
 *
 * @since 5.0.0
 */
public class KeelMySQLConfiguration extends ConfigElement {
    public KeelMySQLConfiguration(@NotNull ConfigElement base) {
        super(base);
    }

    /**
     * 生成MySQL配置的属性字符串
     *
     * @param dataSourceName      数据源名称
     * @param mySQLConnectOptions MySQL连接选项
     * @param poolOptions         连接池选项
     * @return 配置属性字符串
     */
    @NotNull
    public static String generatePropertiesForConfig(@NotNull String dataSourceName, @NotNull MySQLConnectOptions mySQLConnectOptions, @NotNull PoolOptions poolOptions) {
        var builder = new ConfigPropertiesBuilder();
        builder.setPrefix("mysql", dataSourceName);

        builder.add("host", mySQLConnectOptions.getHost());
        builder.add("port", String.valueOf(mySQLConnectOptions.getPort()));
        builder.add("username", mySQLConnectOptions.getUser());
        builder.add("password", mySQLConnectOptions.getPassword());
        builder.add("schema", mySQLConnectOptions.getDatabase());
        builder.add("charset", mySQLConnectOptions.getCharset());
        builder.add("poolMaxSize", String.valueOf(poolOptions.getMaxSize()));
        builder.add("poolShared", (poolOptions.isShared() ? "YES" : "NO"));
        builder.add("poolConnectionTimeout", String.valueOf(poolOptions.getConnectionTimeout()));

        return builder.writeToString();
    }

    /**
     * 获取MySQL连接选项
     *
     * @return MySQL连接选项
     */
    @NotNull
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

        return mySQLConnectOptions;
    }

    /**
     * 获取连接池选项
     *
     * @return 连接池选项
     */
    @NotNull
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
        poolOptions.setShared(getPoolShared());
        poolOptions.setName("Keel-MySQL-Pool-" + this.getDataSourceName());
        return poolOptions;
    }

    /**
     * 获取MySQL主机地址
     *
     * @return 主机地址
     */
    @NotNull
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
    @NotNull
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


    /**
     * 使用客户端对目标MySQL数据库执行一次性SQL查询
     * 客户端将被创建，然后在SQL查询后很快关闭
     * 为了安全使用此方法，请记住启用池共享并为池设置唯一名称
     *
     * @param sql 确认已过滤的SQL语句
     */
    @TechnicalPreview(since = "5.0.0")
    @NotNull
    public Future<ResultMatrix> instantQuery(@NotNull Keel keel, @NotNull String sql) {
        var sqlClient = MySQLBuilder.client()
                                    .with(this.getPoolOptions())
                                    .connectingTo(this.getConnectOptions())
                                    .using(keel.getVertx())
                                    .build();
        return Future.succeededFuture()
                     .compose(v -> sqlClient.preparedQuery(sql).execute()
                                            .compose(rows -> Future.succeededFuture(ResultMatrix.create(rows))))
                     .andThen(ar -> sqlClient.close());
    }

    /**
     * Handle every batch of rows read, or throw any exceptions in rows handler to stop the process.
     * All dynamic resources would be closed inside this function.
     *
     * @param sql                Here we just believe the application would give a confirmed and filtered SQL when call
     *                           this method.
     * @param readWindowSize     how many rows read once
     * @param readWindowFunction the async handler of the read rows
     */
    @NotNull
    public Future<Void> instantQueryForStream(@NotNull Keel keel, @NotNull String sql, int readWindowSize, @NotNull Function<RowSet<Row>, Future<Void>> readWindowFunction) {
        return Future.succeededFuture()
                     .compose(v -> {
                         Pool pool = MySQLBuilder.pool()
                                                 .with(this.getPoolOptions())
                                                 .connectingTo(this.getConnectOptions())
                                                 .using(keel.getVertx())
                                                 .build();
                         return Future.succeededFuture(pool);
                     })
                     .compose(pool -> pool
                             .getConnection()
                             .compose(sqlConnection -> sqlConnection
                                     .prepare(sql)
                                     .compose(preparedStatement -> {
                                         Cursor cursor = preparedStatement.cursor();

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
