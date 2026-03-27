package com.adrewdev.functional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a value that may be present (Some) or absent (None).
 * <p>
 * This class is a 1:1 port of the TypeScript Maybe type from
 * typescript-functional-extensions. It provides a type-safe way to handle
 * potentially null values without using null directly.
 * </p>
 * <p>
 * A Maybe is either:
 * <ul>
 *   <li>{@code Some(value)} - contains a non-null value</li>
 *   <li>{@code None} - represents the absence of a value</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of the contained value
 * @author Ported from typescript-functional-extensions
 * @see Optional
 */
public final class Maybe<T> {

    /**
     * Singleton instance representing the absence of a value.
     * Uses wildcard to allow assignment to any Maybe<T> type.
     */
    @SuppressWarnings("unchecked")
    private static final Maybe<?> NONE = new Maybe<>(Optional.empty());

    /**
     * The underlying Optional wrapping the value.
     */
    private final Optional<T> value;

    /**
     * Private constructor that wraps the given Optional.
     *
     * @param value the Optional containing the value (must not be null)
     * @throws NullPointerException if value is null
     */
    private Maybe(Optional<T> value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        this.value = value;
    }

    /**
     * Creates a Maybe from a potentially null value.
     * <p>
     * If the value is null, returns {@code None}. Otherwise, returns {@code Some(value)}.
     * This is the safest way to create a Maybe when you're not sure if the value is null.
     * </p>
     *
     * @param <T> the type of the value
     * @param value the value to wrap, may be null
     * @return {@code Some(value)} if value is non-null, {@code None} if value is null
     *
     * @example
     * <pre>{@code
     * Maybe<String> some = Maybe.from("hello"); // Some{hello}
     * Maybe<String> none = Maybe.from(null);    // None{}
     * }</pre>
     *
     * @see #some(Object)
     * @see #none()
     */
    public static <T> Maybe<T> from(T value) {
        return (value == null) ? none() : some(value);
    }

    /**
     * Creates a Maybe containing the given non-null value.
     * <p>
     * Use this method when you know the value is non-null and want to
     * explicitly create a Some instance.
     * </p>
     *
     * @param <T> the type of the value
     * @param value the non-null value to wrap
     * @return a new Maybe containing the value
     * @throws NullPointerException if value is null
     *
     * @example
     * <pre>{@code
     * Maybe<String> some = Maybe.some("hello"); // Some{hello}
     * // Maybe.some(null); // throws NullPointerException
     * }</pre>
     *
     * @see #from(Object)
     * @see #none()
     */
    public static <T> Maybe<T> some(T value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        return new Maybe<>(Optional.of(value));
    }

    /**
     * Returns a Maybe representing the absence of a value.
     * <p>
     * This is a singleton - all calls to {@code none()} return the same instance.
     * </p>
     *
     * @param <T> the type of the Maybe (inferred from context)
     * @return the singleton None instance
     *
     * @example
     * <pre>{@code
     * Maybe<String> none = Maybe.none(); // None{}
     * Maybe<Integer> alsoNone = Maybe.none(); // same instance
     * }</pre>
     *
     * @see #empty()
     * @see #from(Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> Maybe<T> none() {
        return (Maybe<T>) NONE;
    }

    /**
     * Returns a Maybe representing the absence of a value.
     * <p>
     * This is an alias for {@link #none()} and returns the same singleton instance.
     * </p>
     *
     * @param <T> the type of the Maybe (inferred from context)
     * @return the singleton None instance
     *
     * @example
     * <pre>{@code
     * Maybe<String> none = Maybe.empty(); // None{}
     * }</pre>
     *
     * @see #none()
     */
    @SuppressWarnings("unchecked")
    public static <T> Maybe<T> empty() {
        return (Maybe<T>) NONE;
    }

    /**
     * Returns a Maybe containing the first element of the list,
     * or None if the list is null or empty.
     *
     * <p>Example:
     * <pre>{@code
     * Maybe<String> first = Maybe.tryFirst(Arrays.asList("a", "b", "c"));
     * first.match(
     *     value -> System.out.println("First: " + value),
     *     () -> System.out.println("List is empty")
     * );
     * }</pre>
     *
     * @param <T> the type of elements in the list
     * @param list the list to get the first element from, may be null
     * @return a Maybe containing the first element, or None if the list is null or empty
     *
     * @see #tryLast(List)
     * @see #from(Object)
     */
    public static <T> Maybe<T> tryFirst(List<T> list) {
        if (list == null || list.isEmpty()) {
            return none();
        }
        return from(list.get(0));
    }

    /**
     * Returns a Maybe containing the last element of the list,
     * or None if the list is null or empty.
     *
     * <p>Example:
     * <pre>{@code
     * Maybe<String> last = Maybe.tryLast(Arrays.asList("a", "b", "c"));
     * last.match(
     *     value -> System.out.println("Last: " + value),
     *     () -> System.out.println("List is empty")
     * );
     * }</pre>
     *
     * @param <T> the type of elements in the list
     * @param list the list to get the last element from, may be null
     * @return a Maybe containing the last element, or None if the list is null or empty
     *
     * @see #tryFirst(List)
     * @see #from(Object)
     */
    public static <T> Maybe<T> tryLast(List<T> list) {
        if (list == null || list.isEmpty()) {
            return none();
        }
        return from(list.get(list.size() - 1));
    }

    /**
     * Returns true if this Maybe contains a value (is Some).
     *
     * @return true if this is Some, false if this is None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").isSome(); // true
     * Maybe.none().isSome();        // false
     * }</pre>
     *
     * @see #isNone()
     * @see #isDefined()
     */
    public boolean isSome() {
        return value.isPresent();
    }

    /**
     * Returns true if this Maybe represents the absence of a value (is None).
     *
     * @return true if this is None, false if this is Some
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").isNone(); // false
     * Maybe.none().isNone();        // true
     * }</pre>
     *
     * @see #isSome()
     * @see #isDefined()
     */
    public boolean isNone() {
        return !value.isPresent();
    }

    /**
     * Returns true if this Maybe contains a value (is Some).
     * <p>
     * This is an alias for {@link #isSome()} and returns the same result.
     * </p>
     *
     * @return true if this is Some, false if this is None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").isDefined(); // true
     * Maybe.none().isDefined();        // false
     * }</pre>
     *
     * @see #isSome()
     * @see #isNone()
     */
    public boolean isDefined() {
        return value.isPresent();
    }

    /**
     * Returns the contained value if present, throws NoSuchElementException otherwise.
     *
     * @return the contained value
     * @throws NoSuchElementException if this is None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").getValueOrThrow(); // "hello"
     * Maybe.none().getValueOrThrow();        // throws NoSuchElementException
     * }</pre>
     *
     * @see #getValueOrThrow(Supplier)
     * @see #getValueOrDefault(Object)
     */
    public T getValueOrThrow() {
        return value.orElseThrow(() -> new NoSuchElementException("Cannot get value from None"));
    }

    /**
     * Returns the contained value if present, throws NoSuchElementException with
     * the provided message otherwise.
     *
     * @param messageSupplier a supplier of the exception message
     * @return the contained value
     * @throws NoSuchElementException if this is None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").getValueOrThrow(() -> "No value found"); // "hello"
     * Maybe.none().getValueOrThrow(() -> "No value found");        // throws NoSuchElementException with message
     * }</pre>
     *
     * @see #getValueOrThrow()
     * @see #getValueOrDefault(Object)
     */
    public T getValueOrThrow(Supplier<String> messageSupplier) {
        return value.orElseThrow(() -> new NoSuchElementException(messageSupplier.get()));
    }

    /**
     * Returns the contained value if present, or the given default value if None.
     *
     * @param defaultValue the default value to return if this is None
     * @return the contained value if present, otherwise the default value
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").getValueOrDefault("default"); // "hello"
     * Maybe.none().getValueOrDefault("default");        // "default"
     * }</pre>
     *
     * @see #getValueOrDefault(Supplier)
     * @see #getValueOrThrow()
     */
    public T getValueOrDefault(T defaultValue) {
        return value.orElse(defaultValue);
    }

    /**
     * Returns the contained value if present, or the value supplied by the given
     * supplier if None.
     * <p>
     * The supplier is only invoked if this is None.
     * </p>
     *
     * @param defaultSupplier a supplier of the default value
     * @return the contained value if present, otherwise the supplied default value
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").getValueOrDefault(() -> "default"); // "hello" (supplier not called)
     * Maybe.none().getValueOrDefault(() -> "default");        // "default" (supplier called)
     * }</pre>
     *
     * @see #getValueOrDefault(Object)
     * @see #getValueOrThrow()
     */
    public T getValueOrDefault(Supplier<T> defaultSupplier) {
        return value.orElseGet(defaultSupplier);
    }

    /**
     * Returns the underlying Optional containing the value.
     *
     * @return the Optional wrapping the value
     *
     * @example
     * <pre>{@code
     * Maybe<String> maybe = Maybe.some("hello");
     * Optional<String> opt = maybe.toOptional(); // Optional["hello"]
     * }</pre>
     *
     * @see #from(Object)
     */
    public Optional<T> toOptional() {
        return value;
    }

    /**
     * Transforms the contained value using the given function if present.
     * <p>
     * If this is Some, applies the mapper function to the value and wraps the result
     * in a new Maybe. If this is None, returns None without calling the mapper.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param mapper a function to apply to the value if present
     * @return a Maybe containing the result of applying the mapper, or None if this is None
     * @throws NullPointerException if mapper is null
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").map(String::toUpperCase); // Some{HELLO}
     * Maybe.<String>none().map(String::toUpperCase); // None{}
     * }</pre>
     *
     * @see #bind(Function)
     * @see #tap(Consumer)
     */
    public <U> Maybe<U> map(Function<T, U> mapper) {
        if (mapper == null) {
            throw new NullPointerException("mapper cannot be null");
        }
        if (isNone()) {
            return Maybe.none();
        }
        U result = mapper.apply(value.get());
        return Maybe.some(result);
    }

    /**
     * Transforms the contained value using the given function that returns a Maybe.
     * <p>
     * Also known as "flatMap" or "chain". If this is Some, applies the binder function
     * to the value and returns the resulting Maybe. If this is None, returns None
     * without calling the binder.
     * </p>
     *
     * @param <U> the type of the transformed value
     * @param binder a function to apply to the value if present, returning a Maybe
     * @return the result of applying the binder, or None if this is None
     * @throws NullPointerException if binder is null
     *
     * @example
     * <pre>{@code
     * Maybe.some(5).bind(x -> Maybe.some(x * 2)); // Some{10}
     * Maybe.<Integer>none().bind(x -> Maybe.some(x * 2)); // None{}
     * }</pre>
     *
     * @see #map(Function)
     * @see #tap(Consumer)
     */
    public <U> Maybe<U> bind(Function<T, Maybe<U>> binder) {
        if (binder == null) {
            throw new NullPointerException("binder cannot be null");
        }
        if (isNone()) {
            return Maybe.none();
        }
        return binder.apply(value.get());
    }

    /**
     * Executes the given consumer with the contained value if present.
     * <p>
     * Useful for performing side effects on the value without transforming it.
     * Returns this Maybe for method chaining.
     * </p>
     *
     * @param consumer a consumer to execute with the value if present
     * @return this Maybe instance for chaining
     * @throws NullPointerException if consumer is null
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").tap(System.out::println); // prints "hello", returns Some{hello}
     * Maybe.<String>none().tap(System.out::println); // does nothing, returns None{}
     * }</pre>
     *
     * @see #map(Function)
     * @see #bind(Function)
     */
    public Maybe<T> tap(Consumer<T> consumer) {
        if (consumer == null) {
            throw new NullPointerException("consumer cannot be null");
        }
        if (isSome()) {
            consumer.accept(value.get());
        }
        return this;
    }

    /**
     * Returns this Maybe if it contains a value and the predicate matches,
     * otherwise returns None.
     * <p>
     * Also known as "filter". If this is None, returns None without evaluating
     * the predicate.
     * </p>
     *
     * @param predicate a predicate to test the value against
     * @return this Maybe if predicate matches, None otherwise
     * @throws NullPointerException if predicate is null
     *
     * @example
     * <pre>{@code
     * Maybe.some(5).where(x -> x > 3);  // Some{5}
     * Maybe.some(2).where(x -> x > 3);  // None{}
     * Maybe.<Integer>none().where(x -> x > 3); // None{}
     * }</pre>
     *
     * @see #ensure(Predicate)
     */
    public Maybe<T> where(java.util.function.Predicate<T> predicate) {
        if (predicate == null) {
            throw new NullPointerException("predicate cannot be null");
        }
        if (isNone()) {
            return Maybe.none();
        }
        if (predicate.test(value.get())) {
            return this;
        }
        return Maybe.none();
    }

    /**
     * Returns this Maybe if it contains a value and the predicate matches,
     * otherwise returns None.
     * <p>
     * This is an alias for {@link #where(java.util.function.Predicate)} and
     * behaves identically.
     * </p>
     *
     * @param predicate a predicate to test the value against
     * @return this Maybe if predicate matches, None otherwise
     * @throws NullPointerException if predicate is null
     *
     * @example
     * <pre>{@code
     * Maybe.some(5).ensure(x -> x > 3);  // Some{5}
     * Maybe.some(2).ensure(x -> x > 3);  // None{}
     * }</pre>
     *
     * @see #where(java.util.function.Predicate)
     */
    public Maybe<T> ensure(java.util.function.Predicate<T> predicate) {
        return where(predicate);
    }

    /**
     * Returns this Maybe if it contains a value, otherwise returns a Maybe
     * containing the given value.
     *
     * @param value the value to return if this is None
     * @return this Maybe if Some, Some(value) if None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").or("default"); // Some{hello}
     * Maybe.none().or("default");        // Some{default}
     * }</pre>
     *
     * @see #or(Supplier)
     * @see #or(Maybe)
     */
    public Maybe<T> or(T value) {
        if (isSome()) {
            return this;
        }
        return Maybe.some(value);
    }

    /**
     * Returns this Maybe if it contains a value, otherwise returns a Maybe
     * containing the value supplied by the given supplier.
     * <p>
     * The supplier is only invoked if this is None.
     * </p>
     *
     * @param valueSupplier a supplier of the fallback value
     * @return this Maybe if Some, Some(supplier.get()) if None
     * @throws NullPointerException if valueSupplier is null
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").or(() -> "default"); // Some{hello} (supplier not called)
     * Maybe.none().or(() -> "default");        // Some{default} (supplier called)
     * }</pre>
     *
     * @see #or(Object)
     * @see #or(Maybe)
     */
    public Maybe<T> or(Supplier<T> valueSupplier) {
        if (valueSupplier == null) {
            throw new NullPointerException("valueSupplier cannot be null");
        }
        if (isSome()) {
            return this;
        }
        return Maybe.some(valueSupplier.get());
    }

    /**
     * Returns this Maybe if it contains a value, otherwise returns the given Maybe.
     *
     * @param other the Maybe to return if this is None
     * @return this Maybe if Some, other if None
     * @throws NullPointerException if other is null
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").or(Maybe.some("world")); // Some{hello}
     * Maybe.none().or(Maybe.some("world"));        // Some{world}
     * }</pre>
     *
     * @see #orElse(Maybe)
     * @see #or(Object)
     */
    public Maybe<T> or(Maybe<T> other) {
        if (other == null) {
            throw new NullPointerException("other cannot be null");
        }
        if (isSome()) {
            return this;
        }
        return other;
    }

    /**
     * Returns this Maybe if it contains a value, otherwise returns the given Maybe.
     * <p>
     * This is an alias for {@link #or(Maybe)} and behaves identically.
     * </p>
     *
     * @param other the Maybe to return if this is None
     * @return this Maybe if Some, other if None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").orElse(Maybe.some("world")); // Some{hello}
     * Maybe.none().orElse(Maybe.some("world"));        // Some{world}
     * }</pre>
     *
     * @see #or(Maybe)
     */
    public Maybe<T> orElse(Maybe<T> other) {
        return or(other);
    }

    /**
     * Performs pattern matching on this Maybe.
     * <p>
     * Calls {@code onSome(value)} if this is Some, or {@code onNone()} if this is None.
     * </p>
     *
     * @param <R> the type of the result
     * @param matcher a matcher providing callbacks for Some and None cases
     * @return the result of calling the appropriate callback
     * @throws NullPointerException if matcher is null
     *
     * @example
     * <pre>{@code
     * Maybe<String> maybe = Maybe.some("hello");
     * String result = maybe.match(new MaybeMatcher<String, String>() {
     *     public String onSome(String value) { return "Got: " + value; }
     *     public String onNone() { return "Nothing"; }
     * }); // "Got: hello"
     * }</pre>
     *
     * @see MaybeMatcher
     * @see #match(Consumer, Runnable)
     */
    public <R> R match(MaybeMatcher<T, R> matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher cannot be null");
        }
        if (isSome()) {
            return matcher.onSome(value.get());
        } else {
            return matcher.onNone();
        }
    }

    /**
     * Performs pattern matching on this Maybe with void callbacks.
     * <p>
     * Calls {@code some.accept(value)} if this is Some, or {@code none.run()} if this is None.
     * </p>
     *
     * @param some a consumer to execute with the value if this is Some
     * @param none a runnable to execute if this is None
     * @throws NullPointerException if some or none is null
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").match(
     *     value -> System.out.println("Got: " + value),
     *     () -> System.out.println("Nothing")
     * ); // prints "Got: hello"
     * }</pre>
     *
     * @see #match(MaybeMatcher)
     */
    public void match(Consumer<T> some, Runnable none) {
        if (some == null) {
            throw new NullPointerException("some consumer cannot be null");
        }
        if (none == null) {
            throw new NullPointerException("none runnable cannot be null");
        }
        if (isSome()) {
            some.accept(value.get());
        } else {
            none.run();
        }
    }

    /**
     * Compares this Maybe with another object for equality.
     * <p>
     * Two Maybes are equal if they are both Some with equal values,
     * or both None.
     * </p>
     *
     * @param obj the object to compare with
     * @return true if the Maybes are equal, false otherwise
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").equals(Maybe.some("hello")); // true
     * Maybe.some("hello").equals(Maybe.some("world")); // false
     * Maybe.none().equals(Maybe.none());               // true
     * }</pre>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Maybe)) {
            return false;
        }
        Maybe<?> other = (Maybe<?>) obj;
        return this.value.equals(other.value);
    }

    /**
     * Returns a hash code for this Maybe.
     *
     * @return the hash code of the underlying Optional
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").hashCode(); // hash code of Optional["hello"]
     * Maybe.none().hashCode();        // hash code of Optional.empty
     * }</pre>
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns a string representation of this Maybe.
     *
     * @return "Some{value}" if this is Some, "None{}" if this is None
     *
     * @example
     * <pre>{@code
     * Maybe.some("hello").toString(); // "Some{hello}"
     * Maybe.none().toString();        // "None{}"
     * }</pre>
     */
    @Override
    public String toString() {
        if (isSome()) {
            return "Some{" + value.get() + "}";
        } else {
            return "None{}";
        }
    }

    // ========================================================================
    // CONVERSION METHODS - FASE 6
    // ========================================================================

    /**
     * Converts this Maybe to a Result.
     *
     * <p>Returns Success if this Maybe contains a value, or Failure with the
     * provided error if it is empty.</p>
     *
     * <p>Example:
     * <pre>{@code
     * Maybe<String> maybe = Maybe.from("value");
     * Result<String, String> result = maybe.toResult("No value provided");
     * result.match(
     *     value -> System.out.println("Success: " + value),
     *     error -> System.out.println("Error: " + error)
     * );
     * }</pre>
     *
     * @param <E> the type of the error
     * @param error the error to use if this Maybe is empty
     * @return a Result containing Success with the value, or Failure with the error
     */
    public <E> Result<T, E> toResult(E error) {
        return value
            .<Result<T, E>>map(Result::success)
            .orElse(Result.failure(error));
    }

    /**
     * Converts this Maybe to a Result with lazy error evaluation.
     *
     * <p>Returns Success if this Maybe contains a value, or Failure with the
     * provided error from the supplier if it is empty.</p>
     *
     * @param <E> the type of the error
     * @param errorSupplier the supplier that provides the error if this Maybe is empty
     * @return a Result containing Success with the value, or Failure with the error
     * @throws NullPointerException if errorSupplier is null
     */
    public <E> Result<T, E> toResult(Supplier<E> errorSupplier) {
        Objects.requireNonNull(errorSupplier, "errorSupplier cannot be null");
        return value
            .<Result<T, E>>map(Result::success)
            .orElseGet(() -> Result.failure(errorSupplier.get()));
    }

    /**
     * Converts this Maybe to a MaybeAsync.
     *
     * <p>Useful for composing with asynchronous operations.</p>
     *
     * <p>Example:
     * <pre>{@code
     * Maybe<String> maybe = Maybe.from("value");
     * MaybeAsync<String> async = maybe.toMaybeAsync();
     * }</pre>
     *
     * @return a MaybeAsync containing the same value as this Maybe
     */
    public MaybeAsync<T> toMaybeAsync() {
        return MaybeAsync.from(value.orElse(null));
    }
}
