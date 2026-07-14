package io.github.sinri.keel.integration.mysql;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeelMySQLConfigurationQueryTest {
    private static final Vertx VERTX = Vertx.vertx();
    private static final Keel KEEL = Keel.create(VERTX);

    @AfterAll
    static void closeVertx() {
        VERTX.close().toCompletionStage().toCompletableFuture().join();
    }

    @Test
    void instantQueryBindsParametersAndAlwaysClosesClient() {
        SqlClient client = mock(SqlClient.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        Tuple tuple = Tuple.of("value", 7);
        RowSet<Row> rows = emptyRows();
        when(client.preparedQuery("select ?, ?")).thenReturn(query);
        when(query.execute(tuple)).thenReturn(Future.succeededFuture(rows));
        when(client.close()).thenReturn(Future.succeededFuture());
        TestConfiguration configuration = configuration(client, null);

        assertEquals(0, await(configuration.instantQuery(VERTX, "select ?, ?", tuple)).size());

        verify(query).execute(tuple);
        verify(client).close();

        RuntimeException failure = new RuntimeException("query failed");
        when(query.execute()).thenReturn(Future.failedFuture(failure));
        CompletionException thrown = assertThrows(CompletionException.class,
                () -> await(configuration.instantQuery(VERTX, "select ?, ?", null)));
        assertSame(failure, thrown.getCause());
        verify(client, times(2)).close();
    }

    @Test
    void streamQueryBindsParametersReadsWindowsAndClosesInOrder() {
        Pool pool = mock(Pool.class);
        SqlConnection connection = mock(SqlConnection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Cursor cursor = mock(Cursor.class);
        Tuple tuple = Tuple.of(42);
        RowSet<Row> first = emptyRows();
        RowSet<Row> second = emptyRows();
        when(pool.getConnection()).thenReturn(Future.succeededFuture(connection));
        when(connection.prepare("select ?")).thenReturn(Future.succeededFuture(statement));
        when(statement.cursor(tuple)).thenReturn(cursor);
        when(cursor.read(10)).thenReturn(Future.succeededFuture(first), Future.succeededFuture(second));
        when(cursor.hasMore()).thenReturn(true, false);
        when(cursor.close()).thenReturn(Future.succeededFuture());
        when(connection.close()).thenReturn(Future.succeededFuture());
        when(pool.close()).thenReturn(Future.succeededFuture());
        AtomicInteger windows = new AtomicInteger();

        await(configuration(null, pool).instantQueryForStream(
                KEEL, "select ?", tuple, 10, rows -> {
                    windows.incrementAndGet();
                    return Future.succeededFuture();
                }));

        assertEquals(2, windows.get());
        verify(cursor, times(2)).read(10);
        var order = inOrder(cursor, connection, pool);
        order.verify(cursor).close();
        order.verify(connection).close();
        order.verify(pool).close();
    }

    @Test
    void streamQueryFailureStopsReadingAndStillClosesEveryResource() {
        Pool pool = mock(Pool.class);
        SqlConnection connection = mock(SqlConnection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        Cursor cursor = mock(Cursor.class);
        RowSet<Row> rows = emptyRows();
        when(pool.getConnection()).thenReturn(Future.succeededFuture(connection));
        when(connection.prepare("select 1")).thenReturn(Future.succeededFuture(statement));
        when(statement.cursor()).thenReturn(cursor);
        when(cursor.read(5)).thenReturn(Future.succeededFuture(rows));
        when(cursor.close()).thenReturn(Future.succeededFuture());
        when(connection.close()).thenReturn(Future.succeededFuture());
        when(pool.close()).thenReturn(Future.succeededFuture());
        RuntimeException failure = new RuntimeException("reader failed");

        CompletionException thrown = assertThrows(CompletionException.class, () -> await(
                configuration(null, pool).instantQueryForStream(
                        KEEL, "select 1", 5, ignored -> Future.failedFuture(failure))));

        assertSame(failure, thrown.getCause());
        verify(cursor).read(5);
        verify(cursor, never()).hasMore();
        var order = inOrder(cursor, connection, pool);
        order.verify(cursor).close();
        order.verify(connection).close();
        order.verify(pool).close();
    }

    private static TestConfiguration configuration(SqlClient client, Pool pool) {
        Properties properties = new Properties();
        properties.setProperty("mysql.test.username", "user");
        properties.setProperty("mysql.test.password", "password");
        ConfigElement root = new ConfigElement("");
        root.loadData(properties);
        try {
            return new TestConfiguration(root.extract("mysql", "test"), client, pool);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static RowSet<Row> emptyRows() {
        RowSet<Row> rows = mock(RowSet.class);
        when(rows.iterator()).thenReturn(new EmptyRowIterator());
        when(rows.next()).thenReturn(null);
        return rows;
    }

    private static <T> T await(Future<T> future) {
        return future.toCompletionStage().toCompletableFuture().join();
    }

    private static final class TestConfiguration extends KeelMySQLConfiguration {
        private final SqlClient client;
        private final Pool pool;

        private TestConfiguration(ConfigElement base, SqlClient client, Pool pool) {
            super(base);
            this.client = client;
            this.pool = pool;
        }

        @Override
        protected SqlClient createSqlClient(Vertx vertx) {
            return client;
        }

        @Override
        protected Pool createPool(Keel keel) {
            return pool;
        }
    }

    private static final class EmptyRowIterator implements RowIterator<Row> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Row next() {
            throw new NoSuchElementException();
        }
    }
}
