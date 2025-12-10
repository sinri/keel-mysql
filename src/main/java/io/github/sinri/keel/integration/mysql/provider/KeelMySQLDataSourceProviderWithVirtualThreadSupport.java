package io.github.sinri.keel.integration.mysql.provider;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.connection.ClosableNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.connection.DynamicClosableNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.datasource.NamedMySQLDataSource;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


/**
 * Keel MySQL数据源提供者类，特别提供了在虚拟线程模式下运行并创建和管理命名MySQL数据源。
 *
 * @since 5.0.0
 * @deprecated use {@link KeelMySQLDataSourceProvider} with {@link ClosableNamedMySQLConnection}
 */
@TechnicalPreview(since = "5.0.0")
@Deprecated(since = "5.0.0", forRemoval = true)
public class KeelMySQLDataSourceProviderWithVirtualThreadSupport extends KeelMySQLDataSourceProvider {

    public KeelMySQLDataSourceProviderWithVirtualThreadSupport(@NotNull Keel keel) {
        super(keel);
    }

    @NotNull
    public <C extends ClosableNamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> loadInVirtualThread(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction
    ) {
        return load(dataSourceName, sqlConnectionWrapper, connectionSetUpFunction);
    }

    @NotNull
    public <C extends ClosableNamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> loadInVirtualThread(
            @NotNull String dataSourceName,
            @NotNull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        return loadInVirtualThread(dataSourceName, sqlConnectionWrapper, null);
    }

    @NotNull
    public <C extends ClosableNamedMySQLConnection> Future<@NotNull NamedMySQLDataSource<C>> loadDefaultInVirtualThread(@NotNull Function<SqlConnection, C> sqlConnectionWrapper) {
        return loadInVirtualThread(defaultMySQLDataSourceName(getKeel()), sqlConnectionWrapper, null);
    }

    @NotNull
    public Future<@NotNull NamedMySQLDataSource<DynamicClosableNamedMySQLConnection>> loadDynamicInVirtualThread(@NotNull String dataSourceName) {
        return loadInVirtualThread(dataSourceName, sqlConnection -> new DynamicClosableNamedMySQLConnection(sqlConnection, dataSourceName));
    }
}
