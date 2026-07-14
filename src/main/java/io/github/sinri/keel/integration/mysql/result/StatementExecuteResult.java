package io.github.sinri.keel.integration.mysql.result;

import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@NullMarked
public class StatementExecuteResult implements Iterable<Row> {
    private final List<RowSet<Row>> rowSets;

    public StatementExecuteResult(RowSet<Row> rowSet) {
        List<RowSet<Row>> resultChain = new LinkedList<>();
        RowSet<Row> current = rowSet;
        while (current != null) {
            resultChain.add(current);
            current = current.next();
        }
        this.rowSets = List.copyOf(resultChain);
    }

    public RowSet<Row> getRowSet() {
        return rowSets.get(0);
    }

    /**
     * 获取完整的执行结果链。
     *
     * <p>批处理执行时，每组参数的结果按执行顺序包含在返回列表中。单次执行时列表仅包含
     * {@link #getRowSet()} 返回的结果。
     *
     * @return 不可修改的执行结果列表
     */
    public List<RowSet<Row>> getRowSets() {
        return rowSets;
    }

    /**
     * 获取获取的总行数
     *
     * @return 总行数
     */
    public int getTotalFetchedRows() {
        return rowSets.stream()
                .mapToInt(RowSet::size)
                .sum();
    }

    /**
     * 获取影响的总行数
     *
     * @return 影响的行数
     */
    public int getTotalAffectedRows() {
        return rowSets.stream()
                .mapToInt(RowSet::rowCount)
                .sum();
    }


    /**
     * 获取最后插入的ID
     *
     * @return 最后插入的ID
     */
    public long getLastInsertedID() {
        return getRowSet().property(MySQLClient.LAST_INSERTED_ID);
    }

    @Override
    public Iterator<Row> iterator() {
        return getRowSet().iterator();
    }

    public ResultMatrix<SimpleResultRow> toMatrix() {
        return ResultMatrix.createSimple(this.getRowSet());
    }

    public <R extends ResultRow> ResultMatrix<R> toMatrix(Class<R> clazz) {
        return toMatrix(jsonObject -> ResultRow.of(jsonObject, clazz));
    }

    public <R extends ResultRow> ResultMatrix<R> toMatrix(Function<JsonObject, R> mapper) {
        return ResultMatrix.createSpecific(this.getRowSet(), mapper);
    }

}
