package com.github.al.realworld.ds;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link Connection} wrapper that delegates all operations except {@link #close()}.
 *
 * <p>The wrapped connection remains open so its lifecycle can be managed externally.</p>
 */
@RequiredArgsConstructor
public class CloseSuppressingConnectionWrapper implements Connection {

    @Delegate
    private final @NonNull Connection connection;

    /**
     * Intentionally does nothing to suppress closing the wrapped connection.
     */
    @Override
    public void close() throws SQLException {
        // do nothing
    }

}
