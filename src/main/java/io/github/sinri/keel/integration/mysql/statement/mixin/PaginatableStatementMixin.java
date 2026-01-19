package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatementForPagination;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

/**
 * SELECT语句混合类，为读取操作提供分页查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public non-sealed interface PaginatableStatementMixin<S> extends AnyStatement<S> {
    default S limit(long limit) {
        return limit(limit, 0);
    }

    S limit(long limit, long offset);

    default RunnableStatementForPagination attachToConnection(SqlConnection sqlConnection) {
        RunnableStatementForPagination runnableStatement = new RunnableStatementForPagination(this);
        runnableStatement.setSQLConnection(sqlConnection);
        return runnableStatement;
    }
}
