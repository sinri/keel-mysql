package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.connection.NamedMySQLConnection;
import org.jspecify.annotations.NullMarked;


/**
 * 抽象命名动作基类，实现了NamedActionInterface接口用于处理命名MySQL连接
 * 该类提供了构造函数来初始化命名MySQL连接，并实现了getNamedSqlConnection方法返回连接
 *
 * @param <C> 扩展 NamedMySQLConnection 的特定连接类
 * @since 5.0.0
 */
@NullMarked
public abstract class AbstractNamedAction<C extends NamedMySQLConnection> implements NamedActionInterface<C> {
    private final C namedSqlConnection;

    /**
     * 构造具有指定命名MySQL连接的抽象命名动作
     *
     * @param namedSqlConnection 与此动作关联的命名MySQL连接实例，不能为null
     */
    public AbstractNamedAction(C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    /**
     * 获取与此动作关联的命名MySQL连接
     *
     * @return 与此动作关联的命名MySQL连接实例，永不为null
     */
    @Override
    public C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
