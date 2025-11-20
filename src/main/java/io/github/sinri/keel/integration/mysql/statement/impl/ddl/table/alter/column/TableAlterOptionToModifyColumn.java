package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * 修改列选项类，用于构建ALTER TABLE MODIFY [COLUMN]语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToModifyColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String columnDefinition = "";
    public @Nullable String position;

    /**
     * 设置要修改的列名称
     *
     * @param columnName 列名称
     * @return 自身实例
     */
    public TableAlterOptionToModifyColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 设置新的列定义
     * @param columnDefinition 列定义
     * @return 自身实例
     */
    public TableAlterOptionToModifyColumn setColumnDefinition(@NotNull String columnDefinition) {
        this.columnDefinition = columnDefinition;
        return this;
    }

    /**
     * 设置位置为第一列
     * @return 自身实例
     */
    public TableAlterOptionToModifyColumn placeToFirst() {
        this.position = "FIRST";
        return this;
    }

    /**
     * 设置位置在指定列之后
     * @param previousColumnName 前一列的名称
     * @return 自身实例
     */
    public TableAlterOptionToModifyColumn placeAfter(@NotNull String previousColumnName) {
        this.position = "AFTER `" + previousColumnName + "`";
        return this;
    }

    @Override
    public String toString() {
        return "MODIFY COLUMN `" + columnName + "` " + columnDefinition + " "
                + (position == null ? "" : position);
    }
}
