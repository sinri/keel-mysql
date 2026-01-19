package io.github.sinri.keel.integration.mysql.result;

import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.function.Function;

@NullMarked
public class StatementExecuteResult implements Iterable<Row> {
    private final RowSet<Row> rowSet;

    public StatementExecuteResult(RowSet<Row> rowSet) {
        this.rowSet = rowSet;
    }

    public RowSet<Row> getRowSet() {
        return rowSet;
    }

    /**
     * 获取获取的总行数
     *
     * @return 总行数
     */
    public int getTotalFetchedRows() {
        return rowSet.size();
    }

    /**
     * 获取影响的总行数
     *
     * @return 影响的行数
     */
    public int getTotalAffectedRows() {
        return rowSet.rowCount();
    }


    /**
     * 获取最后插入的ID
     *
     * @return 最后插入的ID
     */
    public long getLastInsertedID() {
        return rowSet.property(MySQLClient.LAST_INSERTED_ID);
    }

    @Override
    public Iterator<Row> iterator() {
        return rowSet.iterator();
    }

    public ResultMatrix<SimpleResultRow> toMatrix() {
        return ResultMatrix.createSimple(this.getRowSet());
    }

    public <R extends ResultRow> ResultMatrix<R> toMatrix(Class<R> clazz) {
        Function<JsonObject, R> mapper = jsonObject -> {
            try {
                return clazz.getConstructor(JsonObject.class).newInstance(jsonObject);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        };
        return toMatrix(mapper);
    }

    public <R extends ResultRow> ResultMatrix<R> toMatrix(Function<JsonObject, R> mapper) {
        return ResultMatrix.createSpecific(this.getRowSet(), mapper);
    }

}
