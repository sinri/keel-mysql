package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;

/**
 * 删除列选项类，用于构建ALTER TABLE DROP [COLUMN]语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToDropColumn extends TableAlterOption {
    public @NotNull String columnName = "";

    /**
     * 设置要删除的列名称
     *
     * @param columnName 列名称
     * @return 自身实例
     */
    public TableAlterOptionToDropColumn setColumnName(@NotNull String columnName) {
        this.columnName = columnName;
        return this;
    }

    @Override
    public String toString() {
        return "DROP COLUMN `" + columnName + "`";
    }
}
