package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;

/**
 * 命名MySQL动作接口，定义了命名MySQL动作的接口
 *
 * 该接口提供了获取与动作关联的命名MySQL连接的方法，
 * 使用此连接可以执行SQL语句。
 * 所有对MySQL连接的操作都应该包装在由高层管理的事务中；
 * 即不要在任何动作内管理事务。
 *
 * 此接口设计为mixin风格使用，因此可以扩展它来添加自己的方法。
 *
 * @param <C> 扩展NamedMySQLConnection的特定连接类
 * @see AbstractNamedMixinAction
 * @see AbstractNamedAction
 * @since 5.0.0
 */
public interface NamedActionInterface<C extends NamedMySQLConnection> {
    /**
     * 获取关联的命名MySQL连接
     * @return 与实现动作关联的命名MySQL连接实例，永不为null
     */
    @NotNull
    C getNamedSqlConnection();
}
