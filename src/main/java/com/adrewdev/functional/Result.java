package com.adrewdev.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a successful or failed operation.
 *
 * <p>Result is used to handle errors in a functional way, avoiding try/catch
 * and allowing for composable error handling. It implements the Railway Pattern
 * where operations can be chained and errors propagate automatically.</p>
 *
 * <p>A Result is either:
 * <ul>
 *   <li>{@code Success(value)} - contains a non-null success value</li>
 *   <li>{@code Failure(error)} - contains a non-null error value</li>
 * </ul>
 * </p>
 *
 * <p>Example:
 * <pre>{@code
 * Result<User, String> result = Result.try_(
 *     () -> getUser(id),
 *     error -> "Failed to get user: " + error
 * )
 * .ensure(user -> user.isActive(), "User is not active")
 * .bind(user -> Result.success(user.getEmail()));
 *
 * result.match(
 *     email -> System.out.println("Email: " + email),
 *     error -> System.out.println("Error: " + error)
 * );
 * }</pre>
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error
 * @author Ported from typescript-functional-extensions
 */
public final class Result<T, E> {

    /**
     * The Optional containing the success value if present.
     */
    private final Optional<T> successValue;

    /**
     * The Optional containing the error value if present.
     */
    private final Optional<E> errorValue;

    /**
     * Flag indicating whether this Result represents success.
     */
    private final boolean isSuccess;

    /**
     * Private constructor that wraps the given Optional values.
     *
     * @param successValue the Optional containing the success value (must not be null)
     * @param errorValue the Optional containing the error value (must not be null)
     * @param isSuccess true if this represents success, false if failure
     * @throws NullPointerException if successValue or errorValue is null
     */
    private Result(Optional<T> successValue, Optional<E> errorValue, boolean isSuccess) {
        this.successValue = Objects.requireNonNull(successValue, "successValue cannot be null");
        this.errorValue = Objects.requireNonNull(errorValue, "errorValue cannot be null");
        this.isSuccess = isSuccess;
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================

    /**
     * Creates a successful Result with the given value.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param value the success value (must not be null)
     * @return a successful Result containing the value
     * @throws NullPointerException if value is null
     *
     * @example
     * <pre>{@code
     * Result<String, String> success = Result.success("hello");
     * success.isSuccessful();  // true
     * success.isFailure();     // false
     * success.getValueOrThrow(); // "hello"
     * }</pre>
     *
     * @see #failure(Object)
     * @see #try_(Supplier, Function)
     */
    public static <T, E> Result<T, E> success(T value) {
        if (value == null) {
            throw new NullPointerException("success value cannot be null");
        }
        return new Result<>(Optional.of(value), Optional.empty(), true);
    }

    /**
     * Creates a failed Result with the given error.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param error the error value (must not be null)
     * @return a failed Result containing the error
     * @throws NullPointerException if error is null
     *
     * @example
     * <pre>{@code
     * Result<String, String> failure = Result.failure("error occurred");
     * failure.isSuccessful();  // false
     * failure.isFailure();     // true
     * failure.getErrorOrThrow(); // "error occurred"
     * }</pre>
     *
     * @see #success(Object)
     * @see #try_(Supplier, Function)
     */
    public static <T, E> Result<T, E> failure(E error) {
        if (error == null) {
            throw new NullPointerException("error cannot be null");
        }
        return new Result<>(Optional.empty(), Optional.of(error), false);
    }

    /**
     * Executes a supplier that may throw an exception, converting it to a Result.
     *
     * <p>If the supplier succeeds, returns a successful Result.
     * If it throws, returns a failed Result with the error from the handler.</p>
     *
     * <p>Example:
     * <pre>{@code
     * Result<Integer, String> result = Result.try_(
     *     () -> Integer.parseInt("42"),
     *     error -> "Parse failed: " + error.getMessage()
     * );
     * result.match(
     *     value -> System.out.println("Parsed: " + value),
     *     error -> System.out.println("Error: " + error)
     * );
     * }</pre>
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param supplier the supplier to execute (must not be null)
     * @param errorHandler the function to convert exceptions to errors (must not be null)
     * @return a Result containing the success value or error
     * @throws NullPointerException if supplier or errorHandler is null
     *
     * @see #success(Object)
     * @see #failure(Object)
     * @see #of(Supplier)
     */
    public static <T, E> Result<T, E> try_(Supplier<T> supplier, Function<Throwable, E> errorHandler) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(errorHandler, "errorHandler cannot be null");
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(errorHandler.apply(e));
        }
    }

    /**
     * Wraps a Result-returning supplier, catching any exceptions.
     *
     * <p>If the supplier throws an exception, returns a failed Result with the
     * exception as the error.</p>
     *
     * <p>Example:
     * <pre>{@code
     * Result<Integer, String> result = Result.of(() -> {
     *     if (someCondition) {
     *         return Result.success(42);
     *     } else {
     *         return Result.failure("condition not met");
     *     }
     * });
     * }</pre>
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error
     * @param supplier the supplier to execute (must not be null)
     * @return the Result from the supplier, or a failure if an exception occurred
     * @throws NullPointerException if supplier is null
     *
     * @see #try_(Supplier, Function)
     */
    public static <T, E> Result<T, E> of(Supplier<Result<T, E>> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return supplier.get();
        } catch (Throwable e) {
            return failure((E) e);
        }
    }

    // ========================================================================
    // PROPERTIES
    // ========================================================================

    /**
     * Returns true if this Result represents a successful operation.
     *
     * @return true if this is Success, false if this is Failure
     *
     * @example
     * <pre>{@code
     * Result.success("hello").isSuccessful(); // true
     * Result.failure("error").isSuccessful(); // false
     * }</pre>
     *
     * @see #isFailure()
     */
    public boolean isSuccessful() {
        return isSuccess;
    }

    /**
     * Returns true if this Result represents a failed operation.
     *
     * @return true if this is Failure, false if this is Success
     *
     * @example
     * <pre>{@code
     * Result.success("hello").isFailure(); // false
     * Result.failure("error").isFailure(); // true
     * }</pre>
     *
     * @see #isSuccessful()
     */
    public boolean isFailure() {
        return !isSuccess;
    }

    // ========================================================================
    // VALUE ACCESS METHODS
    // ========================================================================

    /**
     * Returns the contained success value if present, throws NoSuchElementException otherwise.
     *
     * @return the contained success value
     * @throws NoSuchElementException if this Result is a failure
     *
     * @example
     * <pre>{@code
     * Result.success("hello").getValueOrThrow(); // "hello"
     * Result.failure("error").getValueOrThrow(); // throws NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrDefault(Object)
     * @see #getErrorOrThrow()
     */
    public T getValueOrThrow() {
        if (!isSuccess) {
            throw new NoSuchElementException("Result is a failure, no success value present");
        }
        return successValue.get();
    }

    /**
     * Returns the contained error value if present, throws NoSuchElementException otherwise.
     *
     * @return the contained error value
     * @throws NoSuchElementException if this Result is successful
     *
     * @example
     * <pre>{@code
     * Result.failure("error").getErrorOrThrow(); // "error"
     * Result.success("hello").getErrorOrThrow(); // throws NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrThrow()
     */
    public E getErrorOrThrow() {
        if (isSuccess) {
            throw new NoSuchElementException("Result is successful, no error present");
        }
        return errorValue.get();
    }

    // ========================================================================
    // PATTERN MATCHING
    // ========================================================================

    /**
     * Performs pattern matching on this Result.
     * <p>
     * Calls {@code onSuccess(value)} if this is Success, or {@code onFailure(error)} if this is Failure.
     * </p>
     *
     * @param <R> the type of the result
     * @param onSuccess the function to execute if this is Success (must not be null)
     * @param onFailure the function to execute if this is Failure (must not be null)
     * @return the result of calling the appropriate callback
     * @throws NullPointerException if onSuccess or onFailure is null
     *
     * @example
     * <pre>{@code
     * Result<Integer, String> result = Result.success(42);
     * String output = result.match(
     *     value -> "Got value: " + value,
     *     error -> "Got error: " + error
     * ); // "Got value: 42"
     * }</pre>
     *
     * @see #match(Consumer, Consumer)
     */
    public <R> R match(Function<T, R> onSuccess, Function<E, R> onFailure) {
        Objects.requireNonNull(onSuccess, "onSuccess cannot be null");
        Objects.requireNonNull(onFailure, "onFailure cannot be null");
        if (isSuccess) {
            return onSuccess.apply(successValue.get());
        } else {
            return onFailure.apply(errorValue.get());
        }
    }

    /**
     * Performs pattern matching on this Result with void callbacks.
     * <p>
     * Calls {@code onSuccess.accept(value)} if this is Success, or {@code onFailure.accept(error)} if this is Failure.
     * </p>
     *
     * @param onSuccess the consumer to execute with the value if this is Success (must not be null)
     * @param onFailure the consumer to execute with the error if this is Failure (must not be null)
     * @throws NullPointerException if onSuccess or onFailure is null
     *
     * @example
     * <pre>{@code
     * Result<Integer, String> result = Result.success(42);
     * result.match(
     *     value -> System.out.println("Value: " + value),
     *     error -> System.out.println("Error: " + error)
     * ); // prints "Value: 42"
     * }</pre>
     *
     * @see #match(Function, Function)
     */
    public void match(Consumer<T> onSuccess, Consumer<E> onFailure) {
        Objects.requireNonNull(onSuccess, "onSuccess cannot be null");
        Objects.requireNonNull(onFailure, "onFailure cannot be null");
        if (isSuccess) {
            onSuccess.accept(successValue.get());
        } else {
            onFailure.accept(errorValue.get());
        }
    }

    // ========================================================================
    // TRANSFORMATION METHODS
    // ========================================================================

    /**
     * Transforms the contained success value using the given function if present.
     * <p>
     * If this is Success, applies the mapper function to the value and wraps the result
     * in a new successful Result. If this is Failure, returns the same Failure without
     * calling the mapper.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param mapper a function to apply to the value if present (must not be null)
     * @return a Result containing the result of applying the mapper, or the same Failure if this is Failure
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").map(String::toUpperCase); // Success{HELLO}
     * Result.failure("error").map(String::toUpperCase); // Failure{error}
     * }</pre>
     *
     * @see #bind(Function)
     * @see #tap(Consumer)
     */
    public <U> Result<U, E> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        if (isFailure()) {
            return Result.failure(errorValue.get());
        }
        U result = mapper.apply(successValue.get());
        return Result.success(result);
    }

    /**
     * Transforms the contained error value using the given function if present.
     * <p>
     * If this is Failure, applies the mapper function to the error and wraps the result
     * in a new failed Result. If this is Success, returns the same Success without
     * calling the mapper.
     * </p>
     *
     * @param <F> the type of the transformed error
     * @param mapper a function to apply to the error if present (must not be null)
     * @return a Result containing the same success value, or a failed Result with the transformed error
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * Result.failure("err").mapError(String::toUpperCase); // Failure{ERR}
     * Result.success("ok").mapError(String::toUpperCase);  // Success{ok}
     * }</pre>
     *
     * @see #map(Function)
     */
    public <F> Result<T, F> mapError(Function<E, F> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        if (isSuccess) {
            return Result.success(successValue.get());
        }
        F result = mapper.apply(errorValue.get());
        return Result.failure(result);
    }

    /**
     * Transforms the contained success value using the given function that returns a Result.
     * <p>
     * Also known as "flatMap" or "chain". If this is Success, applies the binder function
     * to the value and returns the resulting Result. If this is Failure, returns the same
     * Failure without calling the binder.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param binder a function to apply to the value if present, returning a Result (must not be null)
     * @return the result of applying the binder, or the same Failure if this is Failure
     * @throws NullPointerException if binder is null
     *
     * @example
     * <pre>{@code
     * Result.success(5).bind(x -> Result.success(x * 2)); // Success{10}
     * Result.failure("error").bind(x -> Result.success(x * 2)); // Failure{error}
     * }</pre>
     *
     * @see #map(Function)
     * @see #tap(Consumer)
     */
    public <U> Result<U, E> bind(Function<T, Result<U, E>> binder) {
        Objects.requireNonNull(binder, "binder cannot be null");
        if (isFailure()) {
            return Result.failure(errorValue.get());
        }
        return binder.apply(successValue.get());
    }

    /**
     * Executes the given consumer with the contained success value if present.
     * <p>
     * Useful for performing side effects on the value without transforming it.
     * Returns this Result for method chaining.
     * </p>
     *
     * @param consumer a consumer to execute with the value if present (must not be null)
     * @return this Result instance for chaining
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").tap(System.out::println); // prints "hello", returns Success{hello}
     * Result.failure("error").tap(System.out::println); // does nothing, returns Failure{error}
     * }</pre>
     *
     * @see #map(Function)
     * @see #bind(Function)
     */
    public Result<T, E> tap(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (isSuccess) {
            consumer.accept(successValue.get());
        }
        return this;
    }

    /**
     * Executes the given consumer with the contained error value if present.
     * <p>
     * Useful for performing side effects on the error without transforming it.
     * Returns this Result for method chaining.
     * </p>
     *
     * @param consumer a consumer to execute with the error if present (must not be null)
     * @return this Result instance for chaining
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * Result.failure("error").tapError(System.err::println); // prints "error" to stderr
     * Result.success("ok").tapError(System.err::println);    // does nothing
     * }</pre>
     *
     * @see #tap(Consumer)
     */
    public Result<T, E> tapError(Consumer<E> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (isFailure()) {
            consumer.accept(errorValue.get());
        }
        return this;
    }

    /**
     * Returns this Result if it contains a success value and the predicate matches,
     * otherwise returns a Failure with the given error.
     * <p>
     * If this is Failure, returns the same Failure without evaluating the predicate.
     * </p>
     *
     * @param predicate a predicate to test the value against (must not be null)
     * @param error the error to return if the predicate returns false (must not be null)
     * @return this Result if predicate matches, Failure(error) otherwise
     * @throws NullPointerException if predicate or error is null
     *
     * @example
     * <pre>{@code
     * Result.success(5).ensure(x -> x > 3, "too small");  // Success{5}
     * Result.success(2).ensure(x -> x > 3, "too small");  // Failure{too small}
     * Result.failure("error").ensure(x -> x > 3, "too small"); // Failure{error}
     * }</pre>
     *
     * @see #where(Predicate, Function)
     */
    public Result<T, E> ensure(Predicate<T> predicate, E error) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(error, "error cannot be null");
        if (isFailure()) {
            return this;
        }
        if (predicate.test(successValue.get())) {
            return this;
        }
        return Result.failure(error);
    }

    /**
     * Returns this Result if it contains a success value and the predicate matches,
     * otherwise returns a Failure with the error from the given function.
     * <p>
     * If this is Failure, returns the same Failure without evaluating the predicate.
     * </p>
     *
     * @param predicate a predicate to test the value against (must not be null)
     * @param errorFactory a function that creates the error if the predicate returns false (must not be null)
     * @return this Result if predicate matches, Failure(errorFactory.apply(value)) otherwise
     * @throws NullPointerException if predicate or errorFactory is null
     *
     * @example
     * <pre>{@code
     * Result.success(5).where(x -> x > 3, x -> "Value " + x + " is too small");
     * // Success{5}
     * Result.success(2).where(x -> x > 3, x -> "Value " + x + " is too small");
     * // Failure{Value 2 is too small}
     * }</pre>
     *
     * @see #ensure(Predicate, Object)
     */
    public Result<T, E> where(Predicate<T> predicate, Function<T, E> errorFactory) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorFactory, "errorFactory cannot be null");
        if (isFailure()) {
            return this;
        }
        T value = successValue.get();
        if (predicate.test(value)) {
            return this;
        }
        return Result.failure(errorFactory.apply(value));
    }

    // ========================================================================
    // RECOVERY METHODS
    // ========================================================================

    /**
     * Returns this Result if it contains a success value, otherwise returns a Result
     * containing the given value.
     *
     * @param value the value to return if this is Failure
     * @return this Result if Success, Success(value) if Failure
     *
     * @example
     * <pre>{@code
     * Result.success("hello").or("default"); // Success{hello}
     * Result.failure("error").or("default"); // Success{default}
     * }</pre>
     *
     * @see #or(Supplier)
     * @see #or(Result)
     */
    public Result<T, E> or(T value) {
        if (isSuccess) {
            return this;
        }
        return Result.success(value);
    }

    /**
     * Returns this Result if it contains a success value, otherwise returns a Result
     * containing the value supplied by the given supplier.
     * <p>
     * The supplier is only invoked if this is Failure.
     * </p>
     *
     * @param valueSupplier a supplier of the fallback value (must not be null)
     * @return this Result if Success, Success(supplier.get()) if Failure
     * @throws NullPointerException if valueSupplier is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").or(() -> "default"); // Success{hello} (supplier not called)
     * Result.failure("error").or(() -> "default"); // Success{default} (supplier called)
     * }</pre>
     *
     * @see #or(Object)
     * @see #or(Result)
     */
    public Result<T, E> or(Supplier<T> valueSupplier) {
        Objects.requireNonNull(valueSupplier, "valueSupplier cannot be null");
        if (isSuccess) {
            return this;
        }
        return Result.success(valueSupplier.get());
    }

    /**
     * Returns this Result if it contains a success value, otherwise returns the given Result.
     *
     * @param other the Result to return if this is Failure (must not be null)
     * @return this Result if Success, other if Failure
     * @throws NullPointerException if other is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").or(Result.success("world")); // Success{hello}
     * Result.failure("error").or(Result.success("world")); // Success{world}
     * }</pre>
     *
     * @see #orElse(Result)
     * @see #or(Object)
     */
    public Result<T, E> or(Result<T, E> other) {
        Objects.requireNonNull(other, "other cannot be null");
        if (isSuccess) {
            return this;
        }
        return other;
    }

    /**
     * Returns this Result if it contains a success value, otherwise returns the given Result.
     * <p>
     * This is an alias for {@link #or(Result)} and behaves identically.
     * </p>
     *
     * @param other the Result to return if this is Failure
     * @return this Result if Success, other if Failure
     *
     * @example
     * <pre>{@code
     * Result.success("hello").orElse(Result.success("world")); // Success{hello}
     * Result.failure("error").orElse(Result.success("world")); // Success{world}
     * }</pre>
     *
     * @see #or(Result)
     */
    public Result<T, E> orElse(Result<T, E> other) {
        return or(other);
    }

    /**
     * Returns this Result if it contains a success value, otherwise returns a Result
     * containing the value supplied by the given supplier.
     * <p>
     * The supplier is only invoked if this is Failure.
     * </p>
     *
     * @param resultSupplier a supplier of the fallback Result (must not be null)
     * @return this Result if Success, supplier.get() if Failure
     * @throws NullPointerException if resultSupplier is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").orElseGet(() -> Result.success("default"));
     * // Success{hello} (supplier not called)
     * Result.failure("error").orElseGet(() -> Result.success("default"));
     * // Success{default} (supplier called)
     * }</pre>
     *
     * @see #or(Supplier)
     * @see #or(Result)
     */
    public Result<T, E> orElseGet(Supplier<Result<T, E>> resultSupplier) {
        Objects.requireNonNull(resultSupplier, "resultSupplier cannot be null");
        if (isSuccess) {
            return this;
        }
        return resultSupplier.get();
    }

    /**
     * Returns the success value if present, otherwise returns the given default value.
     *
     * @param defaultValue the value to return if this is Failure
     * @return the success value if present, otherwise the default value
     *
     * @example
     * <pre>{@code
     * Result.success("hello").getValueOrDefault("default"); // "hello"
     * Result.failure("error").getValueOrDefault("default"); // "default"
     * }</pre>
     *
     * @see #getValueOrDefault(Supplier)
     * @see #getValueOrThrow()
     */
    public T getValueOrDefault(T defaultValue) {
        if (isSuccess) {
            return successValue.get();
        }
        return defaultValue;
    }

    /**
     * Returns the success value if present, otherwise returns the value supplied by
     * the given supplier.
     * <p>
     * The supplier is only invoked if this is Failure.
     * </p>
     *
     * @param defaultSupplier a supplier of the default value (must not be null)
     * @return the success value if present, otherwise the supplied default value
     * @throws NullPointerException if defaultSupplier is null
     *
     * @example
     * <pre>{@code
     * Result.success("hello").getValueOrDefault(() -> "default");
     * // "hello" (supplier not called)
     * Result.failure("error").getValueOrDefault(() -> "default");
     * // "default" (supplier called)
     * }</pre>
     *
     * @see #getValueOrDefault(Object)
     * @see #getValueOrThrow()
     */
    public T getValueOrDefault(Supplier<T> defaultSupplier) {
        Objects.requireNonNull(defaultSupplier, "defaultSupplier cannot be null");
        if (isSuccess) {
            return successValue.get();
        }
        return defaultSupplier.get();
    }

    // ========================================================================
    // CONVERSION METHODS
    // ========================================================================

    /**
     * Returns the underlying Optional containing the success value.
     * <p>
     * If this Result is a Failure, returns an empty Optional.
     * </p>
     *
     * @return the Optional wrapping the success value
     *
     * @example
     * <pre>{@code
     * Result.success("hello").toOptional(); // Optional["hello"]
     * Result.failure("error").toOptional(); // Optional.empty
     * }</pre>
     *
     * @see #fromOptional(Optional)
     */
    public Optional<T> toOptional() {
        return successValue;
    }

    /**
     * Creates a Result from an Optional, returning Failure with the given error
     * if the Optional is empty.
     *
     * @param <T> the type of the value
     * @param <E> the type of the error
     * @param optional the Optional to convert (must not be null)
     * @param error the error to return if the Optional is empty (must not be null)
     * @return Success(value) if the Optional contains a value, Failure(error) otherwise
     * @throws NullPointerException if optional or error is null
     *
     * @example
     * <pre>{@code
     * Result.fromOptional(Optional.of("hello"), "missing"); // Success{hello}
     * Result.fromOptional(Optional.empty(), "missing");     // Failure{missing}
     * }</pre>
     *
     * @see #toOptional()
     */
    public static <T, E> Result<T, E> fromOptional(Optional<T> optional, E error) {
        Objects.requireNonNull(optional, "optional cannot be null");
        Objects.requireNonNull(error, "error cannot be null");
        return optional
            .map(Result::<T, E>success)
            .orElse(Result.failure(error));
    }

    /**
     * Creates a Result from an Optional, returning Failure with the error from
     * the given supplier if the Optional is empty.
     *
     * @param <T> the type of the value
     * @param <E> the type of the error
     * @param optional the Optional to convert (must not be null)
     * @param errorSupplier a supplier of the error to return if the Optional is empty (must not be null)
     * @return Success(value) if the Optional contains a value, Failure(errorSupplier.get()) otherwise
     * @throws NullPointerException if optional or errorSupplier is null
     *
     * @example
     * <pre>{@code
     * Result.fromOptional(Optional.of("hello"), () -> "missing"); // Success{hello}
     * Result.fromOptional(Optional.empty(), () -> "missing");     // Failure{missing}
     * }</pre>
     *
     * @see #toOptional()
     */
    public static <T, E> Result<T, E> fromOptional(Optional<T> optional, Supplier<E> errorSupplier) {
        Objects.requireNonNull(optional, "optional cannot be null");
        Objects.requireNonNull(errorSupplier, "errorSupplier cannot be null");
        return optional
            .map(Result::<T, E>success)
            .orElseGet(() -> Result.failure(errorSupplier.get()));
    }

    // ========================================================================
    // OBJECT METHODS
    // ========================================================================

    /**
     * Compares this Result with another object for equality.
     * <p>
     * Two Results are equal if they are both Success with equal values,
     * or both Failure with equal errors.
     * </p>
     *
     * @param obj the object to compare with
     * @return true if the Results are equal, false otherwise
     *
     * @example
     * <pre>{@code
     * Result.success("hello").equals(Result.success("hello")); // true
     * Result.success("hello").equals(Result.success("world")); // false
     * Result.failure("error").equals(Result.failure("error")); // true
     * }</pre>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Result)) {
            return false;
        }
        Result<?, ?> other = (Result<?, ?>) obj;
        if (this.isSuccess != other.isSuccess) {
            return false;
        }
        if (this.isSuccess) {
            return this.successValue.equals(other.successValue);
        } else {
            return this.errorValue.equals(other.errorValue);
        }
    }

    /**
     * Returns a hash code for this Result.
     *
     * @return the hash code based on success/error state and value
     *
     * @example
     * <pre>{@code
     * Result.success("hello").hashCode(); // hash based on success state and "hello"
     * Result.failure("error").hashCode(); // hash based on failure state and "error"
     * }</pre>
     */
    @Override
    public int hashCode() {
        return isSuccess ? successValue.hashCode() : errorValue.hashCode();
    }

    /**
     * Returns a string representation of this Result.
     *
     * @return "Success{value}" if this is Success, "Failure{error}" if this is Failure
     *
     * @example
     * <pre>{@code
     * Result.success("hello").toString(); // "Success{hello}"
     * Result.failure("error").toString(); // "Failure{error}"
     * }</pre>
     */
    @Override
    public String toString() {
        if (isSuccess) {
            return "Success{" + successValue.get() + "}";
        } else {
            return "Failure{" + errorValue.get() + "}";
        }
    }
}
