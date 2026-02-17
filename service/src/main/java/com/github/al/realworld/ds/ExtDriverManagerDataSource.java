package com.github.al.realworld.ds;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ExtDriverManagerDataSource extends DriverManagerDataSource {

    private final AtomicInteger openedCount = new AtomicInteger();

    private final AtomicInteger activeCount = new AtomicInteger();

    public ExtDriverManagerDataSource() {
        super();
    }

    public ExtDriverManagerDataSource(String url) {
        super(url);
    }

    public ExtDriverManagerDataSource(String url, String username, String password) {
        super(url, username, password);
    }

    public int getOpenedCount() {
        return openedCount.get();
    }

    public int getActiveCount() {
        return activeCount.get();
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
        openedCount.incrementAndGet();
        activeCount.incrementAndGet();
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
                    activeCount.decrementAndGet();
                }
            }
        }

    }

}
