package io.github.sinri.keel.integration.mysql.condition;

import io.github.sinri.keel.integration.mysql.exception.KeelSQLGenerateError;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合条件类，用于将多个条件组合在一起（使用AND或OR连接）
 *
 * @since 5.0.0
 */
public class GroupCondition implements MySQLCondition {
    public static final String JUNCTION_FOR_AND = "AND";
    public static final String JUNCTION_FOR_OR = "OR";

    protected final @NotNull List<@NotNull MySQLCondition> conditions = new ArrayList<>();
    protected final String junction;

    public GroupCondition(@NotNull String junction) {
        this.junction = junction;
    }

    public GroupCondition(@NotNull String junction, @NotNull List<@NotNull MySQLCondition> conditions) {
        this.junction = junction;
        this.conditions.addAll(conditions);
    }

    public GroupCondition add(@NotNull MySQLCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public GroupCondition add(@NotNull List<@NotNull MySQLCondition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    /**
     * 生成SQL的组合逻辑条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public @NotNull String toString() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder x = new StringBuilder();
        for (MySQLCondition condition : conditions) {
            if (!x.isEmpty()) {
                x.append(" ").append(junction).append(" ");
            }
            x.append(condition);
        }
        return "(" + x + ")";
    }
}
