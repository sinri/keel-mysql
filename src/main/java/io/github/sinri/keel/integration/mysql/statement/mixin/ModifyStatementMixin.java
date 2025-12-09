package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

/**
 * 修改语句混合类，为修改操作提供受影响行数查询功能
 *
 * @since 5.0.0
 */
public interface ModifyStatementMixin extends AnyStatement {

    /**
     * As of 3.0.18 Finished Technical Preview.
     *
     * @return future with affected rows; failed future when failed
     * @since 3.0.11
     */
    default Future<Integer> executeForAffectedRows(@NotNull NamedMySQLConnection namedMySQLConnection) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }
}
