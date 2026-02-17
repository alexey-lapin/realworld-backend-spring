package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;

/**
 * {@link JdbcTransactionManager} that can start transactions from caller-provided JDBC connections.
 */
public class DbqTransactionManager extends JdbcTransactionManager {

    private static final ConnectionHolderSetter CONNECTION_HOLDER_SETTER = ConnectionHolderSetterFactory.create();

    /**
     * Creates a transaction manager for the given data source.
     *
     * @param dataSource target data source
     */
    public DbqTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Begins a transaction and pre-binds an externally managed connection when
     * the definition is {@link ConnectionAwareTransactionDefinition}.
     *
     * @param transaction transaction object returned by {@code doGetTransaction}
     * @param definition  transaction definition to apply
     * @throws TransactionException if transaction begin fails
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        if (definition instanceof ConnectionAwareTransactionDefinition connectionAwareDefinition
            && transaction instanceof JdbcTransactionObjectSupport transactionObject) {
            var connection = new CloseSuppressingConnectionWrapper(connectionAwareDefinition.getConnection());
            var connectionHolder = new ConnectionHolder(connection);
            CONNECTION_HOLDER_SETTER.accept(transactionObject, connectionHolder, true);
        }
        super.doBegin(transaction, definition);
    }

}
