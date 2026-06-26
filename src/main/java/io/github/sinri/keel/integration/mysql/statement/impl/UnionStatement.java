package io.github.sinri.keel.integration.mysql.statement.impl;


import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

/**
 * UNION语句类，用于构建和执行UNION联合查询
 *
 * @since 5.0.0
 */
@NullMarked
public final class UnionStatement extends AbstractStatement<UnionStatement> implements ReadStatementMixin<UnionStatement> {
    final List<String> selections = new ArrayList<>();

    public UnionStatement() {

    }

    public UnionStatement(String firstSelection) {
        selections.add("(" + getSqlComponentSeparator() + firstSelection + getSqlComponentSeparator() + ")");
    }

    public UnionStatement union(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + getSqlComponentSeparator() + selection + getSqlComponentSeparator() + ")");
        } else {
            selections.add(" UNION (" + getSqlComponentSeparator() + selection + getSqlComponentSeparator() + ")");
        }
        return this;
    }

    public UnionStatement unionAll(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + getSqlComponentSeparator() + selection + getSqlComponentSeparator() + ")");
        } else {
            selections.add(" UNION ALL (" + getSqlComponentSeparator() + selection + getSqlComponentSeparator() + ")");
        }
        return this;
    }

    public UnionStatement union(List<String> list) {
        for (String selection : list) {
            union(selection);
        }
        return this;
    }

    public UnionStatement unionAll(List<String> list) {
        for (String selection : list) {
            unionAll(selection);
        }
        return this;
    }

    @Override
    public String buildSql() {
        return String.join(" ", selections) + (getRemarkAsComment().isEmpty() ? "" : ("\n-- " + getRemarkAsComment()));
    }

}
