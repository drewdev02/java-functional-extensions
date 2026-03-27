package com.adrewdev.functional.transaction.jdbc;

import com.adrewdev.functional.transaction.Transaction;
import com.adrewdev.functional.transaction.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link TransactionManager}.
 * 
 * <p>This class manages JDBC transactions by controlling the auto-commit mode
 * of database connections. When a transaction begins, auto-commit is disabled,
 * and the connection must be explicitly committed or rolled back.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * DataSource dataSource = getDataSource();
 * TransactionManager txManager = new JdbcTransactionManager(dataSource);
 * 
 * Result<Customer, String> result = Result.from(getCustomer(id))
 *     .toResult("Customer not found")
 *     .withTransaction(txManager, customer -> 
 *         Result.success(customer)
 *             .tap(Customer::promote)
 *             .tap(Customer::clearAppointments)
 *     );
 * }</pre>
 * 
 * <p>Note: This implementation obtains a new connection for each transaction.
 * For connection pooling and more advanced features, consider using a framework
 * like Spring or a connection pool like HikariCP.</p>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 * @see TransactionManager
 * @see JdbcTransaction
 */
public class JdbcTransactionManager implements TransactionManager {
    
    private final DataSource dataSource;
    
    /**
     * Creates a new JdbcTransactionManager with the given DataSource.
     * 
     * @param dataSource the DataSource to obtain connections from
     * @throws IllegalArgumentException if dataSource is null
     */
    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource != null ? dataSource : 
            throwNullDataSource();
    }
    
    @Override
    public Transaction begin() {
        try {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            return new JdbcTransaction(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }
    
    @Override
    public Transaction getCurrent() {
        // JdbcTransactionManager does not track current transaction
        // Each begin() call creates a new independent transaction
        return null;
    }
    
    private static DataSource throwNullDataSource() {
        throw new IllegalArgumentException("DataSource cannot be null");
    }
}
