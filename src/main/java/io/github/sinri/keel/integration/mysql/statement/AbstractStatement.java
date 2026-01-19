package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/**
 * 抽象SQL语句基类，实现了通用的SQL执行和审计功能
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
