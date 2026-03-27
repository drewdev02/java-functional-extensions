package com.adrewdev.functional.transaction;

/**
 * Interface for managing transactions.
 * Implement this for your specific transaction manager (JTA, Spring, JDBC, etc.)
 * 
 * <p>This interface provides the abstraction for transaction management,
 * allowing different implementations for various persistence technologies.</p>
 * 
 * <p>Example implementations:</p>
 * <ul>
 *   <li>{@code JdbcTransactionManager} - for plain JDBC connections</li>
 *   <li>{@code SpringTransactionManager} - for Spring Framework transactions</li>
 *   <li>{@code JtaTransactionManager} - for JTA distributed transactions</li>
 * </ul>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * TransactionManager txManager = new JdbcTransactionManager(dataSource);
 * 
 * Result<Customer, String> result = Result.from(getCustomer(id))
 *     .toResult("Customer not found")
 *     .withTransaction(txManager, customer -> Result.success(customer)
 *         .tap(Customer::promote)
 *         .tap(Customer::clearAppointments));
 * }</pre>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 */
public interface TransactionManager {
    
    /**
     * Begins a new transaction.
     * 
     * <p>This method creates and starts a new transaction. The returned
     * {@link Transaction} object can be used to commit or rollback the transaction.</p>
     * 
     * <p>The transaction should be properly closed after use, either by calling
     * {@link Transaction#commit()} or {@link Transaction#rollback()}, or by using
     * try-with-resources which will automatically rollback if not committed.</p>
     * 
     * @return the transaction object representing the active transaction
     * @throws RuntimeException if the transaction cannot be started
     * 
     * @see Transaction#commit()
     * @see Transaction#rollback()
     */
    Transaction begin();
    
    /**
     * Gets the current transaction if exists.
     * 
     * <p>This method returns the currently active transaction associated with
     * the current execution context (e.g., current thread). This is useful
     * for checking if a transaction is already in progress.</p>
     * 
     * <p>Implementations may return {@code null} if no transaction is active,
     * or throw an exception depending on the transaction propagation behavior.</p>
     * 
     * @return the current transaction or {@code null} if no transaction is active
     */
    Transaction getCurrent();
}
