package com.adrewdev.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the MaybeAsync class.
 * Tests cover factory methods, transformations, pattern matching, and conversion.
 */
@DisplayName("MaybeAsync")
class MaybeAsyncTest {

    @Nested
    @DisplayName("from(T)")
    class FromWithValueMethod {

        @Test
        @DisplayName("with non-null value returns Some")
        void from_withValue_returnsSome() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.isNone()).isFalse();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null value returns None")
        void from_withNull_returnsNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from((String) null);
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
            assertThat(result.isSome()).isFalse();
        }
    }

    @Nested
    @DisplayName("from(CompletableFuture)")
    class FromWithFutureMethod {

        @Test
        @DisplayName("with successful future returns Some")
        void from_withSuccessfulFuture_returnsSome() throws Exception {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            MaybeAsync<String> maybe = MaybeAsync.from(future);
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with failed future returns None")
        void from_withFailedFuture_returnsNone() throws Exception {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("error"));
            MaybeAsync<String> maybe = MaybeAsync.from(future);
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null future throws NullPointerException")
        void from_withNullFuture_throwsNPE() {
            assertThatThrownBy(() -> MaybeAsync.from((CompletableFuture<String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("future cannot be null");
        }
    }

    @Nested
    @DisplayName("from(Supplier)")
    class FromWithSupplierMethod {

        @Test
        @DisplayName("with successful supplier returns Some")
        void from_withSuccessfulSupplier_returnsSome() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from(() -> "test");
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with throwing supplier returns None")
        void from_withThrowingSupplier_returnsNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from(() -> {
                throw new RuntimeException("error");
            });
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void from_withNullSupplier_throwsNPE() {
            assertThatThrownBy(() -> MaybeAsync.from((Supplier<String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("supplier cannot be null");
        }
    }

    @Nested
    @DisplayName("some()")
    class SomeMethod {

        @Test
        @DisplayName("with non-null value returns Some")
        void some_withValue_returnsSome() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.some("test");
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null value throws NullPointerException")
        void some_withNull_throwsNPE() {
            assertThatThrownBy(() -> MaybeAsync.some(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    @DisplayName("none()")
    class NoneMethod {

        @Test
        @DisplayName("returns None")
        void none_returnsNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            Maybe<String> result = maybe.toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
            assertThat(result.isSome()).isFalse();
        }
    }

    @Nested
    @DisplayName("map()")
    class MapMethod {

        @Test
        @DisplayName("with Some applies function")
        void withSome_appliesFunction() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("hello");
            Maybe<Integer> result = maybe.map(String::length).toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("with None returns None")
        void withNone_returnsNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            Maybe<Integer> result = maybe.map(String::length).toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null mapper throws NullPointerException")
        void withNullMapper_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.map(null));
        }
    }

    @Nested
    @DisplayName("bind()")
    class BindMethod {

        @Test
        @DisplayName("with Some applies function")
        void withSome_appliesFunction() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("hello");
            Maybe<Integer> result = maybe.bind(s -> MaybeAsync.some(s.length())).toCompletableFuture().join();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("with None returns None")
        void withNone_returnsNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            Maybe<Integer> result = maybe.bind(s -> MaybeAsync.some(s.length())).toCompletableFuture().join();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("with null binder throws NullPointerException")
        void withNullBinder_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.bind(null));
        }
    }

    @Nested
    @DisplayName("tap()")
    class TapMethod {

        @Test
        @DisplayName("with Some calls consumer")
        void withSome_callsConsumer() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            AtomicBoolean called = new AtomicBoolean(false);
            MaybeAsync<String> result = maybe.tap(value -> called.set(true));

            result.toCompletableFuture().join();
            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("with None does nothing")
        void withNone_doesNothing() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            AtomicBoolean called = new AtomicBoolean(false);
            MaybeAsync<String> result = maybe.tap(value -> called.set(true));

            result.toCompletableFuture().join();
            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("with null consumer throws NullPointerException")
        void withNullConsumer_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.tap(null));
        }
    }

    @Nested
    @DisplayName("match(MaybeAsyncMatcher)")
    class MatchWithMatcherMethod {

        @Test
        @DisplayName("with Some calls onSome")
        void withSome_callsOnSome() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("hello");
            String result = maybe.match(new MaybeAsyncMatcher<String, String>() {
                @Override
                public String onSome(String value) {
                    return "Got: " + value;
                }

                @Override
                public String onNone() {
                    return "No value";
                }
            }).join();

            assertThat(result).isEqualTo("Got: hello");
        }

        @Test
        @DisplayName("with None calls onNone")
        void withNone_callsOnNone() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            String result = maybe.match(new MaybeAsyncMatcher<String, String>() {
                @Override
                public String onSome(String value) {
                    return "Got: " + value;
                }

                @Override
                public String onNone() {
                    return "No value";
                }
            }).join();

            assertThat(result).isEqualTo("No value");
        }

        @Test
        @DisplayName("with null matcher throws NullPointerException")
        void withNullMatcher_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.match((MaybeAsyncMatcher<String, String>) null));
        }
    }

    @Nested
    @DisplayName("match(Consumer, Runnable)")
    class MatchWithCallbacksMethod {

        @Test
        @DisplayName("with Some calls some consumer")
        void withSome_callsSomeConsumer() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("hello");
            java.util.concurrent.atomic.AtomicReference<String> captured = new java.util.concurrent.atomic.AtomicReference<>();
            maybe.match(
                value -> captured.set(value),
                () -> {}
            ).join();

            assertThat(captured.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("with None calls none runnable")
        void withNone_callsNoneRunnable() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            AtomicBoolean called = new AtomicBoolean(false);
            maybe.match(
                value -> {},
                () -> called.set(true)
            ).join();

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("with null some throws NullPointerException")
        void withNullSome_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.match(null, () -> {}));
        }

        @Test
        @DisplayName("with null none throws NullPointerException")
        void withNullNone_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.match(v -> {}, null));
        }
    }

    @Nested
    @DisplayName("getValueOrThrow()")
    class GetValueOrThrowMethod {

        @Test
        @DisplayName("returns value for Some")
        void getValueOrThrow_withSome_returnsValue() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            String value = maybe.getValueOrThrow().join();

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("throws CompletionException for None")
        void getValueOrThrow_withNone_throwsException() {
            MaybeAsync<String> maybe = MaybeAsync.none();

            assertThatThrownBy(() -> maybe.getValueOrThrow().join())
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("getValueOrThrow(Supplier)")
    class GetValueOrThrowWithMessageMethod {

        @Test
        @DisplayName("returns value for Some")
        void getValueOrThrow_withSome_returnsValue() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            String value = maybe.getValueOrThrow(() -> "custom message").join();

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("throws CompletionException with custom message for None")
        void getValueOrThrow_withNone_throwsExceptionWithMessage() {
            MaybeAsync<String> maybe = MaybeAsync.none();

            assertThatThrownBy(() -> maybe.getValueOrThrow(() -> "custom error message").join())
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class)
                    .hasRootCauseMessage("custom error message");
        }

        @Test
        @DisplayName("with null messageSupplier throws NullPointerException")
        void withNullMessageSupplier_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.getValueOrThrow((Supplier<String>) null));
        }
    }

    @Nested
    @DisplayName("or(T)")
    class OrWithValueMethod {

        @Test
        @DisplayName("with Some returns original")
        void withSome_returnsOriginal() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("original");
            Maybe<String> result = maybe.or("fallback").toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("with None returns fallback value")
        void withNone_returnsFallback() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            Maybe<String> result = maybe.or("fallback").toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("or(Supplier)")
    class OrWithSupplierMethod {

        @Test
        @DisplayName("with Some does not call supplier")
        void withSome_doesNotCallSupplier() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("original");
            AtomicBoolean called = new AtomicBoolean(false);
            maybe.or(() -> {
                called.set(true);
                return "fallback";
            }).toCompletableFuture().join();

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("with None calls supplier")
        void withNone_callsSupplier() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            AtomicBoolean called = new AtomicBoolean(false);
            Maybe<String> result = maybe.or(() -> {
                called.set(true);
                return "fallback";
            }).toCompletableFuture().join();

            assertThat(called.get()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("fallback");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.none();
            assertThrows(NullPointerException.class, () -> maybe.or((Supplier<String>) null));
        }
    }

    @Nested
    @DisplayName("or(MaybeAsync)")
    class OrWithMaybeAsyncMethod {

        @Test
        @DisplayName("with Some returns original")
        void withSome_returnsOriginal() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("original");
            MaybeAsync<String> other = MaybeAsync.from("other");
            Maybe<String> result = maybe.or(other).toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("with None returns other")
        void withNone_returnsOther() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            MaybeAsync<String> other = MaybeAsync.from("other");
            Maybe<String> result = maybe.or(other).toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("other");
        }

        @Test
        @DisplayName("with null other throws NullPointerException")
        void withNullOther_throwsNPE() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            assertThrows(NullPointerException.class, () -> maybe.or((MaybeAsync<String>) null));
        }
    }

    @Nested
    @DisplayName("orElse(MaybeAsync)")
    class OrElseMethod {

        @Test
        @DisplayName("is alias for or(MaybeAsync)")
        void isAliasForOr() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.none();
            MaybeAsync<String> other = MaybeAsync.from("other");
            Maybe<String> result = maybe.orElse(other).toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("other");
        }
    }

    @Nested
    @DisplayName("toCompletableFuture()")
    class ToCompletableFutureMethod {

        @Test
        @DisplayName("returns underlying CompletableFuture")
        void returnsUnderlyingCompletableFuture() throws Exception {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            CompletableFuture<Maybe<String>> future = maybe.toCompletableFuture();

            assertThat(future).isNotNull();
            assertThat(future.join().getValueOrThrow()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("toMaybe()")
    class ToMaybeMethod {

        @Test
        @DisplayName("converts to Maybe (blocking)")
        void convertsToMaybe() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");
            Maybe<String> result = maybe.toMaybe();

            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("converts None to Maybe")
        void convertsNoneToMaybe() {
            MaybeAsync<String> maybe = MaybeAsync.none();
            Maybe<String> result = maybe.toMaybe();

            assertThat(result.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("equals()")
    class EqualsMethod {

        @Test
        @DisplayName("returns true for same instance")
        void equals_sameInstance_returnsTrue() {
            MaybeAsync<String> maybe = MaybeAsync.from("test");

            assertThat(maybe).isSameAs(maybe);
        }

        @Test
        @DisplayName("returns true for equal Some values")
        void equals_equalSomeValues_returnsTrue() {
            MaybeAsync<String> maybe1 = MaybeAsync.from("test");
            MaybeAsync<String> maybe2 = MaybeAsync.from("test");

            assertThat(maybe1).isEqualTo(maybe2);
        }

        @Test
        @DisplayName("returns false for different Some values")
        void equals_differentSomeValues_returnsFalse() {
            MaybeAsync<String> maybe1 = MaybeAsync.from("test1");
            MaybeAsync<String> maybe2 = MaybeAsync.from("test2");

            assertThat(maybe1).isNotEqualTo(maybe2);
        }

        @Test
        @DisplayName("returns true for None instances")
        void equals_noneInstances_returnsTrue() {
            MaybeAsync<String> none1 = MaybeAsync.none();
            MaybeAsync<String> none2 = MaybeAsync.none();

            assertThat(none1).isEqualTo(none2);
        }

        @Test
        @DisplayName("returns false when comparing Some with None")
        void equals_someAndNone_returnsFalse() {
            MaybeAsync<String> some = MaybeAsync.from("test");
            MaybeAsync<String> none = MaybeAsync.none();

            assertThat(some).isNotEqualTo(none);
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCodeMethod {

        @Test
        @DisplayName("returns same hash for equal Maybes")
        void hashCode_equalMaybes_returnSameHash() {
            MaybeAsync<String> maybe1 = MaybeAsync.from("test");
            MaybeAsync<String> maybe2 = MaybeAsync.from("test");

            assertThat(maybe1.hashCode()).isEqualTo(maybe2.hashCode());
        }

        @Test
        @DisplayName("returns same hash for None instances")
        void hashCode_noneInstances_returnSameHash() {
            MaybeAsync<String> none1 = MaybeAsync.none();
            MaybeAsync<String> none2 = MaybeAsync.none();

            assertThat(none1.hashCode()).isEqualTo(none2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("returns MaybeAsync{Some{value}} for Some")
        void toString_withSome_returnsSomeFormat() {
            MaybeAsync<String> maybe = MaybeAsync.from("hello");

            assertThat(maybe.toString()).isEqualTo("MaybeAsync{Some{hello}}");
        }

        @Test
        @DisplayName("returns MaybeAsync{None{}} for None")
        void toString_withNone_returnsNoneFormat() {
            MaybeAsync<String> maybe = MaybeAsync.none();

            assertThat(maybe.toString()).isEqualTo("MaybeAsync{None{}}");
        }
    }
}
