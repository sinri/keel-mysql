package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.integration.mysql.statement.RawStatement;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunnableStatementTupleTest {
    private static final Vertx VERTX = Vertx.vertx();
    private static final Keel KEEL = Keel.create(VERTX);

    @AfterAll
    static void closeVertx() {
        VERTX.close().toCompletionStage().toCompletableFuture().join();
    }

    @Test
    void executeThroughPrepareBindsTupleAndUsesNoArgumentPathForNullOrEmpty() {
        SqlConnection connection = mock(SqlConnection.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        RowSet<Row> rows = rows(2, 3, null);
        when(connection.preparedQuery("select ?, ?")).thenReturn(query);
        when(query.execute(any(Tuple.class))).thenReturn(Future.succeededFuture(rows));
        when(query.execute()).thenReturn(Future.succeededFuture(rows));
        RunnableStatement statement = runnable("select ?, ?", connection);
        Tuple parameters = Tuple.of("first", 2);

        assertSame(rows, statement.executeThroughPrepare(parameters).result().getRowSet());
        statement.executeThroughPrepare(null).result();
        statement.executeThroughPrepare(Tuple.tuple()).result();

        verify(query).execute(parameters);
        verify(query, times(2)).execute();
    }

    @Test
    void executeThroughPreparePropagatesFailure() {
        SqlConnection connection = mock(SqlConnection.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        RuntimeException failure = new RuntimeException("query failed");
        when(connection.preparedQuery("select ?")).thenReturn(query);
        when(query.execute(any(Tuple.class))).thenReturn(Future.failedFuture(failure));

        Future<?> future = runnable("select ?", connection).executeThroughPrepare(Tuple.of(1));

        assertSame(failure, future.cause());
    }

    @Test
    void reusablePreparedStatementRunsSequentiallyAndCloses() {
        SqlConnection connection = mock(SqlConnection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        Tuple first = Tuple.of(1);
        Tuple second = Tuple.of(2);
        RowSet<Row> firstRows = rows(0, 1, null);
        RowSet<Row> secondRows = rows(0, 2, null);
        when(connection.prepare("insert into t values (?)")).thenReturn(Future.succeededFuture(preparedStatement));
        when(preparedStatement.query()).thenReturn(query);
        when(query.execute(first)).thenReturn(Future.succeededFuture(firstRows));
        when(query.execute(second)).thenReturn(Future.succeededFuture(secondRows));
        when(preparedStatement.close()).thenReturn(Future.succeededFuture());

        var results = await(runnable("insert into t values (?)", connection)
                .executeThroughPrepare(KEEL, List.of(first, second)));

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getTotalAffectedRows());
        assertEquals(2, results.get(1).getTotalAffectedRows());
        var order = inOrder(query, preparedStatement);
        order.verify(query).execute(first);
        order.verify(query).execute(second);
        order.verify(preparedStatement).close();
    }

    @Test
    void reusablePreparedStatementStopsAndClosesAfterFailure() {
        SqlConnection connection = mock(SqlConnection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        Tuple first = Tuple.of(1);
        Tuple second = Tuple.of(2);
        Tuple third = Tuple.of(3);
        RuntimeException failure = new RuntimeException("second failed");
        RowSet<Row> firstRows = rows(0, 1, null);
        when(connection.prepare(any())).thenReturn(Future.succeededFuture(preparedStatement));
        when(preparedStatement.query()).thenReturn(query);
        when(query.execute(first)).thenReturn(Future.succeededFuture(firstRows));
        when(query.execute(second)).thenReturn(Future.failedFuture(failure));
        when(preparedStatement.close()).thenReturn(Future.succeededFuture());

        CompletionException thrown = assertThrows(CompletionException.class, () -> await(
                runnable("insert", connection).executeThroughPrepare(KEEL, List.of(first, second, third))));

        assertSame(failure, thrown.getCause());
        verify(query, never()).execute(third);
        verify(preparedStatement).close();
    }

    @Test
    void writeBatchPreservesResultChainAndClosesOnSuccessAndFailure() {
        SqlConnection connection = mock(SqlConnection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        @SuppressWarnings("unchecked") PreparedQuery<RowSet<Row>> query = mock(PreparedQuery.class);
        List<Tuple> batch = List.of(Tuple.of(1), Tuple.of(2));
        RowSet<Row> second = rows(0, 4, null);
        RowSet<Row> first = rows(0, 3, second);
        when(connection.prepare(any())).thenReturn(Future.succeededFuture(preparedStatement));
        when(preparedStatement.query()).thenReturn(query);
        when(query.executeBatch(batch)).thenReturn(Future.succeededFuture(first));
        when(preparedStatement.close()).thenReturn(Future.succeededFuture());
        RunnableStatementForWrite statement = writable("insert", connection);

        var result = await(statement.execute(KEEL, batch));

        assertEquals(7, result.getTotalAffectedRows());
        assertEquals(List.of(first, second), result.getRowSets());
        verify(query).executeBatch(batch);
        verify(preparedStatement).close();

        RuntimeException failure = new RuntimeException("batch failed");
        when(query.executeBatch(List.of())).thenReturn(Future.failedFuture(failure));
        CompletionException thrown = assertThrows(CompletionException.class,
                () -> await(statement.execute(KEEL, List.of())));
        assertSame(failure, thrown.getCause());
        verify(preparedStatement, times(2)).close();
    }

    @Test
    @SuppressWarnings("removal")
    void removedRawStatementConstructorFailsExplicitly() {
        assertThrows(UnsupportedOperationException.class, () -> new RawStatement("select 1", true));
    }

    private static RunnableStatement runnable(String sql, SqlConnection connection) {
        RunnableStatement statement = new RunnableStatement(new RawStatement(sql));
        statement.setSQLConnection(connection);
        return statement;
    }

    private static RunnableStatementForWrite writable(String sql, SqlConnection connection) {
        RunnableStatementForWrite statement = new RunnableStatementForWrite(new RawStatement(sql));
        statement.setSQLConnection(connection);
        return statement;
    }

    @SuppressWarnings("unchecked")
    private static RowSet<Row> rows(int size, int rowCount, RowSet<Row> next) {
        RowSet<Row> rows = mock(RowSet.class);
        when(rows.size()).thenReturn(size);
        when(rows.rowCount()).thenReturn(rowCount);
        when(rows.next()).thenReturn(next);
        return rows;
    }

    private static <T> T await(Future<T> future) {
        return future.toCompletionStage().toCompletableFuture().join();
    }
}
