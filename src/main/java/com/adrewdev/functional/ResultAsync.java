package com.adrewdev.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a future result of an operation that either succeeds or fails.
 *
 * <p>ResultAsync is the asynchronous version of {@link Result}, wrapping a
 * {@link CompletableFuture} that resolves to a Result value. It provides
 * a 1:1 port of the TypeScript ResultAsync type from
 * typescript-functional-extensions.</p>
 *
 * <p>A ResultAsync can be in one of two states:
 * <ul>
 *   <li>{@code Success(value)} - contains a non-null success value (wrapped in CompletableFuture)</li>
 *   <li>{@code Failure(error)} - contains a non-null error value (wrapped in CompletableFuture)</li>
 * </ul>
 * </p>
 *
 * <p>Example:
 * <pre>{@code
 * ResultAsync<String, String> result = ResultAsync.from(fetchUser(id))
 *     .map(User::getEmail)
 *     .recover(error -> "default@email.com");
 *
 * String email = result.toCompletableFuture().join().getValueOrThrow();
 * }</pre>
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error
 * @author Ported from typescript-functional-extensions
 * @see Result
 * @see CompletableFuture
 */
public final class ResultAsync<T, E> {

    /**
     * The underlying CompletableFuture wrapping the Result value.
     */
    private final CompletableFuture<Result<T, E>> future;

    /**
     * Private constructor that wraps the given CompletableFuture.
     *
     * @param future the CompletableFuture containing the Result value (must not be null)
     * @throws NullPointerException if future is null
     */
    private ResultAsync(CompletableFuture<Result<T, E>> future) {
        this.future = Objects.requireNonNull(future, "future cannot be null");
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================

    /**
     * Creates a ResultAsync from a Result.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param result the Result to wrap (must not be null)
     * @return a ResultAsync that resolves to the given Result
     * @throws NullPointerException if result is null
     *
     * @example
     * <pre>{@code
     * Result<String, String> result = Result.success("hello");
     * ResultAsync<String, String> async = ResultAsync.from(result);
     * }</pre>
     *
     * @see #from(CompletableFuture)
     * @see #from(Supplier)
     */
    public static <T, E> ResultAsync<T, E> from(Result<T, E> result) {
        Objects.requireNonNull(result, "result cannot be null");
        return new ResultAsync<>(CompletableFuture.completedFuture(result));
    }

    /**
     * Creates a ResultAsync from a CompletableFuture.
     * <p>
     * The resulting ResultAsync will resolve to Success(value) if the future
     * completes successfully, or Failure(throwable) if the future completes exceptionally.
     * </p>
     *
     * @param <T> the type of the value
     * @param <E> the type of the error (will be Throwable)
     * @param future the CompletableFuture to wrap (must not be null)
     * @return a ResultAsync that wraps the future
     * @throws NullPointerException if future is null
     *
     * @example
     * <pre>{@code
     * CompletableFuture<String> future = fetchUserAsync(id);
     * ResultAsync<String, Throwable> result = ResultAsync.from(future);
     * }</pre>
     *
     * @see #from(CompletableFuture, Function)
     * @see #from(Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T, E> ResultAsync<T, E> from(CompletableFuture<T> future) {
        Objects.requireNonNull(future, "future cannot be null");
        CompletableFuture<Result<T, E>> resultFuture = (CompletableFuture<Result<T, E>>) (CompletableFuture<?>) future
            .thenApply(Result::success)
            .exceptionally(throwable -> Result.failure((E) throwable));
        return new ResultAsync<>(resultFuture);
    }

    /**
     * Creates a ResultAsync from a Supplier of Result.
     * <p>
     * The supplier is executed asynchronously using CompletableFuture.supplyAsync().
     * The resulting ResultAsync will resolve to the Result from the supplier,
     * or Failure(throwable) if the supplier throws an exception.
     * </p>
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param supplier the supplier to execute asynchronously (must not be null)
     * @return a ResultAsync that resolves to the Result from the supplier
     * @throws NullPointerException if supplier is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.from(() -> {
     *     if (condition) {
     *         return Result.success("value");
     *     } else {
     *         return Result.failure("error");
     *     }
     * });
     * }</pre>
     *
     * @see #from(CompletableFuture)
     * @see #from(Result)
     */
    @SuppressWarnings("unchecked")
    public static <T, E> ResultAsync<T, E> from(Supplier<Result<T, E>> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        CompletableFuture<Result<T, E>> resultFuture = CompletableFuture.supplyAsync(supplier)
            .exceptionally(throwable -> Result.failure((E) throwable));
        return new ResultAsync<>(resultFuture);
    }

    /**
     * Creates a ResultAsync from a CompletableFuture with custom error handling.
     * <p>
     * The resulting ResultAsync will resolve to Success(value) if the future
     * completes successfully, or Failure(error) with the error from the handler
     * if the future completes exceptionally.
     * </p>
     *
     * @param <T> the type of the value
     * @param <E> the type of the error
     * @param future the CompletableFuture to wrap (must not be null)
     * @param errorHandler the function to convert exceptions to errors (must not be null)
     * @return a ResultAsync that wraps the future with custom error handling
     * @throws NullPointerException if future or errorHandler is null
     *
     * @example
     * <pre>{@code
     * CompletableFuture<String> future = fetchUserAsync(id);
     * ResultAsync<String, String> result = ResultAsync.from(
     *     future,
     *     error -> "Failed to fetch: " + error.getMessage()
     * );
     * }</pre>
     *
     * @see #from(CompletableFuture)
     */
    @SuppressWarnings("unchecked")
    public static <T, E> ResultAsync<T, E> from(
        CompletableFuture<T> future,
        Function<Throwable, E> errorHandler
    ) {
        Objects.requireNonNull(future, "future cannot be null");
        Objects.requireNonNull(errorHandler, "errorHandler cannot be null");
        CompletableFuture<Result<T, E>> resultFuture = (CompletableFuture<Result<T, E>>) (CompletableFuture<?>) future
            .thenApply(Result::success)
            .exceptionally(throwable -> Result.failure(errorHandler.apply(throwable)));
        return new ResultAsync<>(resultFuture);
    }

    /**
     * Creates a successful ResultAsync.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param value the success value (must not be null)
     * @return a successful ResultAsync containing the value
     * @throws NullPointerException if value is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> success = ResultAsync.success("hello");
     * }</pre>
     *
     * @see #failure(Object)
     * @see #from(Result)
     */
    public static <T, E> ResultAsync<T, E> success(T value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        return from(Result.success(value));
    }

    /**
     * Creates a failed ResultAsync.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param error the error value (must not be null)
     * @return a failed ResultAsync containing the error
     * @throws NullPointerException if error is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> failure = ResultAsync.failure("error occurred");
     * }</pre>
     *
     * @see #success(Object)
     * @see #from(Result)
     */
    public static <T, E> ResultAsync<T, E> failure(E error) {
        if (error == null) {
            throw new NullPointerException("error cannot be null");
        }
        return from(Result.failure(error));
    }

    // ========================================================================
    // INSTANCE METHODS - TRANSFORMATION
    // ========================================================================

    /**
     * Transforms the success value using the given function if present.
     * <p>
     * If this ResultAsync resolves to Success, applies the mapper function to the
     * value and wraps the result in a new ResultAsync. If this resolves to Failure,
     * returns a ResultAsync that resolves to Failure without calling the mapper.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param mapper a function to apply to the value if present (must not be null)
     * @return a ResultAsync containing the result of applying the mapper, or Failure if this is Failure
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> maybe = ResultAsync.success("hello");
     * ResultAsync<Integer, String> result = maybe.map(String::length);
     * // result resolves to Success{5}
     * }</pre>
     *
     * @see #bind(Function)
     * @see #tap(Consumer)
     */
    public <U> ResultAsync<U, E> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> result.map(mapper))
        );
    }

    /**
     * Transforms the error value using the given function if present.
     * <p>
     * If this ResultAsync resolves to Failure, applies the mapper function to the
     * error and wraps the result in a new ResultAsync. If this resolves to Success,
     * returns a ResultAsync that resolves to Success without calling the mapper.
     * </p>
     *
     * @param <E2> the type of the transformed error
     * @param mapper a function to apply to the error if present (must not be null)
     * @return a ResultAsync containing the same success value, or a Failure with the transformed error
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, Integer> result = ResultAsync.failure(500);
     * ResultAsync<String, String> mapped = result.mapError(code -> "Error code: " + code);
     * // mapped resolves to Failure{Error code: 500}
     * }</pre>
     *
     * @see #map(Function)
     */
    public <E2> ResultAsync<T, E2> mapError(Function<E, E2> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> result.mapError(mapper))
        );
    }

    /**
     * Transforms the success value using the given function that returns a ResultAsync.
     * <p>
     * Also known as "flatMap" or "chain". If this ResultAsync resolves to Success,
     * applies the binder function to the value and returns the resulting ResultAsync.
     * If this resolves to Failure, returns a ResultAsync that resolves to Failure without
     * calling the binder.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param binder a function to apply to the value if present, returning a ResultAsync (must not be null)
     * @return a ResultAsync containing the result of applying the binder, or Failure if this is Failure
     * @throws NullPointerException if binder is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> maybe = ResultAsync.success("hello");
     * ResultAsync<Integer, String> result = maybe.bind(s -> ResultAsync.success(s.length()));
     * // result resolves to Success{5}
     * }</pre>
     *
     * @see #map(Function)
     * @see #tap(Consumer)
     */
    @SuppressWarnings("unchecked")
    public <U> ResultAsync<U, E> bind(Function<T, ResultAsync<U, E>> binder) {
        Objects.requireNonNull(binder, "binder cannot be null");
        CompletableFuture<Result<U, E>> composedFuture = future.thenCompose(result ->
            result.isSuccessful()
                ? binder.apply(result.getValueOrThrow()).future
                : (CompletableFuture<Result<U, E>>) (CompletableFuture<?>) CompletableFuture.completedFuture(result)
        );
        return new ResultAsync<>(composedFuture);
    }

    /**
     * Executes the given consumer with the success value if present.
     * <p>
     * Useful for performing side effects on the value without transforming it.
     * Returns a new ResultAsync containing the same value.
     * </p>
     *
     * @param consumer a consumer to execute with the value if present (must not be null)
     * @return a ResultAsync containing the same value
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> maybe = ResultAsync.success("hello");
     * ResultAsync<String, String> result = maybe.tap(System.out::println);
     * // prints "hello", returns ResultAsync containing Success{hello}
     * }</pre>
     *
     * @see #map(Function)
     * @see #bind(Function)
     */
    public ResultAsync<T, E> tap(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> {
                result.tap(consumer);
                return result;
            })
        );
    }

    /**
     * Executes the given consumer with the error value if present.
     * <p>
     * Useful for performing side effects on the error without transforming it.
     * Returns a new ResultAsync containing the same value.
     * </p>
     *
     * @param consumer a consumer to execute with the error if present (must not be null)
     * @return a ResultAsync containing the same value
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.failure("error");
     * ResultAsync<String, String> tapped = result.tapError(System.err::println);
     * // prints "error" to stderr, returns ResultAsync containing Failure{error}
     * }</pre>
     *
     * @see #tap(Consumer)
     */
    public ResultAsync<T, E> tapError(Consumer<E> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> {
                result.tapError(consumer);
                return result;
            })
        );
    }

    /**
     * Returns this ResultAsync if it contains a success value and the predicate matches,
     * otherwise returns a Failure with the given error.
     * <p>
     * If this ResultAsync resolves to Failure, returns the same Failure without
     * evaluating the predicate.
     * </p>
     *
     * @param predicate a predicate to test the value against (must not be null)
     * @param error the error to return if the predicate returns false (must not be null)
     * @return a ResultAsync that is this if predicate matches, Failure(error) otherwise
     * @throws NullPointerException if predicate or error is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<Integer, String> result = ResultAsync.success(5);
     * ResultAsync<Integer, String> ensured = result.ensure(x -> x > 3, "too small");
     * // ensured resolves to Success{5}
     *
     * ResultAsync<Integer, String> result2 = ResultAsync.success(2);
     * ResultAsync<Integer, String> ensured2 = result2.ensure(x -> x > 3, "too small");
     * // ensured2 resolves to Failure{too small}
     * }</pre>
     *
     * @see #where(Predicate, Function)
     */
    public ResultAsync<T, E> ensure(Predicate<T> predicate, E error) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(error, "error cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> result.ensure(predicate, error))
        );
    }

    // ========================================================================
    // INSTANCE METHODS - RECOVERY
    // ========================================================================

    /**
     * Attempts to recover from a failure by applying a recovery function.
     * <p>
     * If this ResultAsync resolves to Success, returns it unchanged.
     * If it resolves to Failure, applies the recovery function to the error
     * and returns Success with the recovered value.
     * </p>
     *
     * @param recoverFn the function to recover from error (must not be null)
     * @return a ResultAsync that is successful with either the original value or recovered value
     * @throws NullPointerException if recoverFn is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.failure("error");
     * ResultAsync<String, String> recovered = result.recover(err -> "recovered from: " + err);
     * // recovered resolves to Success{recovered from: error}
     * }</pre>
     *
     * @see #recoverWith(Function)
     */
    public ResultAsync<T, E> recover(Function<E, T> recoverFn) {
        Objects.requireNonNull(recoverFn, "recoverFn cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> result.recover(recoverFn))
        );
    }

    /**
     * Attempts to recover from a failure with an async operation.
     * <p>
     * If this ResultAsync resolves to Success, returns it unchanged.
     * If it resolves to Failure, applies the recovery function to the error
     * and returns the resulting ResultAsync.
     * </p>
     *
     * @param recoverFn the function to recover from error, returning a ResultAsync (must not be null)
     * @return a ResultAsync that is successful with either the original value or recovered value
     * @throws NullPointerException if recoverFn is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.failure("error");
     * ResultAsync<String, String> recovered = result.recoverWith(err ->
     *     ResultAsync.success("recovered from: " + err)
     * );
     * // recovered resolves to Success{recovered from: error}
     * }</pre>
     *
     * @see #recover(Function)
     */
    public ResultAsync<T, E> recoverWith(Function<E, ResultAsync<T, E>> recoverFn) {
        Objects.requireNonNull(recoverFn, "recoverFn cannot be null");
        return new ResultAsync<>(
            future.thenCompose(result ->
                result.isFailure()
                    ? recoverFn.apply(result.getErrorOrThrow()).future
                    : CompletableFuture.completedFuture(result)
            )
        );
    }

    // ========================================================================
    // INSTANCE METHODS - PATTERN MATCHING
    // ========================================================================

    /**
     * Performs pattern matching on this ResultAsync.
     * <p>
     * Calls {@code matcher.onSuccess(value)} if this resolves to Success, or
     * {@code matcher.onFailure(error)} if this resolves to Failure.
     * </p>
     *
     * @param <R> the type of the result
     * @param matcher a matcher providing callbacks for Success and Failure cases (must not be null)
     * @return a CompletableFuture containing the result of calling the appropriate callback
     * @throws NullPointerException if matcher is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> resultAsync = ResultAsync.success("hello");
     * CompletableFuture<String> result = resultAsync.match(new ResultAsyncMatcher<String, String, String>() {
     *     public String onSuccess(String value) { return "Got: " + value; }
     *     public String onFailure(String error) { return "Error: " + error; }
     * });
     * // result completes with "Got: hello"
     * }</pre>
     *
     * @see ResultAsyncMatcher
     * @see #match(Consumer, Consumer)
     */
    public <R> CompletableFuture<R> match(ResultAsyncMatcher<T, E, R> matcher) {
        Objects.requireNonNull(matcher, "matcher cannot be null");
        return future.thenApply(result -> result.match(
            matcher::onSuccess,
            matcher::onFailure
        ));
    }

    // ========================================================================
    // INSTANCE METHODS - VALUE ACCESS
    // ========================================================================

    /**
     * Gets the success value if present, throws NoSuchElementException otherwise.
     * <p>
     * The returned CompletableFuture will complete with the value if this
     * ResultAsync resolves to Success, or complete exceptionally with
     * NoSuchElementException if it resolves to Failure.
     * </p>
     *
     * @return a CompletableFuture containing the success value
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * String value = result.getValueOrThrow().join(); // "hello"
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * failure.getValueOrThrow().join(); // throws CompletionException with NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrDefault(Object)
     * @see #getErrorOrThrow()
     */
    public CompletableFuture<T> getValueOrThrow() {
        return future.thenApply(result -> result.getValueOrThrow());
    }

    /**
     * Gets the error value if present, throws NoSuchElementException otherwise.
     * <p>
     * The returned CompletableFuture will complete with the error if this
     * ResultAsync resolves to Failure, or complete exceptionally with
     * NoSuchElementException if it resolves to Success.
     * </p>
     *
     * @return a CompletableFuture containing the error value
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * String error = failure.getErrorOrThrow().join(); // "error"
     *
     * ResultAsync<String, String> success = ResultAsync.success("hello");
     * success.getErrorOrThrow().join(); // throws CompletionException with NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrThrow()
     */
    public CompletableFuture<E> getErrorOrThrow() {
        return future.thenApply(result -> result.getErrorOrThrow());
    }

    // ========================================================================
    // INSTANCE METHODS - CONVERSION
    // ========================================================================

    /**
     * Returns the underlying CompletableFuture.
     *
     * @return the CompletableFuture wrapping a Result
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * CompletableFuture<Result<String, String>> future = result.toCompletableFuture();
     * Result<String, String> value = future.join(); // Success{hello}
     * }</pre>
     *
     * @see #toResult()
     */
    public CompletableFuture<Result<T, E>> toCompletableFuture() {
        return future;
    }

    /**
     * Converts to Result (blocking).
     * <p>
     * This method blocks until the CompletableFuture completes.
     * Use {@link #toCompletableFuture()} for non-blocking access.
     * </p>
     *
     * @return the Result value (blocks until complete)
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * Result<String, String> value = result.toResult(); // Success{hello}
     * }</pre>
     *
     * @see #toCompletableFuture()
     */
    public Result<T, E> toResult() {
        return future.join();
    }

    /**
     * Converts to Maybe (blocking).
     * <p>
     * If this ResultAsync resolves to Success, returns a Maybe containing the value.
     * If it resolves to Failure, returns Maybe.none().
     * This method blocks until the CompletableFuture completes.
     * </p>
     *
     * @return a Maybe containing the success value if present, or none if failure
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> success = ResultAsync.success("hello");
     * Maybe<String> maybe = success.toMaybe(); // Some{hello}
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * Maybe<String> maybe2 = failure.toMaybe(); // None
     * }</pre>
     *
     * @see Maybe#some(Object)
     * @see Maybe#none()
     */
    public Maybe<T> toMaybe() {
        Result<T, E> result = future.join();
        return result.isSuccessful()
            ? Maybe.from(result.getValueOrThrow())
            : Maybe.none();
    }

    /**
     * Returns this ResultAsync if it contains a success value, otherwise returns a
     * ResultAsync containing the given value.
     *
     * @param value the fallback value to return if this is Failure
     * @return a ResultAsync containing the original value if Success, or the fallback value if Failure
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * ResultAsync<String, String> orResult = result.or("default");
     * // orResult resolves to Success{hello}
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * ResultAsync<String, String> orResult2 = failure.or("default");
     * // orResult2 resolves to Success{default}
     * }</pre>
     *
     * @see #or(Supplier)
     * @see #orElse(Result)
     */
    public ResultAsync<T, E> or(T value) {
        return new ResultAsync<>(
            future.thenApply(result -> result.or(value))
        );
    }

    /**
     * Returns this ResultAsync if it contains a success value, otherwise returns the
     * given Result.
     *
     * @param other the Result to return if this is Failure (must not be null)
     * @return a ResultAsync containing the original value if Success, or the fallback Result if Failure
     * @throws NullPointerException if other is null
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * ResultAsync<String, String> orResult = result.orElse(Result.success("world"));
     * // orResult resolves to Success{hello}
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * ResultAsync<String, String> orResult2 = failure.orElse(Result.success("world"));
     * // orResult2 resolves to Success{world}
     * }</pre>
     *
     * @see #or(Object)
     */
    public ResultAsync<T, E> orElse(Result<T, E> other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new ResultAsync<>(
            future.thenApply(result -> result.orElse(other))
        );
    }

    /**
     * Gets the success value if present, otherwise returns the given default value.
     * <p>
     * The returned CompletableFuture will complete with the success value if this
     * ResultAsync resolves to Success, or complete with the default value if it
     * resolves to Failure.
     * </p>
     *
     * @param defaultValue the value to return if this is Failure
     * @return a CompletableFuture containing the success value or default
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * String value = result.getValueOrDefault("default").join(); // "hello"
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * String value2 = failure.getValueOrDefault("default").join(); // "default"
     * }</pre>
     *
     * @see #getValueOrThrow()
     */
    public CompletableFuture<T> getValueOrDefault(T defaultValue) {
        return future.thenApply(result -> result.getValueOrDefault(defaultValue));
    }

    // ========================================================================
    // OBJECT METHODS
    // ========================================================================

    /**
     * Compares this ResultAsync with another object for equality.
     * <p>
     * Two ResultAsync instances are equal if their underlying Futures
     * complete with equal Result values.
     * </p>
     *
     * @param obj the object to compare with
     * @return true if the ResultAsync instances are equal, false otherwise
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result1 = ResultAsync.success("hello");
     * ResultAsync<String, String> result2 = ResultAsync.success("hello");
     * // To compare async values, you need to wait for completion:
     * boolean equal = result1.toCompletableFuture().join()
     *                     .equals(result2.toCompletableFuture().join());
     * }</pre>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResultAsync)) {
            return false;
        }
        ResultAsync<?, ?> other = (ResultAsync<?, ?>) obj;
        // Compare the underlying futures by blocking on them
        return this.future.join().equals(other.future.join());
    }

    /**
     * Returns a hash code for this ResultAsync.
     * <p>
     * This method blocks until the CompletableFuture completes.
     * </p>
     *
     * @return the hash code of the underlying Result value
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * int hash = result.hashCode(); // blocks and returns hash of Success{hello}
     * }</pre>
     */
    @Override
    public int hashCode() {
        return future.join().hashCode();
    }

    /**
     * Returns a string representation of this ResultAsync.
     * <p>
     * This method blocks until the CompletableFuture completes.
     * </p>
     *
     * @return "ResultAsync{Success{value}}" if this is Success, "ResultAsync{Failure{error}}" if this is Failure
     *
     * @example
     * <pre>{@code
     * ResultAsync<String, String> result = ResultAsync.success("hello");
     * System.out.println(result.toString()); // "ResultAsync{Success{hello}}"
     *
     * ResultAsync<String, String> failure = ResultAsync.failure("error");
     * System.out.println(failure.toString()); // "ResultAsync{Failure{error}}"
     * }</pre>
     */
    @Override
    public String toString() {
        return "ResultAsync{" + future.join() + "}";
    }
}
