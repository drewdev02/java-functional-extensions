package com.adrewdev.functional.transaction.jdbc;

import com.adrewdev.functional.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JDBC implementation of {@link Transaction}.
 * 
 * <p>This class wraps a JDBC Connection and provides transaction management
 * capabilities. The transaction is committed or rolled back based on explicit
 * calls to {@link #commit()} or {@link #rollback()}, or automatically rolled
 * back when used in a try-with-resources block if not committed.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * try (Transaction tx = transactionManager.begin()) {
 *     // Perform database operations
 *     PreparedStatement stmt = connection.prepareStatement(...);
 *     stmt.executeUpdate();
 *     
 *     // Commit if everything succeeds
 *     tx.commit();
 * }
 * // If an exception occurs or commit() is not called, rollback happens automatically
 * }</pre>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 * @see JdbcTransactionManager
 * @see Transaction
 */
public class JdbcTransaction implements Transaction {
    
    private final Connection connection;
    private final AtomicBoolean committed = new AtomicBoolean(false);
    private final AtomicBoolean rolledBack = new AtomicBoolean(false);
    
    /**
     * Creates a new JdbcTransaction with the given Connection.
     * 
     * @param connection the JDBC connection to manage
     * @throws IllegalArgumentException if connection is null
     */
    public JdbcTransaction(Connection connection) {
        this.connection = connection != null ? connection : throwNullConnection();
    }
    
    @Override
    public void commit() {
        if (!isActive()) {
            throw new IllegalStateException("Transaction is not active");
        }
        
        try {
            connection.commit();
            committed.set(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }
    
    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("Transaction is not active");
        }
        
        try {
            connection.rollback();
            rolledBack.set(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }
    
    @Override
    public boolean isActive() {
        try {
            return !connection.getAutoCommit() && 
                   !committed.get() && 
                   !rolledBack.get();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check transaction status", e);
        }
    }
    
    @Override
    public void close() {
        if (isActive()) {
            rollback();
        }
        
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }
    
    /**
     * Gets the underlying JDBC connection.
     * 
     * @return the JDBC connection managed by this transaction
     */
    public Connection getConnection() {
        return connection;
    }
    
    private static Connection throwNullConnection() {
        throw new IllegalArgumentException("Connection cannot be null");
    }
}
