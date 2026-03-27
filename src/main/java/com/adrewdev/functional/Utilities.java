package com.adrewdev.functional;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class providing helper functions for functional programming.
 * <p>
 * This class contains static utility methods for type checking, placeholder
 * functions, and common transformations. All methods are stateless and thread-safe.
 * </p>
 * <p>
 * This is a 1:1 port of the TypeScript utilities from
 * typescript-functional-extensions.
 * </p>
 *
 * @author Ported from typescript-functional-extensions
 * @see Maybe
 * @see Result
 */
public final class Utilities {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utilities() {
        // Utility class - should not be instantiated
    }

    // ========================================================================
    // TYPE HELPER FUNCTIONS
    // ========================================================================

    /**
     * Checks if a value is defined (not null).
     *
     * @param <T> the type of the value
     * @param value the value to check
     * @return true if value is not null
     *
     * @example
     * <pre>{@code
     * Utilities.isDefined("hello"); // true
     * Utilities.isDefined(null);    // false
     * }</pre>
     *
     * @see #isSome(Maybe)
     * @see #isSuccessful(Result)
     */
    public static <T> boolean isDefined(T value) {
        return value != null;
    }

    /**
     * Checks if a Maybe contains a value.
     *
     * @param <T> the type of the value
     * @param maybe the Maybe to check
     * @return true if Maybe is Some
     *
     * @example
     * <pre>{@code
     * Utilities.isSome(Maybe.some("test")); // true
     * Utilities.isSome(Maybe.none());       // false
     * Utilities.isSome(null);               // false
     * }</pre>
     *
     * @see #isNone(Maybe)
     * @see Maybe#isSome()
     */
    public static <T> boolean isSome(Maybe<T> maybe) {
        return maybe != null && maybe.isSome();
    }

    /**
     * Checks if a Maybe is empty.
     *
     * @param <T> the type of the value
     * @param maybe the Maybe to check
     * @return true if Maybe is None
     *
     * @example
     * <pre>{@code
     * Utilities.isNone(Maybe.none());       // true
     * Utilities.isNone(Maybe.some("test")); // false
     * Utilities.isNone(null);               // true
     * }</pre>
     *
     * @see #isSome(Maybe)
     * @see Maybe#isNone()
     */
    public static <T> boolean isNone(Maybe<T> maybe) {
        return maybe == null || maybe.isNone();
    }

    /**
     * Checks if a Result is successful.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param result the Result to check
     * @return true if Result is successful
     *
     * @example
     * <pre>{@code
     * Utilities.isSuccessful(Result.success("test")); // true
     * Utilities.isSuccessful(Result.failure("err"));  // false
     * Utilities.isSuccessful(null);                   // false
     * }</pre>
     *
     * @see #isFailure(Result)
     * @see Result#isSuccessful()
     */
    public static <T, E> boolean isSuccessful(Result<T, E> result) {
        return result != null && result.isSuccessful();
    }

    /**
     * Checks if a Result is failed.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param result the Result to check
     * @return true if Result is failed
     *
     * @example
     * <pre>{@code
     * Utilities.isFailure(Result.failure("err"));  // true
     * Utilities.isFailure(Result.success("test")); // false
     * Utilities.isFailure(null);                   // true
     * }</pre>
     *
     * @see #isSuccessful(Result)
     * @see Result#isFailure()
     */
    public static <T, E> boolean isFailure(Result<T, E> result) {
        return result == null || result.isFailure();
    }

    /**
     * Checks if an object is a Function.
     *
     * @param obj the object to check
     * @return true if obj is a Function
     *
     * @example
     * <pre>{@code
     * Utilities.isFunction((Function<String, String>) s -> s); // true
     * Utilities.isFunction("not a function");                   // false
     * }</pre>
     *
     * @see Function
     */
    public static boolean isFunction(Object obj) {
        return obj instanceof Function;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Returns a value that should never be used.
     * <p>
     * Useful for placeholder or impossible cases. This method always throws
     * {@link UnsupportedOperationException} when called.
     * </p>
     *
     * @param <T> the type of the value
     * @return never returns normally
     * @throws UnsupportedOperationException always thrown when called
     *
     * @example
     * <pre>{@code
     * // Useful for impossible cases
     * default:
     *     return Utilities.never();
     * }</pre>
     */
    public static <T> T never() {
        throw new UnsupportedOperationException("never() should never be called");
    }

    /**
     * A no-operation function.
     * <p>
     * Useful as a default or placeholder when a function is required but
     * no action should be taken.
     * </p>
     *
     * @example
     * <pre>{@code
     * Utilities.noop(); // does nothing
     * }</pre>
     *
     * @see #noopConsumer()
     */
    public static void noop() {
        // Does nothing
    }

    /**
     * Returns a Consumer that does nothing.
     *
     * @param <T> the type of the input
     * @return a Consumer that does nothing
     *
     * @example
     * <pre>{@code
     * Consumer<String> noOp = Utilities.noopConsumer();
     * noOp.accept("test"); // does nothing
     * }</pre>
     *
     * @see #noop()
     */
    public static <T> Consumer<T> noopConsumer() {
        return value -> {};
    }

    /**
     * Returns the identity function.
     * <p>
     * The identity function returns its input unchanged. This is useful
     * as a default transformer or in functional composition.
     * </p>
     *
     * @param <T> the type of the input/output
     * @return a Function that returns its input unchanged
     *
     * @example
     * <pre>{@code
     * Function<String, String> identity = Utilities.identity();
     * identity.apply("test"); // "test"
     * }</pre>
     *
     * @see Function
     */
    public static <T> Function<T, T> identity() {
        return value -> value;
    }

    // ========================================================================
    // MAYBE UTILITY METHODS
    // ========================================================================

    /**
     * Converts zero to None, non-zero to Some.
     *
     * @param value the integer to convert
     * @return Maybe.some(value) if non-zero, Maybe.none() if zero
     *
     * @example
     * <pre>{@code
     * Utilities.zeroAsNone(0);   // None{}
     * Utilities.zeroAsNone(42);  // Some{42}
     * }</pre>
     *
     * @see Maybe#some(Object)
     * @see Maybe#none()
     */
    public static Maybe<Integer> zeroAsNone(int value) {
        return value == 0 ? Maybe.none() : Maybe.some(value);
    }

    /**
     * Converts empty string to None.
     *
     * @param value the string to convert
     * @return Maybe.some(value) if non-empty, Maybe.none() if empty or null
     *
     * @example
     * <pre>{@code
     * Utilities.emptyStringAsNone("test");  // Some{test}
     * Utilities.emptyStringAsNone("");      // None{}
     * Utilities.emptyStringAsNone(null);    // None{}
     * }</pre>
     *
     * @see Maybe#some(Object)
     * @see Maybe#none()
     * @see #emptyOrWhiteSpaceStringAsNone(String)
     */
    public static Maybe<String> emptyStringAsNone(String value) {
        return (value == null || value.isEmpty()) ? Maybe.none() : Maybe.some(value);
    }

    /**
     * Converts empty or whitespace string to None.
     *
     * @param value the string to convert
     * @return Maybe.some(value) if not blank, Maybe.none() if blank or null
     *
     * @example
     * <pre>{@code
     * Utilities.emptyOrWhiteSpaceStringAsNone("test");  // Some{test}
     * Utilities.emptyOrWhiteSpaceStringAsNone("   ");   // None{}
     * Utilities.emptyOrWhiteSpaceStringAsNone("");      // None{}
     * Utilities.emptyOrWhiteSpaceStringAsNone(null);    // None{}
     * }</pre>
     *
     * @see Maybe#some(Object)
     * @see Maybe#none()
     * @see #emptyStringAsNone(String)
     */
    public static Maybe<String> emptyOrWhiteSpaceStringAsNone(String value) {
        return (value == null || value.trim().isEmpty()) ? Maybe.none() : Maybe.some(value);
    }
}
