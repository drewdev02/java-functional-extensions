package com.adrewdev.functional.dsl

import com.adrewdev.functional.Result

/**
 * Kotlin DSL for Result monad.
 * 
 * Provides idiomatic Kotlin extensions and builders for [Result],
 * including railway pattern support.
 * 
 * @since 1.1.0
 */
object ResultDsl {
    // Builder functions are defined as top-level functions for better ergonomics
}

// ============================================================================
// Builder Functions
// ============================================================================

/**
 * Creates a [Result] by executing a block that may throw exceptions.
 *
 * This is the primary builder function for creating Result instances.
 * It automatically catches any exceptions thrown by the block and converts
 * them to failure values using the provided error handler.
 *
 * @param errorHandler function to convert Throwable to error type E
 * @param block the code to execute
 * @return [Result.Success] if block succeeds, [Result.Failure] if it throws
 *
 * @example
 * ```kotlin
 * // With custom error handler
 * val result = result<String, String>({ e -> "Error: ${e.message}" }) {
 *     readFile(path)  // May throw IOException
 * }
 *
 * // With sealed error types
 * sealed class Error {
 *     data class IoError(val message: String) : Error()
 *     data class ParseError(val message: String) : Error()
 * }
 *
 * val result = result<String, Error>({ e ->
 *     when (e) {
 *         is IOException -> Error.IoError(e.message ?: "")
 *         else -> Error.ParseError(e.message ?: "")
 *     }
 * }) {
 *     parseFile(path)
 * }
 *
 * // Railway pattern
 * val email = result {
 *     val user = getUser(id).bind()
 *     ensure(user.active) { Error.UserInactive(user.id) }
 *     user.email
 * }
 * ```
 *
 * @see result the overload with default String error handler
 */
inline fun <T, E> result(
    crossinline errorHandler: (Throwable) -> E,
    crossinline block: () -> T
): Result<T, E> {
    return Result.of({ block() }, { errorHandler(it) })
}

/**
 * Creates a [Result] by executing a block that may throw exceptions.
 *
 * This overload uses a default error handler that converts any Throwable
 * to a String error message. Use this for simple cases where you don't
 * need custom error types.
 *
 * @param block the code to execute
 * @return [Result.Success] with value, or [Result.Failure] with error message
 *
 * @example
 * ```kotlin
 * // Simple case with String errors
 * val result = result {
 *     readFile(path)  // Returns Result<T, String>
 * }
 *
 * // Railway pattern with String errors
 * val email = result {
 *     val user = getUser(id).bind()
 *     ensure(user.active) { "User ${user.id} is inactive" }
 *     user.email
 * }
 *
 * // Chaining operations
 * val output = result {
 *     val data = readData().bind()
 *     val processed = processData(data).bind()
 *     writeOutput(processed).bind()
 * }
 * ```
 *
 * @see result the overload with custom error handler
 */
inline fun <T> result(crossinline block: () -> T): Result<T, String> {
    return result(
        errorHandler = { it.message ?: "Unknown error" },
        block = block
    )
}

// ============================================================================
// Extension Functions
// ============================================================================

/**
 * Converts an exception-throwing operation to [Result].
 * 
 * @receiver the operation that may throw
 * @param errorHandler function to convert Throwable to E
 * @return [Result.Success] or [Result.Failure]
 */
inline fun <T, E> (() -> T).toResult(
    crossinline errorHandler: (Throwable) -> E
): Result<T, E> {
    return result(errorHandler, this)
}

/**
 * Extracts the value from [Result] or short-circuits with failure.
 *
 * This is the key function for railway pattern programming. Inside a
 * [result] builder block, calling bind() on a failed Result will cause
 * the entire block to short-circuit and return that failure.
 *
 * @receiver the Result to extract from
 * @return the success value
 * @throws NoSuchElementException if this Result is failure
 *
 * @example
 * ```kotlin
 * // Basic railway pattern
 * val email = result {
 *     val user = getUser(id).bind()  // Extracts or returns failure
 *     user.email
 * }
 *
 * // Chaining multiple operations
 * val output = result {
 *     val data = readData().bind()      // Short-circuits on failure
 *     val processed = process(data).bind()  // Short-circuits on failure
 *     save(processed).bind()            // Short-circuits on failure
 * }
 *
 * // With Maybe
 * val email = result {
 *     val user = getUserMaybe(id).bind()  // Converts None to failure
 *     user.email
 * }
 * ```
 *
 * @see ResultDslScope.bind the scope version for resultScope
 * @see ensure for validation with short-circuit
 */
@JvmName("bindResult")
inline fun <T, E> Result<T, E>.bind(): T {
    return getValueOrThrow()
}

/**
 * Extracts the value from [Maybe] or short-circuits with none.
 * This enables railway pattern with Maybe inside Result blocks.
 * 
 * @receiver the Maybe to extract from
 * @return the value
 * @throws NoSuchElementException if this Maybe is None
 * 
 * @example
 * ```kotlin
 * val email = result {
 *     val user = getUserMaybe(id).bind()  // Extracts or throws
 *     user.email
 * }
 * ```
 */
@JvmName("bindMaybe")
inline fun <T> com.adrewdev.functional.Maybe<T>.bind(): T {
    return getValueOrThrow()
}

/**
 * Validates a condition and returns failure if false.
 *
 * This function is essential for railway pattern validation. It checks
 * a boolean condition and, if false, converts the Result to a failure
 * with the provided error value.
 *
 * @param condition the condition to validate
 * @param error the error to return if condition is false
 * @return this Result if condition is true, or failure with error
 *
 * @example
 * ```kotlin
 * // Basic validation
 * val result = result {
 *     val user = getUser(id).bind()
 *     ensure(user.active) { Error.Inactive(user.id) }
 *     user.email
 * }
 *
 * // Multiple validations
 * val email = result {
 *     val user = getUser(id).bind()
 *     ensure(user.active) { Error.Inactive(user.id) }
 *     ensure(user.email.isNotEmpty()) { Error.EmptyEmail }
 *     ensure(user.email.contains("@")) { Error.InvalidEmail(user.email) }
 *     user.email
 * }
 *
 * // With String errors
 * val validated = result {
 *     val value = getValue().bind()
 *     ensure(value > 0) { "Value must be positive" }
 *     ensure(value < 100) { "Value must be less than 100" }
 *     value
 * }
 * ```
 *
 * @see ensure the predicate overload for value-based validation
 */
fun <T, E> Result<T, E>.ensure(condition: Boolean, error: E): Result<T, E> {
    return if (condition) this else Result.failure(error)
}

/**
 * Validates a condition on the success value.
 * 
 * @param predicate the predicate to validate
 * @param error the error to return if predicate is false
 * @return this Result if predicate is true, or failure with error
 */
inline fun <T, E> Result<T, E>.ensure(
    crossinline predicate: (T) -> Boolean,
    crossinline error: (T) -> E
): Result<T, E> {
    return this.where({ predicate(it) }, { error(it) })
}

/**
 * Transforms the success value.
 * 
 * @param transform the transformation function
 * @return [Result.Success] with transformed value, or [Result.Failure]
 */
inline fun <T, U, E> Result<T, E>.map(
    crossinline transform: (T) -> U
): Result<U, E> {
    return this.map { transform(it) }
}

/**
 * Chains Result-returning functions (flatMap).
 * 
 * @param transform the function that returns Result
 * @return the result of chaining the functions
 */
inline fun <T, U, E> Result<T, E>.flatMap(
    crossinline transform: (T) -> Result<U, E>
): Result<U, E> {
    return this.bind { transform(it) }
}

/**
 * Recovers from failure by providing a fallback value.
 * 
 * @param recover function to convert error to fallback value
 * @return success with original value or recovered value
 */
inline fun <T, E> Result<T, E>.recover(
    crossinline recover: (E) -> T
): Result<T, E> {
    return this.recover { recover(it) }
}

/**
 * Recovers from failure with a fallback Result.
 * 
 * @param recover function to convert error to fallback Result
 * @return original success or recovered Result
 */
inline fun <T, E> Result<T, E>.recoverWith(
    crossinline recover: (E) -> Result<T, E>
): Result<T, E> {
    return if (isSuccessful()) {
        this
    } else {
        recover(getErrorOrThrow())
    }
}

/**
 * Performs side effect on success value.
 * 
 * @param action the side effect to perform
 * @return this Result unchanged
 */
inline fun <T, E> Result<T, E>.tapSuccess(
    crossinline action: (T) -> Unit
): Result<T, E> {
    return this.tapSuccess { action(it) }
}

/**
 * Performs side effect on failure value.
 * 
 * @param action the side effect to perform
 * @return this Result unchanged
 */
inline fun <T, E> Result<T, E>.tapFailure(
    crossinline action: (E) -> Unit
): Result<T, E> {
    return this.tapFailure { action(it) }
}

/**
 * Transforms the error value.
 * 
 * @param transform the transformation function
 * @return [Result.Success] unchanged, or [Result.Failure] with transformed error
 */
inline fun <T, E, F> Result<T, E>.mapError(
    crossinline transform: (E) -> F
): Result<T, F> {
    return this.mapError { transform(it) }
}

// ============================================================================
// Pattern Matching Support
// ============================================================================

/**
 * Kotlin wrapper for Result that enables when expressions.
 * 
 * This sealed class provides exhaustive pattern matching for Result values.
 * 
 * @param T the type of the success value
 * @param E the type of the error
 */
sealed class ResultK<out T, out E> {
    /**
     * Represents a Success case containing a value.
     * 
     * @param T the type of the contained value
     * @property value the success value
     */
    data class Success<out T>(val value: T) : ResultK<T, Nothing>()
    
    /**
     * Represents a Failure case containing an error.
     * 
     * @param E the type of the contained error
     * @property error the error value
     */
    data class Failure<out E>(val error: E) : ResultK<Nothing, E>()
    
    companion object {
        /**
         * Converts a Java [Result] to [ResultK] for pattern matching.
         * 
         * @param result the Result to convert
         * @return [ResultK.Success] if the Result is successful, [ResultK.Failure] otherwise
         */
        fun <T, E> from(result: Result<T, E>): ResultK<T, E> {
            return if (result.isSuccessful()) {
                Success(result.getValueOrThrow())
            } else {
                Failure(result.getErrorOrThrow())
            }
        }
    }
}

/**
 * Converts [Result] to [ResultK] for pattern matching.
 *
 * This extension function wraps a Java [Result] in a Kotlin sealed class,
 * enabling exhaustive pattern matching with when expressions.
 *
 * @receiver the Result to convert
 * @return [ResultK.Success] if the Result is successful, [ResultK.Failure] otherwise
 *
 * @example
 * ```kotlin
 * // Basic pattern matching
 * when (result.toResultK()) {
 *     is ResultK.Success -> println("Value: ${result.value}")
 *     is ResultK.Failure -> println("Error: ${result.error}")
 * }
 *
 * // With sealed error types
 * when (val r = getUser(id).toResultK()) {
 *     is ResultK.Success -> processUser(r.value)
 *     is ResultK.Failure -> when (r.error) {
 *         is Error.NotFound -> logNotFound(r.error.id)
 *         is Error.Inactive -> logInactive(r.error.id)
 *     }
 * }
 *
 * // In expression position
 * fun getMessage(result: Result<String, Error>): String =
 *     when (result.toResultK()) {
 *         is ResultK.Success -> "Success: ${result.value}"
 *         is ResultK.Failure -> "Error: ${result.error}"
 *     }
 * ```
 *
 * @see ResultK the sealed class for pattern matching
 */
fun <T, E> Result<T, E>.toResultK(): ResultK<T, E> {
    return ResultK.from(this)
}
