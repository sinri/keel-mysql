package io.github.sinri.keel.integration.mysql.provider;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelHolder;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.DynamicNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.datasource.NamedMySQLDataSource;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.PoolOptions;
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
public class KeelMySQLDataSourceProvider implements KeelHolder {
    private final @NotNull Keel keel;

    public KeelMySQLDataSourceProvider(@NotNull Keel keel) {
        this.keel = keel;
    }

    /**
     * 获取默认MySQL数据源名称
     *
     * @return 默认数据源名称
     */
    @NotNull
    public static String defaultMySQLDataSourceName(@NotNull Keel keel) {
        try {
            return keel.getConfiguration().readString(List.of("mysql", "default_data_source_name"));
        } catch (ConfigTree.NotConfiguredException e) {
            return "default";
        }
    }

    @NotNull
    public static KeelMySQLConfiguration getDefaultMySQLConfiguration(@NotNull Keel keel) {
        return getMySQLConfiguration(keel, defaultMySQLDataSourceName(keel));
    }

    @NotNull
    public static KeelMySQLConfiguration getMySQLConfiguration(@NotNull Keel keel, @NotNull String dataSourceName) {
        var configuration = keel.getConfiguration().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        return new KeelMySQLConfiguration(configuration);
    }

    @NotNull
    public <C extends NamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> load(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction
    ) {
        KeelMySQLConfiguration mySQLConfiguration = getMySQLConfiguration(getKeel(), dataSourceName);
        if (connectionSetUpFunction == null) {
            connectionSetUpFunction = sqlConnection -> Future.succeededFuture();
        }
        var dataSource = NamedMySQLDataSource.create(
                getKeel(),
                mySQLConfiguration,
                connectionSetUpFunction,
                sqlConnectionWrapper
        );
        PoolOptions poolOptions = mySQLConfiguration.getPoolOptions();
        Promise<Void> initializedPromise = Promise.promise();
        getVertx().setTimer(
                poolOptions.getConnectionTimeoutUnit().toMillis(poolOptions.getConnectionTimeout()),
                x -> {
                    initializedPromise.tryFail("MySQL Pool Connection Timeout on testing, the configuration might need adjusting.");
                });
        dataSource.withConnection(c -> {
            initializedPromise.tryComplete();
            return Future.succeededFuture();
        });
        return initializedPromise.future().map(v -> dataSource);
    }

    @NotNull
    public <C extends NamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> load(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        return load(dataSourceName, sqlConnectionWrapper, null);
    }

    @NotNull
    public <C extends NamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> loadDefault(@NotNull Function<SqlConnection, C> sqlConnectionWrapper) {
        return load(defaultMySQLDataSourceName(getKeel()), sqlConnectionWrapper, null);
    }

    @NotNull
    public Future<@NotNull NamedMySQLDataSource<DynamicNamedMySQLConnection>> loadDynamic(@NotNull String dataSourceName) {
        return load(dataSourceName, sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, dataSourceName));
    }

    @Override
    final public @NotNull Keel getKeel() {
        return keel;
    }
}
