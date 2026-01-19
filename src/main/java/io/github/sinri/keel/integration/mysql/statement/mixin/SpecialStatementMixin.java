package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed interface SpecialStatementMixin<S> extends AnyStatement<S> {
    default RunnableStatement attachToConnection(SqlConnection sqlConnection) {
        return attachToConnectionForCertainRunnableStatement(sqlConnection, RunnableStatement.class);
    }
}
