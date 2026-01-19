package io.github.sinri.keel.integration.mysql.result.matrix;


import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.row.SimpleResultRow;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * 结果矩阵接口，用于处理和操作SQL查询结果
 * 可继承此类以获取自定义数据矩阵
 *
 * @since 5.0.0
 */
@NullMarked
public interface ResultMatrix<R extends ResultRow> extends Iterable<R> {
    static ResultMatrix<SimpleResultRow> createSimple(RowSet<Row> rowSet) {
        return new SimpleResultMatrix(rowSet);
    }

    static <T extends ResultRow> ResultMatrix<T> createSpecific(RowSet<Row> rowSet, Function<JsonObject, T> mapper) {
        return new SpecificResultMatrix<>(rowSet, mapper);
    }

    int size();

    R getFirstRow() throws KeelSQLResultRowIndexError;

    R getRowByIndex(int index) throws KeelSQLResultRowIndexError;

    List<R> getRowList();

    default JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (var r : this) {
            array.add(r.getRow());
        }
        return array;
    }

    Stream<R> stream();

    /**
     * 构建分类行映射图。
     * <p>
     * 从每一行 R 中，找出一个类别 K 值，构建以 K 为键、拥有 K 对应类别的 R 构成的列表为值的映射 Map。
     *
     * @param categoryGenerator 分类生成器
     * @return 分类行映射图
     */
    default <K> Future<Map<K, List<R>>> buildCategorizedRowsMap(Function<R, K> categoryGenerator) {
        Map<K, List<R>> map = new HashMap<>();
        this.forEach(r -> {
            K category = categoryGenerator.apply(r);
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(r);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 构建唯一键绑定行映射图。
     * <p>
     * 从每一行 R 中，找出一个唯一键 K 值，构建以 K 为键、拥有 K 对应 R 为值的映射 Map。
     *
     * @param uniqueKeyGenerator 唯一键生成器
     * @return 唯一键绑定行映射图
     * @throws IllegalStateException 如果唯一键重复
     */
    default <K> Future<Map<K, R>> buildUniqueKeyBoundRowMap(Function<R, K> uniqueKeyGenerator) {
        Map<K, R> map = new HashMap<>();
        for (var r : this) {
            K uniqueKey = uniqueKeyGenerator.apply(r);
            if (map.containsKey(uniqueKey)) {
                throw new IllegalStateException("Duplicate unique key " + uniqueKey + " in result matrix");
            } else {
                map.put(uniqueKey, r);
            }
        }
        return Future.succeededFuture(map);
    }

    /**
     * 构建自定义映射图（类似矩阵转置）
     * <p>
     * 从每一行 R 中，构建一个 K-V 映射，K 为映射键，V 为映射值。
     * 映射键和映射值的生成由 rowToMapHandler 处理。
     *
     * @param rowToMapHandler 行到映射处理器
     * @return 自定义映射图
     */
    default <K, V> Future<Map<K, V>> buildCustomizedMap(BiConsumer<Map<K, V>, R> rowToMapHandler) {
        Map<K, V> map = new HashMap<>();
        for (var r : this) {
            rowToMapHandler.accept(map, r);
        }
        return Future.succeededFuture(map);
    }

    /**
     * 构建收缩列表（根据一组键收缩结果矩阵）。
     * <p>
     * 对于每一行 R，构建一个键实体 JsonObject 和一个主体实体 JsonObject。
     * 键实体包含不收缩的字段键，主体实体包含收缩的字段键。
     * 然后，将键实体和主体实体分别映射到键映射 Map 和主体映射 Map。
     * 最后，将主体映射 Map 转换为列表 List<JsonObject>。
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

        for (R r : this) {
            JsonObject keyEntity = new JsonObject();
            JsonObject bodyEntity = new JsonObject();

            r.forEach(entry -> {
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
        }
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
