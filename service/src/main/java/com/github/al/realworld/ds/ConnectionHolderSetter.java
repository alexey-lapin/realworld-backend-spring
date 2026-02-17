package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

/**
 * Adapter contract that binds a {@link ConnectionHolder} to Spring's transaction object.
 */
@FunctionalInterface
public interface ConnectionHolderSetter {

    /**
     * Sets the connection holder on the target transaction object.
     *
     * @param receiver            transaction object instance that accepts the holder
     * @param connectionHolder    holder wrapping the connection to bind
     * @param newConnectionHolder whether the holder is newly created for the transaction
     */
    void accept(JdbcTransactionObjectSupport receiver, ConnectionHolder connectionHolder, boolean newConnectionHolder);

}
