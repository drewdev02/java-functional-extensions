package com.adrewdev.functional.dsl

import com.adrewdev.functional.Maybe

/**
 * Kotlin DSL for Maybe monad.
 * 
 * Provides idiomatic Kotlin extensions and builders for [Maybe].
 * 
 * @since 1.1.0
 */
object MaybeDsl {
    // Builder functions are defined as top-level functions for better ergonomics
}

// ============================================================================
// Builder Functions
// ============================================================================

/**
 * Creates a [Maybe] from a nullable value.
 *
 * This is the primary builder function for creating Maybe instances.
 * It safely wraps nullable values, converting null to [Maybe.None].
 *
 * @param value the nullable value to wrap
 * @return [Maybe.Some] if value is not null, [Maybe.None] otherwise
 *
 * @example
 * ```kotlin
 * // With lambda - exceptions are caught
 * val maybe = maybe { getUser(id) }
 *
 * // With direct value
 * val some = maybe("hello")
 *
 * // With nullable expression
 * val maybeUser = maybe { findUserByEmail(email) }
 * ```
 *
 * @see maybe for the non-nullable overload
 * @see T?.toMaybe for the extension function
 */
inline fun <T> maybe(crossinline value: () -> T?): Maybe<T> {
    return Maybe.from(value())
}

/**
 * Creates a [Maybe] from a non-nullable value.
 *
 * This overload is used when you have a non-nullable value that you want
 * to wrap in a Maybe for API consistency.
 *
 * @param value the non-nullable value to wrap
 * @return [Maybe.Some] containing the value
 *
 * @example
 * ```kotlin
 * // Direct value
 * val some = maybe("hello")
 *
 * // With computed value
 * val result = maybe { computeValue() }
 *
 * // Chaining
 * val email = maybe(user.email)
 *     .map { it.lowercase() }
 *     .getValueOrThrow()
 * ```
 *
 * @see maybe for the nullable overload
 */
fun <T> maybe(value: T): Maybe<T> {
    return Maybe.some(value)
}

// ============================================================================
// Extension Functions
// ============================================================================

/**
 * Converts a nullable type to [Maybe].
 *
 * This extension function provides a convenient way to convert any
 * nullable value to a Maybe instance.
 *
 * @receiver the nullable value
 * @return [Maybe.Some] if not null, [Maybe.None] otherwise
 *
 * @example
 * ```kotlin
 * // Convert nullable to Maybe
 * val maybe = nullableValue.toMaybe()
 *
 * // Chain operations
 * val email = getUser(id)?.email.toMaybe()
 *     .map { it.lowercase() }
 *     .or("default@email.com")
 *
 * // Pattern matching
 * when (value.toMaybe().toMaybeK()) {
 *     is MaybeK.Some -> println(value)
 *     MaybeK.None -> println("No value")
 * }
 * ```
 */
fun <T> T?.toMaybe(): Maybe<T> {
    return Maybe.from(this)
}

/**
 * Transforms the value inside [Maybe] using the given function.
 *
 * If the Maybe is [Maybe.Some], applies the transformation function
 * to the value and wraps the result in [Maybe.Some]. If the Maybe is
 * [Maybe.None], returns [Maybe.None] without calling the function.
 *
 * @param transform the transformation function
 * @return [Maybe.Some] with transformed value, or [Maybe.None]
 *
 * @example
 * ```kotlin
 * // Transform value
 * val upper = maybe("hello").map { it.uppercase() }
 *
 * // Chain transformations
 * val result = maybe { getUser(id) }
 *     .map { it.email }
 *     .map { it.lowercase() }
 *     .getValueOrThrow()
 *
 * // None propagates
 * val noneResult = Maybe.none<String>()
 *     .map { it.length }  // Never called
 *     // Still None
 * ```
 */
inline fun <T, R> Maybe<T>.map(crossinline transform: (T) -> R): Maybe<R> {
    return this.map { transform(it) }
}

/**
 * Chains Maybe-returning functions.
 * 
 * @param transform the function that returns Maybe
 * @return the result of applying the function, or [Maybe.None]
 */
inline fun <T, R> Maybe<T>.bind(crossinline transform: (T) -> Maybe<R>): Maybe<R> {
    return this.bind { transform(it) }
}

/**
 * Performs a side effect on the value.
 * 
 * @param action the side effect to perform
 * @return this [Maybe] unchanged
 */
inline fun <T> Maybe<T>.tap(crossinline action: (T) -> Unit): Maybe<T> {
    return this.tap { action(it) }
}

/**
 * Provides a fallback value if [Maybe] is [Maybe.None].
 *
 * If the Maybe is [Maybe.Some], returns it unchanged.
 * If the Maybe is [Maybe.None], returns [Maybe.Some] with the fallback value.
 *
 * @param value the fallback value
 * @return this [Maybe] if [Maybe.Some], or [Maybe.Some] with fallback
 *
 * @example
 * ```kotlin
 * // With direct value
 * val email = maybe { getUser(id) }
 *     .map { it.email }
 *     .or("default@email.com")
 *     .getValueOrThrow()
 *
 * // With lambda for lazy evaluation
 * val email = maybe { getUser(id) }
 *     .map { it.email }
 *     .or { computeDefaultEmail() }
 *     .getValueOrThrow()
 * ```
 *
 * @see or the lambda overload for lazy evaluation
 */
fun <T> Maybe<T>.or(value: T): Maybe<T> {
    return this.or(value)
}

/**
 * Provides a lazy fallback value if [Maybe] is [Maybe.None].
 * 
 * @param valueSupplier the supplier of fallback value
 * @return this [Maybe] if [Maybe.Some], or [Maybe.Some] with fallback
 */
inline fun <T> Maybe<T>.or(crossinline valueSupplier: () -> T): Maybe<T> {
    return this.or { valueSupplier() }
}

// ============================================================================
// Pattern Matching Support
// ============================================================================

/**
 * Kotlin wrapper for Maybe that enables when expressions.
 * 
 * This sealed class provides exhaustive pattern matching for Maybe values.
 * 
 * @param T the type of the contained value
 */
sealed class MaybeK<out T> {
    /**
     * Represents a Some case containing a value.
     * 
     * @param T the type of the contained value
     * @property value the value contained in Some
     */
    data class Some<out T>(val value: T) : MaybeK<T>()
    
    /**
     * Represents a None case (absence of value).
     */
    object None : MaybeK<Nothing>()
    
    companion object {
        /**
         * Converts a Java [Maybe] to [MaybeK] for pattern matching.
         * 
         * @param maybe the Maybe to convert
         * @return [MaybeK.Some] if the Maybe contains a value, [MaybeK.None] otherwise
         */
        fun <T> from(maybe: Maybe<T>): MaybeK<T> {
            return if (maybe.isSome()) {
                Some(maybe.getValueOrThrow())
            } else {
                None
            }
        }
    }
}

/**
 * Converts [Maybe] to [MaybeK] for pattern matching.
 *
 * This extension function wraps a Java [Maybe] in a Kotlin sealed class,
 * enabling exhaustive pattern matching with when expressions.
 *
 * @receiver the Maybe to convert
 * @return [MaybeK.Some] if the Maybe contains a value, [MaybeK.None] otherwise
 *
 * @example
 * ```kotlin
 * // Basic pattern matching
 * when (maybe.toMaybeK()) {
 *     is MaybeK.Some -> println("Value: ${maybe.value}")
 *     MaybeK.None -> println("No value")
 * }
 *
 * // With transformation
 * val result = when (getUser(id).toMaybeK()) {
 *     is MaybeK.Some -> maybe.value.email
 *     MaybeK.None -> "default@email.com"
 * }
 *
 * // In when expression
 * fun process(maybe: Maybe<String>): String = when (maybe.toMaybeK()) {
 *     is MaybeK.Some -> "Got: ${maybe.value}"
 *     MaybeK.None -> "Nothing"
 * }
 * ```
 *
 * @see MaybeK the sealed class for pattern matching
 */
fun <T> Maybe<T>.toMaybeK(): MaybeK<T> {
    return MaybeK.from(this)
}
