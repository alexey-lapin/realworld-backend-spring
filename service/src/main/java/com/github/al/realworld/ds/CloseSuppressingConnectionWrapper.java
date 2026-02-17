package com.github.al.realworld.ds;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class CloseSuppressingConnectionWrapper implements Connection {

    @Delegate
    private final @NonNull Connection connection;

    @Override
    public void close() throws SQLException {
        // do nothing
    }

}
