package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RunnableStatementForModify extends RunnableStatement {
    public RunnableStatementForModify(AnyStatement<?> statement) {
        super(statement);
    }

    /**
     *
     * @return future with affected rows; failed future when failed
     */
    public Future<Integer> executeForAffectedRows() {
        return execute()
                .compose(statementExecuteResult -> {
                    var afx = statementExecuteResult.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }
}
