package com.adrewdev.functional;

/**
 * Matcher interface for pattern matching on {@link MaybeAsync} instances.
 * <p>
 * This interface provides callbacks for both the Some and None cases,
 * allowing exhaustive pattern matching without explicit if-else checks.
 * </p>
 *
 * @param <T> the type of the value contained in Some
 * @param <R> the type of the result returned by the match
 *
 * @example
 * <pre>{@code
 * MaybeAsync<String> maybeAsync = MaybeAsync.from(fetchUser(id));
 * CompletableFuture<String> result = maybeAsync.match(new MaybeAsyncMatcher<String, String>() {
 *     public String onSome(String value) {
 *         return "Got: " + value;
 *     }
 *
 *     public String onNone() {
 *         return "No value";
 *     }
 * });
 * }</pre>
 *
 * @see MaybeAsync#match(MaybeAsyncMatcher)
 */
public interface MaybeAsyncMatcher<T, R> {

    /**
     * Called when the MaybeAsync resolves to Some, receiving the contained value.
     *
     * @param value the non-null value contained in the Maybe
     * @return the result of the match
     */
    R onSome(T value);

    /**
     * Called when the MaybeAsync resolves to None.
     *
     * @return the result of the match
     */
    R onNone();
}
