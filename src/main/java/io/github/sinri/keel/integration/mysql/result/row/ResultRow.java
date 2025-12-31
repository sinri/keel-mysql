package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.function.Function;

/**
 * 结果行接口，用于将{@link Row}实例转储为{@link JsonObject}，
 * 然后将其包装为{@link ResultRow}实例
 *
 * @since 5.0.0
 */
public interface ResultRow extends JsonifiableDataUnit {
    /**
     * 从JSON对象创建结果行对象
     * @param tableRow 表格行JSON对象
     * @param clazz 结果行类
     * @return 结果行对象
     */
    static <R extends ResultRow> @NotNull R of(@NotNull JsonObject tableRow, @NotNull Class<R> clazz) {
        try {
            Constructor<R> constructor = clazz.getConstructor(JsonObject.class);
            return constructor.newInstance(tableRow);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从SQL行对象创建结果行对象
     * @param tableRow SQL行对象
     * @param clazz 结果行类
     * @return 结果行对象
     */
    static <R extends ResultRow> @NotNull R of(@NotNull Row tableRow, @NotNull Class<R> clazz) {
        return of(tableRow.toJson(), clazz);
    }

    /**
     * 将结果行集合批量转换为JSON数组
     *
     * @param rows 结果行集合
     * @return JSON数组
     */
    static @NotNull JsonArray batchToJsonArray(@NotNull Collection<? extends ResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    /**
     * 使用转换器将结果行集合批量转换为JSON数组
     * @param rows 结果行集合
     * @param transformer 结果行转换器
     * @return JSON数组
     */
    static @NotNull JsonArray batchToJsonArray(@NotNull Collection<? extends ResultRow> rows, @NotNull Function<ResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    /**
     * 获取行数据
     * @return 行数据JSON对象
     */
    default @NotNull JsonObject getRow() {
        return toJsonObject();
    }

    /**
     * 读取日期时间字段
     * @param field 字段名
     * @return 日期时间字符串
     */
    @Nullable
    default String readDateTime(@NotNull String field) {
        String s = readString(field);
        if (s == null) return null;
        return LocalDateTime.parse(s)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 读取日期字段
     * @param field 字段名
     * @return 日期字符串
     */
    @Nullable
    default String readDate(@NotNull String field) {
        return readString(field);
    }

    /**
     * 读取时间字段
     * @param field 字段名
     * @return 时间字符串
     */
    @Nullable
    default String readTime(@NotNull String field) {
        var s = readString(field);
        if (s == null) return null;
        return s
                .replaceAll("[PTS]+", "")
                .replaceAll("[HM]", ":");
    }

    /**
     * 读取时间戳字段
     * @param field 字段名
     * @return 时间戳字符串
     */
    @Nullable
    default String readTimestamp(@NotNull String field) {
        return readDateTime(field);
    }
}
