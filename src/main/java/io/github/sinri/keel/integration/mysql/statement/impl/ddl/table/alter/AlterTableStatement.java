package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * ALTER TABLE语句类，用于构建MySQL ALTER TABLE语句
 * 
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/alter-table.html">ALTER TABLE Statement</a>
 * @since 5.0.0
 */
public class AlterTableStatement extends AbstractStatement {
    private final List<TableAlterOption> alterOptions = new ArrayList<>();
    private @Nullable String schemaName = null;
    private @NotNull String tableName = "";
    private @Nullable TableAlterPartitionOptions partitionOptions = null;

    /**
     * 设置模式名称
     *
     * @param schemaName 模式名称
     * @return 自身实例
     */
    public AlterTableStatement setSchemaName(@Nullable String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    /**
     * 设置表名称
     * @param tableName 表名称
     * @return 自身实例
     */
    public AlterTableStatement setTableName(@NotNull String tableName) {
        this.tableName = tableName;
        return this;
    }

    /**
     * 设置分区选项
     * @param partitionOptions 分区选项
     * @return 自身实例
     */
    public AlterTableStatement setPartitionOptions(@Nullable TableAlterPartitionOptions partitionOptions) {
        this.partitionOptions = partitionOptions;
        return this;
    }

    /**
     * 获取表表达式
     * @return 表表达式字符串
     */
    protected String getTableExpression() {
        return (schemaName == null ? "" : ("`" + schemaName + "`")) + ".`" + tableName + "`";
    }

    @Override
    public @NotNull String toString() {
        // ALTER TABLE tbl_name
        //    [alter_option [, alter_option] ...]
        //    [partition_options]
        return "ALTER TABLE " + getTableExpression() + " " + SQL_COMPONENT_SEPARATOR
                + alterOptions.stream().map(Object::toString)
                              .reduce((a, b) -> a + "," + b) + " " + SQL_COMPONENT_SEPARATOR
                + (partitionOptions == null ? "" : partitionOptions);
    }

}
