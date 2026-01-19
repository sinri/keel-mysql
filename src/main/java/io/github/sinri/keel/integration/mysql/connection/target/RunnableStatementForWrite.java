package io.github.sinri.keel.integration.mysql.connection.target;

import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RunnableStatementForWrite extends RunnableStatementForModify {
    public RunnableStatementForWrite(AnyStatement<?> statement) {
        super(statement);
    }

    /**
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     */
    public Future<Long> executeForLastInsertedID() {
        return execute()
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }
}
