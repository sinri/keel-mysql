package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
@NullMarked
public final class TableCreateDefinitionForKey extends TableCreateIndexDefinition {
    private @Nullable String indexName = null;

    public TableCreateDefinitionForKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        var indexType = getIndexType();
        var indexOption = getIndexOption();
        return "KEY " + (indexName != null ? indexName : "") + " " + (indexType != null ? indexType : "") + " "
                + getKeyPartsExpression() + " "
                + (indexOption != null ? indexOption : "") + " ";
    }
}
