package com.github.al.realworld.ds;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link DriverManagerDataSource} variant that tracks opened and currently active connections.
 */
public class CountingDriverManagerDataSource extends DriverManagerDataSource {

    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    private final AtomicInteger activeConnectionCount = new AtomicInteger();

    /**
     * Creates an unconfigured counting data source.
     */
    public CountingDriverManagerDataSource() {
        super();
    }

    /**
     * Creates a counting data source using the given JDBC URL.
     *
     * @param url JDBC URL
     */
    public CountingDriverManagerDataSource(String url) {
        super(url);
    }

    /**
     * Creates a counting data source using explicit connection credentials.
     *
     * @param url      JDBC URL
     * @param username database username
     * @param password database password
     */
    public CountingDriverManagerDataSource(String url, String username, String password) {
        super(url, username, password);
    }

    /**
     * Returns the total number of opened connections.
     *
     * @return cumulative opened connection count
     */
    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }

    /**
     * Returns the current number of active connections.
     *
     * @return active connection count
     */
    public int getActiveConnectionCount() {
        return activeConnectionCount.get();
    }

    /**
     * Obtains a connection and registers it for counter tracking.
     *
     * @return counted connection wrapper
     * @throws SQLException if acquiring the connection fails
     */
    @Override
    public Connection getConnection() throws SQLException {
        return registerConnection(super.getConnection());
    }

    /**
     * Obtains a connection with explicit credentials and registers it for counter tracking.
     *
     * @param username database username
     * @param password database password
     * @return counted connection wrapper
     * @throws SQLException if acquiring the connection fails
     */
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
