package io.github.sinri.keel.integration.mysql.provider;

import io.github.sinri.keel.base.KeelHolder;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.ClosableNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.connection.DynamicClosableNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.datasource.NamedMySQLDataSourceWithVirtualThreadSupport;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Keel MySQL数据源提供者类，用于创建和管理命名MySQL数据源
 *
 * @since 5.0.0
 */
@TechnicalPreview(since = "5.0.0")
public interface KeelMySQLDataSourceProviderWithVirtualThreadSupport extends KeelHolder {

    /**
     * 获取默认MySQL数据源名称
     *
     * @return 默认数据源名称
     */
    @NotNull
    default String defaultMySQLDataSourceName() {
        try {
            return getKeel().getConfiguration().readString(List.of("mysql", "default_data_source_name"));
        } catch (ConfigTree.NotConfiguredException e) {
            return "default";
        }
    }

    /**
     * 初始化命名MySQL数据源
     *
     * @param dataSourceName       数据源名称
     * @param sqlConnectionWrapper SQL连接包装器
     * @return 命名MySQL数据源
     */
    default @NotNull <C extends ClosableNamedMySQLConnection> NamedMySQLDataSourceWithVirtualThreadSupport<C> initializeNamedMySQLDataSourceWithVirtualThreadSupport(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        return initializeNamedMySQLDataSourceWithVirtualThreadSupport(dataSourceName, sqlConnectionWrapper, null, Promise.promise());
    }

    /**
     * 加载命名MySQL数据源并在实际可用性确认后返回Future
     *
     * @param dataSourceName       数据源名称
     * @param sqlConnectionWrapper SQL连接包装器
     * @return 包含命名MySQL数据源的Future
     */
    default <C extends ClosableNamedMySQLConnection> Future<NamedMySQLDataSourceWithVirtualThreadSupport<C>> loadNamedMySQLDataSource(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        Promise<Void> initializedPromise = Promise.promise();
        var dataSource = initializeNamedMySQLDataSourceWithVirtualThreadSupport(dataSourceName, sqlConnectionWrapper, null, initializedPromise);
        return initializedPromise.future().map(v -> dataSource);
    }

    /**
     * 初始化命名MySQL数据源，支持自定义连接设置函数
     *
     * @param dataSourceName          数据源名称
     * @param sqlConnectionWrapper    SQL连接包装器
     * @param connectionSetUpFunction 连接设置函数
     * @param initializedPromise      初始化完成Promise
     * @return 命名MySQL数据源
     */
    @NotNull
    default <C extends ClosableNamedMySQLConnection> NamedMySQLDataSourceWithVirtualThreadSupport<C> initializeNamedMySQLDataSourceWithVirtualThreadSupport(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            Promise<Void> initializedPromise
    ) {
        var configuration = getKeel().getConfiguration().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        KeelMySQLConfiguration mySQLConfigure = new KeelMySQLConfiguration(configuration);
        if (connectionSetUpFunction == null) {
            connectionSetUpFunction = sqlConnection -> Future.succeededFuture();
        }
        var dataSource = NamedMySQLDataSourceWithVirtualThreadSupport.create(getKeel(), mySQLConfigure, connectionSetUpFunction, sqlConnectionWrapper);

        getVertx().setTimer(
                mySQLConfigure.getPoolOptions().getConnectionTimeout() * 1000L,
                x -> {
                    initializedPromise.tryFail("MySQL Pool Connection Timeout on testing, the configuration might need adjusting.");
                });
        dataSource.withConnection(c -> {
            initializedPromise.tryComplete();
            return Future.succeededFuture();
        });

        return dataSource;
    }

    /**
     * 初始化动态命名MySQL数据源
     *
     * @param dataSourceName 数据源名称
     * @return 动态命名MySQL数据源
     */
    default @NotNull NamedMySQLDataSourceWithVirtualThreadSupport<DynamicClosableNamedMySQLConnection> initializeDynamicNamedMySQLDataSource(@NotNull String dataSourceName) {
        return initializeNamedMySQLDataSourceWithVirtualThreadSupport(
                dataSourceName,
                sqlConnection -> new DynamicClosableNamedMySQLConnection(sqlConnection, dataSourceName));
    }
}
