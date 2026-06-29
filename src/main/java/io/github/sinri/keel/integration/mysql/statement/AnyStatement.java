package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.integration.mysql.connection.target.AnyStatementWithSqlConnection;
import io.github.sinri.keel.integration.mysql.statement.mixin.ModifyStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.ReadStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.SpecialStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.mixin.WriteIntoStatementMixin;
import io.github.sinri.keel.integration.mysql.statement.templated.TemplatedStatement;
import io.vertx.sqlclient.SqlConnection;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.InvocationTargetException;


/**
 * SQL语句接口，定义了所有SQL语句的通用行为
 *
 * @since 5.0.0
 */
@NullMarked
public sealed interface AnyStatement<S> extends SelfInterface<S>
        permits AbstractStatement, ModifyStatementMixin, ReadStatementMixin, SpecialStatementMixin, WriteIntoStatementMixin, TemplatedStatement {
    /**
     * 返回新建语句实例时将采用的默认 SQL 组件分隔符。
     */
    static String getDefaultSqlComponentSeparator() {
        return AbstractStatement.DEFAULT_SQL_COMPONENT_SEPARATOR;
    }

    /**
     * 设置新建语句实例时的默认 SQL 组件分隔符；已创建实例不受影响。
     *
     * @param sqlComponentSeparator SQL组件分隔符
     */
    static void setDefaultSqlComponentSeparator(String sqlComponentSeparator) {
        AbstractStatement.DEFAULT_SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    /**
     * 设置本语句实例的 SQL 组件分隔符。
     *
     * @param sqlComponentSeparator SQL组件分隔符
     * @return 自身引用，便于链式调用
     */
    S setSqlComponentSeparator(String sqlComponentSeparator);

    S setRemarkAsComment(String remarkAsComment);

    default <R extends AnyStatementWithSqlConnection> R attachToConnectionForCertainRunnableStatement(SqlConnection sqlConnection, Class<R> clazz) {
        try {
            R r = clazz.getConstructor(AnyStatement.class)
                       .newInstance(this);
            r.setSQLConnection(sqlConnection);
            return r;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 已废弃的语句级执行协议开关。
     * <p>
     * 自 5.0.4 起，预编译协议、普通查询协议以及 {@code Tuple} 参数绑定均由
     * {@link io.github.sinri.keel.integration.mysql.connection.target.RunnableStatement}
     * 的执行方法决定，不再在语句对象上保存执行模式。
     *
     * @param toPrepareStatement 已废弃参数
     * @return 不会返回
     * @deprecated since 5.0.4, use {@code RunnableStatement.executeThroughPrepare(...)}
     * or {@code RunnableStatement.executeThroughQuery()} instead.
     */
    @Deprecated(since = "5.0.4", forRemoval = true)
    default S setToPrepareStatement(boolean toPrepareStatement) {
        throw new UnsupportedOperationException(
                "Statement-level prepare switch was removed in 5.0.4; choose the execution method on RunnableStatement."
        );
    }

    String buildSql();

    /**
     * 已废弃的语句级执行协议开关查询。
     * <p>
     * 自 5.0.4 起，执行模式不再属于语句对象状态。调用方应在执行层显式选择
     * {@code executeThroughPrepare(...)} 或 {@code executeThroughQuery()}。
     *
     * @return 不会返回
     * @deprecated since 5.0.4, use explicit execution methods on
     * {@code RunnableStatement} instead.
     */
    @Deprecated(since = "5.0.4", forRemoval = true)
    default boolean isToPrepareStatement() {
        throw new UnsupportedOperationException(
                "Statement-level prepare switch was removed in 5.0.4; choose the execution method on RunnableStatement."
        );
    }

}
