package io.github.sinri.keel.integration.mysql.statement.impl;

import io.github.sinri.keel.integration.mysql.Quoter;
import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.WriteIntoStatementMixin;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;


/**
 * INSERT/REPLACE语句实现类，用于构建和执行数据写入操作
 *
 * @since 5.0.0
 */
@NullMarked
public class WriteIntoStatement extends AbstractStatement implements WriteIntoStatementMixin {
    /**
     * insert [ignore] into schema.table (column...) values (value...),... ON DUPLICATE KEY UPDATE assignment_list
     * insert [ignore] into schema.table (column...) [select ...| table ...] ON DUPLICATE KEY UPDATE assignment_list
     */

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";

    private final List<String> columns = new ArrayList<>();

    private final List<List<String>> batchValues = new ArrayList<>();

    private final Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();

    private final String writeType;

    private String ignoreMark = "";
    private @Nullable String schema;

    private String table = "TABLE-NOT-SET";
    private @Nullable String sourceSelectSQL;
    private @Nullable String sourceTableName;

    public WriteIntoStatement() {
        this.writeType = INSERT;
    }

    public WriteIntoStatement(String writeType) {
        this.writeType = writeType;
    }

    public WriteIntoStatement intoTable(String table) {
        if (table.isBlank()) throw new IllegalArgumentException("Table is blank");
        this.table = table;
        return this;
    }

    public WriteIntoStatement intoTable(@Nullable String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public WriteIntoStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public WriteIntoStatement columns(List<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public WriteIntoStatement addDataMatrix(List<List<@Nullable Object>> batch) {
        for (List<@Nullable Object> row : batch) {
            this.addDataRow(row);
        }
        return this;
    }

    public <T extends @Nullable Object> WriteIntoStatement addDataRow(List<T> row) {
        List<String> t = new ArrayList<>();
        for (Object item : row) {
            if (item == null) {
                t.add("NULL");
            } else {
                t.add(new Quoter(String.valueOf(item)).toString());
            }
        }
        this.batchValues.add(t);
        return this;
    }

    public WriteIntoStatement macroWriteRows(Collection<RowToWrite> rows) {
        if (rows.isEmpty()) {
            throw new RuntimeException();
        }
        columns.clear();
        this.batchValues.clear();

        rows.forEach(row -> {
            if (row.map.isEmpty()) {
                throw new RuntimeException();
            }

            List<String> dataRow = new ArrayList<>();

            if (columns.isEmpty()) {
                columns.addAll(row.map.keySet());
            }

            columns.forEach(key -> {
                var value = row.map.get(key);
                dataRow.add(value);
            });

            this.batchValues.add(dataRow);
        });

        return this;
    }

    public WriteIntoStatement macroWriteOneRow(RowToWrite row) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        row.map.forEach((column, expression) -> {
            columns.add(column);
            dataRow.add(expression);
        });
        this.batchValues.add(dataRow);
        return this;
    }

    public WriteIntoStatement macroWriteOneRow(Handler<RowToWrite> rowEditor) {
        RowToWrite rowToWrite = new RowToWrite();
        rowEditor.handle(rowToWrite);
        return macroWriteOneRow(rowToWrite);
    }

    public WriteIntoStatement fromSelection(String selectionSQL) {
        this.sourceSelectSQL = selectionSQL;
        return this;
    }

    public WriteIntoStatement fromTable(String tableName) {
        this.sourceTableName = tableName;
        return this;
    }

    public WriteIntoStatement onDuplicateKeyUpdate(String column, String updateExpression) {
        this.onDuplicateKeyUpdateAssignmentMap.put(column, updateExpression);
        return this;
    }

    /**
     * @param fieldName the raw column name
     * @return as `onDuplicateKeyUpdate` does
     */
    public WriteIntoStatement onDuplicateKeyUpdateField(String fieldName) {
        return this.onDuplicateKeyUpdate(fieldName, "values(" + fieldName + ")");
    }

    /**
     * @param fieldNameList the raw column name list
     * @return as `onDuplicateKeyUpdate` does
     */
    public WriteIntoStatement onDuplicateKeyUpdateFields(List<String> fieldNameList) {
        for (var fieldName : fieldNameList) {
            this.onDuplicateKeyUpdate(fieldName, "values(" + fieldName + ")");
        }
        return this;
    }

    /**
     * @param fieldName the raw column name
     * @return as `onDuplicateKeyUpdate` does
     */
    public WriteIntoStatement onDuplicateKeyUpdateExceptField(String fieldName) {
        if (columns.isEmpty()) {
            throw new RuntimeException("Columns not set yet");
        }
        for (var x : columns) {
            if (x.equalsIgnoreCase(fieldName)) {
                continue;
            }
            this.onDuplicateKeyUpdate(x, "values(" + x + ")");
        }
        return this;
    }

    /**
     * @param fieldNameList the raw column name list
     * @return as `onDuplicateKeyUpdate` does
     */
    public WriteIntoStatement onDuplicateKeyUpdateExceptFields(List<String> fieldNameList) {
        if (columns.isEmpty()) {
            throw new RuntimeException("Columns not set yet");
        }
        for (var x : columns) {
            if (fieldNameList.contains(x)) continue;
            this.onDuplicateKeyUpdate(x, "values(" + x + ")");
        }
        return this;
    }

    public String toString() {
        String sql = writeType + " " + ignoreMark + " INTO ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        sql += " (" + String.join(",", columns) + ")";
        if (sourceTableName != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "TABLE " + sourceTableName;
        } else if (sourceSelectSQL != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + sourceSelectSQL;
        } else {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "VALUES" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            for (List<String> row : batchValues) {
                items.add("(" + String.join(",", row) + ")");
            }
            sql += String.join("," + AbstractStatement.SQL_COMPONENT_SEPARATOR, items);
        }
        if (!onDuplicateKeyUpdateAssignmentMap.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ON DUPLICATE KEY UPDATE" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            onDuplicateKeyUpdateAssignmentMap.forEach((key, value) -> items.add(key + " = " + value));
            sql += String.join("," + AbstractStatement.SQL_COMPONENT_SEPARATOR, items);
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }

    /**
     * 按照最大块尺寸分裂！
     *
     * @param chunkSize an integer
     * @return a list of WriteIntoStatement
     */
    public List<WriteIntoStatementMixin> divide(int chunkSize) {
        if (sourceTableName != null || sourceSelectSQL != null) {
            return List.of(this);
        }

        List<WriteIntoStatementMixin> list = new ArrayList<>();
        int size = this.batchValues.size();
        for (int chunkStartIndex = 0; chunkStartIndex < size; chunkStartIndex += chunkSize) {
            WriteIntoStatement chunkWIS = new WriteIntoStatement(this.writeType);

            chunkWIS.columns.addAll(this.columns);
            chunkWIS.onDuplicateKeyUpdateAssignmentMap.putAll(this.onDuplicateKeyUpdateAssignmentMap);
            chunkWIS.ignoreMark = this.ignoreMark;
            chunkWIS.schema = this.schema;
            chunkWIS.table = this.table;
            chunkWIS.batchValues.addAll(this.batchValues.subList(chunkStartIndex, Math.min(size, chunkStartIndex + chunkSize)));

            list.add(chunkWIS);
        }
        return list;
    }

    @NullMarked
    public static class RowToWrite {
        final Map<String, @Nullable String> map = new TreeMap<>();

        /**
         * @param jsonObject One row as a JsonObject
         */
        public static RowToWrite fromJsonObject(JsonObject jsonObject) {
            RowToWrite rowToWrite = new RowToWrite();
            jsonObject.forEach(entry -> rowToWrite.put(entry.getKey(), entry.getValue()));
            return rowToWrite;
        }

        /**
         * @param jsonArray Rows in a JsonArray; each item of the array should be a JsonObject to be a row.
         */
        public static Collection<RowToWrite> fromJsonObjectArray(JsonArray jsonArray) {
            Collection<RowToWrite> rows = new ArrayList<>();
            jsonArray.forEach(item -> {
                Objects.requireNonNull(item);
                if (item instanceof JsonObject o) {
                    rows.add(fromJsonObject(o));
                } else {
                    throw new IllegalArgumentException("JsonArray contains non JsonObject item.");
                }
            });
            return rows;
        }

        /**
         * @param jsonObjects each item to be a row.
         */
        public static Collection<RowToWrite> fromJsonObjectArray(List<JsonObject> jsonObjects) {
            Collection<RowToWrite> rows = new ArrayList<>();
            jsonObjects.forEach(item -> {
                Objects.requireNonNull(item);
                rows.add(fromJsonObject(item));
            });
            return rows;
        }

        public RowToWrite putNow(String columnName) {
            return this.putExpression(columnName, "now()");
        }

        public RowToWrite putExpression(String columnName, String expression) {
            map.put(columnName, expression);
            return this;
        }

        public RowToWrite put(String columnName, @Nullable Object value) {
            if (value == null) {
                return this.putExpression(columnName, "NULL");
            } else if (value instanceof Number) {
                return putExpression(columnName, String.valueOf(value));
            } else {
                return putExpression(columnName, new Quoter(value.toString()).toString());
            }
        }
    }
}
