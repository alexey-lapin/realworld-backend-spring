package com.github.al.realworld.ds;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

@FunctionalInterface
public interface ConnectionHolderSetter {

    void accept(JdbcTransactionObjectSupport receiver, ConnectionHolder connectionHolder, boolean newConnectionHolder);

}
