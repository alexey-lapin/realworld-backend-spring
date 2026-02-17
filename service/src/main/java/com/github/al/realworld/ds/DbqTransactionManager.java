package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DbqTransactionManager extends JdbcTransactionManager {

    private static final String TRANSACTION_OBJECT_CLASS_NAME =
            DataSourceTransactionManager.class.getName() + "$DataSourceTransactionObject";

    private static final MethodType SET_CONNECTION_HOLDER_METHOD_TYPE =
            MethodType.methodType(void.class, ConnectionHolder.class, boolean.class);

    private static final MethodType SETTER_FUNCTION_SIGNATURE =
            MethodType.methodType(
                    void.class,
                    JdbcTransactionObjectSupport.class,
                    ConnectionHolder.class,
                    boolean.class
            );

    private static final ConnectionHolderSetter CONNECTION_HOLDER_SETTER = initializeConnectionHolderSetter();

    public DbqTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    private static ConnectionHolderSetter initializeConnectionHolderSetter() {
        try {
            var callerLookup = MethodHandles.lookup();
            var springLookup = MethodHandles.privateLookupIn(DataSourceTransactionManager.class, callerLookup);
            var transactionObjectClass = Class.forName(TRANSACTION_OBJECT_CLASS_NAME);

            var setConnectionHolderHandle = springLookup.findVirtual(
                    transactionObjectClass,
                    "setConnectionHolder",
                    SET_CONNECTION_HOLDER_METHOD_TYPE
            );

            var instantiatedSignature = MethodType.methodType(
                    void.class,
                    transactionObjectClass,
                    ConnectionHolder.class,
                    boolean.class
            );

            var callSite = LambdaMetafactory.metafactory(
                    springLookup,
                    "accept",
                    MethodType.methodType(ConnectionHolderSetter.class),
                    SETTER_FUNCTION_SIGNATURE,
                    setConnectionHolderHandle,
                    instantiatedSignature
            );

            return (ConnectionHolderSetter) callSite.getTarget().invokeExact();
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to initialize transaction connection holder setter", ex);
        }
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
