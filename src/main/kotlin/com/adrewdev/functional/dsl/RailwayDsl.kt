package com.adrewdev.functional.dsl

import com.adrewdev.functional.Result

/**
 * Advanced Railway Pattern DSL for Kotlin.
 * 
 * Provides Arrow Raise-style syntax for railway pattern
 * with bind(), ensure(), and advanced combinators.
 * 
 * @since 1.5.0
 * 
 * @example
 * ```kotlin
 * sealed class Error {
 *     data class UserNotFound(val id: Int) : Error()
 *     data class Inactive(val id: Int) : Error()
 *     data class InvalidEmail(val email: String) : Error()
 * }
 * 
 * data class User(val id: Int, val email: String, val active: Boolean)
 * 
 * fun getUser(id: Int): Result<User, Error> = ...
 * fun validateEmail(email: String): Result<String, Error> = ...
 * 
 * val email: Result<String, Error> = result {
 *     val user = getUser(1).bind()
 *     ensure(user.active) { Error.Inactive(user.id) }
 *     val validatedEmail = validateEmail(user.email).bind()
 *     validatedEmail
 * }
 * ```
 */
object RailwayDsl {
    // Scope class and builders are defined as top-level for better ergonomics
}

/**
 * Scope for railway pattern DSL with Arrow Raise-style syntax.
 * 
 * This class provides the context for railway pattern operations
 * within a [result] builder block.
 * 
 * @param T the type of the success value
 * @param E the type of the error
 * 
 * @example
 * ```kotlin
 * val result: Result<Int, String> = result {
 *     val x = Result.success<Int, String>(10).bind()
 *     ensure(x > 0) { "Must be positive" }
 *     x * 2
 * }
 * ```
 */
class ResultDslScope<T, E> {
    /**
     * Short-circuit exception for railway pattern.
     * 
     * This exception is thrown when [bind()] encounters a failure
     * or when [ensure()] validates to false. It carries the error
     * value up to the [result] builder which converts it to a
     * [Result.Failure].
     * 
     * @property error the error value that caused the short-circuit
     */
    class ResultShortCircuitException(val error: Any?) : Exception()
    
    /**
     * Extracts value from Result or short-circuits with error.
     * 
     * This is the key function for railway pattern. If the receiver
     * is a [Result.Success], it returns the contained value. If it's
     * a [Result.Failure], it throws a [ResultShortCircuitException]
     * which will be caught by the [result] builder and converted to
     * a [Result.Failure].
     * 
     * @receiver the Result to extract from
     * @return the success value
     * @throws ResultShortCircuitException if this Result is failure
     * 
     * @example
     * ```kotlin
     * val result: Result<Int, String> = result {
     *     val x = Result.success<Int, String>(10).bind()  // Returns 10
     *     val y = Result.failure<Int, String>("error").bind()  // Short-circuits
     *     x + y  // This line never executes
     * }
     * // result is Result.Failure("error")
     * ```
     */
    fun <U> Result<U, E>.bind(): U {
        if (this.isFailure()) {
            throw ResultShortCircuitException(this.getErrorOrThrow())
        }
        return this.getValueOrThrow()
    }
    
    /**
     * Validates condition and short-circuits if false.
     * 
     * This function checks a boolean condition. If true, it does nothing.
     * If false, it throws a [ResultShortCircuitException] with the provided
     * error, which will be caught by the [result] builder.
     * 
     * @param condition the condition to validate
     * @param error the error to return if condition is false
     * @throws ResultShortCircuitException if condition is false
     * 
     * @example
     * ```kotlin
     * val result: Result<Int, String> = result {
     *     val x = Result.success<Int, String>(42).bind()
     *     ensure(x > 0) { "Must be positive" }
     *     x  // Only reached if x > 0
     * }
     * ```
     */
    fun ensure(condition: Boolean, error: E) {
        if (!condition) {
            throw ResultShortCircuitException(error)
        }
    }
    
    /**
     * Validates value with predicate and short-circuits if false.
     * 
     * This is an extension-style ensure that validates the receiver
     * value against a predicate. If the predicate returns false,
     * it throws a [ResultShortCircuitException] with an error
     * derived from the value.
     * 
     * @param predicate the predicate to validate against
     * @param error function to create error from invalid value
     * @throws ResultShortCircuitException if predicate is false
     * 
     * @example
     * ```kotlin
     * val result: Result<String, String> = result {
     *     val email = Result.success<String, String>("user@test.com").bind()
     *     email.ensure({ it.endsWith("@test.com") }) { 
     *         "Invalid domain: $it" 
     *     }
     *     email
     * }
     * ```
     */
    fun <U> U.ensure(predicate: (U) -> Boolean, error: (U) -> E) {
        if (!predicate(this)) {
            throw ResultShortCircuitException(error(this))
        }
    }
}

// ============================================================================
// Builder Functions
// ============================================================================

/**
 * Creates a [Result] with a DSL scope for railway pattern.
 *
 * This function provides Arrow Raise-style syntax for railway pattern
 * programming. Within the scope, you can use:
 * - [ResultDslScope.bind()] to extract values or short-circuit
 * - [ResultDslScope.ensure()] to validate conditions
 *
 * The block executes in a [ResultDslScope] context. If any operation
 * short-circuits (via [bind()] on failure or [ensure()] on false),
 * the block exits early and returns a [Result.Failure] with the error.
 *
 * @param block the railway DSL scope
 * @return [Result.Success] if all operations succeed, [Result.Failure] otherwise
 *
 * @example
 * ```kotlin
 * // Basic railway pattern
 * sealed class Error {
 *     data class NotFound(val id: Int) : Error()
 *     data class Inactive(val id: Int) : Error()
 *     data class InvalidEmail(val email: String) : Error()
 * }
 *
 * data class User(val id: Int, val email: String, val active: Boolean)
 *
 * fun getUser(id: Int): Result<User, Error> = ...
 *
 * val email: Result<String, Error> = resultScope {
 *     val user = getUser(1).bind()
 *     ensure(user.active) { Error.Inactive(user.id) }
 *     ensure(user.email.endsWith("@test.com")) {
 *         Error.InvalidEmail(user.email)
 *     }
 *     user.email
 * }
 *
 * // Multiple operations
 * val output: Result<String, Error> = resultScope {
 *     val user = getUser(id).bind()
 *     val email = validateEmail(user.email).bind()
 *     val template = getEmailTemplate().bind()
 *     formatEmail(email, template)
 * }
 *
 * // Pattern matching on result
 * when (email.toResultK()) {
 *     is ResultK.Success -> println(email.value)
 *     is ResultK.Failure -> when (email.error) {
 *         is Error.NotFound -> println("Not found")
 *         is Error.Inactive -> println("Inactive")
 *         is Error.InvalidEmail -> println("Invalid email")
 *     }
 * }
 * ```
 *
 * @see ResultDslScope
 * @see ResultDslScope.bind
 * @see ResultDslScope.ensure
 * @see result the exception-handling builder
 */
inline fun <T, E> resultScope(
    crossinline block: ResultDslScope<T, E>.() -> T
): Result<T, E> {
    val scope = ResultDslScope<T, E>()
    return try {
        val value = block(scope)
        Result.success(value)
    } catch (e: ResultDslScope.ResultShortCircuitException) {
        @Suppress("UNCHECKED_CAST")
        Result.failure(e.error as E)
    } catch (e: Throwable) {
        @Suppress("UNCHECKED_CAST")
        Result.failure(e as E)
    }
}

// ============================================================================
// Advanced Combinators
// ============================================================================

/**
 * Combines two Results into a pair.
 *
 * If both Results are successful, returns a [Result.Success] containing
 * a [Pair] of both values. If either is a failure, returns the first
 * failure encountered.
 *
 * @param T1 the type of the first success value
 * @param T2 the type of the second success value
 * @param E the type of the error
 * @param receiver the first Result
 * @param other the second Result to combine with
 * @return [Result.Success] with Pair, or [Result.Failure]
 *
 * @example
 * ```kotlin
 * // Basic zip
 * val r1: Result<String, String> = Result.success("hello")
 * val r2: Result<Int, String> = Result.success(42)
 *
 * val zipped: Result<Pair<String, Int>, String> = r1.zip(r2)
 * // Result.Success("hello" to 42)
 *
 * // With transformation
 * val combined = r1.zip(r2) { str, num -> "$str has $num letters" }
 * // Result.Success("hello has 42 letters")
 *
 * // Failure propagation
 * val failure = Result.failure<String, String>("error")
 * val zippedWithFailure = r1.zip(failure)
 * // Result.Failure("error")
 * ```
 *
 * @see zip the transform overload
 */
fun <T1, T2, E> Result<T1, E>.zip(
    other: Result<T2, E>
): Result<Pair<T1, T2>, E> {
    if (!this.isSuccessful()) {
        return Result.failure(this.getErrorOrThrow())
    }
    if (!other.isSuccessful()) {
        return Result.failure(other.getErrorOrThrow())
    }
    return Result.success(Pair(this.getValueOrThrow(), other.getValueOrThrow()))
}

/**
 * Combines two Results with a transform function.
 * 
 * If both Results are successful, applies the transform function to
 * both values and returns the result wrapped in [Result.Success].
 * If either is a failure, returns the first failure encountered.
 * 
 * @param T1 the type of the first success value
 * @param T2 the type of the second success value
 * @param R the type of the transformed result
 * @param E the type of the error
 * @param receiver the first Result
 * @param other the second Result to combine with
 * @param transform function to combine the two values
 * @return [Result.Success] with transformed value, or [Result.Failure]
 * 
 * @example
 * ```kotlin
 * val result = Result.success("hello")
 *     .zip(Result.success(5)) { s, i -> "$s has $i letters" }
 * // Result.Success("hello has 5 letters")
 * ```
 */
inline fun <T1, T2, R, E> Result<T1, E>.zip(
    other: Result<T2, E>,
    crossinline transform: (T1, T2) -> R
): Result<R, E> {
    return zip(other).map { (a, b) -> transform(a, b) }
}

/**
 * Combines a list of Results, returning all values or first error.
 *
 * Iterates through the list and collects all success values.
 * If any Result is a failure, immediately returns that failure.
 * If all are successful, returns a [Result.Success] with the list
 * of all values.
 *
 * @param T the type of the success values
 * @param E the type of the error
 * @param results the list of Results to combine
 * @return [Result.Success] with list of values, or [Result.Failure]
 *
 * @example
 * ```kotlin
 * // All succeed
 * val results = listOf(
 *     Result.success(1),
 *     Result.success(2),
 *     Result.success(3)
 * )
 *
 * val all: Result<List<Int>, String> = all(results)
 * // Result.Success(listOf(1, 2, 3))
 *
 * // First failure stops processing
 * val withFailure = listOf(
 *     Result.success(1),
 *     Result.failure<Int, String>("error"),
 *     Result.success(3)
 * )
 *
 * val allWithFailure = all(withFailure)
 * // Result.Failure("error")
 *
 * // Real-world: batch validation
 * val emails = listOf("a@test.com", "b@test.com", "c@test.com")
 * val validated = all(emails.map { email ->
 *     result {
 *         ensure(email.endsWith("@test.com")) { "Invalid: $email" }
 *         email
 *     }
 * })
 * ```
 *
 * @see any for first success
 */
fun <T, E> all(results: List<Result<T, E>>): Result<List<T>, E> {
    val values = mutableListOf<T>()
    for (result in results) {
        if (result.isFailure()) {
            return Result.failure(result.getErrorOrThrow())
        }
        values.add(result.getValueOrThrow())
    }
    return Result.success(values)
}



/**
 * Returns first successful Result or last failure.
 *
 * Iterates through the list and returns the first successful Result.
 * If all are failures, returns the last failure.
 * If the list is empty, returns a failure with "Empty list" error.
 *
 * @param T the type of the success value
 * @param E the type of the error
 * @param results the list of Results to check
 * @return first [Result.Success], or last [Result.Failure]
 *
 * @example
 * ```kotlin
 * // First success wins
 * val results = listOf(
 *     Result.failure<Int, String>("error1"),
 *     Result.success(42),
 *     Result.success(100)
 * )
 *
 * val any: Result<Int, String> = any(results)
 * // Result.Success(42) - first success
 *
 * // All failures
 * val allFailures = listOf(
 *     Result.failure<Int, String>("error1"),
 *     Result.failure<Int, String>("error2")
 * )
 *
 * val anyFailure = any(allFailures)
 * // Result.Failure("error2") - last failure
 *
 * // Real-world: fallback chain
 * val email = any(listOf(
 *     fetchPrimaryEmail(),
 *     fetchSecondaryEmail(),
 *     fetchBackupEmail(),
 *     Result.failure("No email available")
 * ))
 * ```
 *
 * @see all for all values
 */
fun <T, E> any(results: List<Result<T, E>>): Result<T, E> {
    var lastFailure: Result<T, E>? = null
    for (result in results) {
        if (result.isSuccessful()) {
            return result
        }
        lastFailure = result
    }
    return lastFailure ?: Result.failure("Empty list" as E)
}

// ============================================================================
// Additional Utility Functions
// ============================================================================

/**
 * Combines three Results into a triple.
 * 
 * If all three Results are successful, returns a [Result.Success] containing
 * a [Triple] of all values. If any is a failure, returns the first failure.
 * 
 * @param T1 the type of the first success value
 * @param T2 the type of the second success value
 * @param T3 the type of the third success value
 * @param E the type of the error
 * @param r1 the first Result
 * @param r2 the second Result
 * @param r3 the third Result
 * @return [Result.Success] with Triple, or [Result.Failure]
 * 
 * @example
 * ```kotlin
 * val r1 = Result.success("hello")
 * val r2 = Result.success(42)
 * val r3 = Result.success(true)
 * 
 * val zipped = zip(r1, r2, r3)
 * // Result.Success(Triple("hello", 42, true))
 * ```
 */
fun <T1, T2, T3, E> zip(
    r1: Result<T1, E>,
    r2: Result<T2, E>,
    r3: Result<T3, E>
): Result<Triple<T1, T2, T3>, E> {
    if (!r1.isSuccessful()) {
        return Result.failure(r1.getErrorOrThrow())
    }
    if (!r2.isSuccessful()) {
        return Result.failure(r2.getErrorOrThrow())
    }
    if (!r3.isSuccessful()) {
        return Result.failure(r3.getErrorOrThrow())
    }
    return Result.success(
        Triple(
            r1.getValueOrThrow(),
            r2.getValueOrThrow(),
            r3.getValueOrThrow()
        )
    )
}

/**
 * Combines three Results with a transform function.
 * 
 * @param T1 the type of the first success value
 * @param T2 the type of the second success value
 * @param T3 the type of the third success value
 * @param R the type of the transformed result
 * @param E the type of the error
 * @param r1 the first Result
 * @param r2 the second Result
 * @param r3 the third Result
 * @param transform function to combine the three values
 * @return [Result.Success] with transformed value, or [Result.Failure]
 */
inline fun <T1, T2, T3, R, E> zip(
    r1: Result<T1, E>,
    r2: Result<T2, E>,
    r3: Result<T3, E>,
    crossinline transform: (T1, T2, T3) -> R
): Result<R, E> {
    return zip(r1, r2, r3).map { (a, b, c) -> transform(a, b, c) }
}
