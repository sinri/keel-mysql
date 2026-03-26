package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * 抽象SQL语句基类，实现了通用的SQL执行和审计功能。
 * <p>
 * <strong>安全说明：</strong>本类及其子类（{@code SelectStatement}、{@code UpdateStatement}、
 * {@code DeleteStatement}、{@code WriteIntoStatement} 等）中的表名、列名、别名、排序表达式等
 * 标识符参数会被直接拼接进生成的 SQL 字符串，不做转义或校验。调用方必须确保这些参数来自可信来源
 * （如硬编码或内部逻辑），切勿将不可信的用户输入作为标识符传入，否则存在 SQL 注入风险。
 * 字面量值的注入防护由 {@link io.github.sinri.keel.integration.mysql.Quoter} 负责。
 *
 * @since 5.0.0
 */
@NullMarked
abstract public non-sealed class AbstractStatement<S> implements AnyStatement<S> {
    protected static String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    protected final String statement_uuid;
    private String remarkAsComment = "";
    private boolean toPrepareStatement = true;

    /**
     * 构造抽象语句，生成唯一标识符
     */
    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    /**
     * 获取备注注释
     *
     * @return 备注注释
     */
    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    /**
     * 设置备注注释
     *
     * @param remarkAsComment 备注注释
     * @return 自身实例
     */
    @Override
    public S setRemarkAsComment(String remarkAsComment) {
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return getImplementation();
    }

    @Override
    public S setToPrepareStatement(boolean toPrepareStatement) {
        this.toPrepareStatement = toPrepareStatement;
        return getImplementation();
    }

    @Override
    public boolean isToPrepareStatement() {
        return toPrepareStatement;
    }

    @Override
    abstract public String buildSql();

    @Override
    final public String toString() {
        return buildSql();
    }

}
