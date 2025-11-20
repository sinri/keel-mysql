package io.github.sinri.keel.integration.mysql.result.matrix;

import io.github.sinri.keel.core.utils.TimeUtils;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 结果矩阵实现类，用于具体实现结果矩阵接口的功能
 *
 * @since 5.0.0
 */
class ResultMatrixImpl implements ResultMatrix {
    //private final RowSet<Row> rowSet;
    private final List<Row> rowList = new ArrayList<>();
    private final int totalFetchedRows;
    private final int totalAffectedRows;
    private final @Nullable Long lastInsertedID;

    /**
     * 构造结果矩阵实现对象
     *
     * @param rowSet SQL行集合
     */
    public ResultMatrixImpl(RowSet<Row> rowSet) {
        //this.rowSet = rowSet;
        for (var row : rowSet) {
            rowList.add(row);
        }
        this.totalFetchedRows = rowSet.size();
        this.totalAffectedRows = rowSet.rowCount();
        this.lastInsertedID = rowSet.property(MySQLClient.LAST_INSERTED_ID);
    }

//    public RowSet<Row> getRowSet() {
//        return rowSet;
//    }

    @Override
    public int getTotalFetchedRows() {
        return totalFetchedRows;
    }

    @Override
    public int getTotalAffectedRows() {
        return totalAffectedRows;
    }

    @Override
    public long getLastInsertedID() {
        return Objects.requireNonNull(lastInsertedID);
    }

    @Override
    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (var row : rowList) {
            array.add(row.toJson());
        }
        return array;
    }

    @Override
    public List<JsonObject> getRowList() {
        List<JsonObject> l = new ArrayList<>();
        for (var item : rowList) {
            l.add(item.toJson());
        }
        return l;
    }

    @Override
    public JsonObject getFirstRow() throws KeelSQLResultRowIndexError {
        return getRowByIndex(0);
    }

    /**
     * 根据索引获取行数据
     * @param index 行索引
     * @return 指定行的数据
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Override
    public JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError {
        try {
            return rowList.get(index).toJson();
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new KeelSQLResultRowIndexError(indexOutOfBoundsException);
        }
    }

    /**
     * 根据索引构建表行对象
     * @param index 行索引
     * @param classOfTableRow 表行类
     * @return 表行对象
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     * @throws RuntimeException 封装类时可能抛出异常
     */
    @Override
    public <T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError {
        return ResultRow.of(getRowByIndex(index), classOfTableRow);
    }

    /**
     * 构建所有表行对象列表
     * @param classOfTableRow 表行类
     * @return 表行对象列表
     * @throws RuntimeException 封装类时可能抛出异常
     */
    @Override
    public <T extends ResultRow> List<T> buildTableRowList(Class<T> classOfTableRow) {
        ArrayList<T> list = new ArrayList<>();
        for (var x : rowList) {
            list.add(ResultRow.of(x, classOfTableRow));
        }
        return list;
    }

    /**
     * 获取第一行指定列的日期时间值
     * @param columnName 列名
     * @return 日期时间值
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Override
    public String getOneColumnOfFirstRowAsDateTime(String columnName) throws KeelSQLResultRowIndexError {
        return TimeUtils.getMySQLFormatLocalDateTimeExpression(getFirstRow().getString(columnName));
    }

    @Override
    public String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getString(columnName);
    }

    @Override
    public Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError {
        return Numeric.create(getFirstRow().getNumber(columnName));
    }

    @Override
    public Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getInteger(columnName);
    }

    @Override
    public Long getOneColumnOfFirstRowAsLong(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getLong(columnName);
    }

    /**
     * 获取指定列的所有日期时间值
     * @param columnName 列名
     * @return 日期时间值列表
     */
    @Override
    public List<String> getOneColumnAsDateTime(String columnName) {
        List<String> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(TimeUtils.getMySQLFormatLocalDateTimeExpression(row.getString(columnName)));
        }
        return x;
    }

    @Override
    public List<String> getOneColumnAsString(String columnName) {
        List<String> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getString(columnName));
        }
        return x;
    }

    @Override
    public List<Numeric> getOneColumnAsNumeric(String columnName) {
        List<Numeric> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getNumeric(columnName));
        }
        return x;
    }

    @Override
    public List<Long> getOneColumnAsLong(String columnName) {
        List<Long> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getLong(columnName));
        }
        return x;
    }

    @Override
    public List<Integer> getOneColumnAsInteger(String columnName) {
        List<Integer> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getInteger(columnName));
        }
        return x;
    }
}
