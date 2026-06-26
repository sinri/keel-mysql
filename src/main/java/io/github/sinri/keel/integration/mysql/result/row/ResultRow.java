package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 结果行接口，用于将{@link Row}实例转储为{@link JsonObject}，
 * 然后将其包装为{@link ResultRow}实例
 *
 * @since 5.0.0
 */
@NullMarked
public interface ResultRow extends JsonifiableDataUnit {
    /**
     * {@code (Class&lt;? extends ResultRow&gt; -&gt; Constructor)} 反射构造器缓存，
     * 避免每次实例化都重复执行 {@link Class#getConstructor} 查找。
     */
    ConcurrentHashMap<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    /**
     * 从缓存中获取指定结果行类的 {@code (JsonObject)} 构造器；缓存未命中时执行反射查找并写入缓存。
     *
     * @param clazz 结果行类
     * @param <R>   结果行类型
     * @return 构造器
     */
    @SuppressWarnings("unchecked")
    private static <R extends ResultRow> Constructor<R> findConstructor(Class<R> clazz) {
        return (Constructor<R>) CONSTRUCTOR_CACHE.computeIfAbsent(clazz, c -> {
            try {
                return c.getConstructor(JsonObject.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 从JSON对象创建结果行对象
     *
     * @param tableRow 表格行JSON对象
     * @param clazz    结果行类
     * @return 结果行对象
     */
    static <R extends ResultRow> R of(JsonObject tableRow, Class<R> clazz) {
        Constructor<R> constructor = findConstructor(clazz);
        try {
            return constructor.newInstance(tableRow);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从SQL行对象创建结果行对象
     *
     * @param tableRow SQL行对象
     * @param clazz    结果行类
     * @return 结果行对象
     */
    static <R extends ResultRow> R of(Row tableRow, Class<R> clazz) {
        return of(tableRow.toJson(), clazz);
    }

    /**
     * 将结果行集合批量转换为JSON数组
     *
     * @param rows 结果行集合
     * @return JSON数组
     */
    static JsonArray batchToJsonArray(Collection<? extends ResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    /**
     * 使用转换器将结果行集合批量转换为JSON数组
     *
     * @param rows        结果行集合
     * @param transformer 结果行转换器
     * @return JSON数组
     */
    static JsonArray batchToJsonArray(Collection<? extends ResultRow> rows, Function<ResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    /**
     * 获取行数据
     *
     * @return 行数据JSON对象
     */
    default JsonObject getRow() {
        return toJsonObject();
    }

    /**
     * 读取日期时间字段
     *
     * @param field 字段名
     * @return 日期时间字符串
     */
    default @Nullable String readDateTime(String field) {
        String s = readString(field);
        if (s == null) return null;
        return LocalDateTime.parse(s)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 读取日期字段
     *
     * @param field 字段名
     * @return 日期字符串
     */
    default @Nullable String readDate(String field) {
        return readString(field);
    }

    /**
     * 读取时间字段
     *
     * @param field 字段名
     * @return 时间字符串
     */
    default @Nullable String readTime(String field) {
        var s = readString(field);
        if (s == null) return null;
        return s
                .replaceAll("[PTS]+", "")
                .replaceAll("[HM]", ":");
    }

    /**
     * 读取时间戳字段
     *
     * @param field 字段名
     * @return 时间戳字符串
     */
    default @Nullable String readTimestamp(String field) {
        return readDateTime(field);
    }

    /**
     * 读取 JSON 字段。
     * <p>
     * 适用于 MySQL 5.7+ 的 {@code JSON} 列。底层值可能由驱动呈现为
     * {@link JsonObject}、{@link JsonArray}、字符串字面量或标量值，
     * 本方法对其进行归一化：
     * <p>
     * - 若值为 {@link JsonObject} / {@link JsonArray}，原样返回；
     * <p>
     * - 若值为以 {@code &#123;} 或 {@code [} 起始的字符串，
     * 自动解析为 {@link JsonObject} / {@link JsonArray}；
     * <p>
     * - 字段缺失或 SQL {@code NULL} 时返回 {@code null}；
     * <p>
     * - 其余情形（如 JSON 中的纯标量值）原样返回。
     *
     * @param field 字段名
     * @return JSON 值，可能为 {@link JsonObject}、{@link JsonArray} 或标量；字段缺失或为 SQL {@code NULL} 时返回 {@code null}
     * @since 5.0.4
     */
    default @Nullable Object readJson(String field) {
        Object value = readValue(field);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonObject || value instanceof JsonArray) {
            return value;
        }
        if (value instanceof String s) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                char first = trimmed.charAt(0);
                if (first == '{') {
                    return new JsonObject(trimmed);
                }
                if (first == '[') {
                    return new JsonArray(trimmed);
                }
            }
        }
        return value;
    }
}
