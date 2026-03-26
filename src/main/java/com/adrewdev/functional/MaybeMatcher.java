package com.adrewdev.functional;

/**
 * Matcher interface for pattern matching on {@link Maybe} instances.
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
 * Maybe<String> maybe = Maybe.from(input);
 * String result = maybe.match(new MaybeMatcher<String, String>() {
 *     public String onSome(String value) {
 *         return "Got: " + value;
 *     }
 *
 *     public String onNone() {
 *         return "Nothing";
 *     }
 * });
 * }</pre>
 *
 * @see Maybe#match(MaybeMatcher)
 */
public interface MaybeMatcher<T, R> {

    /**
     * Called when the Maybe is Some, receiving the contained value.
     *
     * @param value the non-null value contained in the Maybe
     * @return the result of the match
     */
    R onSome(T value);

    /**
     * Called when the Maybe is None.
     *
     * @return the result of the match
     */
    R onNone();
}
