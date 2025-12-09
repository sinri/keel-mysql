package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.ClosableNamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@TechnicalPreview(since = "5.0.0")
public interface NamedMySQLDataSourceWithVirtualThreadSupport<C extends ClosableNamedMySQLConnection>
        extends NamedMySQLDataSource<C> {

    static <T extends ClosableNamedMySQLConnection> NamedMySQLDataSourceWithVirtualThreadSupport<T> create(
            @NotNull Keel keel,
            @NotNull KeelMySQLConfiguration configuration,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            @NotNull Function<SqlConnection, T> sqlConnectionWrapper
    ) {
        return new NamedMySQLDataSourceWithVirtualThreadSupportImpl<>(keel, configuration, connectionSetUpFunction, sqlConnectionWrapper);
    }

    static <T extends ClosableNamedMySQLConnection> NamedMySQLDataSourceWithVirtualThreadSupport<T> create(
            @NotNull Keel keel,
            @NotNull KeelMySQLConfiguration configuration,
            @NotNull Function<SqlConnection, T> sqlConnectionWrapper
    ) {
        return new NamedMySQLDataSourceWithVirtualThreadSupportImpl<>(keel, configuration, sqlConnectionWrapper);
    }

    @TechnicalPreview(since = "5.0.0")
    @NotNull
    <T> T withConnectionInVirtualThread(@NotNull Function<C, T> function);

    @TechnicalPreview(since = "5.0.0")
    @NotNull
    <T> T withTransactionInVirtualThread(@NotNull Function<C, T> function);

    /**
     * In virtual thread mode, fetch a closable named mysql connection (wrapper) to use in try-with closure.
     */
    @TechnicalPreview(since = "5.0.0")
    C fetchMySQLConnectionInVirtualThread();
}
