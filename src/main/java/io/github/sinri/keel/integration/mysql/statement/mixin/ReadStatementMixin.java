package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatementForRead;
import io.github.sinri.keel.integration.mysql.connection.target.StreamableStatement;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;


/**
 * 读取语句混合类，为读取操作提供各种查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public non-sealed interface ReadStatementMixin<S> extends AnyStatement<S> {
    default RunnableStatementForRead attachToConnection(SqlConnection sqlConnection) {
        RunnableStatementForRead runnableStatement = new RunnableStatementForRead(this);
        runnableStatement.setSQLConnection(sqlConnection);
        return runnableStatement;
    }

    default StreamableStatement attachToConnectionForStream(Vertx vertx, SqlConnection sqlConnection) {
        StreamableStatement runnableStatement = new StreamableStatement(this);
        runnableStatement.setSQLConnection(sqlConnection);
        runnableStatement.setVertx(vertx);
        return runnableStatement;
    }
}
