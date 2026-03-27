package com.adrewdev.functional.dsl

import com.adrewdev.functional.Result
import com.adrewdev.functional.ResultTransaction
import com.adrewdev.functional.Unit
import com.adrewdev.functional.transaction.TransactionManager
import java.util.function.Function

/**
 * Transaction scope extensions for Result monad.
 * 
 * Provides idiomatic Kotlin extensions for executing operations
 * within a transaction scope with automatic rollback on failure.
 * 
 * @since 1.2.0
 */
object TransactionDsl {
    // Extension functions are defined below
}

/**
 * Executes the operation within a transaction scope.
 * 
 * <p>If this Result is a failure, the operation is not executed and
 * no transaction is started. If this Result is successful, a new
 * transaction is begun and the operation is executed.</p>
 * 
 * <p>The transaction is committed if the operation succeeds, or rolled back
 * if the operation fails or throws an exception.</p>
 * 
 * <p>This overload allows the operation to return a Result with a different
 * value type while maintaining the same error type.</p>
 * 
 * @param U the type of the output value
 * @param transactionManager the transaction manager to use
 * @param operation the operation to execute within the transaction
 * @return the result of the operation, or this failure if this Result failed
 * 
 * @example
 * ```kotlin
 * // Basic transaction scope
 * val result = getCustomer(id)
 *     .toResult("Customer not found")
 *     .withTransaction(transactionManager) { customer ->
 *         Result.success(customer)
 *             .tap { it.promote() }
 *             .tap { it.clearAppointments() }
 *     }
 * 
 * // Type transformation
 * val result = Result.success(customerId)
 *     .withTransaction(transactionManager) { id ->
 *         getCustomer(id).toResult("Not found")
 *     }
 * 
 * // The transaction will:
 * // - Commit if all operations succeed
 * // - Rollback automatically if any operation fails
 * ```
 * 
 * @see ResultTransaction.withTransaction
 */
fun <T, U, E> Result<T, E>.withTransaction(
    transactionManager: TransactionManager,
    operation: (T) -> Result<U, E>
): Result<U, E> {
    return ResultTransaction.withTransaction(this, transactionManager, operation)
}

/**
 * Executes a transactional operation.
 * 
 * <p>This function starts a new transaction and executes the provided operation.
 * The transaction is committed if the operation succeeds, or rolled back if
 * the operation fails or throws an exception.</p>
 * 
 * <p>Use this function when you want to execute a standalone transactional
 * operation without chaining from a previous Result.</p>
 * 
 * @param T the type of the value
 * @param E the type of the error
 * @param transactionManager the transaction manager to use
 * @param operation the operation to execute within the transaction
 * @return the result of the operation
 * 
 * @example
 * ```kotlin
 * // Standalone transaction
 * val result = resultWithTransaction(transactionManager) {
 *     Result.success("value")
 *         .tap { repository.save(entity1) }
 *         .tap { repository.save(entity2) }
 * }
 * 
 * // With Unit
 * val unitResult: Result<Unit, String> = resultWithTransaction(transactionManager) {
 *     Result.success(Unit)
 *         .tap { repository.delete(oldRecords) }
 * }
 * ```
 * 
 * @see ResultTransaction.withTransaction
 */
fun <T, E> resultWithTransaction(
    transactionManager: TransactionManager,
    operation: () -> Result<T, E>
): Result<T, E> {
    return ResultTransaction.withTransaction(transactionManager) {
        operation()
    }
}
