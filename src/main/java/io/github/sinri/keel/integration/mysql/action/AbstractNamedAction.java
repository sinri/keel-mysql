package io.github.sinri.keel.integration.mysql.action;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import org.jetbrains.annotations.NotNull;


/**
 * Abstract class that implements the NamedActionInterface for handling named MySQL connections.
 * This class provides a constructor to initialize the named MySQL connection and implements the getNamedSqlConnection method to return the connection.
 *
 * @param <C> a specific connection class that extends NamedMySQLConnection
 * @since 3.2.11 Moved from `io.github.sinri.keel.mysql.AbstractNamedAction` and refined.
 */
public abstract class AbstractNamedAction<C extends NamedMySQLConnection> implements NamedActionInterface<C> {
    private final @NotNull C namedSqlConnection;

    /**
     * Constructs an AbstractNamedAction with the specified named MySQL connection.
     *
     * @param namedSqlConnection the named MySQL connection instance to be associated with this action; must not be null
     */
    public AbstractNamedAction(@NotNull C namedSqlConnection) {
        this.namedSqlConnection = namedSqlConnection;
    }

    /**
     * Retrieves the named MySQL connection associated with this action.
     *
     * @return the instance of named MySQL connection associated with this action; never null
     */
    @NotNull
    @Override
    public C getNamedSqlConnection() {
        return namedSqlConnection;
    }
}
