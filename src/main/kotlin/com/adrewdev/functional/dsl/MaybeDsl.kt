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
 * @param value the nullable value to wrap
 * @return [Maybe.Some] if value is not null, [Maybe.None] otherwise
 * 
 * @example
 * ```kotlin
 * val maybe = maybe { getUser(id) }
 * ```
 */
inline fun <T> maybe(crossinline value: () -> T?): Maybe<T> {
    return Maybe.from(value())
}

/**
 * Creates a [Maybe] from a non-nullable value.
 * 
 * @param value the non-nullable value to wrap
 * @return [Maybe.Some] containing the value
 * 
 * @example
 * ```kotlin
 * val some = maybe("hello")
 * ```
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
 * @receiver the nullable value
 * @return [Maybe.Some] if not null, [Maybe.None] otherwise
 */
fun <T> T?.toMaybe(): Maybe<T> {
    return Maybe.from(this)
}

/**
 * Transforms the value inside [Maybe] using the given function.
 * 
 * @param transform the transformation function
 * @return [Maybe.Some] with transformed value, or [Maybe.None]
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
 * @param value the fallback value
 * @return this [Maybe] if [Maybe.Some], or [Maybe.Some] with fallback
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
 * This extension function enables exhaustive when expressions on Maybe values.
 * 
 * @receiver the Maybe to convert
 * @return [MaybeK.Some] if the Maybe contains a value, [MaybeK.None] otherwise
 * 
 * @example
 * ```kotlin
 * val result = when (maybe.toMaybeK()) {
 *     is MaybeK.Some -> maybe.value
 *     MaybeK.None -> "default"
 * }
 * ```
 */
fun <T> Maybe<T>.toMaybeK(): MaybeK<T> {
    return MaybeK.from(this)
}
