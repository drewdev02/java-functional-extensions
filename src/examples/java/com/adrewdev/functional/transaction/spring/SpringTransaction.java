package com.adrewdev.functional.transaction.spring;

import com.adrewdev.functional.transaction.Transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Spring Framework implementation of {@link Transaction}.
 * 
 * <p>This class wraps a Spring TransactionStatus and provides transaction
 * management capabilities through Spring's PlatformTransactionManager.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * PlatformTransactionManager springTxManager = getTransactionManager();
 * TransactionManager txManager = new SpringTransactionManager(springTxManager);
 * 
 * try (Transaction tx = txManager.begin()) {
 *     // Perform database operations
 *     repository.save(entity);
 *     
 *     // Commit if everything succeeds
 *     tx.commit();
 * }
 * // If an exception occurs or commit() is not called, rollback happens automatically
 * }</pre>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 * @see SpringTransactionManager
 * @see Transaction
 */
public class SpringTransaction implements Transaction {
    
    private final PlatformTransactionManager transactionManager;
    private final TransactionStatus status;
    private boolean completed = false;
    
    /**
     * Creates a new SpringTransaction with the given PlatformTransactionManager
     * and TransactionStatus.
     * 
     * @param transactionManager the Spring PlatformTransactionManager
     * @param status the current TransactionStatus from Spring
     * @throws IllegalArgumentException if any parameter is null
     */
    public SpringTransaction(
        PlatformTransactionManager transactionManager,
        TransactionStatus status
    ) {
        this.transactionManager = transactionManager != null ? transactionManager : 
            throwNullTransactionManager();
        this.status = status != null ? status : throwNullStatus();
    }
    
    @Override
    public void commit() {
        if (!isActive()) {
            throw new IllegalStateException("Transaction is not active");
        }
        
        transactionManager.commit(status);
        completed = true;
    }
    
    @Override
    public void rollback() {
        if (!isActive()) {
            throw new IllegalStateException("Transaction is not active");
        }
        
        transactionManager.rollback(status);
        completed = true;
    }
    
    @Override
    public boolean isActive() {
        return !completed && !status.isCompleted();
    }
    
    /**
     * Gets the underlying Spring TransactionStatus.
     * 
     * @return the TransactionStatus managed by this transaction
     */
    public TransactionStatus getStatus() {
        return status;
    }
    
    private static PlatformTransactionManager throwNullTransactionManager() {
        throw new IllegalArgumentException("PlatformTransactionManager cannot be null");
    }
    
    private static TransactionStatus throwNullStatus() {
        throw new IllegalArgumentException("TransactionStatus cannot be null");
    }
}
