package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatementForWrite;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 写入语句混合类，为写入操作提供INSERT相关功能
 *
 * @since 5.0.0
 */
@NullMarked
public non-sealed interface WriteIntoStatementMixin<S> extends AnyStatement<S> {

    default RunnableStatementForWrite attachToConnection(SqlConnection sqlConnection) {
        RunnableStatementForWrite runnableStatement = new RunnableStatementForWrite(this);
        runnableStatement.setSQLConnection(sqlConnection);
        return runnableStatement;
    }

    /**
     * 按照最大块尺寸分裂！
     *
     * @param chunkSize an integer
     * @return a list of WriteIntoStatement
     */
    List<WriteIntoStatementMixin<S>> divide(int chunkSize);
}
