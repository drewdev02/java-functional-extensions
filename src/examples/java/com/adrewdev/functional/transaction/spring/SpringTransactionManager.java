package com.adrewdev.functional.transaction.spring;

import com.adrewdev.functional.transaction.Transaction;
import com.adrewdev.functional.transaction.TransactionManager;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Spring Framework implementation of {@link TransactionManager}.
 * 
 * <p>This class integrates with Spring's transaction management infrastructure,
 * allowing the use of functional Result types within Spring-managed transactions.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * \@Configuration
 * public class AppConfig {
 *     \@Bean
 *     public TransactionManager transactionManager(
 *         PlatformTransactionManager springTxManager
 *     ) {
 *         return new SpringTransactionManager(springTxManager);
 *     }
 * }
 * 
 * // In your service:
 * \@Autowired
 * private TransactionManager txManager;
 * 
 * public Result<Customer, String> promoteCustomer(Long id) {
 *     return Result.from(getCustomer(id))
 *         .toResult("Customer not found")
 *         .withTransaction(txManager, customer -> 
 *             Result.success(customer)
 *                 .tap(Customer::promote)
 *                 .tap(Customer::clearAppointments)
 *         );
 * }
 * }</pre>
 * 
 * <p>This implementation respects Spring's transaction propagation settings.
 * By default, it uses {@code PROPAGATION_REQUIRED}, which means it will join
 * an existing transaction if one is already active.</p>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 * @see TransactionManager
 * @see SpringTransaction
 * @see PlatformTransactionManager
 */
public class SpringTransactionManager implements TransactionManager {
    
    private final PlatformTransactionManager transactionManager;
    
    /**
     * Creates a new SpringTransactionManager with the given Spring PlatformTransactionManager.
     * 
     * @param transactionManager the Spring PlatformTransactionManager to delegate to
     * @throws IllegalArgumentException if transactionManager is null
     */
    public SpringTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager != null ? transactionManager : 
            throwNullTransactionManager();
    }
    
    @Override
    public Transaction begin() {
        TransactionStatus status = transactionManager.getTransaction(
            new DefaultTransactionDefinition()
        );
        return new SpringTransaction(transactionManager, status);
    }
    
    @Override
    public Transaction getCurrent() {
        // Spring's transaction management is thread-bound
        // This would require access to TransactionSynchronizationManager
        // which is optional, so we return null for simplicity
        return null;
    }
    
    private static PlatformTransactionManager throwNullTransactionManager() {
        throw new IllegalArgumentException("PlatformTransactionManager cannot be null");
    }
}
