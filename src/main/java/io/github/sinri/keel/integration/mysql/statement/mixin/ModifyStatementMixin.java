package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatementForModify;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

/**
 * 修改语句混合类，为修改操作提供受影响行数查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public non-sealed interface ModifyStatementMixin<S> extends AnyStatement<S> {
    default RunnableStatementForModify attachToConnection(SqlConnection sqlConnection) {
        RunnableStatementForModify runnableStatement = new RunnableStatementForModify(this);
        runnableStatement.setSQLConnection(sqlConnection);
        return runnableStatement;
    }
}
