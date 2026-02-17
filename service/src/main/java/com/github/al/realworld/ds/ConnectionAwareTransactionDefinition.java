package com.github.al.realworld.ds;

import lombok.Getter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.TransactionDefinition;

import java.sql.Connection;

/**
 * {@link TransactionDefinition} carrying a caller-provided JDBC {@link Connection}.
 */
@Getter
public class ConnectionAwareTransactionDefinition implements TransactionDefinition {

    private final Connection connection;

    private final @Nullable String name;

    /**
     * Creates a transaction definition bound to an existing connection.
     *
     * @param connection connection to associate with the transaction
     */
    public ConnectionAwareTransactionDefinition(Connection connection) {
        this(connection, null);
    }

    /**
     * Creates a transaction definition bound to an existing connection and name.
     *
     * @param connection connection to associate with the transaction
     * @param name       optional transaction name
     */
    public ConnectionAwareTransactionDefinition(@NonNull Connection connection, @Nullable String name) {
        this.connection = connection;
        this.name = name;
    }

}
