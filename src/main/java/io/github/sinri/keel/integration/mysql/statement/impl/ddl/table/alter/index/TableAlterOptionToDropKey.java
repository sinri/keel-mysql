package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;


/**
 * 删除索引选项类，用于构建ALTER TABLE DROP {INDEX | KEY}语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToDropKey extends TableAlterOption {
    private @NotNull String indexName = "";

    /**
     * 设置要删除的索引名称
     *
     * @param indexName 索引名称
     * @return 自身实例
     */
    public TableAlterOptionToDropKey setIndexName(@NotNull String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        return "DROP KEY `" + indexName + "`";
    }
}
