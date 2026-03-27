package com.adrewdev.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the Utilities class.
 * Tests cover type helper functions, utility methods, and Maybe utilities.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UtilitiesTest {

    // ========================================================================
    // TYPE HELPER FUNCTIONS TESTS
    // ========================================================================

    @Nested
    @DisplayName("isDefined()")
    class IsDefinedMethod {

        @Test
        @DisplayName("with non-null value returns true")
        void withNonNull_returnsTrue() {
            assertThat(Utilities.isDefined("test")).isTrue();
            assertThat(Utilities.isDefined(42)).isTrue();
            assertThat(Utilities.isDefined(new Object())).isTrue();
        }

        @Test
        @DisplayName("with null value returns false")
        void withNull_returnsFalse() {
            assertThat(Utilities.isDefined(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSome()")
    class IsSomeMethod {

        @Test
        @DisplayName("with Some returns true")
        void withSome_returnsTrue() {
            assertThat(Utilities.isSome(Maybe.some("test"))).isTrue();
            assertThat(Utilities.isSome(Maybe.some(42))).isTrue();
        }

        @Test
        @DisplayName("with None returns false")
        void withNone_returnsFalse() {
            assertThat(Utilities.isSome(Maybe.none())).isFalse();
        }

        @Test
        @DisplayName("with null returns false")
        void withNull_returnsFalse() {
            assertThat(Utilities.isSome(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isNone()")
    class IsNoneMethod {

        @Test
        @DisplayName("with None returns true")
        void withNone_returnsTrue() {
            assertThat(Utilities.isNone(Maybe.none())).isTrue();
        }

        @Test
        @DisplayName("with Some returns false")
        void withSome_returnsFalse() {
            assertThat(Utilities.isNone(Maybe.some("test"))).isFalse();
            assertThat(Utilities.isNone(Maybe.some(42))).isFalse();
        }

        @Test
        @DisplayName("with null returns true")
        void withNull_returnsTrue() {
            assertThat(Utilities.isNone(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("isSuccessful()")
    class IsSuccessfulMethod {

        @Test
        @DisplayName("with Success returns true")
        void withSuccess_returnsTrue() {
            assertThat(Utilities.isSuccessful(Result.success("test"))).isTrue();
            assertThat(Utilities.isSuccessful(Result.success(42))).isTrue();
        }

        @Test
        @DisplayName("with Failure returns false")
        void withFailure_returnsFalse() {
            assertThat(Utilities.isSuccessful(Result.failure("error"))).isFalse();
        }

        @Test
        @DisplayName("with null returns false")
        void withNull_returnsFalse() {
            assertThat(Utilities.isSuccessful(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFailure()")
    class IsFailureMethod {

        @Test
        @DisplayName("with Failure returns true")
        void withFailure_returnsTrue() {
            assertThat(Utilities.isFailure(Result.failure("error"))).isTrue();
        }

        @Test
        @DisplayName("with Success returns false")
        void withSuccess_returnsFalse() {
            assertThat(Utilities.isFailure(Result.success("test"))).isFalse();
            assertThat(Utilities.isFailure(Result.success(42))).isFalse();
        }

        @Test
        @DisplayName("with null returns true")
        void withNull_returnsTrue() {
            assertThat(Utilities.isFailure(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("isFunction()")
    class IsFunctionMethod {

        @Test
        @DisplayName("with Function returns true")
        void withFunction_returnsTrue() {
            Function<String, String> func = s -> s;
            assertThat(Utilities.isFunction(func)).isTrue();
        }

        @Test
        @DisplayName("with non-Function returns false")
        void withNonFunction_returnsFalse() {
            assertThat(Utilities.isFunction("not a function")).isFalse();
            assertThat(Utilities.isFunction(42)).isFalse();
            assertThat(Utilities.isFunction(new Object())).isFalse();
        }
    }

    // ========================================================================
    // UTILITY METHODS TESTS
    // ========================================================================

    @Nested
    @DisplayName("never()")
    class NeverMethod {

        @Test
        @DisplayName("throws UnsupportedOperationException")
        void throwsUnsupportedOperationException() {
            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    Utilities::never
            );
            assertThat(exception.getMessage()).isEqualTo("never() should never be called");
        }
    }

    @Nested
    @DisplayName("noop()")
    class NoopMethod {

        @Test
        @DisplayName("does nothing")
        void doesNothing() {
            assertDoesNotThrow(Utilities::noop);
        }

        @Test
        @DisplayName("can be called multiple times")
        void canBeCalledMultipleTimes() {
            assertDoesNotThrow(() -> {
                Utilities.noop();
                Utilities.noop();
                Utilities.noop();
            });
        }
    }

    @Nested
    @DisplayName("noopConsumer()")
    class NoopConsumerMethod {

        @Test
        @DisplayName("returns a Consumer that does nothing")
        void doesNothing() {
            Consumer<String> consumer = Utilities.noopConsumer();
            assertDoesNotThrow(() -> consumer.accept("test"));
        }

        @Test
        @DisplayName("can accept null values")
        void canAcceptNull() {
            Consumer<String> consumer = Utilities.noopConsumer();
            assertDoesNotThrow(() -> consumer.accept(null));
        }

        @Test
        @DisplayName("returns functional Consumer each time")
        void returnsFunctionalConsumer() {
            Consumer<String> consumer1 = Utilities.noopConsumer();
            Consumer<String> consumer2 = Utilities.noopConsumer();

            assertThat(consumer1).isNotNull();
            assertThat(consumer2).isNotNull();
            
            // Both should work independently
            assertDoesNotThrow(() -> consumer1.accept("test1"));
            assertDoesNotThrow(() -> consumer2.accept("test2"));
        }
    }

    @Nested
    @DisplayName("identity()")
    class IdentityMethod {

        @Test
        @DisplayName("returns input unchanged")
        void returnsInput() {
            Function<String, String> identity = Utilities.identity();
            assertThat(identity.apply("test")).isEqualTo("test");
        }

        @Test
        @DisplayName("works with integers")
        void worksWithIntegers() {
            Function<Integer, Integer> identity = Utilities.identity();
            assertThat(identity.apply(42)).isEqualTo(42);
        }

        @Test
        @DisplayName("works with null")
        void worksWithNull() {
            Function<String, String> identity = Utilities.identity();
            assertThat(identity.apply(null)).isNull();
        }

        @Test
        @DisplayName("returns same function instance for type")
        void returnsSameFunctionInstance() {
            Function<String, String> identity1 = Utilities.identity();
            Function<String, String> identity2 = Utilities.identity();

            assertThat(identity1.apply("test")).isEqualTo(identity2.apply("test"));
        }
    }

    // ========================================================================
    // MAYBE UTILITY METHODS TESTS
    // ========================================================================

    @Nested
    @DisplayName("zeroAsNone()")
    class ZeroAsNoneMethod {

        @Test
        @DisplayName("with zero returns None")
        void withZero_returnsNone() {
            assertThat(Utilities.zeroAsNone(0).isNone()).isTrue();
        }

        @Test
        @DisplayName("with positive number returns Some")
        void withPositiveNumber_returnsSome() {
            Maybe<Integer> result = Utilities.zeroAsNone(42);
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(42);
        }

        @Test
        @DisplayName("with negative number returns Some")
        void withNegativeNumber_returnsSome() {
            Maybe<Integer> result = Utilities.zeroAsNone(-10);
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(-10);
        }
    }

    @Nested
    @DisplayName("emptyStringAsNone()")
    class EmptyStringAsNoneMethod {

        @Test
        @DisplayName("with empty string returns None")
        void withEmptyString_returnsNone() {
            assertThat(Utilities.emptyStringAsNone("").isNone()).isTrue();
        }

        @Test
        @DisplayName("with null returns None")
        void withNull_returnsNone() {
            assertThat(Utilities.emptyStringAsNone(null).isNone()).isTrue();
        }

        @Test
        @DisplayName("with non-empty string returns Some")
        void withNonEmpty_returnsSome() {
            Maybe<String> result = Utilities.emptyStringAsNone("test");
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with whitespace-only string returns Some")
        void withWhitespaceString_returnsSome() {
            Maybe<String> result = Utilities.emptyStringAsNone("   ");
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("emptyOrWhiteSpaceStringAsNone()")
    class EmptyOrWhiteSpaceStringAsNoneMethod {

        @Test
        @DisplayName("with blank string returns None")
        void withBlankString_returnsNone() {
            assertThat(Utilities.emptyOrWhiteSpaceStringAsNone("   ").isNone()).isTrue();
        }

        @Test
        @DisplayName("with empty string returns None")
        void withEmptyString_returnsNone() {
            assertThat(Utilities.emptyOrWhiteSpaceStringAsNone("").isNone()).isTrue();
        }

        @Test
        @DisplayName("with null returns None")
        void withNull_returnsNone() {
            assertThat(Utilities.emptyOrWhiteSpaceStringAsNone(null).isNone()).isTrue();
        }

        @Test
        @DisplayName("with non-blank string returns Some")
        void withNonBlank_returnsSome() {
            Maybe<String> result = Utilities.emptyOrWhiteSpaceStringAsNone("test");
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with string containing non-whitespace returns Some")
        void withNonWhitespace_returnsSome() {
            Maybe<String> result = Utilities.emptyOrWhiteSpaceStringAsNone(" a ");
            assertThat(result.isSome()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(" a ");
        }

        @Test
        @DisplayName("with tab and newline returns None")
        void withTabAndNewline_returnsNone() {
            assertThat(Utilities.emptyOrWhiteSpaceStringAsNone("\t\n").isNone()).isTrue();
        }
    }
}
