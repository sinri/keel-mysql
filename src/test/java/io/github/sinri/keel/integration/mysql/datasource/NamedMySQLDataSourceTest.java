package io.github.sinri.keel.integration.mysql.datasource;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.integration.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.integration.mysql.connection.DynamicNamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.integration.mysql.exception.KeelMySQLException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NamedMySQLDataSourceTest {

    @Test
    void withConnectionShouldCloseAndRestoreCounterAfterSuccess() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.succeededFuture());
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );

        String result = dataSource.withConnection(connection -> Future.succeededFuture("done"))
                                  .toCompletionStage()
                                  .toCompletableFuture()
                                  .join();

        assertEquals("done", result);
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void withConnectionShouldCloseAndRestoreCounterWhenCallbackThrowsSynchronously() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.succeededFuture());
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );
        RuntimeException businessFailure = new RuntimeException("synchronous failure");

        Throwable failure = awaitFailure(dataSource.withConnection(connection -> {
            throw businessFailure;
        }));

        KeelMySQLException sqlException = assertInstanceOf(KeelMySQLException.class, failure);
        assertSame(businessFailure, sqlException.getCause());
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void withConnectionShouldCloseAndRestoreCounterWhenCallbackReturnsFailedFuture() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.succeededFuture());
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );
        RuntimeException businessFailure = new RuntimeException("asynchronous failure");

        Throwable failure = awaitFailure(dataSource.withConnection(
                connection -> Future.failedFuture(businessFailure)
        ));

        KeelMySQLException sqlException = assertInstanceOf(KeelMySQLException.class, failure);
        assertSame(businessFailure, sqlException.getCause());
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void connectionWrapperFailureShouldCloseUnderlyingConnection() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.succeededFuture());
        RuntimeException wrapperFailure = new RuntimeException("wrapper failure");
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> {
                    throw wrapperFailure;
                }
        );

        Throwable failure = awaitFailure(dataSource.withConnection(
                connection -> Future.succeededFuture("unused")
        ));

        KeelMySQLConnectionException connectionException =
                assertInstanceOf(KeelMySQLConnectionException.class, failure);
        assertSame(wrapperFailure, connectionException.getCause());
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void withConnectionShouldFailAndRestoreCounterWhenCloseFails() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        RuntimeException closeFailure = new RuntimeException("close failure");
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.failedFuture(closeFailure));
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );

        Throwable failure = awaitFailure(dataSource.withConnection(
                connection -> Future.succeededFuture("done")
        ));

        assertSame(closeFailure, failure);
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void withConnectionShouldPreserveBusinessFailureWhenCloseAlsoFails() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        RuntimeException businessFailure = new RuntimeException("business failure");
        RuntimeException closeFailure = new RuntimeException("close failure");
        SqlConnection sqlConnection = createSqlConnection(closeCalls, Future.failedFuture(closeFailure));
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );

        Throwable failure = awaitFailure(dataSource.withConnection(
                connection -> Future.failedFuture(businessFailure)
        ));

        KeelMySQLException sqlException = assertInstanceOf(KeelMySQLException.class, failure);
        assertSame(businessFailure, sqlException.getCause());
        assertEquals(1, sqlException.getSuppressed().length);
        assertSame(closeFailure, sqlException.getSuppressed()[0]);
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void withTransactionShouldRollbackAndCloseWhenCallbackThrowsSynchronously() throws Exception {
        AtomicInteger closeCalls = new AtomicInteger();
        AtomicInteger rollbackCalls = new AtomicInteger();
        Transaction transaction = (Transaction) Proxy.newProxyInstance(
                Transaction.class.getClassLoader(),
                new Class<?>[]{Transaction.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("rollback")) {
                        rollbackCalls.incrementAndGet();
                        return Future.succeededFuture();
                    }
                    if (method.getName().equals("toString")) {
                        return "TestTransaction";
                    }
                    throw new UnsupportedOperationException(method.toString());
                }
        );
        SqlConnection sqlConnection = createSqlConnection(
                closeCalls,
                Future.succeededFuture(),
                Future.succeededFuture(transaction)
        );
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = createDataSource(
                sqlConnection,
                connection -> new DynamicNamedMySQLConnection(connection, "test")
        );
        RuntimeException businessFailure = new RuntimeException("synchronous failure");

        Throwable failure = awaitFailure(dataSource.withTransaction(connection -> {
            throw businessFailure;
        }));

        assertInstanceOf(KeelMySQLException.class, failure);
        assertSame(businessFailure, findRootCause(failure));
        assertEquals(1, rollbackCalls.get());
        assertEquals(1, closeCalls.get());
        assertEquals(0, dataSource.getCurrentActiveConnectionCount());
    }

    @Test
    void fetchConnectionInVirtualThreadShouldRejectPlatformThreadBeforeConnecting() throws Exception {
        Vertx vertx = Vertx.vertx();
        NamedMySQLDataSource<DynamicNamedMySQLConnection> dataSource = new NamedMySQLDataSource<>(
                vertx,
                createConfiguration(),
                sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, "test")
        );

        try {
            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    dataSource::fetchConnectionInVirtualThread
            );
            assertEquals("This method must be called from a virtual thread", exception.getMessage());
            assertEquals(0, dataSource.getCurrentPoolSize());
            assertEquals(0, dataSource.getCurrentActiveConnectionCount());
        } finally {
            dataSource.close().toCompletionStage().toCompletableFuture().join();
            vertx.close().toCompletionStage().toCompletableFuture().join();
        }
    }

    private KeelMySQLConfiguration createConfiguration() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("mysql.test.host", "127.0.0.1");
        properties.setProperty("mysql.test.username", "test_user");
        properties.setProperty("mysql.test.password", "test_password");

        ConfigElement root = new ConfigElement("");
        root.loadData(properties);
        return new KeelMySQLConfiguration(root.extract("mysql", "test"));
    }

    private NamedMySQLDataSource<DynamicNamedMySQLConnection> createDataSource(
            SqlConnection sqlConnection,
            Function<SqlConnection, DynamicNamedMySQLConnection> wrapper
    ) throws Exception {
        Pool pool = (Pool) Proxy.newProxyInstance(
                Pool.class.getClassLoader(),
                new Class<?>[]{Pool.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getConnection" -> Future.succeededFuture(sqlConnection);
                    case "size" -> 1;
                    case "close" -> Future.succeededFuture();
                    case "toString" -> "TestPool";
                    default -> throw new UnsupportedOperationException(method.toString());
                }
        );
        return new NamedMySQLDataSource<>(pool, createConfiguration(), wrapper);
    }

    private SqlConnection createSqlConnection(AtomicInteger closeCalls, Future<Void> closeResult) {
        return createSqlConnection(closeCalls, closeResult, null);
    }

    private SqlConnection createSqlConnection(
            AtomicInteger closeCalls,
            Future<Void> closeResult,
            Future<Transaction> beginResult
    ) {
        return (SqlConnection) Proxy.newProxyInstance(
                SqlConnection.class.getClassLoader(),
                new Class<?>[]{SqlConnection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close") && method.getParameterCount() == 0) {
                        closeCalls.incrementAndGet();
                        return closeResult;
                    }
                    if (method.getName().equals("begin") && method.getParameterCount() == 0) {
                        if (beginResult == null) {
                            throw new UnsupportedOperationException("begin");
                        }
                        return beginResult;
                    }
                    if (method.getName().equals("toString")) {
                        return "TestSqlConnection";
                    }
                    throw new UnsupportedOperationException(method.toString());
                }
        );
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private Throwable awaitFailure(Future<?> future) {
        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> future.toCompletionStage().toCompletableFuture().join()
        );
        return exception.getCause();
    }
}
