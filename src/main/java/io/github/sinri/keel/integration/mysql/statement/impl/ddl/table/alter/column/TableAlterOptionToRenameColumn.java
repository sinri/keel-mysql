package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.column;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jspecify.annotations.NullMarked;

/**
 * 重命名列选项类，用于构建ALTER TABLE RENAME COLUMN语句
 *
 * @since 5.0.0
 */
@NullMarked
public final class TableAlterOptionToRenameColumn extends TableAlterOption {
    private String columnName = "";
    private String newColumnName = "";

    /**
     * 设置原列名称
     *
     * @param columnName 原列名称
     * @return 自身实例
     */
    public TableAlterOptionToRenameColumn setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    /**
     * 设置新列名称
     *
     * @param newColumnName 新列名称
     * @return 自身实例
     */
    public TableAlterOptionToRenameColumn setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME COLUMN `" + columnName + "` TO `" + newColumnName + "`";
    }
}
