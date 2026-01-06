package io.github.sinri.keel.integration.mysql.statement.component;

import io.github.sinri.keel.integration.mysql.Quoter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


/**
 * 更新SET赋值组件类，用于构建UPDATE语句的SET子句
 *
 * @since 5.0.0
 */
@NullMarked
public class UpdateSetAssignmentComponent {
    private final String fieldName;
    private String expression = "NULL";

    /**
     * 构造更新SET赋值组件
     *
     * @param fieldName 字段名称
     */
    public UpdateSetAssignmentComponent(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 设置表达式赋值
     *
     * @param expression 表达式字符串
     * @return 自身实例
     */
    public UpdateSetAssignmentComponent assignmentToExpression(String expression) {
        this.expression = expression;
        return this;
    }

    /**
     * 设置值赋值
     *
     * @param expression 值对象
     * @return 自身实例
     */
    public UpdateSetAssignmentComponent assignmentToValue(@Nullable Object expression) {
        if (expression == null) {
            this.expression = "NULL";
        } else if (expression instanceof Number) {
            this.expression = expression.toString();
        } else {
            this.expression = new Quoter(expression.toString()).toString();
        }
        return this;
    }

    /**
     * 设置为NULL赋值
     *
     * @return 自身实例
     */
    public UpdateSetAssignmentComponent assignmentToNull() {
        this.expression = "NULL";
        return this;
    }

    /**
     * 设置为CASE操作符赋值
     *
     * @param caseOperator CASE操作符
     * @return 自身实例
     */
    public UpdateSetAssignmentComponent assignmentToCaseOperator(CaseOperator caseOperator) {
        this.expression = caseOperator.toString();
        return this;
    }

    @Override
    public String toString() {
        return fieldName + "=" + expression;
    }

}
