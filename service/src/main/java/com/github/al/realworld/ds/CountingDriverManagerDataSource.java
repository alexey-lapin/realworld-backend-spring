package com.github.al.realworld.ds;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CountingDriverManagerDataSource extends DriverManagerDataSource {

    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    private final AtomicInteger activeConnectionCount = new AtomicInteger();

    public CountingDriverManagerDataSource() {
        super();
    }

    public CountingDriverManagerDataSource(String url) {
        super(url);
    }

    public CountingDriverManagerDataSource(String url, String username, String password) {
        super(url, username, password);
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }

    public int getActiveConnectionCount() {
        return activeConnectionCount.get();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return registerConnection(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return registerConnection(super.getConnection(username, password));
    }

    private Connection registerConnection(Connection connection) {
        openedConnectionCount.incrementAndGet();
        activeConnectionCount.incrementAndGet();
        return new CloseCallbackConnection(connection);
    }

    @RequiredArgsConstructor
    private class CloseCallbackConnection implements Connection {

        @Delegate
        private final @NonNull Connection connection;

        private final AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void close() throws SQLException {
            if (closed.compareAndSet(false, true)) {
                try {
                    connection.close();
                } finally {
                    activeConnectionCount.decrementAndGet();
                }
            }
        }

    }

}
