package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.result.pagination.PaginationResult;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RunnableStatementForReadAndPagination extends RunnableStatementForRead {
    public RunnableStatementForReadAndPagination(AnyStatement<?> statement) {
        super(statement);
    }

    private SelectStatement getSelectStatementMixin() {
        return (SelectStatement) getStatement();
    }

    /**
     * Call from this instance, as the original query as Select Statement for all rows in a certain order.
     *
     * @param pageNo   since 1.
     * @param pageSize a number
     */
    public Future<PaginationResult> executeForPagination(long pageNo, long pageSize) {
        SelectStatement selectStatement = this.getSelectStatementMixin();
        if (pageSize <= 0) throw new IllegalArgumentException("page size <= 0");
        if (pageNo < 1) throw new IllegalArgumentException("page no < 1");
        var countStatement = new SelectStatement(selectStatement)
                .resetColumns()
                .columnWithAlias("count(*)", "total")
                .limit(0, 0);
        selectStatement.limit(pageSize, (pageNo - 1) * pageSize);

        Future<Long> totalFuture = countStatement.attachToConnection(getSqlConnection())
                                                 .executeForResultMatrix()
                                                 .compose(resultMatrix -> {
                                                     try {
                                                         long total = resultMatrix.getFirstRow()
                                                                                  .readLongRequired("total");
                                                         return Future.succeededFuture(total);
                                                     } catch (KeelSQLResultRowIndexError e) {
                                                         throw new RuntimeException(e);
                                                     }
                                                 });
        Future<ResultMatrix<SimpleResultRow>> pageFuture = selectStatement.attachToConnection(getSqlConnection())
                                                                          .executeForResultMatrix();
        return Future.all(totalFuture, pageFuture)
                     .compose(compositeFuture -> {
                         Long total = compositeFuture.resultAt(0);
                         ResultMatrix<SimpleResultRow> resultMatrix = compositeFuture.resultAt(1);
                         return Future.succeededFuture(new PaginationResult(total, resultMatrix));
                     });
    }

}
