package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.ClosableNamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@TechnicalPreview(since = "5.0.0")
class NamedMySQLDataSourceWithVirtualThreadSupportImpl<C extends ClosableNamedMySQLConnection>
        extends NamedMySQLDataSourceImpl<C>
        implements NamedMySQLDataSourceWithVirtualThreadSupport<C> {
    public NamedMySQLDataSourceWithVirtualThreadSupportImpl(@NotNull Keel keel, @NotNull KeelMySQLConfiguration configuration, @NotNull Function<SqlConnection, C> sqlConnectionWrapper) {
        super(keel, configuration, sqlConnectionWrapper);
    }

    public NamedMySQLDataSourceWithVirtualThreadSupportImpl(@NotNull Keel keel, @NotNull KeelMySQLConfiguration configuration, @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction, @NotNull Function<SqlConnection, C> sqlConnectionWrapper) {
        super(keel, configuration, connectionSetUpFunction, sqlConnectionWrapper);
    }

    @Override
    public <T> @NotNull T withConnectionInVirtualThread(@NotNull Function<C, T> function) {
        try (C c = fetchMySQLConnectionInVirtualThread()) {
            return function.apply(c);
        }
    }

    @Override
    public <T> @NotNull T withTransactionInVirtualThread(@NotNull Function<C, T> function) {
        try (C c = fetchMySQLConnectionInVirtualThread()) {
            Transaction transaction = c.getSqlConnection().begin().await();
            T t;
            try {
                t = function.apply(c);
            } catch (Throwable e) {
                transaction.rollback().await();
                throw e;
            }
            transaction.commit().await();
            return t;
        }
    }

    /**
     * In virtual thread mode, fetch a closable named mysql connection (wrapper) to use in try-with closure.
     */
    public C fetchMySQLConnectionInVirtualThread() {
        SqlConnection sqlConnection = getPool().getConnection().await();
        return getSqlConnectionWrapper().apply(sqlConnection);
    }
}
