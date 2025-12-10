package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@TechnicalPreview(since = "5.0.0")
public class VirtualThreadExtension<C extends NamedMySQLConnection> {
    @NotNull
    private final NamedMySQLDataSource<C> mappedDataSource;

    public VirtualThreadExtension(@NotNull NamedMySQLDataSource<C> mappedDataSource) {
        this.mappedDataSource = mappedDataSource;
    }

    protected @NotNull NamedMySQLDataSource<C> getMappedDataSource() {
        return this.mappedDataSource;
    }

    public <T> @NotNull T withConnection(@NotNull Function<C, T> function) {
        try (C c = fetchMySQLConnection()) {
            return function.apply(c);
        }
    }

    public <T> @NotNull T withTransaction(@NotNull Function<C, T> function) {
        try (C c = fetchMySQLConnection()) {
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
    public C fetchMySQLConnection() {
        SqlConnection sqlConnection = getMappedDataSource().getPool().getConnection().await();
        return getMappedDataSource().getSqlConnectionWrapper().apply(sqlConnection);
    }
}
