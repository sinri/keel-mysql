package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.util.Objects;
import java.util.function.Function;

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
        try (ClosableNamedMySQLConnection<C> closableNamedMySQLConnection = fetchClosableMySQLConnection()) {
            return function.apply(closableNamedMySQLConnection.getNamedMySQLConnection());
        }
    }

    public <T extends @Nullable Objects> T withTransaction(Function<C, T> function) {
        try (ClosableNamedMySQLConnection<C> closableNamedMySQLConnection = fetchClosableMySQLConnection()) {
            C c = closableNamedMySQLConnection.getNamedMySQLConnection();
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
    public ClosableNamedMySQLConnection<C> fetchClosableMySQLConnection() {
        SqlConnection sqlConnection = getMappedDataSource().getPool().getConnection().await();
        C c = getMappedDataSource().getSqlConnectionWrapper().apply(sqlConnection);
        return new ClosableNamedMySQLConnection<>(c);
    }

    public static class ClosableNamedMySQLConnection<C extends NamedMySQLConnection> implements Closeable {
        private final C namedMySQLConnection;

        public ClosableNamedMySQLConnection(C namedMySQLConnection) {
            this.namedMySQLConnection = namedMySQLConnection;
        }

        public C getNamedMySQLConnection() {
            return namedMySQLConnection;
        }

        @Override
        public void close() {
            namedMySQLConnection.close().await();
        }
    }
}
