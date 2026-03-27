package com.adrewdev.functional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the ResultAsync class.
 * Tests cover factory methods, transformations, pattern matching, recovery, and conversion.
 */
@DisplayName("ResultAsync")
class ResultAsyncTest {

    @Nested
    @DisplayName("from(Result)")
    class FromWithResultMethod {

        @Test
        @DisplayName("with successful Result returns Success")
        void from_withSuccessfulResult_returnsSuccess() throws Exception {
            Result<String, String> result = Result.success("test");
            ResultAsync<String, String> resultAsync = ResultAsync.from(result);
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isSuccessful()).isTrue();
            assertThat(resolved.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with failed Result returns Failure")
        void from_withFailedResult_returnsFailure() throws Exception {
            Result<String, String> result = Result.failure("error");
            ResultAsync<String, String> resultAsync = ResultAsync.from(result);
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null result throws NullPointerException")
        void from_withNullResult_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.from((Result<String, String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("result cannot be null");
        }
    }

    @Nested
    @DisplayName("from(CompletableFuture)")
    class FromWithFutureMethod {

        @Test
        @DisplayName("with successful future returns Success")
        void from_withSuccessfulFuture_returnsSuccess() throws Exception {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            ResultAsync<String, Throwable> resultAsync = ResultAsync.from(future);
            Result<String, Throwable> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isSuccessful()).isTrue();
            assertThat(resolved.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with failed future returns Failure")
        void from_withFailedFuture_returnsFailure() throws Exception {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("error"));
            ResultAsync<String, Throwable> resultAsync = ResultAsync.from(future);
            Result<String, Throwable> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("with null future throws NullPointerException")
        void from_withNullFuture_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.from((CompletableFuture<String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("future cannot be null");
        }
    }

    @Nested
    @DisplayName("from(Supplier)")
    class FromWithSupplierMethod {

        @Test
        @DisplayName("with successful supplier returning Success returns Success")
        void from_withSuccessfulSupplierReturningSuccess_returnsSuccess() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.from(() -> Result.success("test"));
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isSuccessful()).isTrue();
            assertThat(resolved.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with successful supplier returning Failure returns Failure")
        void from_withSuccessfulSupplierReturningFailure_returnsFailure() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.from(() -> Result.failure("error"));
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with throwing supplier returns Failure")
        void from_withThrowingSupplier_returnsFailure() throws Exception {
            ResultAsync<String, Throwable> resultAsync = ResultAsync.from(() -> {
                throw new RuntimeException("error");
            });
            Result<String, Throwable> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("with null supplier throws NullPointerException")
        void from_withNullSupplier_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.from((Supplier<Result<String, String>>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("supplier cannot be null");
        }
    }

    @Nested
    @DisplayName("from(CompletableFuture, Function)")
    class FromWithFutureAndErrorHandlerMethod {

        @Test
        @DisplayName("with successful future returns Success")
        void from_withSuccessfulFuture_returnsSuccess() throws Exception {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            ResultAsync<String, String> resultAsync = ResultAsync.from(
                future,
                error -> "Handled: " + error.getMessage()
            );
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isSuccessful()).isTrue();
            assertThat(resolved.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with failed future uses error handler")
        void from_withFailedFuture_usesErrorHandler() throws Exception {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("original error"));
            ResultAsync<String, String> resultAsync = ResultAsync.from(
                future,
                error -> {
                    Throwable cause = error.getCause();
                    return "Handled: " + (cause != null ? cause.getMessage() : error.getMessage());
                }
            );
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isEqualTo("Handled: original error");
        }

        @Test
        @DisplayName("with null future throws NullPointerException")
        void from_withNullFuture_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.from(
                (CompletableFuture<String>) null,
                error -> "error"
            )).isInstanceOf(NullPointerException.class)
                    .hasMessage("future cannot be null");
        }

        @Test
        @DisplayName("with null error handler throws NullPointerException")
        void from_withNullErrorHandler_throwsNPE() {
            CompletableFuture<String> future = CompletableFuture.completedFuture("test");
            assertThatThrownBy(() -> ResultAsync.from(future, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorHandler cannot be null");
        }
    }

    @Nested
    @DisplayName("success()")
    class SuccessMethod {

        @Test
        @DisplayName("with non-null value returns Success")
        void success_withValue_returnsSuccess() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isSuccessful()).isTrue();
            assertThat(resolved.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("with null value throws NullPointerException")
        void success_withNull_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.success(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    @DisplayName("failure()")
    class FailureMethod {

        @Test
        @DisplayName("with error returns Failure")
        void failure_withError_returnsFailure() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> resolved = resultAsync.toCompletableFuture().join();

            assertThat(resolved.isFailure()).isTrue();
            assertThat(resolved.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null error throws NullPointerException")
        void failure_withNull_throwsNPE() {
            assertThatThrownBy(() -> ResultAsync.failure(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("error cannot be null");
        }
    }

    @Nested
    @DisplayName("map()")
    class MapMethod {

        @Test
        @DisplayName("with Success applies function")
        void withSuccess_appliesFunction() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("hello");
            Result<Integer, String> result = resultAsync.map(String::length).toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("with Failure returns Failure")
        void withFailure_returnsFailure() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<Integer, String> result = resultAsync.map(String::length).toCompletableFuture().join();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null mapper throws NullPointerException")
        void withNullMapper_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            assertThrows(NullPointerException.class, () -> resultAsync.map(null));
        }
    }

    @Nested
    @DisplayName("mapError()")
    class MapErrorMethod {

        @Test
        @DisplayName("with Failure applies function")
        void withFailure_appliesFunction() throws Exception {
            ResultAsync<String, Integer> resultAsync = ResultAsync.failure(500);
            Result<String, String> result = resultAsync.mapError(code -> "Error code: " + code)
                    .toCompletableFuture().join();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("Error code: 500");
        }

        @Test
        @DisplayName("with Success returns Success")
        void withSuccess_returnsSuccess() throws Exception {
            ResultAsync<String, Integer> resultAsync = ResultAsync.success("value");
            Result<String, String> result = resultAsync.mapError(code -> "Error code: " + code)
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("value");
        }

        @Test
        @DisplayName("with null mapper throws NullPointerException")
        void withNullMapper_throwsNPE() {
            ResultAsync<String, Integer> resultAsync = ResultAsync.failure(500);
            assertThrows(NullPointerException.class, () -> resultAsync.mapError(null));
        }
    }

    @Nested
    @DisplayName("bind()")
    class BindMethod {

        @Test
        @DisplayName("with Success applies function")
        void withSuccess_appliesFunction() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("hello");
            Result<Integer, String> result = resultAsync.bind(s -> ResultAsync.success(s.length()))
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("with Failure returns Failure")
        void withFailure_returnsFailure() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<Integer, String> result = resultAsync.bind(s -> ResultAsync.success(s.length()))
                    .toCompletableFuture().join();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }

        @Test
        @DisplayName("with null binder throws NullPointerException")
        void withNullBinder_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            assertThrows(NullPointerException.class, () -> resultAsync.bind(null));
        }
    }

    @Nested
    @DisplayName("tap()")
    class TapMethod {

        @Test
        @DisplayName("with Success calls consumer")
        void withSuccess_callsConsumer() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            AtomicBoolean called = new AtomicBoolean(false);
            resultAsync.tap(value -> called.set(true)).toCompletableFuture().join();

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("with Failure does nothing")
        void withFailure_doesNothing() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            AtomicBoolean called = new AtomicBoolean(false);
            resultAsync.tap(value -> called.set(true)).toCompletableFuture().join();

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("with null consumer throws NullPointerException")
        void withNullConsumer_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            assertThrows(NullPointerException.class, () -> resultAsync.tap(null));
        }
    }

    @Nested
    @DisplayName("tapError()")
    class TapErrorMethod {

        @Test
        @DisplayName("with Failure calls consumer")
        void withFailure_callsConsumer() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            AtomicBoolean called = new AtomicBoolean(false);
            resultAsync.tapError(error -> called.set(true)).toCompletableFuture().join();

            assertThat(called.get()).isTrue();
        }

        @Test
        @DisplayName("with Success does nothing")
        void withSuccess_doesNothing() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("value");
            AtomicBoolean called = new AtomicBoolean(false);
            resultAsync.tapError(error -> called.set(true)).toCompletableFuture().join();

            assertThat(called.get()).isFalse();
        }

        @Test
        @DisplayName("with null consumer throws NullPointerException")
        void withNullConsumer_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            assertThrows(NullPointerException.class, () -> resultAsync.tapError(null));
        }
    }

    @Nested
    @DisplayName("ensure()")
    class EnsureMethod {

        @Test
        @DisplayName("with matching predicate returns Success")
        void withMatchingPredicate_returnsSuccess() throws Exception {
            ResultAsync<Integer, String> resultAsync = ResultAsync.success(5);
            Result<Integer, String> result = resultAsync.ensure(x -> x > 3, "too small")
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo(5);
        }

        @Test
        @DisplayName("with non-matching predicate returns Failure")
        void withNonMatchingPredicate_returnsFailure() throws Exception {
            ResultAsync<Integer, String> resultAsync = ResultAsync.success(2);
            Result<Integer, String> result = resultAsync.ensure(x -> x > 3, "too small")
                    .toCompletableFuture().join();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("too small");
        }

        @Test
        @DisplayName("with Failure returns Failure")
        void withFailure_returnsFailure() throws Exception {
            ResultAsync<Integer, String> resultAsync = ResultAsync.failure("original error");
            Result<Integer, String> result = resultAsync.ensure(x -> x > 3, "too small")
                    .toCompletableFuture().join();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("original error");
        }

        @Test
        @DisplayName("with null predicate throws NullPointerException")
        void withNullPredicate_throwsNPE() {
            ResultAsync<Integer, String> resultAsync = ResultAsync.success(5);
            assertThrows(NullPointerException.class, () -> resultAsync.ensure(null, "error"));
        }

        @Test
        @DisplayName("with null error throws NullPointerException")
        void withNullError_throwsNPE() {
            ResultAsync<Integer, String> resultAsync = ResultAsync.success(5);
            assertThrows(NullPointerException.class, () -> resultAsync.ensure(x -> x > 3, null));
        }
    }

    @Nested
    @DisplayName("recover()")
    class RecoverMethod {

        @Test
        @DisplayName("with Success returns unchanged Success")
        void withSuccess_returnsUnchangedSuccess() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("value");
            Result<String, String> result = resultAsync.recover(err -> "recovered from: " + err)
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("value");
        }

        @Test
        @DisplayName("with Failure applies recovery function")
        void withFailure_appliesRecoveryFunction() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> result = resultAsync.recover(err -> "recovered from: " + err)
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("recovered from: error");
        }

        @Test
        @DisplayName("with null recoverFn throws NullPointerException")
        void withNullRecoverFn_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            assertThrows(NullPointerException.class, () -> resultAsync.recover(null));
        }
    }

    @Nested
    @DisplayName("recoverWith()")
    class RecoverWithMethod {

        @Test
        @DisplayName("with Success returns unchanged Success")
        void withSuccess_returnsUnchangedSuccess() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("value");
            Result<String, String> result = resultAsync.recoverWith(err -> ResultAsync.success("recovered"))
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("value");
        }

        @Test
        @DisplayName("with Failure applies async recovery function")
        void withFailure_appliesAsyncRecoveryFunction() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> result = resultAsync.recoverWith(err -> ResultAsync.success("recovered from: " + err))
                    .toCompletableFuture().join();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("recovered from: error");
        }

        @Test
        @DisplayName("with null recoverFn throws NullPointerException")
        void withNullRecoverFn_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            assertThrows(NullPointerException.class, () -> resultAsync.recoverWith(null));
        }
    }

    @Nested
    @DisplayName("match(ResultAsyncMatcher)")
    class MatchWithMatcherMethod {

        @Test
        @DisplayName("with Success calls onSuccess")
        void withSuccess_callsOnSuccess() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("hello");
            String result = resultAsync.match(new ResultAsyncMatcher<String, String, String>() {
                @Override
                public String onSuccess(String value) {
                    return "Got: " + value;
                }

                @Override
                public String onFailure(String error) {
                    return "Error: " + error;
                }
            }).join();

            assertThat(result).isEqualTo("Got: hello");
        }

        @Test
        @DisplayName("with Failure calls onFailure")
        void withFailure_callsOnFailure() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            String result = resultAsync.match(new ResultAsyncMatcher<String, String, String>() {
                @Override
                public String onSuccess(String value) {
                    return "Got: " + value;
                }

                @Override
                public String onFailure(String error) {
                    return "Error: " + error;
                }
            }).join();

            assertThat(result).isEqualTo("Error: error");
        }

        @Test
        @DisplayName("with null matcher throws NullPointerException")
        void withNullMatcher_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            assertThrows(NullPointerException.class, () -> resultAsync.match((ResultAsyncMatcher<String, String, String>) null));
        }
    }

    @Nested
    @DisplayName("getValueOrThrow()")
    class GetValueOrThrowMethod {

        @Test
        @DisplayName("returns value for Success")
        void getValueOrThrow_withSuccess_returnsValue() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            String value = resultAsync.getValueOrThrow().join();

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("throws CompletionException for Failure")
        void getValueOrThrow_withFailure_throwsException() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");

            assertThatThrownBy(() -> resultAsync.getValueOrThrow().join())
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("getErrorOrThrow()")
    class GetErrorOrThrowMethod {

        @Test
        @DisplayName("returns error for Failure")
        void getErrorOrThrow_withFailure_returnsError() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error message");
            String error = resultAsync.getErrorOrThrow().join();

            assertThat(error).isEqualTo("error message");
        }

        @Test
        @DisplayName("throws CompletionException for Success")
        void getErrorOrThrow_withSuccess_throwsException() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("value");

            assertThatThrownBy(() -> resultAsync.getErrorOrThrow().join())
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("toCompletableFuture()")
    class ToCompletableFutureMethod {

        @Test
        @DisplayName("returns underlying CompletableFuture")
        void returnsUnderlyingCompletableFuture() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            CompletableFuture<Result<String, String>> future = resultAsync.toCompletableFuture();

            assertThat(future).isNotNull();
            assertThat(future.join().getValueOrThrow()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("toResult()")
    class ToResultMethod {

        @Test
        @DisplayName("converts to Result (blocking)")
        void convertsToResult() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            Result<String, String> result = resultAsync.toResult();

            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("converts Failure to Result")
        void convertsFailureToResult() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> result = resultAsync.toResult();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("toMaybe()")
    class ToMaybeMethod {

        @Test
        @DisplayName("converts Success to Some")
        void convertsSuccessToSome() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            Maybe<String> maybe = resultAsync.toMaybe();

            assertThat(maybe.isSome()).isTrue();
            assertThat(maybe.getValueOrThrow()).isEqualTo("test");
        }

        @Test
        @DisplayName("converts Failure to None")
        void convertsFailureToNone() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Maybe<String> maybe = resultAsync.toMaybe();

            assertThat(maybe.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("or(T)")
    class OrWithValueMethod {

        @Test
        @DisplayName("with Success returns original")
        void withSuccess_returnsOriginal() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("original");
            Result<String, String> result = resultAsync.or("fallback").toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("with Failure returns fallback value")
        void withFailure_returnsFallback() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> result = resultAsync.or("fallback").toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("orElse(Result)")
    class OrElseMethod {

        @Test
        @DisplayName("with Success returns original")
        void withSuccess_returnsOriginal() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("original");
            Result<String, String> result = resultAsync.orElse(Result.success("other")).toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("original");
        }

        @Test
        @DisplayName("with Failure returns fallback Result")
        void withFailure_returnsFallback() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            Result<String, String> result = resultAsync.orElse(Result.success("other")).toCompletableFuture().join();

            assertThat(result.getValueOrThrow()).isEqualTo("other");
        }

        @Test
        @DisplayName("with null Result throws NullPointerException")
        void withNullResult_throwsNPE() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            assertThrows(NullPointerException.class, () -> resultAsync.orElse(null));
        }
    }

    @Nested
    @DisplayName("getValueOrDefault(T)")
    class GetValueOrDefaultMethod {

        @Test
        @DisplayName("returns value for Success")
        void getValueOrDefault_withSuccess_returnsValue() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");
            String value = resultAsync.getValueOrDefault("default").join();

            assertThat(value).isEqualTo("test");
        }

        @Test
        @DisplayName("returns default for Failure")
        void getValueOrDefault_withFailure_returnsDefault() throws Exception {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");
            String value = resultAsync.getValueOrDefault("default").join();

            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("equals()")
    class EqualsMethod {

        @Test
        @DisplayName("returns true for same instance")
        void equals_sameInstance_returnsTrue() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("test");

            assertThat(resultAsync).isSameAs(resultAsync);
        }

        @Test
        @DisplayName("returns true for equal Success values")
        void equals_equalSuccessValues_returnsTrue() {
            ResultAsync<String, String> result1 = ResultAsync.success("test");
            ResultAsync<String, String> result2 = ResultAsync.success("test");

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("returns false for different Success values")
        void equals_differentSuccessValues_returnsFalse() {
            ResultAsync<String, String> result1 = ResultAsync.success("test1");
            ResultAsync<String, String> result2 = ResultAsync.success("test2");

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("returns true for equal Failure values")
        void equals_equalFailureValues_returnsTrue() {
            ResultAsync<String, String> failure1 = ResultAsync.failure("error");
            ResultAsync<String, String> failure2 = ResultAsync.failure("error");

            assertThat(failure1).isEqualTo(failure2);
        }

        @Test
        @DisplayName("returns false when comparing Success with Failure")
        void equals_successAndFailure_returnsFalse() {
            ResultAsync<String, String> success = ResultAsync.success("test");
            ResultAsync<String, String> failure = ResultAsync.failure("test");

            assertThat(success).isNotEqualTo(failure);
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCodeMethod {

        @Test
        @DisplayName("returns same hash for equal Results")
        void hashCode_equalResults_returnSameHash() {
            ResultAsync<String, String> result1 = ResultAsync.success("test");
            ResultAsync<String, String> result2 = ResultAsync.success("test");

            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("returns same hash for equal Failures")
        void hashCode_equalFailures_returnSameHash() {
            ResultAsync<String, String> failure1 = ResultAsync.failure("error");
            ResultAsync<String, String> failure2 = ResultAsync.failure("error");

            assertThat(failure1.hashCode()).isEqualTo(failure2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString()")
    class ToStringMethod {

        @Test
        @DisplayName("returns ResultAsync{Success{value}} for Success")
        void toString_withSuccess_returnsSuccessFormat() {
            ResultAsync<String, String> resultAsync = ResultAsync.success("hello");

            assertThat(resultAsync.toString()).isEqualTo("ResultAsync{Success{hello}}");
        }

        @Test
        @DisplayName("returns ResultAsync{Failure{error}} for Failure")
        void toString_withFailure_returnsFailureFormat() {
            ResultAsync<String, String> resultAsync = ResultAsync.failure("error");

            assertThat(resultAsync.toString()).isEqualTo("ResultAsync{Failure{error}}");
        }
    }
}
