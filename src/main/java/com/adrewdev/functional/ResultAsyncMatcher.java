package com.adrewdev.functional;

/**
 * Matcher interface for pattern matching on {@link ResultAsync} instances.
 * <p>
 * This interface provides callbacks for both the Success and Failure cases,
 * allowing exhaustive pattern matching without explicit if-else checks.
 * </p>
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error value
 * @param <R> the type of the result returned by the match
 *
 * @example
 * <pre>{@code
 * ResultAsync<String, String> resultAsync = ResultAsync.from(fetchUser(id));
 * CompletableFuture<String> result = resultAsync.match(new ResultAsyncMatcher<String, String, String>() {
 *     public String onSuccess(String value) {
 *         return "Got: " + value;
 *     }
 *
 *     public String onFailure(String error) {
 *         return "Error: " + error;
 *     }
 * });
 * }</pre>
 *
 * @see ResultAsync#match(ResultAsyncMatcher)
 */
public interface ResultAsyncMatcher<T, E, R> {

    /**
     * Called when the ResultAsync resolves to Success, receiving the contained value.
     *
     * @param value the non-null success value
     * @return the result of the match
     */
    R onSuccess(T value);

    /**
     * Called when the ResultAsync resolves to Failure, receiving the contained error.
     *
     * @param error the non-null error value
     * @return the result of the match
     */
    R onFailure(E error);
}
