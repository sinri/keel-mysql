package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelHolder;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


/**
 * 命名MySQL数据源类，将数据源与命名MySQL连接配对
 *
 * @param <C> 连接类型
 * @since 5.0.0
 */
public interface NamedMySQLDataSource<C extends NamedMySQLConnection> extends KeelHolder {

    static <T extends NamedMySQLConnection> NamedMySQLDataSource<T> create(
            @NotNull Keel keel,
            @NotNull KeelMySQLConfiguration configuration,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            @NotNull Function<SqlConnection, T> sqlConnectionWrapper
    ) {
        return new NamedMySQLDataSourceImpl<>(keel, configuration, connectionSetUpFunction, sqlConnectionWrapper);
    }

    static <T extends NamedMySQLConnection> NamedMySQLDataSource<T> create(
            @NotNull Keel keel,
            @NotNull KeelMySQLConfiguration configuration,
            @NotNull Function<SqlConnection, T> sqlConnectionWrapper
    ) {
        return new NamedMySQLDataSourceImpl<>(keel, configuration, sqlConnectionWrapper);
    }

    /**
     * 获取MySQL配置
     *
     * @return MySQL配置对象
     */
    @NotNull
    KeelMySQLConfiguration getConfiguration();

    /**
     * 获取池中初始化且当前未使用的连接数
     *
     * @return 空闲连接数
     */
    default int getCurrentIdleConnectionCount() {
        return getCurrentInitializedConnectionCount() - getCurrentActiveConnectionCount();
    }

    /**
     * 获取池中初始化的连接总数
     *
     * @return 初始化连接数
     */
    int getCurrentInitializedConnectionCount();

    /**
     * 获取当前正在使用的连接数（即从池中借出的连接）
     *
     * @return 活跃连接数
     */
    int getCurrentActiveConnectionCount();

    /**
     * 获取MySQL完整版本信息
     *
     * @return MySQL版本信息，可能为null
     */
    @Nullable String getFullVersionRef();

    /**
     * 使用连接执行操作
     *
     * @param function 连接操作函数
     * @return 操作结果Future
     */
    @NotNull
    <T> Future<T> withConnection(@NotNull Function<C, Future<T>> function);


    /**
     * 在事务中使用连接执行操作
     *
     * @param function 事务操作函数
     * @return 事务结果Future
     */
    @NotNull
    <T> Future<T> withTransaction(@NotNull Function<C, Future<T>> function);


    /**
     * 关闭数据源
     *
     * @return 关闭操作Future
     */
    @NotNull
    Future<Void> close();


}
