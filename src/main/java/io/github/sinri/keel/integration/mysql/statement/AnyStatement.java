package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.integration.mysql.connection.target.AnyStatementWithSqlConnection;
import io.github.sinri.keel.integration.mysql.statement.mixin.*;
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
        permits AbstractStatement, ModifyStatementMixin, ReadStatementMixin, PaginatableStatementMixin, SpecialStatementMixin, WriteIntoStatementMixin, TemplatedStatement {
    static String getSqlComponentSeparator() {
        return AbstractStatement.SQL_COMPONENT_SEPARATOR;
    }

    /**
     * 设置SQL组件分隔符
     *
     * @param sqlComponentSeparator SQL组件分隔符
     */
    static void setSqlComponentSeparator(String sqlComponentSeparator) {
        AbstractStatement.SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

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

    //    default RunnableStatement attachToConnection(SqlConnection sqlConnection) {
    //        return attachToConnectionForCertainRunnableStatement(sqlConnection, RunnableStatement.class);
    //    }

    S setToPrepareStatement(boolean toPrepareStatement);

    String buildSql();

    /**
     * 判断是否使用预处理语句
     *
     * @return 是否使用预处理语句
     */
    boolean isToPrepareStatement();
}
