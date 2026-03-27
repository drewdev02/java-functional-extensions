package com.adrewdev.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for Result.java - FASE 1: Setup + Core
 */
@DisplayName("Result")
class ResultTest {

    @Nested
    @DisplayName("success()")
    class SuccessMethod {

        @Test
        @DisplayName("with value returns successful Result")
        void withValue_returnsSuccessfulResult() {
            Result<String, String> result = Result.success("test");

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null throws NullPointerException")
        void withNull_throwsNPE() {
            assertThatThrownBy(() -> Result.success(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("success value cannot be null");
        }

        @Test
        @DisplayName("creates Result with correct type")
        void createsResultWithCorrectType() {
            Result<Integer, String> result = Result.success(42);

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("failure()")
    class FailureMethod {

        @Test
        @DisplayName("with error returns failed Result")
        void withError_returnsFailedResult() {
            Result<String, String> result = Result.failure("error");

            assertThat(result.isSuccessful()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null throws NullPointerException")
        void withNull_throwsNPE() {
            assertThatThrownBy(() -> Result.failure(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("error cannot be null");
        }

        @Test
        @DisplayName("creates Result with correct type")
        void createsResultWithCorrectType() {
            Result<String, Integer> result = Result.failure(500);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("try_()")
    class TryMethod {

        @Test
        @DisplayName("with successful supplier returns Success")
        void withSuccessfulSupplier_returnsSuccess() {
            Result<Integer, String> result = Result.try_(
                () -> 42,
                error -> "Error: " + error
            );

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(42);
        }

        @Test
        @DisplayName("with throwing supplier returns Failure")
        void withThrowingSupplier_returnsFailure() {
            Result<Integer, String> result = Result.try_(
                () -> {
                    throw new RuntimeException("test error");
                },
                error -> "Error: " + error.getMessage()
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("Error: test error");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            assertThatThrownBy(() -> Result.try_(null, error -> "error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("supplier cannot be null");
        }

        @Test
        @DisplayName("with null error handler throws NullPointerException")
        void withNullErrorHandler_throwsNPE() {
            assertThatThrownBy(() -> Result.try_(() -> 42, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("errorHandler cannot be null");
        }

        @Test
        @DisplayName("catches checked exceptions")
        void catchesCheckedExceptions() {
            Result<Integer, String> result = Result.try_(
                () -> {
                    throw new RuntimeException("checked exception");
                },
                error -> "Caught: " + error.getMessage()
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("Caught: checked exception");
        }
    }

    @Nested
    @DisplayName("of()")
    class OfMethod {

        @Test
        @DisplayName("with successful supplier returns its Result")
        void withSuccessfulSupplier_returnsItsResult() {
            Result<Integer, String> result = Result.of(() -> Result.success(42));

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(42);
        }

        @Test
        @DisplayName("with failure supplier returns its Result")
        void withFailureSupplier_returnsItsResult() {
            Result<Integer, String> result = Result.of(() -> Result.failure("error"));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with throwing supplier returns Failure with exception")
        void withThrowingSupplier_returnsFailureWithException() {
            Result<Integer, Throwable> result = Result.of(() -> {
                throw new RuntimeException("test");
            });

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isInstanceOf(RuntimeException.class);
            assertThat(result.getErrorOrThrow()).hasMessage("test");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            assertThatThrownBy(() -> Result.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("supplier cannot be null");
        }
    }

    @Nested
    @DisplayName("isSuccessful() / isFailure()")
    class IsSuccessFailure {

        @Test
        @DisplayName("isSuccessful returns true for Success")
        void isSuccessful_returnsTrueForSuccess() {
            assertThat(Result.success("value").isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("isSuccessful returns false for Failure")
        void isSuccessful_returnsFalseForFailure() {
            assertThat(Result.failure("error").isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("isFailure returns false for Success")
        void isFailure_returnsFalseForSuccess() {
            assertThat(Result.success("value").isFailure()).isFalse();
        }

        @Test
        @DisplayName("isFailure returns true for Failure")
        void isFailure_returnsTrueForFailure() {
            assertThat(Result.failure("error").isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("getValueOrThrow()")
    class GetValueOrThrow {

        @Test
        @DisplayName("returns value for Success")
        void returnsValueForSuccess() {
            Result<String, String> result = Result.success("hello");

            assertThat(result.getValueOrThrow()).isEqualTo("hello");
        }

        @Test
        @DisplayName("throws NoSuchElementException for Failure")
        void throwsExceptionForFailure() {
            Result<String, String> result = Result.failure("error");

            assertThatThrownBy(result::getValueOrThrow)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Result is a failure, no success value present");
        }
    }

    @Nested
    @DisplayName("getErrorOrThrow()")
    class GetErrorOrThrow {

        @Test
        @DisplayName("returns error for Failure")
        void returnsErrorForFailure() {
            Result<String, String> result = Result.failure("error message");

            assertThat(result.getErrorOrThrow()).isEqualTo("error message");
        }

        @Test
        @DisplayName("throws NoSuchElementException for Success")
        void throwsExceptionForSuccess() {
            Result<String, String> result = Result.success("value");

            assertThatThrownBy(result::getErrorOrThrow)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Result is successful, no error present");
        }
    }

    @Nested
    @DisplayName("match()")
    class MatchMethod {

        @Test
        @DisplayName("calls onSuccess for Success with Function")
        void callsOnSuccessForSuccessWithFunction() {
            Result<Integer, String> result = Result.success(42);

            String output = result.match(
                value -> "Value: " + value,
                error -> "Error: " + error
            );

            assertThat(output).isEqualTo("Value: 42");
        }

        @Test
        @DisplayName("calls onFailure for Failure with Function")
        void callsOnFailureForFailureWithFunction() {
            Result<Integer, String> result = Result.failure("something went wrong");

            String output = result.match(
                value -> "Value: " + value,
                error -> "Error: " + error
            );

            assertThat(output).isEqualTo("Error: something went wrong");
        }

        @Test
        @DisplayName("calls onSuccess for Success with Consumer")
        void callsOnSuccessForSuccessWithConsumer() {
            Result<Integer, String> result = Result.success(42);
            StringBuilder sb = new StringBuilder();

            result.match(
                (Consumer<Integer>) value -> sb.append("Value: ").append(value),
                (Consumer<String>) error -> sb.append("Error: ").append(error)
            );

            assertThat(sb.toString()).isEqualTo("Value: 42");
        }

        @Test
        @DisplayName("calls onFailure for Failure with Consumer")
        void callsOnFailureForFailureWithConsumer() {
            Result<Integer, String> result = Result.failure("error");
            StringBuilder sb = new StringBuilder();

            result.match(
                (Consumer<Integer>) value -> sb.append("Value: ").append(value),
                (Consumer<String>) error -> sb.append("Error: ").append(error)
            );

            assertThat(sb.toString()).isEqualTo("Error: error");
        }

        @Test
        @DisplayName("with null onSuccess throws NullPointerException")
        void withNullOnSuccess_throwsNPE() {
            Result<Integer, String> result = Result.success(42);

            assertThatThrownBy(() -> result.match(null, error -> "error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("onSuccess cannot be null");
        }

        @Test
        @DisplayName("with null onFailure throws NullPointerException")
        void withNullOnFailure_throwsNPE() {
            Result<Integer, String> result = Result.success(42);

            assertThatThrownBy(() -> result.match(value -> "value", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("onFailure cannot be null");
        }
    }

    @Nested
    @DisplayName("map()")
    class MapMethod {

        @Test
        @DisplayName("transforms success value")
        void transformsSuccessValue() {
            Result<Integer, String> result = Result.success(5);

            Result<String, String> mapped = result.map(x -> "Number: " + x);

            assertThat(mapped.isSuccessful()).isTrue();
            assertThat(mapped.getValueOrThrow()).isEqualTo("Number: 5");
        }

        @Test
        @DisplayName("does not transform failure")
        void doesNotTransformFailure() {
            Result<Integer, String> result = Result.failure("error");

            Result<String, String> mapped = result.map(x -> "Number: " + x);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null mapper throws NullPointerException")
        void withNullMapper_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.map(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("mapper cannot be null");
        }
    }

    @Nested
    @DisplayName("mapError()")
    class MapErrorMethod {

        @Test
        @DisplayName("transforms error value")
        void transformsErrorValue() {
            Result<String, Integer> result = Result.failure(5);

            Result<String, String> mapped = result.mapError(x -> "Error code: " + x);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.getErrorOrThrow()).isEqualTo("Error code: 5");
        }

        @Test
        @DisplayName("does not transform success")
        void doesNotTransformSuccess() {
            Result<String, Integer> result = Result.success("value");

            Result<String, String> mapped = result.mapError(x -> "Error code: " + x);

            assertThat(mapped.isSuccessful()).isTrue();
            assertThat(mapped.getValueOrThrow()).isEqualTo("value");
        }

        @Test
        @DisplayName("with null mapper throws NullPointerException")
        void withNullMapper_throwsNPE() {
            Result<String, Integer> result = Result.failure(5);

            assertThatThrownBy(() -> result.mapError(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("mapper cannot be null");
        }
    }

    @Nested
    @DisplayName("bind()")
    class BindMethod {

        @Test
        @DisplayName("chains successful Results")
        void chainsSuccessfulResults() {
            Result<Integer, String> result = Result.success(5);

            Result<Integer, String> bound = result.bind(x -> Result.success(x * 2));

            assertThat(bound.isSuccessful()).isTrue();
            assertThat(bound.getValueOrThrow()).isEqualTo(10);
        }

        @Test
        @DisplayName("propagates failure")
        void propagatesFailure() {
            Result<Integer, String> result = Result.failure("original error");

            Result<Integer, String> bound = result.bind(x -> Result.success(x * 2));

            assertThat(bound.isFailure()).isTrue();
            assertThat(bound.getErrorOrThrow()).isEqualTo("original error");
        }

        @Test
        @DisplayName("can return failure from binder")
        void canReturnFailureFromBinder() {
            Result<Integer, String> result = Result.success(5);

            Result<Integer, String> bound = result.bind(x -> Result.failure("bind error"));

            assertThat(bound.isFailure()).isTrue();
            assertThat(bound.getErrorOrThrow()).isEqualTo("bind error");
        }

        @Test
        @DisplayName("with null binder throws NullPointerException")
        void withNullBinder_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.bind(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("binder cannot be null");
        }
    }

    @Nested
    @DisplayName("tap()")
    class TapMethod {

        @Test
        @DisplayName("executes consumer for Success")
        void executesConsumerForSuccess() {
            Result<Integer, String> result = Result.success(42);
            StringBuilder sb = new StringBuilder();

            Result<Integer, String> tapped = result.tap(x -> sb.append(x));

            assertThat(sb.toString()).isEqualTo("42");
            assertThat(tapped).isSameAs(result);
        }

        @Test
        @DisplayName("does not execute consumer for Failure")
        void doesNotExecuteConsumerForFailure() {
            Result<Integer, String> result = Result.failure("error");
            StringBuilder sb = new StringBuilder();

            Result<Integer, String> tapped = result.tap(x -> sb.append(x));

            assertThat(sb.toString()).isEmpty();
            assertThat(tapped).isSameAs(result);
        }

        @Test
        @DisplayName("with null consumer throws NullPointerException")
        void withNullConsumer_throwsNPE() {
            Result<Integer, String> result = Result.success(42);

            assertThatThrownBy(() -> result.tap(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("consumer cannot be null");
        }
    }

    @Nested
    @DisplayName("tapError()")
    class TapErrorMethod {

        @Test
        @DisplayName("executes consumer for Failure")
        void executesConsumerForFailure() {
            Result<Integer, String> result = Result.failure("error");
            StringBuilder sb = new StringBuilder();

            Result<Integer, String> tapped = result.tapError(e -> sb.append(e));

            assertThat(sb.toString()).isEqualTo("error");
            assertThat(tapped).isSameAs(result);
        }

        @Test
        @DisplayName("does not execute consumer for Success")
        void doesNotExecuteConsumerForSuccess() {
            Result<Integer, String> result = Result.success(42);
            StringBuilder sb = new StringBuilder();

            Result<Integer, String> tapped = result.tapError(e -> sb.append(e));

            assertThat(sb.toString()).isEmpty();
            assertThat(tapped).isSameAs(result);
        }

        @Test
        @DisplayName("with null consumer throws NullPointerException")
        void withNullConsumer_throwsNPE() {
            Result<Integer, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.tapError(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("consumer cannot be null");
        }
    }

    @Nested
    @DisplayName("ensure()")
    class EnsureMethod {

        @Test
        @DisplayName("returns Success when predicate matches")
        void returnsSuccessWhenPredicateMatches() {
            Result<Integer, String> result = Result.success(5);

            Result<Integer, String> ensured = result.ensure(x -> x > 3, "too small");

            assertThat(ensured.isSuccessful()).isTrue();
            assertThat(ensured.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("returns Failure when predicate does not match")
        void returnsFailureWhenPredicateDoesNotMatch() {
            Result<Integer, String> result = Result.success(2);

            Result<Integer, String> ensured = result.ensure(x -> x > 3, "too small");

            assertThat(ensured.isFailure()).isTrue();
            assertThat(ensured.getErrorOrThrow()).isEqualTo("too small");
        }

        @Test
        @DisplayName("propagates existing Failure")
        void propagatesExistingFailure() {
            Result<Integer, String> result = Result.failure("original error");

            Result<Integer, String> ensured = result.ensure(x -> x > 3, "too small");

            assertThat(ensured.isFailure()).isTrue();
            assertThat(ensured.getErrorOrThrow()).isEqualTo("original error");
        }

        @Test
        @DisplayName("with null predicate throws NullPointerException")
        void withNullPredicate_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.ensure(null, "error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate cannot be null");
        }

        @Test
        @DisplayName("with null error throws NullPointerException")
        void withNullError_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.ensure(x -> x > 3, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("error cannot be null");
        }
    }

    @Nested
    @DisplayName("where()")
    class WhereMethod {

        @Test
        @DisplayName("returns Success when predicate matches")
        void returnsSuccessWhenPredicateMatches() {
            Result<Integer, String> result = Result.success(5);

            Result<Integer, String> filtered = result.where(
                x -> x > 3,
                x -> "Value " + x + " is too small"
            );

            assertThat(filtered.isSuccessful()).isTrue();
            assertThat(filtered.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("returns Failure with error from factory when predicate does not match")
        void returnsFailureWith_errorFromFactoryWhenPredicateDoesNotMatch() {
            Result<Integer, String> result = Result.success(2);

            Result<Integer, String> filtered = result.where(
                x -> x > 3,
                x -> "Value " + x + " is too small"
            );

            assertThat(filtered.isFailure()).isTrue();
            assertThat(filtered.getErrorOrThrow()).isEqualTo("Value 2 is too small");
        }

        @Test
        @DisplayName("propagates existing Failure")
        void propagatesExistingFailure() {
            Result<Integer, String> result = Result.failure("original error");

            Result<Integer, String> filtered = result.where(
                x -> x > 3,
                x -> "Value " + x + " is too small"
            );

            assertThat(filtered.isFailure()).isTrue();
            assertThat(filtered.getErrorOrThrow()).isEqualTo("original error");
        }

        @Test
        @DisplayName("with null predicate throws NullPointerException")
        void withNullPredicate_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.where(null, x -> "error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate cannot be null");
        }

        @Test
        @DisplayName("with null errorFactory throws NullPointerException")
        void withNullErrorFactory_throwsNPE() {
            Result<Integer, String> result = Result.success(5);

            assertThatThrownBy(() -> result.where(x -> x > 3, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("errorFactory cannot be null");
        }
    }

    @Nested
    @DisplayName("or()")
    class OrMethod {

        @Test
        @DisplayName("returns this for Success with value")
        void returnsThisForSuccessWithValue() {
            Result<String, String> result = Result.success("original");

            Result<String, String> orResult = result.or("default");

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("returns Success with value for Failure")
        void returnsSuccessWithValueForFailure() {
            Result<String, String> result = Result.failure("error");

            Result<String, String> orResult = result.or("default");

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("default");
        }

        @Test
        @DisplayName("returns this for Success with supplier")
        void returnsThisForSuccessWithSupplier() {
            Result<String, String> result = Result.success("original");

            Result<String, String> orResult = result.or(() -> "default");

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("calls supplier for Failure")
        void callsSupplierForFailure() {
            Result<String, String> result = Result.failure("error");

            Result<String, String> orResult = result.or(() -> "default");

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("default");
        }

        @Test
        @DisplayName("returns this for Success with Result")
        void returnsThisForSuccessWithResult() {
            Result<String, String> result = Result.success("original");

            Result<String, String> orResult = result.or(Result.success("other"));

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("returns other Result for Failure")
        void returnsOtherResultForFailure() {
            Result<String, String> result = Result.failure("error");

            Result<String, String> orResult = result.or(Result.success("other"));

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("other");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.or((Supplier<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("valueSupplier cannot be null");
        }

        @Test
        @DisplayName("with null Result throws NullPointerException")
        void withNullResult_throwsNPE() {
            Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.or((Result<String, String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("other cannot be null");
        }
    }

    @Nested
    @DisplayName("orElse()")
    class OrElseMethod {

        @Test
        @DisplayName("is alias for or with Result")
        void isAliasForOrWithResult() {
            Result<String, String> success = Result.success("original");
            Result<String, String> failure = Result.failure("error");

            assertThat(success.orElse(Result.success("other")).getValueOrThrow()).isEqualTo("original");
            assertThat(failure.orElse(Result.success("other")).getValueOrThrow()).isEqualTo("other");
        }
    }

    @Nested
    @DisplayName("orElseGet()")
    class OrElseGetMethod {

        @Test
        @DisplayName("returns this for Success")
        void returnsThisForSuccess() {
            Result<String, String> result = Result.success("original");

            Result<String, String> orResult = result.orElseGet(() -> Result.success("default"));

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("calls supplier for Failure")
        void callsSupplierForFailure() {
            Result<String, String> result = Result.failure("error");

            Result<String, String> orResult = result.orElseGet(() -> Result.success("default"));

            assertThat(orResult.isSuccessful()).isTrue();
            assertThat(orResult.getValueOrThrow()).isEqualTo("default");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.orElseGet(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("resultSupplier cannot be null");
        }
    }

    @Nested
    @DisplayName("getValueOrDefault()")
    class GetValueOrDefault {

        @Test
        @DisplayName("returns value for Success with default value")
        void returnsValueForSuccessWithDefaultValue() {
            Result<String, String> result = Result.success("value");

            String value = result.getValueOrDefault("default");

            assertThat(value).isEqualTo("value");
        }

        @Test
        @DisplayName("returns default for Failure with default value")
        void returnsDefaultForFailureWithDefaultValue() {
            Result<String, String> result = Result.failure("error");

            String value = result.getValueOrDefault("default");

            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("returns value for Success with supplier")
        void returnsValueForSuccessWithSupplier() {
            Result<String, String> result = Result.success("value");

            String value = result.getValueOrDefault(() -> "default");

            assertThat(value).isEqualTo("value");
        }

        @Test
        @DisplayName("calls supplier for Failure with supplier")
        void callsSupplierForFailureWithSupplier() {
            Result<String, String> result = Result.failure("error");

            String value = result.getValueOrDefault(() -> "default");

            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void withNullSupplier_throwsNPE() {
            Result<String, String> result = Result.failure("error");

            assertThatThrownBy(() -> result.getValueOrDefault((Supplier<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("defaultSupplier cannot be null");
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class ToOptionalMethod {

        @Test
        @DisplayName("returns Optional with value for Success")
        void returnsOptionalWithValueForSuccess() {
            Result<String, String> result = Result.success("value");

            Optional<String> optional = result.toOptional();

            assertThat(optional).isPresent().hasValue("value");
        }

        @Test
        @DisplayName("returns empty Optional for Failure")
        void returnsEmptyOptionalForFailure() {
            Result<String, String> result = Result.failure("error");

            Optional<String> optional = result.toOptional();

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromOptional()")
    class FromOptionalMethod {

        @Test
        @DisplayName("creates Success from Optional with value")
        void createsSuccessFromOptionalWithValue() {
            Result<String, String> result = Result.fromOptional(
                Optional.of("value"),
                "error"
            );

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("value");
        }

        @Test
        @DisplayName("creates Failure from empty Optional")
        void createsFailureFromEmptyOptional() {
            Result<String, String> result = Result.fromOptional(
                Optional.empty(),
                "error"
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("creates Failure with error from supplier for empty Optional")
        void createsFailureWithErrorFromSupplierForEmptyOptional() {
            Result<String, String> result = Result.fromOptional(
                Optional.empty(),
                () -> "error from supplier"
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error from supplier");
        }

        @Test
        @DisplayName("with null optional throws NullPointerException")
        void withNullOptional_throwsNPE() {
            assertThatThrownBy(() -> Result.fromOptional(null, "error"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("optional cannot be null");
        }

        @Test
        @DisplayName("with null error throws NullPointerException")
        void withNullError_throwsNPE() {
            assertThatThrownBy(() -> Result.fromOptional(Optional.of("value"), (String) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("error cannot be null");
        }

        @Test
        @DisplayName("with null errorSupplier throws NullPointerException")
        void withNullErrorSupplier_throwsNPE() {
            assertThatThrownBy(() -> Result.fromOptional(Optional.of("value"), (Supplier<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("errorSupplier cannot be null");
        }
    }

    @Nested
    @DisplayName("equals()")
    class EqualsMethod {

        @Test
        @DisplayName("returns true for same reference")
        void returnsTrueForSameReference() {
            Result<String, String> result = Result.success("value");
            assertThat(result).isSameAs(result);
        }

        @Test
        @DisplayName("returns true for equal Success Results")
        void returnsTrueForEqualSuccessResults() {
            Result<String, String> result1 = Result.success("value");
            Result<String, String> result2 = Result.success("value");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("returns true for equal Failure Results")
        void returnsTrueForEqualFailureResults() {
            Result<String, String> result1 = Result.failure("error");
            Result<String, String> result2 = Result.failure("error");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("returns false for different values")
        void returnsFalseForDifferentValues() {
            Result<String, String> result1 = Result.success("value1");
            Result<String, String> result2 = Result.success("value2");

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("returns false for Success vs Failure")
        void returnsFalseForSuccessVsFailure() {
            Result<String, String> success = Result.success("value");
            Result<String, String> failure = Result.failure("value");

            assertThat(success).isNotEqualTo(failure);
        }

        @Test
        @DisplayName("returns false for null")
        void returnsFalseForNull() {
            Result<String, String> result = Result.success("value");

            assertThat(result).isNotEqualTo(null);
        }

        @Test
        @DisplayName("returns false for different type")
        void returnsFalseForDifferentType() {
            Result<String, String> result = Result.success("value");

            assertThat(result).isNotEqualTo("value");
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCodeMethod {

        @Test
        @DisplayName("returns same hash for equal Success Results")
        void returnsSameHashForEqualSuccessResults() {
            Result<String, String> result1 = Result.success("value");
            Result<String, String> result2 = Result.success("value");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("returns same hash for equal Failure Results")
        void returnsSameHashForEqualFailureResults() {
            Result<String, String> result1 = Result.failure("error");
            Result<String, String> result2 = Result.failure("error");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("returns different hash for different values")
        void returnsDifferentHashForDifferentValues() {
            Result<String, String> result1 = Result.success("value1");
            Result<String, String> result2 = Result.success("value2");

            assertThat(result1.hashCode()).isNotEqualTo(result2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("returns Success{value} for Success")
        void returnsSuccessValueForSuccess() {
            Result<String, String> result = Result.success("hello");

            assertThat(result.toString()).isEqualTo("Success{hello}");
        }

        @Test
        @DisplayName("returns Failure{error} for Failure")
        void returnsFailureErrorForFailure() {
            Result<String, String> result = Result.failure("error");

            assertThat(result.toString()).isEqualTo("Failure{error}");
        }
    }
}
