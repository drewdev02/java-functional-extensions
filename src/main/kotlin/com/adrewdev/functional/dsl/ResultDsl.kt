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
 * @param errorHandler function to convert Throwable to error type E
 * @param block the code to execute
 * @return [Result.Success] if block succeeds, [Result.Failure] if it throws
 * 
 * @example
 * ```kotlin
 * val result = result<String, String>({ e -> "Error: ${e.message}" }) {
 *     readFile(path)  // May throw IOException
 * }
 * ```
 */
inline fun <T, E> result(
    crossinline errorHandler: (Throwable) -> E,
    crossinline block: () -> T
): Result<T, E> {
    return Result.of({ block() }, { errorHandler(it) })
}

/**
 * Creates a [Result] by executing a block that may throw exceptions.
 * Uses default error handler that converts Throwable to String.
 * 
 * @param block the code to execute
 * @return [Result.Success] with value, or [Result.Failure] with error message
 * 
 * @example
 * ```kotlin
 * val result = result {
 *     readFile(path)  // Returns Result<T, String>
 * }
 * ```
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
 * This is the key function for railway pattern.
 * 
 * @receiver the Result to extract from
 * @return the success value
 * @throws NoSuchElementException if this Result is failure
 * 
 * @example
 * ```kotlin
 * val email = result {
 *     val user = getUser(id).bind()  // Extracts or returns failure
 *     user.email
 * }
 * ```
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
 * @param condition the condition to validate
 * @param error the error to return if condition is false
 * @return this Result if condition is true, or failure with error
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
 * This extension function enables exhaustive when expressions on Result values.
 * 
 * @receiver the Result to convert
 * @return [ResultK.Success] if the Result is successful, [ResultK.Failure] otherwise
 * 
 * @example
 * ```kotlin
 * val output = when (result.toResultK()) {
 *     is ResultK.Success -> "Got: ${result.value}"
 *     is ResultK.Failure -> "Error: ${result.error}"
 * }
 * ```
 */
fun <T, E> Result<T, E>.toResultK(): ResultK<T, E> {
    return ResultK.from(this)
}
