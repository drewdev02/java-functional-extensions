package com.adrewdev.functional.dsl

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.util.concurrent.CompletableFuture
import kotlin.test.*

/**
 * User data class for testing.
 */
private data class TestUser(val id: Int, val email: String)

/**
 * Tests for Kotlin Coroutines DSL.
 * 
 * Verifies the correct behavior of async builders and extensions
 * for MaybeAsync and ResultAsync with coroutines support.
 */
@DisplayName("Coroutines DSL")
class CoroutinesDslTest {

    // ========================================================================
    // MaybeAsync Builder Tests
    // ========================================================================

    @Nested
    @DisplayName("maybeAsync() builder")
    inner class MaybeAsyncBuilder {

        @Test
        fun `with suspending function returns Some`() = runBlocking {
            val maybe = maybeAsync {
                delay(10)
                "hello"
            }

            val result = maybe.await()
            assertTrue(result.isSome(), "Expected Some but got None")
            assertEquals("hello", result.getValueOrThrow())
        }

        @Test
        fun `with null value returns None`() = runBlocking {
            val maybe = maybeAsync<String> {
                delay(10)
                null
            }

            val result = maybe.await()
            assertTrue(result.isNone(), "Expected None but got Some")
        }

        @Test
        fun `with CompletableFuture wraps correctly`() = runBlocking {
            val future = CompletableFuture<String>()
            future.complete("world")

            val maybe = maybeAsync(future)
            val result = maybe.await()

            assertTrue(result.isSome(), "Expected Some but got None")
            assertEquals("world", result.getValueOrThrow())
        }

        @Test
        fun `with CompletableFuture that completes exceptionally returns None`() = runBlocking {
            val future = CompletableFuture<String>()
            future.completeExceptionally(RuntimeException("error"))

            val maybe = maybeAsync(future)
            val result = maybe.await()

            assertTrue(result.isNone(), "Expected None for exceptional future")
        }

        @Test
        fun `with complex suspending operation`() = runBlocking {
            data class TestUser(val id: Int, val name: String)

            suspend fun fetchUser(id: Int): TestUser? {
                delay(10)
                return TestUser(id, "User $id")
            }

            val maybe = maybeAsync {
                fetchUser(42)
            }

            val result = maybe.await()
            assertTrue(result.isSome(), "Expected Some but got None")
            assertEquals(42, result.getValueOrThrow().id)
            assertEquals("User 42", result.getValueOrThrow().name)
        }

        @Test
        fun `with multiple async operations`() = runBlocking {
            val maybe1 = maybeAsync {
                delay(10)
                10
            }

            val maybe2 = maybeAsync {
                delay(10)
                20
            }

            val result1 = maybe1.awaitGetValue()
            val result2 = maybe2.awaitGetValue()

            assertEquals(30, result1 + result2)
        }
    }

    // ========================================================================
    // ResultAsync Builder Tests
    // ========================================================================

    @Nested
    @DisplayName("resultAsync() builder")
    inner class ResultAsyncBuilder {

        @Test
        fun `with suspending success`() = runBlocking {
            val result = resultAsync {
                delay(10)
                42
            }

            val awaitResult = result.await()
            assertTrue(awaitResult.isSuccessful(), "Expected Success but got Failure")
            assertEquals(42, awaitResult.getValueOrThrow())
        }

        @Test
        fun `with suspending exception`() = runBlocking {
            val result = resultAsync<Int> {
                delay(10)
                throw RuntimeException("error")
            }

            val awaitResult = result.await()
            assertTrue(awaitResult.isFailure(), "Expected Failure but got Success")
            assertTrue(awaitResult.getErrorOrThrow().contains("error"))
        }

        @Test
        fun `with custom error handler`() = runBlocking {
            data class CustomError(val message: String, val code: Int)

            val result = resultAsync<Int, CustomError>(
                errorHandler = { CustomError(it.message ?: "Unknown", 500) }
            ) {
                delay(10)
                throw RuntimeException("test error")
            }

            val awaitResult = result.await()
            assertTrue(awaitResult.isFailure(), "Expected Failure but got Success")
            assertEquals("test error", awaitResult.getErrorOrThrow().message)
            assertEquals(500, awaitResult.getErrorOrThrow().code)
        }

        @Test
        fun `with null exception message uses default`() = runBlocking {
            val result = resultAsync<Int> {
                delay(10)
                throw RuntimeException()
            }

            val awaitResult = result.await()
            assertTrue(awaitResult.isFailure(), "Expected Failure but got Success")
            assertEquals("Unknown error", awaitResult.getErrorOrThrow())
        }

        @Test
        fun `with complex suspending operation`() = runBlocking {
            suspend fun fetchData(): String {
                delay(10)
                return "data"
            }

            val result = resultAsync {
                fetchData()
            }

            val awaitResult = result.await()
            assertTrue(awaitResult.isSuccessful(), "Expected Success but got Failure")
            assertEquals("data", awaitResult.getValueOrThrow())
        }

        @Test
        fun `with multiple async operations`() = runBlocking {
            val result1 = resultAsync {
                delay(10)
                10
            }

            val result2 = resultAsync {
                delay(10)
                20
            }

            val value1 = result1.awaitGetValue()
            val value2 = result2.awaitGetValue()

            assertEquals(30, value1 + value2)
        }
    }

    // ========================================================================
    // await() Extension Tests
    // ========================================================================

    @Nested
    @DisplayName("await() extensions")
    inner class AwaitExtensions {

        @Test
        fun `await on MaybeAsync`() = runBlocking {
            val maybe = maybeAsync {
                delay(10)
                "hello"
            }

            val result = maybe.await()
            assertEquals("hello", result.getValueOrThrow())
        }

        @Test
        fun `awaitGetValue on MaybeAsync`() = runBlocking {
            val value = maybeAsync {
                delay(10)
                42
            }.awaitGetValue()

            assertEquals(42, value)
        }

        @Test
        fun `awaitGetValue on MaybeAsync with None throws`() = runBlocking {
            val maybe = maybeAsync<String> {
                delay(10)
                null
            }

            assertFailsWith<NoSuchElementException> {
                maybe.awaitGetValue()
            }
        }

        @Test
        fun `await on ResultAsync`() = runBlocking {
            val result = resultAsync {
                delay(10)
                "success"
            }

            val awaitResult = result.await()
            assertEquals("success", awaitResult.getValueOrThrow())
        }

        @Test
        fun `awaitGetValue on ResultAsync`() = runBlocking {
            val value = resultAsync {
                delay(10)
                42
            }.awaitGetValue()

            assertEquals(42, value)
        }

        @Test
        fun `awaitGetValue on ResultAsync with failure throws`() = runBlocking {
            val result = resultAsync<Int> {
                delay(10)
                throw RuntimeException("error")
            }

            assertFailsWith<NoSuchElementException> {
                result.awaitGetValue()
            }
        }

        @Test
        fun `awaitGetError on ResultAsync`() = runBlocking {
            val error = resultAsync<Int> {
                delay(10)
                throw RuntimeException("test error")
            }.awaitGetError()

            assertTrue(error.contains("test error"))
        }

        @Test
        fun `awaitGetError on ResultAsync with success throws`() = runBlocking {
            val result = resultAsync {
                delay(10)
                42
            }

            assertFailsWith<NoSuchElementException> {
                result.awaitGetError()
            }
        }
    }

    // ========================================================================
    // Railway Pattern with Async Tests
    // ========================================================================

    @Nested
    @DisplayName("Railway pattern with async")
    inner class AsyncRailwayPattern {

        @Test
        fun `bindAsync chains async operations`() = runBlocking {
            val result = resultAsync<Int> {
                val x = resultAsync { 10 }.bindAsync { resultAsync { it + 5 } }.awaitGetValue()
                x
            }

            val value = result.awaitGetValue()
            assertEquals(15, value)
        }

        @Test
        fun `bindAsync short-circuits on failure`() = runBlocking {
            val result = resultAsync<Int> {
                resultAsync { 10 }.bindAsync {
                    resultAsync<Int> { throw RuntimeException("error") }
                }.awaitGetValue()
            }

            assertTrue(result.await().isFailure(), "Expected Failure due to short-circuit")
        }

        @Test
        fun `bindAsync with multiple chains`() = runBlocking {
            val a = resultAsync { 10 }.bindAsync { resultAsync { it * 2 } }.awaitGetValue()
            val b = resultAsync { 5 }.bindAsync { resultAsync { it + 3 } }.awaitGetValue()
            
            assertEquals(28, a + b) // (10 * 2) + (5 + 3) = 20 + 8 = 28
        }

        @Test
        fun `bindAsync with initial failure`() = runBlocking {
            val intermediate = resultAsync<Int> {
                throw RuntimeException("initial error")
            }.bindAsync { resultAsync { it + 5 } }

            val result = intermediate.await()
            assertTrue(result.isFailure(), "Expected Failure from initial error")
        }

        @Test
        fun `bindAsync with complex async operations`() = runBlocking {
            data class TestUser(val id: Int, val email: String)
            data class TestEmail(val address: String, val validated: Boolean)

            suspend fun fetchUser(id: Int): TestUser {
                delay(10)
                return TestUser(id, "user@test.com")
            }

            suspend fun validateEmail(email: String): TestEmail {
                delay(10)
                return TestEmail(email, true)
            }

            val result = resultAsync<String> {
                val user = resultAsync { fetchUser(1) }
                    .bindAsync { u ->
                        resultAsync { validateEmail(u.email) }
                    }
                    .awaitGetValue()

                user.address
            }

            val email = result.awaitGetValue()
            assertEquals("user@test.com", email)
        }
    }

    // ========================================================================
    // CompletableFuture Extension Tests
    // ========================================================================

    @Nested
    @DisplayName("CompletableFuture extensions")
    inner class CompletableFutureExtensions {

        @Test
        fun `await on CompletableFuture`() = runBlocking {
            val future = CompletableFuture<String>()
            future.complete("hello")

            val result = future.await()
            assertEquals("hello", result)
        }

        @Test
        fun `await on CompletableFuture with exception`() = runBlocking {
            val future = CompletableFuture<String>()
            future.completeExceptionally(RuntimeException("error"))

            assertFailsWith<RuntimeException> {
                future.await()
            }
        }

        @Test
        fun `awaitMaybe on CompletableFuture`() = runBlocking {
            val future = CompletableFuture<String>()
            future.complete("world")

            val maybe = future.awaitMaybe()
            assertTrue(maybe.isSome(), "Expected Some but got None")
            assertEquals("world", maybe.getValueOrThrow())
        }

        @Test
        fun `awaitMaybe on CompletableFuture with exception`() = runBlocking {
            val future = CompletableFuture<String>()
            future.completeExceptionally(RuntimeException("error"))

            assertFailsWith<RuntimeException> {
                future.awaitMaybe()
            }
        }

        @Test
        fun `awaitResult on CompletableFuture with success`() = runBlocking {
            val future = CompletableFuture<Int>()
            future.complete(42)

            val result = future.awaitResult()
            assertTrue(result.isSuccessful(), "Expected Success but got Failure")
            assertEquals(42, result.getValueOrThrow())
        }

        @Test
        fun `awaitResult on CompletableFuture with failure`() = runBlocking {
            val future = CompletableFuture<Int>()
            future.completeExceptionally(RuntimeException("error"))

            val result = future.awaitResult()
            assertTrue(result.isFailure(), "Expected Failure but got Success")
            assertTrue(result.getErrorOrThrow().contains("error"))
        }

        @Test
        fun `awaitResult with custom error handler`() = runBlocking {
            val future = CompletableFuture<Int>()
            future.completeExceptionally(RuntimeException("test"))

            val result = future.awaitResult { e -> "Custom: ${e.message}" }
            assertTrue(result.isFailure(), "Expected Failure but got Success")
            assertEquals("Custom: test", result.getErrorOrThrow())
        }
    }

    // ========================================================================
    // Real-World Examples Tests
    // ========================================================================

    @Nested
    @DisplayName("Real-world examples")
    inner class RealWorldExamples {

        private suspend fun fetchUser(id: Int): TestUser {
            delay(10)
            return TestUser(id, "user@test.com")
        }

        private suspend fun fetchEmail(userId: Int): String {
            delay(10)
            return "user@test.com"
        }

        @Test
        fun `simple async railway example`() = runBlocking {
            val user = resultAsync { fetchUser(1) }.awaitGetValue()
            val result = resultAsync { user.email }

            assertTrue(result.await().isSuccessful())
            assertEquals("user@test.com", result.awaitGetValue())
        }

        @Test
        fun `async railway with bindAsync`() = runBlocking {
            val result = resultAsync<Int> {
                resultAsync { 10 }.bindAsync { value ->
                    resultAsync { value * 2 }
                }.awaitGetValue()
            }

            val value = result.awaitGetValue()
            assertEquals(20, value)
        }

        @Test
        fun `async maybe chaining`() = runBlocking {
            val result = maybeAsync {
                val user = maybeAsync { fetchUser(1) }.awaitGetValue()
                user.email
            }

            val email = result.awaitGetValue()
            assertEquals("user@test.com", email)
        }

        @Test
        fun `async maybe with null propagation`() = runBlocking {
            suspend fun findUser(id: Int): TestUser? {
                delay(10)
                return null // Simulate not found
            }

            val result = maybeAsync {
                findUser(1) // Returns null
            }

            assertTrue(result.await().isNone())
        }

        @Test
        fun `async result with exception handling`() = runBlocking {
            suspend fun riskyOperation(): String {
                delay(10)
                throw RuntimeException("Something went wrong")
            }

            val result = resultAsync {
                riskyOperation()
            }

            assertTrue(result.await().isFailure())
            assertTrue(result.awaitGetError().contains("Something went wrong"))
        }
    }

    // ========================================================================
    // Concurrency and Thread Safety Tests
    // ========================================================================

    @Nested
    @DisplayName("Concurrency and thread safety")
    inner class ConcurrencyTests {

        @Test
        fun `concurrent async operations`() = runBlocking {
            val results = (1..10).map { i ->
                async {
                    resultAsync {
                        delay(10)
                        i * 2
                    }.awaitGetValue()
                }
            }

            val values = results.awaitAll()
            assertEquals((1..10).map { it * 2 }, values.sorted())
        }

        @Test
        fun `parallel maybe operations`() = runBlocking {
            val maybes = (1..5).map { i ->
                async {
                    maybeAsync {
                        delay(10)
                        "value $i"
                    }.awaitGetValue()
                }
            }

            val values = maybes.awaitAll()
            assertEquals(5, values.size)
            assertTrue(values.all { it.startsWith("value ") }, "All values should start with 'value '")
        }

        @Test
        fun `async operations with coroutine context`() = runBlocking {
            val result = withContext(Dispatchers.Default) {
                resultAsync {
                    delay(10)
                    "executed"
                }.awaitGetValue()
            }

            assertEquals("executed", result)
        }
    }
}
