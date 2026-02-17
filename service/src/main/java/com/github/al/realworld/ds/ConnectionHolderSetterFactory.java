package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

final class ConnectionHolderSetterFactory {

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

    private ConnectionHolderSetterFactory() {
    }

    static ConnectionHolderSetter create() {
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

}
