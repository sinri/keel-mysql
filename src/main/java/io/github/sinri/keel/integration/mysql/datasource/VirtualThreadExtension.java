package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.vertx.sqlclient.Transaction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

@Deprecated(since = "5.0.0", forRemoval = true)
@TechnicalPreview(since = "5.0.0")
@NullMarked
public class VirtualThreadExtension<C extends NamedMySQLConnection> {

    private final NamedMySQLDataSource<C> mappedDataSource;

    public VirtualThreadExtension(NamedMySQLDataSource<C> mappedDataSource) {
        this.mappedDataSource = mappedDataSource;
    }

    protected NamedMySQLDataSource<C> getMappedDataSource() {
        return this.mappedDataSource;
    }

    public <T extends @Nullable Objects> T withConnection(Function<C, T> function) {
        try (C c = fetchClosableMySQLConnection()) {
            return function.apply(c);
        }
    }

    public <T extends @Nullable Objects> T withTransaction(Function<C, T> function) {
        try (C c = fetchClosableMySQLConnection()) {
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
     * In virtual thread mode, fetch a closable named mysql connection to use in try-with closure.
     */
    public C fetchClosableMySQLConnection() {
        var sqlConnection = getMappedDataSource().getPool().getConnection().await();
        return getMappedDataSource().getSqlConnectionWrapper().apply(sqlConnection);
    }


}
