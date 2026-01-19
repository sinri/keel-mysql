package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.pagination.PaginationResult;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.github.sinri.keel.integration.mysql.statement.impl.SelectStatement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
@NullMarked
public class RunnableStatementForPagination extends RunnableStatementForRead {
    public RunnableStatementForPagination(AnyStatement<?> statement) {
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
                                                 .execute()
                                                 .compose(resultMatrix -> {
                                                     try {
                                                         Long total = resultMatrix.getOneColumnOfFirstRowAsLong("total");
                                                         Objects.requireNonNull(total);
                                                         return Future.succeededFuture(total);
                                                     } catch (KeelSQLResultRowIndexError e) {
                                                         throw new RuntimeException(e);
                                                     }
                                                 });
        Future<ResultMatrix> pageFuture = selectStatement.attachToConnection(getSqlConnection())
                                                         .execute();
        return Future.all(totalFuture, pageFuture)
                     .compose(compositeFuture -> {
                         Long total = compositeFuture.resultAt(0);
                         ResultMatrix resultMatrix = compositeFuture.resultAt(1);
                         return Future.succeededFuture(new PaginationResult(total, resultMatrix));
                     });
    }

}
