package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;


import org.jspecify.annotations.NullMarked;

/**
 * 重命名表选项类，用于构建ALTER TABLE RENAME [TO | AS]语句
 *
 * @since 5.0.0
 */
@NullMarked
public final class TableAlterOptionToRenameTable extends TableAlterOption {
    private String newTableName = "";

    /**
     * 设置新的表名称
     *
     * @param newTableName 新表名称
     * @return 自身实例
     */
    public TableAlterOptionToRenameTable setNewTableName(String newTableName) {
        this.newTableName = newTableName;
        return this;
    }

    @Override
    public String toString() {
        return "RENAME TO `" + newTableName + "`";
    }
}
