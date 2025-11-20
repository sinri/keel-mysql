package io.github.sinri.keel.integration.mysql.result.row;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 抽象表行类，继承自SimpleResultRow，用于表示数据库表行
 *
 * @since 5.0.0
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(@NotNull JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * 获取源数据库模式名称
     * @return 源数据库模式名称，默认返回null
     */
    @Nullable
    public String sourceSchemaName() {
        return null;
    }

    /**
     * 获取源表名称
     * @return 表名称
     */
    @NotNull
    abstract public String sourceTableName();
}
