package io.github.sinri.keel.integration.mysql.statement.impl;

import io.github.sinri.keel.integration.mysql.statement.AbstractStatement;
import io.github.sinri.keel.integration.mysql.statement.component.ConditionsComponent;
import io.github.sinri.keel.integration.mysql.statement.component.UpdateSetAssignmentComponent;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * UPDATE语句实现类，用于构建和执行UPDATE更新操作
 *
 * @since 5.0.0
 */
public class UpdateStatement extends AbstractStatement implements ModifyStatementMixin {
    /**
     * @since 3.0.19 changed to use UpdateSetAssignmentComponent as item
     */
    final List<UpdateSetAssignmentComponent> assignments = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    final List<String> sortRules = new ArrayList<>();
    /**
     * UPDATE [LOW_PRIORITY] [IGNORE] table_reference
     * SET assignment_list
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    @NotNull
    String ignoreMark = "";
    @Nullable
    String schema;
    @NotNull
    String table = "TABLE-NOT-SET";
    long limit = 0;

    public UpdateStatement() {
    }

    public UpdateStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public UpdateStatement table(@NotNull String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public UpdateStatement table(@Nullable String schema, @NotNull String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    /**
     * @since 3.0.19 Technical Preview
     */
    public UpdateStatement setWithAssignment(@NotNull UpdateSetAssignmentComponent updateSetAssignmentComponent) {
        this.assignments.add(updateSetAssignmentComponent);
        return this;
    }

    /**
     * @since 3.0.19
     */
    public UpdateStatement setWithAssignments(@NotNull Collection<UpdateSetAssignmentComponent> updateSetAssignmentComponents) {
        this.assignments.addAll(updateSetAssignmentComponents);
        return this;
    }

    public UpdateStatement setWithExpression(@NotNull Map<String, String> columnExpressionMapping) {
        columnExpressionMapping.forEach((k, v) -> assignments
                .add(new UpdateSetAssignmentComponent(k)
                        .assignmentToExpression(v)));
        return this;
    }

    public UpdateStatement setWithExpression(@NotNull String column, @NotNull String expression) {
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToExpression(expression));
        return this;
    }

    public UpdateStatement setWithValue(@NotNull String column, @Nullable Number value) {
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToValue(value));
        return this;
    }

    public UpdateStatement setWithValue(String column, String value) {
        //assignments.add(column + "=" + (new Quoter(value)));
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToValue(value));
        return this;
    }

    public UpdateStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public UpdateStatement orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public UpdateStatement orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public UpdateStatement limit(long limit) {
        this.limit = limit;
        return this;
    }

    public @NotNull String toString() {
        String sql = "UPDATE " + ignoreMark;
        if (schema != null) {
            sql += " " + schema + ".";
        }
        sql += table;

        // since 3.0.19
        List<String> setPairs = new ArrayList<>();
        assignments.forEach(assignment -> setPairs.add(assignment.toString()));
        sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "SET " + String.join(", ", setPairs);

        if (!whereConditionsComponent.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "WHERE " + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + String.join(",", sortRules);
        }
        if (limit > 0) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "LIMIT " + limit;
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }
}
