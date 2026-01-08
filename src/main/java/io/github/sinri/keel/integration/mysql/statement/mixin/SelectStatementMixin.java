package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

/**
 * SELECT语句混合类，为读取操作提供分页查询功能
 *
 * @since 5.0.0
 */
@NullMarked
public interface SelectStatementMixin extends ReadStatementMixin {
    /**
     * Call from this instance, as the original query as Select Statement for all rows in certain order.
     *
     * @param pageNo   since 1.
     * @param pageSize a number
     */
    Future<PaginationResult> queryForPagination(
            NamedMySQLConnection sqlConnection,
            long pageNo,
            long pageSize
    );

    @NullMarked
    record PaginationResult(long total, ResultMatrix resultMatrix) {
        /**
         * @since 4.0.8
         */
        public JsonObject toJsonObject() {
            return new JsonObject()
                    .put("total", total)
                    .put("list", resultMatrix.toJsonArray());
        }
    }
}
