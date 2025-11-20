package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;


import org.jetbrains.annotations.Nullable;

/**
 * @since 5.0.0
 */
public final class TableCreateDefinitionForPrimaryKey extends TableCreateIndexDefinition {
    private @Nullable String constraintSymbol;

    public TableCreateDefinitionForPrimaryKey setConstraintSymbol(@Nullable String constraintSymbol) {
        this.constraintSymbol = constraintSymbol;
        return this;
    }

    @Override
    public String toString() {
        String indexType = getIndexType();
        String indexOption = getIndexOption();
        return (constraintSymbol != null ? ("CONSTRAINT " + constraintSymbol) : "") + " PRIMARY KEY "
                + (indexType != null ? indexType : "") + " "
                + getKeyPartsExpression() + " "
                + (indexOption != null ? indexOption : "") + " ";

    }
}
