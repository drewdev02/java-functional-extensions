package com.adrewdev.functional;

import com.adrewdev.functional.transaction.Transaction;
import com.adrewdev.functional.transaction.TransactionManager;

import java.util.function.Function;
import java.util.function.Supplier;

// Import Unit class

/**
 * Transaction scope extensions for Result.
 * 
 * <p>This class provides static methods for executing operations within a transaction
 * scope, ensuring automatic rollback on failure and commit on success.</p>
 * 
 * <p>The transaction scope pattern ensures that multiple operations are executed
 * atomically - either all succeed or all are rolled back.</p>
 * 
 * <p>Usage example:</p>
 * <pre>{@code
 * TransactionManager txManager = new JdbcTransactionManager(dataSource);
 * 
 * // Basic transaction scope
 * Result<Customer, String> result = Result.success(customerId)
 *     .bind(id -> getCustomer(id))
 *     .toResult("Customer not found")
 *     .withTransaction(txManager, customer -> 
 *         Result.success(customer)
 *             .tap(Customer::promote)
 *             .tap(Customer::clearAppointments)
 *     )
 *     .tap(customer -> emailGateway.sendNotification(customer.getEmail()));
 * 
 * // The transaction will:
 * // - Commit if all operations succeed
 * // - Rollback automatically if any operation fails
 * // - Rollback automatically if an exception is thrown
 * }</pre>
 * 
 * <p>For Kotlin users, extension functions are available in the
 * {@code com.adrewdev.functional.dsl} package.</p>
 * 
 * @since 1.2.0
 * @author java-functional-extensions
 * @see TransactionManager
 * @see Transaction
 */
public final class ResultTransaction {
    
    private ResultTransaction() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Executes an operation within a transaction scope.
     * 
     * <p>If the initial result is a failure, the operation is not executed and
     * no transaction is started. If the initial result is successful, a new
     * transaction is begun and the operation is executed.</p>
     * 
     * <p>The transaction is committed if the operation succeeds, or rolled back
     * if the operation fails or throws an exception.</p>
     * 
     * <p>This overload allows the operation to return a Result with a different
     * value type while maintaining the same error type.</p>
     * 
     * @param <T> the type of the input value
     * @param <U> the type of the output value
     * @param <E> the type of the error
     * @param result the initial result to process
     * @param transactionManager the transaction manager to use
     * @param operation the operation to execute within the transaction
     * @return the result of the operation, or the original failure if initial result failed
     * 
     * @example
     * <pre>{@code
     * // Same type transformation
     * Result<Customer, String> result = Result.from(getCustomer(id))
     *     .toResult("Customer not found")
     *     .withTransaction(transactionManager, customer -> Result.success(customer)
     *         .tap(Customer::promote)
     *         .tap(Customer::clearAppointments));
     * 
     * // Different type transformation
     * Result<String, String> result = Result.success(customerId)
     *     .withTransaction(transactionManager, id -> 
     *         getCustomer(id).toResult("Not found")
     *     );
     * }</pre>
     */
    public static <T, U, E> Result<U, E> withTransaction(
        Result<T, E> result,
        TransactionManager transactionManager,
        Function<T, Result<U, E>> operation
    ) {
        if (!result.isSuccessful()) {
            return (Result<U, E>) result;
        }
        
        try (Transaction tx = transactionManager.begin()) {
            Result<U, E> operationResult = operation.apply(result.getValueOrThrow());
            
            if (operationResult.isSuccessful()) {
                tx.commit();
            } else {
                tx.rollback();
            }
            
            return operationResult;
        } catch (Exception e) {
            return Result.failure((E) ("Transaction failed: " + e.getMessage()));
        }
    }
    
    /**
     * Executes an operation within a transaction scope.
     * 
     * <p>This method starts a new transaction and executes the provided operation.
     * The transaction is committed if the operation succeeds, or rolled back if
     * the operation fails or throws an exception.</p>
     * 
     * <p>Use this method when you want to execute a standalone transactional operation
     * without chaining from a previous Result.</p>
     * 
     * @param <T> the type of the value
     * @param <E> the type of the error
     * @param transactionManager the transaction manager to use
     * @param operation the operation to execute within the transaction
     * @return the result of the operation
     * 
     * @example
     * <pre>{@code
     * Result<Unit, String> result = ResultTransaction.withTransaction(
     *     transactionManager,
     *     () -> Result.success(Unit.VALUE)
     *         .tap(() -> repository.save(entity1))
     *         .tap(() -> repository.save(entity2))
     * );
     * }</pre>
     */
    public static <T, E> Result<T, E> withTransaction(
        TransactionManager transactionManager,
        Supplier<Result<T, E>> operation
    ) {
        try (Transaction tx = transactionManager.begin()) {
            Result<T, E> operationResult = operation.get();
            
            if (operationResult.isSuccessful()) {
                tx.commit();
            } else {
                tx.rollback();
            }
            
            return operationResult;
        } catch (Exception e) {
            return Result.failure((E) ("Transaction failed: " + e.getMessage()));
        }
    }
    
}
