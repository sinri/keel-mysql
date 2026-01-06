package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

/**
 * 修改语句混合类，为修改操作提供受影响行数查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface ModifyStatementMixin extends AnyStatement {

    /**
     *
     * @return future with affected rows; failed future when failed
     */
    default Future<Integer> executeForAffectedRows(NamedMySQLConnection namedMySQLConnection) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }
}
