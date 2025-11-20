package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.index;

import io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.alter.TableAlterOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 添加索引选项类，用于构建ALTER TABLE ADD {INDEX | KEY}语句
 *
 * @since 5.0.0
 */
public final class TableAlterOptionToAddKey extends TableAlterOption {
    private final List<String> keyParts = new ArrayList<>();
    private @Nullable String indexName = null;
    private @Nullable String indexType = null;
    private @Nullable String indexOption = null;

    /**
     * 设置索引名称
     *
     * @param indexName 索引名称
     * @return 自身实例
     */
    public TableAlterOptionToAddKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    /**
     * 设置索引类型
     * @param indexType 索引类型
     * @return 自身实例
     */
    public TableAlterOptionToAddKey setIndexType(@Nullable String indexType) {
        this.indexType = indexType;
        return this;
    }

    /**
     * 设置索引选项
     * @param indexOption 索引选项
     * @return 自身实例
     */
    public TableAlterOptionToAddKey setIndexOption(@Nullable String indexOption) {
        this.indexOption = indexOption;
        return this;
    }

    /**
     * 添加索引列
     * @param keyPart 索引列名称
     * @return 自身实例
     */
    public TableAlterOptionToAddKey addKeyPart(@NotNull String keyPart) {
        this.keyParts.add(keyPart);
        return this;
    }

    @Override
    public String toString() {
        List<String> list = keyParts.stream().map(x -> "`" + x + "`").collect(Collectors.toList());
        return "ADD INDEX "
                + (indexName != null ? ("`" + indexName + "`") : "") + " "
                + (indexType != null ? (indexType) : "") + " "
                + "("
                + String.join(", ", list)
                + ") "
                + (indexOption != null ? (indexOption) : "");
    }
}
