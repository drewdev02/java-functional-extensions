package com.adrewdev.functional.transaction;

/**
 * Represents an active transaction.
 * 
 * <p>This interface abstracts a database or resource transaction. It provides
 * methods to commit or rollback the transaction, and implements {@link AutoCloseable}
 * for try-with-resources support.</p>
 * 
 * <p>When used in a try-with-resources block, the transaction will automatically
 * rollback if not explicitly committed, ensuring data consistency in case of errors.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * try (Transaction tx = transactionManager.begin()) {
 *     // Perform database operations
 *     repository.save(entity);
 *     
 *     // If everything succeeds, commit the transaction
 *     tx.commit();
 * }
 * // If an exception occurs or commit() is not called, rollback happens automatically
 * }</pre>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 */
public interface Transaction extends AutoCloseable {
    
    /**
     * Commits the transaction.
     * 
     * <p>This method permanently applies all changes made during the transaction
     * to the underlying data store. After calling commit, the transaction is no
     * longer active and cannot be used for further operations.</p>
     * 
     * <p>Calling commit on an inactive transaction or after commit/rollback has
     * already been called may result in an exception.</p>
     * 
     * @throws RuntimeException if the commit fails or transaction is not active
     */
    void commit();
    
    /**
     * Rolls back the transaction.
     * 
     * <p>This method undoes all changes made during the transaction, restoring
     * the data store to its state before the transaction began. After calling
     * rollback, the transaction is no longer active.</p>
     * 
     * <p>Calling rollback on an inactive transaction or after commit/rollback has
     * already been called may result in an exception.</p>
     * 
     * @throws RuntimeException if the rollback fails
     */
    void rollback();
    
    /**
     * Checks if the transaction is active.
     * 
     * <p>A transaction is considered active if it has been started and neither
     * committed nor rolled back yet.</p>
     * 
     * @return {@code true} if the transaction is active, {@code false} otherwise
     */
    boolean isActive();
    
    /**
     * Closes the transaction (rollback if not committed).
     * 
     * <p>This method is called automatically when using try-with-resources.
     * If the transaction is still active (not committed or rolled back),
     * it will be rolled back to ensure data consistency.</p>
     * 
     * <p>This default implementation makes the Transaction interface safe
     * to use in try-with-resources blocks, providing automatic rollback
     * on errors or early exits.</p>
     * 
     * <p>Example:</p>
     * <pre>{@code
     * try (Transaction tx = transactionManager.begin()) {
     *     performOperations();
     *     tx.commit();  // Explicit commit - close() will do nothing
     * }
     * 
     * try (Transaction tx = transactionManager.begin()) {
     *     performOperations();
     *     // No commit - close() will automatically rollback
     * }
     * }</pre>
     */
    @Override
    default void close() {
        if (isActive()) {
            rollback();
        }
    }
}
