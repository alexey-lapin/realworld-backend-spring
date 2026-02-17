package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;

public class DbqTransactionManager extends JdbcTransactionManager {

    private static final ConnectionHolderSetter CONNECTION_HOLDER_SETTER = ConnectionHolderSetterFactory.create();

    public DbqTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

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
