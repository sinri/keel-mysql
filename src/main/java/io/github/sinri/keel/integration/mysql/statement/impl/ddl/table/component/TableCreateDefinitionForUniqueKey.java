package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;


import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * @since 5.0.0
 */
@NullMarked
public final class TableCreateDefinitionForUniqueKey extends TableCreateIndexDefinition {
    private @Nullable String constraintSymbol;
    private @Nullable String indexName = null;

    public TableCreateDefinitionForUniqueKey setConstraintSymbol(@Nullable String constraintSymbol) {
        this.constraintSymbol = constraintSymbol;
        return this;
    }

    public TableCreateDefinitionForUniqueKey setIndexName(@Nullable String indexName) {
        this.indexName = indexName;
        return this;
    }

    @Override
    public String toString() {
        String indexType = getIndexType();
        String indexOption = getIndexOption();
        return (constraintSymbol != null ? ("CONSTRAINT " + constraintSymbol + " UNIQUE KEY") : "") + " "
                + (indexName != null ? indexName : "") + " "
                + (indexType != null ? indexType : "") + " "
                + getKeyPartsExpression() + " "
                + (indexOption != null ? indexOption : "") + " ";

    }
}
