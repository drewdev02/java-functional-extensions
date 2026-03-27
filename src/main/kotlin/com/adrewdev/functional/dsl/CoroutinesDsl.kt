package com.adrewdev.functional.dsl

import com.adrewdev.functional.Maybe
import com.adrewdev.functional.MaybeAsync
import com.adrewdev.functional.Result
import com.adrewdev.functional.ResultAsync
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture

/**
 * Kotlin Coroutines DSL for async operations.
 * 
 * Provides builders and extensions for [MaybeAsync] and [ResultAsync]
 * with native coroutines support.
 * 
 * @since 1.1.0
 */
object CoroutinesDsl

// ============================================================================
// MaybeAsync Builder Functions
// ============================================================================

/**
 * Creates a [MaybeAsync] from a suspending operation.
 *
 * This builder function wraps a suspending operation in a MaybeAsync,
 * automatically handling nullable return values.
 *
 * @param block the suspending operation to execute
 * @return [MaybeAsync] containing the result
 *
 * @example
 * ```kotlin
 * // Basic usage
 * val maybe = maybeAsync { fetchUser(id) }
 *
 * // With pattern matching
 * runBlocking {
 *     when (maybe.await().toMaybeK()) {
 *         is MaybeK.Some -> println(maybe.value)
 *         MaybeK.None -> println("No user")
 *     }
 * }
 *
 * // Chaining operations
 * val email = maybeAsync { fetchUser(id) }
 *     .map { it.email }
 *     .toCompletableFuture()
 *     .join()
 * ```
 *
 * @see maybeAsync the CompletableFuture overload
 */
fun <T> maybeAsync(block: suspend () -> T?): MaybeAsync<T> {
    val future: CompletableFuture<T> = CompletableFuture.supplyAsync {
        kotlinx.coroutines.runBlocking { block() }
    }
    return MaybeAsync.from(future)
}

/**
 * Creates a [MaybeAsync] from an existing [CompletableFuture].
 * 
 * @param future the future to wrap
 * @return [MaybeAsync] wrapping the future
 */
fun <T> maybeAsync(future: CompletableFuture<T>): MaybeAsync<T> {
    return MaybeAsync.from(future)
}

// ============================================================================
// MaybeAsync Extension Functions
// ============================================================================

/**
 * Awaits the completion of [MaybeAsync] and returns [Maybe].
 *
 * This suspending function waits for the async operation to complete
 * and returns the result as a Maybe.
 *
 * @receiver the async Maybe to await
 * @return [Maybe] with the result
 *
 * @example
 * ```kotlin
 * // Basic await
 * runBlocking {
 *     val maybe = maybeAsync { fetchUser(id) }.await()
 *     maybe.match(
 *         some = { println("User: $it") },
 *         none = { println("No user") }
 *     )
 * }
 *
 * // With pattern matching
 * runBlocking {
 *     when (maybeAsync { fetchUser(id) }.await().toMaybeK()) {
 *         is MaybeK.Some -> println(maybe.value)
 *         MaybeK.None -> println("Not found")
 *     }
 * }
 * ```
 *
 * @see awaitGetValue for direct value extraction
 */
suspend fun <T> MaybeAsync<T>.await(): Maybe<T> {
    return toCompletableFuture().await()
}

/**
 * Awaits the completion and extracts the value.
 * 
 * @receiver the async Maybe to await
 * @return the value contained in the Maybe
 * @throws NoSuchElementException if Maybe is None
 */
suspend fun <T> MaybeAsync<T>.awaitGetValue(): T {
    return await().getValueOrThrow()
}

// ============================================================================
// ResultAsync Builder Functions
// ============================================================================

/**
 * Creates a [ResultAsync] from a suspending operation that may throw.
 *
 * This builder function wraps a suspending operation in a ResultAsync,
 * automatically catching exceptions and converting them to failures.
 *
 * @param errorHandler function to convert Throwable to error type E
 * @param block the suspending operation to execute
 * @return [ResultAsync] containing the result
 *
 * @example
 * ```kotlin
 * // With custom error handler
 * val result = resultAsync<String, Error>({ e ->
 *     when (e) {
 *         is IOException -> Error.NetworkError(e.message ?: "")
 *         else -> Error.Unknown(e.message ?: "")
 *     }
 * }) {
 *     fetchEmail(id)  // Suspending network call
 * }
 *
 * // Railway pattern with async
 * suspend fun getUserEmail(id: Int): Result<String, Error> = resultAsync {
 *     val user = fetchUser(id).bind()
 *     ensure(user.active) { Error.Inactive(user.id) }
 *     user.email
 * }.await()
 *
 * // Awaiting result
 * runBlocking {
 *     val email = resultAsync { fetchEmail(id) }.awaitGetValue()
 *     println(email)
 * }
 * ```
 *
 * @see resultAsync the overload with default String error handler
 */
fun <T, E> resultAsync(
    errorHandler: (Throwable) -> E,
    block: suspend () -> T
): ResultAsync<T, E> {
    val supplier: () -> com.adrewdev.functional.Result<T, E> = {
        try {
            val value = kotlinx.coroutines.runBlocking { block() }
            com.adrewdev.functional.Result.success(value)
        } catch (e: Throwable) {
            com.adrewdev.functional.Result.failure(errorHandler(e))
        }
    }
    return ResultAsync.from(supplier)
}

/**
 * Creates a [ResultAsync] from a suspending operation with default error handler.
 * 
 * @param block the suspending operation to execute
 * @return [ResultAsync] containing the result with String error
 */
fun <T> resultAsync(block: suspend () -> T): ResultAsync<T, String> {
    return resultAsync(
        errorHandler = { it.message ?: "Unknown error" },
        block = block
    )
}

// ============================================================================
// ResultAsync Extension Functions
// ============================================================================

/**
 * Awaits the completion of [ResultAsync] and returns [Result].
 * 
 * @receiver the async Result to await
 * @return [Result] with the success value or error
 */
suspend fun <T, E> ResultAsync<T, E>.await(): Result<T, E> {
    return toCompletableFuture().await()
}

/**
 * Awaits and extracts the success value.
 * 
 * @receiver the async Result to await
 * @return the success value
 * @throws NoSuchElementException if Result is failure
 */
suspend fun <T, E> ResultAsync<T, E>.awaitGetValue(): T {
    return await().getValueOrThrow()
}

/**
 * Awaits and extracts the error value.
 * 
 * @receiver the async Result to await
 * @return the error value
 * @throws NoSuchElementException if Result is success
 */
suspend fun <T, E> ResultAsync<T, E>.awaitGetError(): E {
    return await().getErrorOrThrow()
}

// ============================================================================
// Railway Pattern with Coroutines
// ============================================================================

/**
 * Chains async Result-returning suspending functions.
 * 
 * @param transform suspending function that returns ResultAsync
 * @return ResultAsync with chained result
 */
fun <T, U, E> ResultAsync<T, E>.bindAsync(
    transform: suspend (T) -> ResultAsync<U, E>
): ResultAsync<U, E> {
    return ResultAsync.from {
        val result = this@bindAsync.toCompletableFuture().join()
        if (result.isSuccessful()) {
            val value = result.getValueOrThrow()
            kotlinx.coroutines.runBlocking { transform(value) }.toCompletableFuture().join()
        } else {
            @Suppress("UNCHECKED_CAST")
            result as com.adrewdev.functional.Result<U, E>
        }
    }
}

// ============================================================================
// CompletableFuture Extensions
// ============================================================================

/**
 * Awaits the completion of a [CompletableFuture] and returns the result.
 * 
 * @receiver the CompletableFuture to await
 * @return the completed value
 */
suspend fun <T> CompletableFuture<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel(false)
        }
        whenComplete { value, exception ->
            if (exception != null) {
                continuation.resumeWith(kotlin.Result.failure(exception))
            } else {
                continuation.resumeWith(kotlin.Result.success(value))
            }
        }
    }
}

/**
 * Awaits the completion of a [CompletableFuture] and wraps the result in [Maybe].
 * 
 * @receiver the CompletableFuture to await
 * @return [Maybe] containing the result
 */
suspend fun <T> CompletableFuture<T>.awaitMaybe(): Maybe<T> {
    return Maybe.from(await())
}

/**
 * Awaits the completion of a [CompletableFuture] and wraps the result in [Result].
 * 
 * @receiver the CompletableFuture to await
 * @param errorHandler function to convert Throwable to error type E
 * @return [Result] containing the success value or error
 */
suspend fun <T, E> CompletableFuture<T>.awaitResult(
    errorHandler: (Throwable) -> E
): com.adrewdev.functional.Result<T, E> {
    return try {
        com.adrewdev.functional.Result.success(await())
    } catch (e: Throwable) {
        com.adrewdev.functional.Result.failure(errorHandler(e))
    }
}

/**
 * Awaits the completion of a [CompletableFuture] and wraps the result in [Result].
 * Uses default error handler that converts Throwable to String.
 * 
 * @receiver the CompletableFuture to await
 * @return [Result] containing the success value or String error
 */
suspend fun <T> CompletableFuture<T>.awaitResult(): com.adrewdev.functional.Result<T, String> {
    return awaitResult { it.message ?: "Unknown error" }
}
