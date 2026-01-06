package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter;

import org.jspecify.annotations.NullMarked;

/**
 * ALTER TABLE分区选项类，用于处理表分区相关的操作
 *
 * @since 5.0.0
 */
@NullMarked
public class TableAlterPartitionOptions {
    private final String raw;

    /**
     * 构造分区选项
     *
     * @param raw 分区选项的原始字符串
     */
    public TableAlterPartitionOptions(String raw) {
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
