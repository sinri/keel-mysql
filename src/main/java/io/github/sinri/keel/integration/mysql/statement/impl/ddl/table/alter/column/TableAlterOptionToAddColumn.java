package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * 添加列选项类，用于构建ALTER TABLE ADD [COLUMN]语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToAddColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String columnDefinition = "";
    public @Nullable String position;

    /**
     * 设置列名称
     *
     * @param columnName 列名称
     * @return 自身实例
     */
    public TableAlterOptionToAddColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 设置列定义
     * @param columnDefinition 列定义
     * @return 自身实例
     */
    public TableAlterOptionToAddColumn setColumnDefinition(@NotNull String columnDefinition) {
        this.columnDefinition = columnDefinition;
        return this;
    }

    /**
     * 设置位置为第一列
     * @return 自身实例
     */
    public TableAlterOptionToAddColumn placeToFirst() {
        this.position = "FIRST";
        return this;
    }

    /**
     * 设置位置在指定列之后
     * @param previousColumnName 前一列的名称
     * @return 自身实例
     */
    public TableAlterOptionToAddColumn placeAfter(@NotNull String previousColumnName) {
        this.position = "AFTER `" + previousColumnName + "`";
        return this;
    }

    @Override
    public String toString() {
        return "ADD COLUMN `" + columnName + "` " + columnDefinition + " "
                + (position == null ? "" : position);
    }
}
