package io.github.sinri.keel.integration.mysql.provider;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.DynamicNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.datasource.NamedMySQLDataSource;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Keel MySQL数据源提供者类，用于创建和管理命名MySQL数据源
 *
 * @since 5.0.0
 */
@NullMarked
public class KeelMySQLDataSourceProvider {

    public KeelMySQLDataSourceProvider() {
    }

    /**
     * 获取默认MySQL数据源名称
     *
     * @return 默认数据源名称
     */
    public static String defaultMySQLDataSourceName() {
        try {
            return ConfigElement.root().readString(List.of("mysql", "default_data_source_name"));
        } catch (NotConfiguredException e) {
            return "default";
        }
    }


    public static KeelMySQLConfiguration getDefaultMySQLConfiguration() {
        return getMySQLConfiguration(defaultMySQLDataSourceName());
    }


    public static KeelMySQLConfiguration getMySQLConfiguration(String dataSourceName) {
        var configuration = ConfigElement.root().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        return new KeelMySQLConfiguration(configuration);
    }


    public <C extends NamedMySQLConnection> Future<NamedMySQLDataSource<C>> load(
            Vertx vertx,
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction
    ) {
        KeelMySQLConfiguration mySQLConfiguration = getMySQLConfiguration(dataSourceName);
        NamedMySQLDataSource<C> dataSource = new NamedMySQLDataSource<>(
                vertx,
                mySQLConfiguration,
                connectionSetUpFunction,
                sqlConnectionWrapper
        );
        return waitForLoading(vertx, dataSource)
                .compose(v -> {
                    return Future.succeededFuture(dataSource);
                });
    }


    protected Future<Void> waitForLoading(Vertx vertx, NamedMySQLDataSource<?> dataSource) {
        KeelMySQLConfiguration mySQLConfiguration = dataSource.getConfiguration();
        PoolOptions poolOptions = mySQLConfiguration.getPoolOptions();
        Promise<Void> initializedPromise = Promise.promise();
        vertx.setTimer(
                poolOptions.getConnectionTimeoutUnit().toMillis(poolOptions.getConnectionTimeout()),
                x -> {
                    initializedPromise.tryFail("MySQL Pool Connection Timeout on testing, the configuration might need adjusting.");
                });
        dataSource.withConnection(c -> {
            initializedPromise.tryComplete();
            return Future.succeededFuture();
        });
        return initializedPromise.future();
    }


    public <C extends NamedMySQLConnection> Future<NamedMySQLDataSource<C>> load(
            Vertx vertx,
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        return load(vertx, dataSourceName, sqlConnectionWrapper, null);
    }


    public Future<NamedMySQLDataSource<DynamicNamedMySQLConnection>> loadDefault(Vertx vertx) {
        return loadDynamic(vertx, defaultMySQLDataSourceName());
    }


    public Future<NamedMySQLDataSource<DynamicNamedMySQLConnection>> loadDynamic(Vertx vertx, String dataSourceName) {
        return load(vertx, dataSourceName, sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, dataSourceName));
    }

}
