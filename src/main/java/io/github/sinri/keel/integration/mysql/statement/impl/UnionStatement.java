package io.github.sinri.keel.integration.mysql.statement.impl;


import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class UnionStatement extends AbstractStatement implements ReadStatementMixin {
    final List<String> selections = new ArrayList<>();

    public UnionStatement() {

    }

    public UnionStatement(@NotNull String firstSelection) {
        selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + firstSelection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
    }

    public UnionStatement union(@NotNull String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        }
        return this;
    }

    public UnionStatement unionAll(@NotNull String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION ALL (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        }
        return this;
    }

    public UnionStatement union(@NotNull List<String> list) {
        for (String selection : list) {
            union(selection);
        }
        return this;
    }

    public UnionStatement unionAll(@NotNull List<String> list) {
        for (String selection : list) {
            unionAll(selection);
        }
        return this;
    }

    public String toString() {
        return String.join(" ", selections) + (getRemarkAsComment().isEmpty() ? "" : ("\n-- " + getRemarkAsComment()));
    }

}
