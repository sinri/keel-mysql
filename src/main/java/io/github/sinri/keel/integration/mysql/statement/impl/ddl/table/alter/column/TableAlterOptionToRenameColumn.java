package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;

/**
 * 重命名列选项类，用于构建ALTER TABLE RENAME COLUMN语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToRenameColumn extends TableAlterOption {
    public @NotNull String columnName = "";
    public @NotNull String newColumnName = "";

    /**
     * 设置原列名称
     *
     * @param columnName 原列名称
     * @return 自身实例
     */
    public TableAlterOptionToRenameColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 设置新列名称
     * @param newColumnName 新列名称
     * @return 自身实例
     */
    public TableAlterOptionToRenameColumn setNewColumnName(@NotNull String newColumnName) {
        this.newColumnName = newColumnName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME COLUMN `" + columnName + "` TO `" + newColumnName + "`";
    }
}
