package io.github.sinri.keel.integration.mysql.result.stream;

import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import org.jspecify.annotations.NullMarked;

/**
 * 结果流读取器接口，用于处理流式结果读取
 *
 * @since 5.0.0
 */
@NullMarked
public interface ResultStreamReader {
    /**
     * 将SQL行映射为实体对象
     *
     * @param row   SQL行
     * @param clazz 实体类
     * @return 实体对象
     */
    static <T> T mapRowToEntity(Row row, Class<T> clazz) {
        return row.toJson().mapTo(clazz);
    }

    /**
     * 将SQL行映射为结果行对象
     *
     * @param row   SQL行
     * @param clazz 结果行类
     * @return 结果行对象
     */
    static <R extends ResultRow> R mapRowToResultRow(Row row, Class<R> clazz) {
        return ResultRow.of(row, clazz);
    }

    /**
     * 读取一行数据
     *
     * @param row SQL行
     * @return 读取结果Future
     */
    Future<Void> read(Row row);
}
