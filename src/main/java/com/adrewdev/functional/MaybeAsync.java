package com.adrewdev.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a future value that may or may not exist.
 * <p>
 * MaybeAsync is the asynchronous version of {@link Maybe}, wrapping a
 * {@link CompletableFuture} that resolves to a Maybe value. It provides
 * a 1:1 port of the TypeScript MaybeAsync type from
 * typescript-functional-extensions.
 * </p>
 * <p>
 * A MaybeAsync is either:
 * <ul>
 *   <li>{@code Some(value)} - contains a non-null value (wrapped in CompletableFuture)</li>
 *   <li>{@code None} - represents the absence of a value (wrapped in CompletableFuture)</li>
 * </ul>
 * </p>
 * <p>
 * Example:
 * <pre>{@code
 * MaybeAsync<String> maybe = MaybeAsync.from(fetchUser(id))
 *     .map(user -> user.getEmail())
 *     .or("default@email.com");
 *
 * String email = maybe.toCompletableFuture().join().getValueOrThrow();
 * }</pre>
 *
 * @param <T> the type of the contained value
 * @author Ported from typescript-functional-extensions
 * @see Maybe
 * @see CompletableFuture
 */
public final class MaybeAsync<T> {

    /**
     * The underlying CompletableFuture wrapping the Maybe value.
     */
    private final CompletableFuture<Maybe<T>> future;

    /**
     * Private constructor that wraps the given CompletableFuture.
     *
     * @param future the CompletableFuture containing the Maybe value (must not be null)
     * @throws NullPointerException if future is null
     */
    private MaybeAsync(CompletableFuture<Maybe<T>> future) {
        this.future = Objects.requireNonNull(future, "future cannot be null");
    }

    // ========================================================================
    // STATIC FACTORY METHODS
    // ========================================================================

    /**
     * Creates a MaybeAsync from a potentially null value.
     * <p>
     * If the value is null, returns a MaybeAsync that resolves to None.
     * Otherwise, returns a MaybeAsync that resolves to Some(value).
     * </p>
     *
     * @param <T> the type of the value
     * @param value the value to wrap, may be null
     * @return a MaybeAsync that resolves to Maybe.from(value)
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> some = MaybeAsync.from("hello"); // resolves to Some{hello}
     * MaybeAsync<String> none = MaybeAsync.from(null);    // resolves to None{}
     * }</pre>
     *
     * @see #some(Object)
     * @see #none()
     */
    public static <T> MaybeAsync<T> from(T value) {
        return new MaybeAsync<>(CompletableFuture.completedFuture(Maybe.from(value)));
    }

    /**
     * Creates a MaybeAsync from a CompletableFuture.
     * <p>
     * The resulting MaybeAsync will resolve to Some(value) if the future
     * completes successfully, or None if the future completes with an exception.
     * </p>
     *
     * @param <T> the type of the value
     * @param future the CompletableFuture to wrap (must not be null)
     * @return a MaybeAsync that wraps the future
     * @throws NullPointerException if future is null
     *
     * @example
     * <pre>{@code
     * CompletableFuture<String> future = fetchUserAsync(id);
     * MaybeAsync<String> maybe = MaybeAsync.from(future);
     * }</pre>
     *
     * @see #from(Supplier)
     */
    public static <T> MaybeAsync<T> from(CompletableFuture<T> future) {
        Objects.requireNonNull(future, "future cannot be null");
        return new MaybeAsync<>(
            future.thenApply(Maybe::from)
                  .exceptionally(throwable -> Maybe.none())
        );
    }

    /**
     * Creates a MaybeAsync from a Supplier.
     * <p>
     * The supplier is executed asynchronously using CompletableFuture.supplyAsync().
     * The resulting MaybeAsync will resolve to Some(value) if the supplier
     * succeeds, or None if the supplier throws an exception.
     * </p>
     *
     * @param <T> the type of the value
     * @param supplier the supplier to execute asynchronously (must not be null)
     * @return a MaybeAsync that resolves to Maybe.from(supplier.get())
     * @throws NullPointerException if supplier is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from(() -> database.lookup(id));
     * }</pre>
     *
     * @see #from(CompletableFuture)
     */
    public static <T> MaybeAsync<T> from(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return new MaybeAsync<>(
            CompletableFuture.supplyAsync(supplier)
                .thenApply(Maybe::from)
                .exceptionally(throwable -> Maybe.none())
        );
    }

    /**
     * Creates a MaybeAsync containing the given non-null value.
     * <p>
     * Use this method when you know the value is non-null and want to
     * explicitly create a Some instance.
     * </p>
     *
     * @param <T> the type of the value
     * @param value the non-null value to wrap
     * @return a MaybeAsync containing Some(value)
     * @throws NullPointerException if value is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> some = MaybeAsync.some("hello"); // Some{hello}
     * // MaybeAsync.some(null); // throws NullPointerException
     * }</pre>
     *
     * @see #from(Object)
     * @see #none()
     */
    public static <T> MaybeAsync<T> some(T value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        return new MaybeAsync<>(CompletableFuture.completedFuture(Maybe.some(value)));
    }

    /**
     * Returns a MaybeAsync representing the absence of a value.
     * <p>
     * This returns a singleton instance - all calls to {@code none()} return
     * the same MaybeAsync that resolves to Maybe.none().
     * </p>
     *
     * @param <T> the type of the MaybeAsync (inferred from context)
     * @return a MaybeAsync that resolves to Maybe.none()
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> none = MaybeAsync.none(); // resolves to None{}
     * }</pre>
     *
     * @see #from(Object)
     * @see #some(Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> MaybeAsync<T> none() {
        return new MaybeAsync<>(CompletableFuture.completedFuture(Maybe.none()));
    }

    // ========================================================================
    // INSTANCE METHODS - TRANSFORMATION
    // ========================================================================

    /**
     * Transforms the contained value using the given function if present.
     * <p>
     * If this MaybeAsync resolves to Some, applies the mapper function to the
     * value and wraps the result in a new MaybeAsync. If this resolves to None,
     * returns a MaybeAsync that resolves to None without calling the mapper.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param mapper a function to apply to the value if present (must not be null)
     * @return a MaybeAsync containing the result of applying the mapper, or None if this is None
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<Integer> result = maybe.map(String::length);
     * // result resolves to Some{5}
     * }</pre>
     *
     * @see #bind(Function)
     * @see #tap(Consumer)
     */
    public <U> MaybeAsync<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new MaybeAsync<>(
            future.thenApply(maybe -> maybe.map(mapper))
        );
    }

    /**
     * Transforms the contained value using the given function that returns a MaybeAsync.
     * <p>
     * Also known as "flatMap" or "chain". If this MaybeAsync resolves to Some,
     * applies the binder function to the value and returns the resulting MaybeAsync.
     * If this resolves to None, returns a MaybeAsync that resolves to None without
     * calling the binder.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param binder a function to apply to the value if present, returning a MaybeAsync (must not be null)
     * @return a MaybeAsync containing the result of applying the binder, or None if this is None
     * @throws NullPointerException if binder is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<Integer> result = maybe.bind(s -> MaybeAsync.some(s.length()));
     * // result resolves to Some{5}
     * }</pre>
     *
     * @see #map(Function)
     * @see #tap(Consumer)
     */
    public <U> MaybeAsync<U> bind(Function<T, MaybeAsync<U>> binder) {
        Objects.requireNonNull(binder, "binder cannot be null");
        return new MaybeAsync<>(
            future.thenCompose(maybe -> 
                maybe.isSome() 
                    ? binder.apply(maybe.getValueOrThrow()).toCompletableFuture()
                    : CompletableFuture.completedFuture(Maybe.<U>none())
            )
        );
    }

    /**
     * Executes the given consumer with the contained value if present.
     * <p>
     * Useful for performing side effects on the value without transforming it.
     * Returns a new MaybeAsync containing the same value.
     * </p>
     *
     * @param consumer a consumer to execute with the value if present (must not be null)
     * @return a MaybeAsync containing the same value
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<String> result = maybe.tap(System.out::println);
     * // prints "hello", returns MaybeAsync containing Some{hello}
     * }</pre>
     *
     * @see #map(Function)
     * @see #bind(Function)
     */
    public MaybeAsync<T> tap(Consumer<T> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        return new MaybeAsync<>(
            future.thenApply(maybe -> {
                maybe.tap(consumer);
                return maybe;
            })
        );
    }

    // ========================================================================
    // INSTANCE METHODS - PATTERN MATCHING
    // ========================================================================

    /**
     * Performs pattern matching on this MaybeAsync.
     * <p>
     * Calls {@code matcher.onSome(value)} if this resolves to Some, or
     * {@code matcher.onNone()} if this resolves to None.
     * </p>
     *
     * @param <R> the type of the result
     * @param matcher a matcher providing callbacks for Some and None cases (must not be null)
     * @return a CompletableFuture containing the result of calling the appropriate callback
     * @throws NullPointerException if matcher is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * CompletableFuture<String> result = maybe.match(new MaybeAsyncMatcher<String, String>() {
     *     public String onSome(String value) { return "Got: " + value; }
     *     public String onNone() { return "Nothing"; }
     * });
     * // result completes with "Got: hello"
     * }</pre>
     *
     * @see MaybeAsyncMatcher
     * @see #match(Consumer, Runnable)
     */
    public <R> CompletableFuture<R> match(MaybeAsyncMatcher<T, R> matcher) {
        Objects.requireNonNull(matcher, "matcher cannot be null");
        return future.thenApply(maybe -> maybe.match(new MaybeMatcher<T, R>() {
            @Override
            public R onSome(T value) {
                return matcher.onSome(value);
            }

            @Override
            public R onNone() {
                return matcher.onNone();
            }
        }));
    }

    /**
     * Performs pattern matching on this MaybeAsync with void callbacks.
     * <p>
     * Calls {@code some.accept(value)} if this resolves to Some, or
     * {@code none.run()} if this resolves to None.
     * </p>
     *
     * @param some a consumer to execute with the value if this is Some (must not be null)
     * @param none a runnable to execute if this is None (must not be null)
     * @return a CompletableFuture that completes after the callback is executed
     * @throws NullPointerException if some or none is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * CompletableFuture<Void> result = maybe.match(
     *     value -> System.out.println("Got: " + value),
     *     () -> System.out.println("Nothing")
     * );
     * // prints "Got: hello"
     * }</pre>
     *
     * @see #match(MaybeAsyncMatcher)
     */
    public CompletableFuture<Void> match(Consumer<T> some, Runnable none) {
        Objects.requireNonNull(some, "some consumer cannot be null");
        Objects.requireNonNull(none, "none runnable cannot be null");
        return future.thenAccept(maybe -> maybe.match(some, none));
    }

    // ========================================================================
    // INSTANCE METHODS - VALUE ACCESS
    // ========================================================================

    /**
     * Gets the contained value if present, throws NoSuchElementException otherwise.
     * <p>
     * The returned CompletableFuture will complete with the value if this
     * MaybeAsync resolves to Some, or complete exceptionally with
     * NoSuchElementException if it resolves to None.
     * </p>
     *
     * @return a CompletableFuture containing the value
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * String value = maybe.getValueOrThrow().join(); // "hello"
     *
     * MaybeAsync<String> none = MaybeAsync.none();
     * none.getValueOrThrow().join(); // throws CompletionException with NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrThrow(Supplier)
     * @see #getValueOrDefault(Object)
     */
    public CompletableFuture<T> getValueOrThrow() {
        return future.thenApply(maybe -> maybe.getValueOrThrow());
    }

    /**
     * Gets the contained value if present, throws NoSuchElementException with
     * the provided message otherwise.
     * <p>
     * The returned CompletableFuture will complete with the value if this
     * MaybeAsync resolves to Some, or complete exceptionally with
     * NoSuchElementException if it resolves to None.
     * </p>
     *
     * @param messageSupplier a supplier of the exception message (must not be null)
     * @return a CompletableFuture containing the value
     * @throws NullPointerException if messageSupplier is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * String value = maybe.getValueOrThrow(() -> "No value found").join(); // "hello"
     * }</pre>
     *
     * @see #getValueOrThrow()
     * @see #getValueOrDefault(Object)
     */
    public CompletableFuture<T> getValueOrThrow(Supplier<String> messageSupplier) {
        Objects.requireNonNull(messageSupplier, "messageSupplier cannot be null");
        return future.thenApply(maybe -> maybe.getValueOrThrow(messageSupplier));
    }

    // ========================================================================
    // INSTANCE METHODS - RECOVERY
    // ========================================================================

    /**
     * Returns this MaybeAsync if it resolves to a value, otherwise returns a
     * MaybeAsync containing the given value.
     *
     * @param value the fallback value to return if this is None
     * @return a MaybeAsync containing the original value if Some, or the fallback value if None
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<String> result = maybe.or("default");
     * // result resolves to Some{hello}
     *
     * MaybeAsync<String> none = MaybeAsync.none();
     * MaybeAsync<String> result2 = none.or("default");
     * // result2 resolves to Some{default}
     * }</pre>
     *
     * @see #or(Supplier)
     * @see #or(MaybeAsync)
     */
    public MaybeAsync<T> or(T value) {
        return new MaybeAsync<>(
            future.thenApply(maybe -> maybe.or(value))
        );
    }

    /**
     * Returns this MaybeAsync if it resolves to a value, otherwise returns a
     * MaybeAsync containing the value supplied by the given supplier.
     * <p>
     * The supplier is only invoked if this MaybeAsync resolves to None.
     * </p>
     *
     * @param valueSupplier a supplier of the fallback value (must not be null)
     * @return a MaybeAsync containing the original value if Some, or the supplied fallback value if None
     * @throws NullPointerException if valueSupplier is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<String> result = maybe.or(() -> "default");
     * // supplier not called, result resolves to Some{hello}
     *
     * MaybeAsync<String> none = MaybeAsync.none();
     * MaybeAsync<String> result2 = none.or(() -> "default");
     * // supplier called, result2 resolves to Some{default}
     * }</pre>
     *
     * @see #or(Object)
     * @see #or(MaybeAsync)
     */
    public MaybeAsync<T> or(Supplier<T> valueSupplier) {
        Objects.requireNonNull(valueSupplier, "valueSupplier cannot be null");
        return new MaybeAsync<>(
            future.thenApply(maybe -> maybe.or(valueSupplier))
        );
    }

    /**
     * Returns this MaybeAsync if it resolves to a value, otherwise returns the
     * given MaybeAsync.
     *
     * @param other the MaybeAsync to return if this is None (must not be null)
     * @return this MaybeAsync if Some, other if None
     * @throws NullPointerException if other is null
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * MaybeAsync<String> other = MaybeAsync.from("world");
     * MaybeAsync<String> result = maybe.or(other);
     * // result resolves to Some{hello}
     *
     * MaybeAsync<String> none = MaybeAsync.none();
     * MaybeAsync<String> result2 = none.or(other);
     * // result2 resolves to Some{world}
     * }</pre>
     *
     * @see #orElse(MaybeAsync)
     * @see #or(Object)
     */
    public MaybeAsync<T> or(MaybeAsync<T> other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new MaybeAsync<>(
            future.thenCompose(maybe ->
                maybe.isSome()
                    ? CompletableFuture.completedFuture(maybe)
                    : other.toCompletableFuture()
            )
        );
    }

    /**
     * Returns this MaybeAsync if it resolves to a value, otherwise returns the
     * given MaybeAsync.
     * <p>
     * This is an alias for {@link #or(MaybeAsync)} and behaves identically.
     * </p>
     *
     * @param other the MaybeAsync to return if this is None
     * @return this MaybeAsync if Some, other if None
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> none = MaybeAsync.none();
     * MaybeAsync<String> other = MaybeAsync.from("world");
     * MaybeAsync<String> result = none.orElse(other);
     * // result resolves to Some{world}
     * }</pre>
     *
     * @see #or(MaybeAsync)
     */
    public MaybeAsync<T> orElse(MaybeAsync<T> other) {
        return or(other);
    }

    // ========================================================================
    // INSTANCE METHODS - CONVERSION
    // ========================================================================

    /**
     * Returns the underlying CompletableFuture.
     *
     * @return the CompletableFuture wrapping a Maybe
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * CompletableFuture<Maybe<String>> future = maybe.toCompletableFuture();
     * Maybe<String> result = future.join(); // Some{hello}
     * }</pre>
     *
     * @see #toMaybe()
     */
    public CompletableFuture<Maybe<T>> toCompletableFuture() {
        return future;
    }

    /**
     * Converts to Maybe (blocking).
     * <p>
     * This method blocks until the CompletableFuture completes.
     * Use {@link #toCompletableFuture()} for non-blocking access.
     * </p>
     *
     * @return the Maybe value (blocks until complete)
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * Maybe<String> result = maybe.toMaybe(); // Some{hello}
     * }</pre>
     *
     * @see #toCompletableFuture()
     */
    public Maybe<T> toMaybe() {
        return future.join();
    }

    // ========================================================================
    // OBJECT METHODS
    // ========================================================================

    /**
     * Compares this MaybeAsync with another object for equality.
     * <p>
     * Two MaybeAsync instances are equal if their underlying Futures
     * complete with equal Maybe values.
     * </p>
     *
     * @param obj the object to compare with
     * @return true if the MaybeAsync instances are equal, false otherwise
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe1 = MaybeAsync.from("hello");
     * MaybeAsync<String> maybe2 = MaybeAsync.from("hello");
     * // To compare async values, you need to wait for completion:
     * boolean equal = maybe1.toCompletableFuture().join()
     *                     .equals(maybe2.toCompletableFuture().join());
     * }</pre>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MaybeAsync)) {
            return false;
        }
        MaybeAsync<?> other = (MaybeAsync<?>) obj;
        // Compare the underlying futures by blocking on them
        return this.future.join().equals(other.future.join());
    }

    /**
     * Returns a hash code for this MaybeAsync.
     * <p>
     * This method blocks until the CompletableFuture completes.
     * </p>
     *
     * @return the hash code of the underlying Maybe value
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * int hash = maybe.hashCode(); // blocks and returns hash of Some{hello}
     * }</pre>
     */
    @Override
    public int hashCode() {
        return future.join().hashCode();
    }

    /**
     * Returns a string representation of this MaybeAsync.
     * <p>
     * This method blocks until the CompletableFuture completes.
     * </p>
     *
     * @return "MaybeAsync{Some{value}}" if this is Some, "MaybeAsync{None{}}" if this is None
     *
     * @example
     * <pre>{@code
     * MaybeAsync<String> maybe = MaybeAsync.from("hello");
     * System.out.println(maybe.toString()); // "MaybeAsync{Some{hello}}"
     *
     * MaybeAsync<String> none = MaybeAsync.none();
     * System.out.println(none.toString()); // "MaybeAsync{None{}}"
     * }</pre>
     */
    @Override
    public String toString() {
        return "MaybeAsync{" + future.join() + "}";
    }
}
