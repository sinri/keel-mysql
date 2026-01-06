package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 写入语句混合类，为写入操作提供INSERT相关功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface WriteIntoStatementMixin extends ModifyStatementMixin {
    /**
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<Long> executeForLastInsertedID(NamedMySQLConnection namedMySQLConnection) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

    /**
     * 按照最大块尺寸分裂！
     *
     * @param chunkSize an integer
     * @return a list of WriteIntoStatement
     * @since 2.3
     */
    List<WriteIntoStatementMixin> divide(int chunkSize);
}
