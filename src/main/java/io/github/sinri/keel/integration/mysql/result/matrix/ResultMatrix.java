package io.github.sinri.keel.integration.mysql.result.matrix;


import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * 结果矩阵接口，用于处理和操作SQL查询结果
 * 可继承此类以获取自定义数据矩阵
 *
 * @since 5.0.0
 */
@NullMarked
public interface ResultMatrix {

    /**
     * 从RowSet创建结果矩阵
     *
     * @param rowSet SQL行集合
     * @return 结果矩阵
     */
    static ResultMatrix create(RowSet<Row> rowSet) {
        return new ResultMatrixImpl(rowSet);
    }

    /**
     * 获取结果行列表
     *
     * @return 结果行列表
     */
    List<JsonObject> getRowList();

    /**
     * 获取获取的总行数
     *
     * @return 总行数
     */
    int getTotalFetchedRows();

    /**
     * 获取影响的总行数
     *
     * @return 影响的行数
     */
    int getTotalAffectedRows();

    /**
     * 获取最后插入的ID
     *
     * @return 最后插入的ID
     */
    long getLastInsertedID();

    /**
     * 转换为JSON数组
     *
     * @return JSON数组
     */
    JsonArray toJsonArray();

    /**
     * 获取第一行数据
     *
     * @return 第一行数据
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    JsonObject getFirstRow() throws KeelSQLResultRowIndexError;

    /**
     * 根据索引获取行数据
     *
     * @param index 行索引
     * @return 指定行的数据
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError;

    /**
     * 根据索引构建表行对象
     *
     * @param index           行索引
     * @param classOfTableRow 表行类
     * @return 表行对象
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    <T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError;

    /**
     * 获取第一行指定列的日期时间值
     *
     * @param columnName 列名
     * @return 日期时间值
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Nullable String getOneColumnOfFirstRowAsDateTime(String columnName) throws KeelSQLResultRowIndexError;

    /**
     * 获取第一行指定列的字符串值
     *
     * @param columnName 列名
     * @return 字符串值
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Nullable String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError;

    /**
     * 获取第一行指定列的数值对象
     *
     * @param columnName 列名
     * @return 数值对象
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Nullable Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError;

    /**
     * 获取第一行指定列的整数值
     *
     * @param columnName 列名
     * @return 整数值
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Nullable Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError;

    /**
     * 获取第一行指定列的长整数值
     *
     * @param columnName 列名
     * @return 长整数值
     * @throws KeelSQLResultRowIndexError 行索引错误时抛出
     */
    @Nullable Long getOneColumnOfFirstRowAsLong(String columnName) throws KeelSQLResultRowIndexError;

    /**
     * 获取指定列的所有日期时间值
     *
     * @param columnName 列名
     * @return 日期时间值列表
     */
    List<@Nullable String> getOneColumnAsDateTime(String columnName);

    /**
     * 获取指定列的所有字符串值
     *
     * @param columnName 列名
     * @return 字符串值列表
     */
    List<@Nullable String> getOneColumnAsString(String columnName);

    /**
     * 获取指定列的所有数值对象
     *
     * @param columnName 列名
     * @return 数值对象列表
     */
    List<@Nullable Numeric> getOneColumnAsNumeric(String columnName);

    /**
     * 获取指定列的所有长整数值
     *
     * @param columnName 列名
     * @return 长整数值列表
     */
    List<@Nullable Long> getOneColumnAsLong(String columnName);

    /**
     * 获取指定列的所有整数值
     *
     * @param columnName 列名
     * @return 整数值列表
     */
    List<@Nullable Integer> getOneColumnAsInteger(String columnName);

    /**
     * 构建所有表行对象列表
     *
     * @param classOfTableRow 表行类
     * @return 表行对象列表
     * @throws RuntimeException 封装类时可能抛出异常
     */
    <T extends ResultRow> List<T> buildTableRowList(Class<T> classOfTableRow);

    /**
     * 构建分类行映射图
     *
     * @param categoryGenerator 分类生成器
     * @return 分类行映射图
     */
    default <K> Future<Map<K, List<JsonObject>>> buildCategorizedRowsMap(Function<JsonObject, K> categoryGenerator) {
        Map<K, List<JsonObject>> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> {
            K category = categoryGenerator.apply(item);
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 构建唯一键绑定行映射图
     *
     * @param uniqueKeyGenerator 唯一键生成器
     * @return 唯一键绑定行映射图
     */
    default <K> Future<Map<K, JsonObject>> buildUniqueKeyBoundRowMap(Function<JsonObject, K> uniqueKeyGenerator) {
        Map<K, JsonObject> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> {
            K uniqueKey = uniqueKeyGenerator.apply(item);
            map.put(uniqueKey, item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 构建分类表行映射图（分类映射到表行列表）
     *
     * @param classOfTableRow   表行类
     * @param categoryGenerator 分类生成器
     * @return 分类表行映射图
     */
    default <K, T extends ResultRow> Future<Map<K, List<T>>> buildCategorizedRowsMap(Class<T> classOfTableRow, Function<T, K> categoryGenerator) {
        Map<K, List<T>> map = new HashMap<>();
        var list = buildTableRowList(classOfTableRow);
        list.forEach(item -> {
            K category = categoryGenerator.apply(item);
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 构建唯一键绑定表行映射图（一个唯一键映射到一个结果行）
     * 警告：如果uniqueKeyGenerator提供重复键，映射值将不确定
     *
     * @param classOfTableRow    表行类
     * @param uniqueKeyGenerator 唯一键生成器
     * @return 唯一键绑定表行映射图
     */
    default <K, T extends ResultRow> Future<Map<K, T>> buildUniqueKeyBoundRowMap(Class<T> classOfTableRow, Function<T, K> uniqueKeyGenerator) {
        Map<K, T> map = new HashMap<>();
        var list = buildTableRowList(classOfTableRow);
        list.forEach(item -> {
            K category = uniqueKeyGenerator.apply(item);
            map.put(category, item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 构建自定义映射图（类似矩阵转置）
     *
     * @param rowToMapHandler 行到映射处理器
     * @return 自定义映射图
     */
    default <K, V> Future<Map<K, V>> buildCustomizedMap(
            BiConsumer<Map<K, V>, JsonObject> rowToMapHandler
    ) {
        Map<K, V> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> rowToMapHandler.accept(map, item));
        return Future.succeededFuture(map);
    }

    /**
     * 构建收缩列表（根据一组键收缩结果矩阵）
     *
     * @param shrinkByKeys      不收缩的字段键
     * @param shrinkBodyListKey 结果中收缩主体的键
     * @return 收缩后的列表
     */
    default Future<List<JsonObject>> buildShrinkList(
            Collection<String> shrinkByKeys,
            String shrinkBodyListKey
    ) {
        Map<String, JsonObject> keyMap = new HashMap<>();
        Map<String, List<JsonObject>> bodyMap = new HashMap<>();
        List<JsonObject> rowList = getRowList();
        rowList.forEach(item -> {
            JsonObject keyEntity = new JsonObject();
            JsonObject bodyEntity = new JsonObject();

            item.forEach(entry -> {
                if (shrinkByKeys.contains(entry.getKey())) {
                    keyEntity.put(entry.getKey(), entry.getValue());
                } else {
                    bodyEntity.put(entry.getKey(), entry.getValue());
                }
            });

            shrinkByKeys.forEach(sk -> {
                if (!keyEntity.containsKey(sk)) {
                    keyEntity.putNull(sk);
                }
            });
            String skEntityHash = getSortedJsonObject(keyEntity).toString();

            keyMap.put(skEntityHash, keyEntity);
            bodyMap.computeIfAbsent(skEntityHash, s -> new ArrayList<>())
                   .add(bodyEntity);
        });
        List<JsonObject> resultList = new ArrayList<>();
        new TreeMap<>(bodyMap).forEach((k, v) -> {
            JsonObject x = new JsonObject();
            JsonObject keyEntity = keyMap.get(k);
            keyEntity.forEach(e -> x.put(e.getKey(), e.getValue()));
            List<JsonObject> jsonObjects = bodyMap.get(k);
            x.put(shrinkBodyListKey, jsonObjects);
        });
        return Future.succeededFuture(resultList);
    }

    private JsonObject getSortedJsonObject(JsonObject object) {
        JsonObject result = new JsonObject();
        List<String> keyList = new ArrayList<>(object.getMap().keySet());
        keyList.sort(Comparator.naturalOrder());
        keyList.forEach(key -> {
            Object value = object.getValue(key);
            if (value instanceof JsonObject) {
                result.put(key, getSortedJsonObject((JsonObject) value));
            } else if (value instanceof JsonArray) {
                result.put(key, getSortedJsonArray((JsonArray) value));
            } else {
                result.put(key, value);
            }
        });
        return result;
    }

    private JsonArray getSortedJsonArray(JsonArray array) {
        List<Object> list = new ArrayList<>();
        array.forEach(list::add);
        list.sort(Comparator.comparing(Object::toString));
        return new JsonArray(list);
    }
}
