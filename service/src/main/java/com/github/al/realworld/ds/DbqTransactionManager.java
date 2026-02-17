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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DbqTransactionManager extends JdbcTransactionManager {

//        private static MethodHandle mh;

    private static ConnectionHolderSetter connectionHolderSetter;

    static {
        try {
            MethodHandles.Lookup myLookup = MethodHandles.lookup();

            // This lookup is only to crack Spring's private nested class / method.
            MethodHandles.Lookup springLookup =
                    MethodHandles.privateLookupIn(DataSourceTransactionManager.class, myLookup);

            Class<?> inner = Class.forName(
                    DataSourceTransactionManager.class.getName() + "$DataSourceTransactionObject"
            );

            // Direct MH: (inner, ConnectionHolder, boolean)void
            MethodHandle impl = springLookup.findVirtual(
                    inner,
                    "setConnectionHolder",
                    MethodType.methodType(void.class, ConnectionHolder.class, boolean.class)
            );

            MethodType samType = MethodType.methodType(
                    void.class,
                    JdbcTransactionObjectSupport.class,
                    ConnectionHolder.class,
                    boolean.class
            );

            MethodType instantiatedType = MethodType.methodType(
                    void.class,
                    inner,
                    ConnectionHolder.class,
                    boolean.class
            );

            CallSite cs = LambdaMetafactory.metafactory(
                    myLookup, // IMPORTANT: define lambda in *this* class context
                    "accept",
                    MethodType.methodType(ConnectionHolderSetter.class),
                    samType,
                    impl,              // direct handle from springLookup
                    instantiatedType
            );

            connectionHolderSetter = (ConnectionHolderSetter) cs.getTarget().invokeExact();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public DbqTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        if (definition instanceof ConnectionAwareTransactionDefinition def) {
            if (transaction instanceof JdbcTransactionObjectSupport txObject) {
                try {
                    var connection = new CloseSuppressingConnectionWrapper(def.getConnection());
//                        mh.invoke(txObject, new ConnectionHolder(connection), true);
                    connectionHolderSetter.accept(txObject, new ConnectionHolder(connection), true);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.doBegin(transaction, definition);
    }

}
