package com.github.al.realworld.ds;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.TransactionDefinition;

import java.sql.Connection;

@RequiredArgsConstructor
@Getter
public class ConnectionAwareTransactionDefinition implements TransactionDefinition {

    private final @NonNull Connection connection;

}
