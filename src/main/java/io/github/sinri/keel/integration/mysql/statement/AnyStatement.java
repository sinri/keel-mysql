package io.github.sinri.keel.integration.mysql.statement;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


/**
 * SQL语句接口，定义了所有SQL语句的通用行为
 *
 * @since 5.0.0
 */
@NullMarked
public interface AnyStatement<S> extends SelfInterface<S> {
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

    S setNamedMySQLConnection(@Nullable NamedMySQLConnection connection);

    NamedMySQLConnection getNamedMySQLConnection() throws IllegalStateException;

    S setRemarkAsComment(String remarkAsComment);

    /**
     * 在方法 {@link AnyStatement#setNamedMySQLConnection(NamedMySQLConnection)} 指定的MySQL连接上执行SQL语句
     */
    Future<ResultMatrix> execute();

    /**
     * 在指定的 MySQL 连接上执行SQL语句
     *
     * @param namedSqlConnection MySQL 命名连接
     * @return 执行结果的 Future
     */
    Future<ResultMatrix> execute(NamedMySQLConnection namedSqlConnection);

    S setPrepareStatement(boolean prepareStatement);

    /**
     * 判断是否使用预处理语句
     *
     * @return 是否使用预处理语句
     */
    boolean isPrepareStatement();
}
