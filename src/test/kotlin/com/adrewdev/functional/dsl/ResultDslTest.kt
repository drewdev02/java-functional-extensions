package com.adrewdev.functional.dsl

import com.adrewdev.functional.Maybe
import com.adrewdev.functional.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.*

/**
 * Tests for the Kotlin Result DSL.
 */
@DisplayName("Result DSL")
class ResultDslTest {
    
    @Nested
    @DisplayName("result() builder")
    inner class ResultBuilder {
        
        @Test
        fun `with success returns Success`() {
            val result = result { 42 }
            assertTrue(result.isSuccessful())
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `with exception returns Failure`() {
            val result = result<Int> {
                throw RuntimeException("error")
            }
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("error"))
        }
        
        @Test
        fun `with custom error handler`() {
            data class CustomError(val message: String)
            
            val result = result<Int, CustomError>(
                errorHandler = { CustomError(it.message ?: "") }
            ) {
                throw RuntimeException("test")
            }
            assertTrue(result.isFailure())
            assertEquals("test", result.getErrorOrThrow().message)
        }
        
        @Test
        fun `with lambda returning null throws`() {
            // Note: Result.success() doesn't allow null values in Java implementation
            val result = result<String?> { null }
            // This will be a failure because null is not allowed
            assertTrue(result.isFailure())
        }
        
        @Test
        fun `with successful computation`() {
            val result = result {
                val x = 10
                val y = 20
                x + y
            }
            assertTrue(result.isSuccessful())
            assertEquals(30, result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("Railway pattern with bind()")
    inner class RailwayPattern {
        
        @Test
        fun `bind extracts value on success`() {
            val result = result {
                val x = result { 10 }.bind()
                val y = result { 20 }.bind()
                x + y
            }
            
            assertEquals(30, result.getValueOrThrow())
        }
        
        @Test
        fun `bind short-circuits on failure`() {
            val result = result {
                val x = result { 10 }.bind()
                val y = result<Int> { throw RuntimeException("error") }.bind()
                x + y
            }
            
            assertTrue(result.isFailure())
        }
        
        @Test
        fun `bind with Maybe inside Result block`() {
            val result = result {
                val maybeValue = Maybe.some(42)
                val value = maybeValue.bind()
                value * 2
            }
            
            assertTrue(result.isSuccessful())
            assertEquals(84, result.getValueOrThrow())
        }
        
        @Test
        fun `bind with Maybe None throws`() {
            val result = result {
                val maybeValue: Maybe<Int> = Maybe.none()
                val value = maybeValue.bind()
                value * 2
            }
            
            assertTrue(result.isFailure())
        }
        
        @Test
        fun `complex railway pattern with multiple binds`() {
            fun step1(): Result<Int, String> = result { 10 }
            fun step2(x: Int): Result<Int, String> = result { x * 2 }
            fun step3(x: Int): Result<Int, String> = result { x + 5 }
            
            val result = result {
                val a = step1().bind()
                val b = step2(a).bind()
                val c = step3(b).bind()
                c
            }
            
            assertTrue(result.isSuccessful())
            assertEquals(25, result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("ensure() validation")
    inner class EnsureValidation {
        
        @Test
        fun `ensure with true condition returns success`() {
            val result = result { 42 }
                .ensure(true, "Must be true")
            
            assertTrue(result.isSuccessful())
        }
        
        @Test
        fun `ensure with false condition returns failure`() {
            val result = result { 42 }
                .ensure(false, "Must be true")
            
            assertTrue(result.isFailure())
            assertEquals("Must be true", result.getErrorOrThrow())
        }
        
        @Test
        fun `ensure with predicate validates value`() {
            val result = result { 42 }
                .ensure({ it > 0 }, { "Must be positive: $it" })
            
            assertTrue(result.isSuccessful())
        }
        
        @Test
        fun `ensure with failing predicate returns failure`() {
            val result = result { -5 }
                .ensure({ it > 0 }, { "Must be positive: $it" })
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("-5"))
        }
        
        @Test
        fun `ensure on failure returns original failure`() {
            val result = result<Int> { throw RuntimeException("original") }
                .ensure(true, "new error")
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("original"))
        }
        
        @Test
        fun `ensure with string validation`() {
            val result = result { "hello@test.com" }
                .ensure(
                    { it.endsWith("@test.com") },
                    { "Invalid email domain: $it" }
                )
            
            assertTrue(result.isSuccessful())
        }
        
        @Test
        fun `ensure with failing string validation`() {
            val result = result { "hello@other.com" }
                .ensure(
                    { it.endsWith("@test.com") },
                    { "Invalid email domain: $it" }
                )
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("hello@other.com"))
        }
    }
    
    @Nested
    @DisplayName("map() and flatMap()")
    inner class MapAndFlatMap {
        
        @Test
        fun `map transforms success value`() {
            val result = result { "hello" }
                .map { it.uppercase() }
            
            assertEquals("HELLO", result.getValueOrThrow())
        }
        
        @Test
        fun `map on failure returns failure unchanged`() {
            val result = result<Int> { throw RuntimeException("error") }
                .map { it * 2 }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("error"))
        }
        
        @Test
        fun `flatMap chains Result-returning functions`() {
            val result = result { "hello" }
                .flatMap { result { it.length } }
            
            assertEquals(5, result.getValueOrThrow())
        }
        
        @Test
        fun `flatMap on failure returns failure unchanged`() {
            val result = result<Int> { throw RuntimeException("error") }
                .flatMap { result { it * 2 } }
            
            assertTrue(result.isFailure())
        }
        
        @Test
        fun `map chains multiple transformations`() {
            val result = result { "hello" }
                .map { it.uppercase() }
                .map { it.length }
                .map { it * 2 }
            
            assertEquals(10, result.getValueOrThrow())
        }
        
        @Test
        fun `flatMap with error propagation`() {
            val result = result { "hello" }
                .flatMap { _: String -> result<Int> { throw RuntimeException("chain error") } }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("chain error"))
        }
    }
    
    @Nested
    @DisplayName("recover()")
    inner class Recover {
        
        @Test
        fun `recover with success returns original`() {
            val result = result { 42 }
                .recover { 0 }
            
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `recover with failure returns fallback`() {
            val result = result<Int> { throw RuntimeException() }
                .recover { 42 }
            
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `recover receives error value`() {
            val result = result<Int> { throw RuntimeException("test error") }
                .recover { error ->
                    assertEquals("test error", error)
                    100
                }
            
            assertEquals(100, result.getValueOrThrow())
        }
        
        @Test
        fun `recoverWith with success returns original`() {
            val result = result { 42 }
                .recoverWith { result { 0 } }
            
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `recoverWith with failure returns fallback Result`() {
            val result = result<Int> { throw RuntimeException() }
                .recoverWith { result { 42 } }
            
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `recoverWith can return failure`() {
            val result = result<Int> { throw RuntimeException("original") }
                .recoverWith { result { throw RuntimeException("recovered error") } }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("recovered error"))
        }
    }
    
    @Nested
    @DisplayName("tapSuccess() and tapFailure()")
    inner class TapExtensions {
        
        @Test
        fun `tapSuccess performs side effect on success`() {
            var tapped = false
            result { 42 }
                .tapSuccess { tapped = true }
            
            assertTrue(tapped)
        }
        
        @Test
        fun `tapSuccess does not perform side effect on failure`() {
            var tapped = false
            result<Int> { throw RuntimeException() }
                .tapSuccess { tapped = true }
            
            assertFalse(tapped)
        }
        
        @Test
        fun `tapFailure performs side effect on failure`() {
            var tapped = false
            result<Int> { throw RuntimeException() }
                .tapFailure { tapped = true }
            
            assertTrue(tapped)
        }
        
        @Test
        fun `tapFailure does not perform side effect on success`() {
            var tapped = false
            result { 42 }
                .tapFailure { tapped = true }
            
            assertFalse(tapped)
        }
        
        @Test
        fun `tapSuccess captures value`() {
            var captured: Int? = null
            result { 42 }
                .tapSuccess { captured = it }
            
            assertEquals(42, captured)
        }
        
        @Test
        fun `tapFailure captures error`() {
            var captured: String? = null
            result<Int> { throw RuntimeException("test error") }
                .tapFailure { captured = it }
            
            assertTrue(captured?.contains("test error") == true)
        }
        
        @Test
        fun `tapSuccess returns original Result`() {
            val original = result { 42 }
            val result = original.tapSuccess { }
            
            assertEquals(original, result)
        }
        
        @Test
        fun `tapFailure returns original Result`() {
            val original = result<Int> { throw RuntimeException() }
            val result = original.tapFailure { }
            
            assertEquals(original, result)
        }
    }
    
    @Nested
    @DisplayName("mapError()")
    inner class MapError {
        
        @Test
        fun `mapError transforms failure value`() {
            val result = result<Int> { throw RuntimeException("error") }
                .mapError { it.uppercase() }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("ERROR"))
        }
        
        @Test
        fun `mapError on success returns success unchanged`() {
            val result = result { 42 }
                .mapError { "transformed" }
            
            assertTrue(result.isSuccessful())
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `mapError changes error type`() {
            val result = result<Int> { throw RuntimeException("test") }
                .mapError { "Error: ${it.toString()}" }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("Error:"))
        }
        
        @Test
        fun `mapError with string error`() {
            val result = result<Int> { throw RuntimeException("test") }
                .mapError { it.toString() }
            
            assertTrue(result.isFailure())
            assertTrue(result.getErrorOrThrow().contains("test"))
        }
    }
    
    @Nested
    @DisplayName("Pattern matching with when")
    inner class PatternMatching {
        
        @Test
        fun `matches Success`() {
            val result = result { 42 }.toResultK()
            
            val value = when (result) {
                is ResultK.Success -> result.value
                is ResultK.Failure -> 0
            }
            
            assertEquals(42, value)
        }
        
        @Test
        fun `matches Failure`() {
            val result = result<Int> { throw RuntimeException("error") }.toResultK()
            
            val error = when (result) {
                is ResultK.Success -> "none"
                is ResultK.Failure -> result.error
            }
            
            assertTrue(error.toString().contains("error"))
        }
        
        @Test
        fun `matches Success with string`() {
            val result = result { "hello" }.toResultK()
            
            val output = when (result) {
                is ResultK.Success -> "Got: ${result.value}"
                is ResultK.Failure -> "Error: ${result.error}"
            }
            
            assertEquals("Got: hello", output)
        }
        
        @Test
        fun `matches Failure with custom error`() {
            data class ApiError(val code: Int, val message: String)
            
            val result: Result<String, ApiError> = Result.failure(ApiError(500, "Server error"))
            val resultK = result.toResultK()
            
            val output = when (resultK) {
                is ResultK.Success -> "Data: ${resultK.value}"
                is ResultK.Failure -> "API Error ${resultK.error.code}: ${resultK.error.message}"
            }
            
            assertEquals("API Error 500: Server error", output)
        }
        
        @Test
        fun `pattern matching in when expression with transformation`() {
            val result = result { 10 }.toResultK()
            
            val transformed = when (result) {
                is ResultK.Success -> result.value * 2
                is ResultK.Failure -> -1
            }
            
            assertEquals(20, transformed)
        }
        
        @Test
        fun `pattern matching with Failure returns error`() {
            val result = result<Int> { throw RuntimeException("test") }.toResultK()
            
            val errorMessage = when (result) {
                is ResultK.Success -> "Success: ${result.value}"
                is ResultK.Failure -> "Failure: ${result.error}"
            }
            
            assertTrue(errorMessage.contains("Failure"))
            assertTrue(errorMessage.contains("test"))
        }
    }
    
    @Nested
    @DisplayName("toResult() extension")
    inner class ToResultExtension {
        
        @Test
        fun `toResult with success returns Success`() {
            val operation = { 42 }
            val result = operation.toResult { it.message ?: "error" }
            
            assertTrue(result.isSuccessful())
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `toResult with exception returns Failure`() {
            val operation = { throw RuntimeException("test error") }
            val result = operation.toResult { it.message ?: "error" }
            
            assertTrue(result.isFailure())
            assertEquals("test error", result.getErrorOrThrow())
        }
        
        @Test
        fun `toResult with custom error type`() {
            data class AppError(val message: String, val code: Int)
            
            val operation = { throw RuntimeException("database error") }
            val result = operation.toResult { 
                AppError(it.message ?: "unknown", 500) 
            }
            
            assertTrue(result.isFailure())
            assertEquals("database error", result.getErrorOrThrow().message)
            assertEquals(500, result.getErrorOrThrow().code)
        }
    }
    
    @Nested
    @DisplayName("Complete railway example")
    inner class CompleteExample {
        
        @Test
        fun `complete railway pattern example`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            
            val getUser = { id: Int -> Result.success<User, String>(User(id, "user@test.com", true)) }
            
            val emailResult = result({ e: Throwable -> e.message ?: "error" }) {
                val user = getUser(1).bind()
                Result.success<Unit, String>(Unit).ensure(user.active, "User not active")
                Result.success<Unit, String>(Unit).ensure(user.email.endsWith("@test.com"), "Invalid email domain")
                user.email
            }
            
            assertTrue(emailResult.isSuccessful())
            assertEquals("user@test.com", emailResult.getValueOrThrow())
        }
        
        @Test
        fun `railway pattern with inactive user`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            val inactiveUser = User(2, "inactive@test.com", false)
            
            val emailResult = Result.success<User, String>(inactiveUser)
                .ensure({ it.active }, { "User not active" })
                .map { it.email }
            
            assertTrue(emailResult.isFailure())
            assertEquals("User not active", emailResult.getErrorOrThrow())
        }
        
        @Test
        fun `railway pattern with invalid email domain`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            val userWithBadEmail = User(3, "user@other.com", true)
            
            val emailResult = Result.success<User, String>(userWithBadEmail)
                .ensure({ it.active }, { "User not active" })
                .ensure({ it.email.endsWith("@test.com") }, { "Invalid email domain" })
                .map { it.email }
            
            assertTrue(emailResult.isFailure())
            assertEquals("Invalid email domain", emailResult.getErrorOrThrow())
        }
        
        @Test
        fun `railway pattern with multiple steps`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            
            fun getUser(id: Int): Result<User, String> {
                return Result.success(User(id, "user@test.com", true))
            }
            
            fun validateEmail(email: String): Result<String, String> {
                return Result.success<String, String>(email).ensure(email.contains("@"), "Invalid email format")
            }
            
            fun sendEmail(email: String): Result<String, String> {
                return Result.success("Email sent to $email")
            }
            
            val result = result({ e: Throwable -> e.message ?: "error" }) {
                val user = getUser(1).bind()
                val email = validateEmail(user.email).bind()
                sendEmail(email).bind()
            }
            
            assertTrue(result.isSuccessful())
            assertEquals("Email sent to user@test.com", result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("Integration tests")
    inner class IntegrationTests {
        
        @Test
        fun `complex chain with map flatMap and recover`() {
            val result = result { "hello" }
                .map { it.uppercase() }
                .flatMap { result { it.length } }
                .recover { 0 }
            
            assertEquals(5, result.getValueOrThrow())
        }
        
        @Test
        fun `chain with tap for logging`() {
            val log = mutableListOf<String>()
            
            val result = result { "test" }
                .tapSuccess { log.add("Before map: $it") }
                .map { str: String -> str.uppercase() }
                .tapSuccess { log.add("After map: $it") }
            
            assertEquals("TEST", result.getValueOrThrow())
            assertEquals(2, log.size)
            assertEquals("Before map: test", log[0])
            assertEquals("After map: TEST", log[1])
        }
        
        @Test
        fun `chain with recover for fallback`() {
            val result = result<Int> { throw RuntimeException() }
                .recover { 42 }
                .map { it * 2 }
            
            assertEquals(84, result.getValueOrThrow())
        }
        
        @Test
        fun `failure propagates through entire chain`() {
            var tapCalled = false
            var mapCalled = false
            
            val result = result<Int> { throw RuntimeException() }
                .tapSuccess { tapCalled = true }
                .map { 
                    mapCalled = true
                    it * 2 
                }
                .recover { 100 }
            
            assertEquals(100, result.getValueOrThrow())
            assertFalse(tapCalled)
            assertFalse(mapCalled)
        }
        
        @Test
        fun `ensure with map and flatMap chain`() {
            val result = result { "hello" }
                .ensure({ it.length > 3 }, { "Too short" })
                .map { it.uppercase() }
                .flatMap { result { it.length } }
            
            assertEquals(5, result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {
        
        @Test
        fun `empty string is valid Success`() {
            val result = result { "" }
            assertTrue(result.isSuccessful())
            assertEquals("", result.getValueOrThrow())
        }
        
        @Test
        fun `zero is valid Success`() {
            val result = result { 0 }
            assertTrue(result.isSuccessful())
            assertEquals(0, result.getValueOrThrow())
        }
        
        @Test
        fun `false is valid Success`() {
            val result = result { false }
            assertTrue(result.isSuccessful())
            assertFalse(result.getValueOrThrow())
        }
        
        @Test
        fun `null value throws in success`() {
            // Note: Result.success() doesn't allow null values in Java implementation
            val result = result<String?> { null }
            // This will be a failure because null is not allowed
            assertTrue(result.isFailure())
        }
        
        @Test
        fun `exception with null message uses default`() {
            val result = result<Int> { 
                throw object : RuntimeException() {
                    override val message: String? = null
                }
            }
            
            assertTrue(result.isFailure())
            assertEquals("Unknown error", result.getErrorOrThrow())
        }
        
        @Test
        fun `nested Result can be flattened with flatMap`() {
            val nested = result { result { "inner" } }
            val flattened = nested.flatMap { it }
            
            assertEquals("inner", flattened.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("Interoperability with Java Result")
    inner class JavaInterop {
        
        @Test
        fun `Kotlin DSL works with Java Result methods`() {
            val kotlinResult = result { "hello" }
            
            // Use Java map method
            val javaMapped = kotlinResult.map { it.uppercase() }
            assertEquals("HELLO", javaMapped.getValueOrThrow())
        }
        
        @Test
        fun `Java Result can be converted to ResultK`() {
            val javaResult: Result<Int, String> = Result.success(42)
            val resultK = javaResult.toResultK()
            
            val value = when (resultK) {
                is ResultK.Success -> resultK.value
                is ResultK.Failure -> 0
            }
            
            assertEquals(42, value)
        }
        
        @Test
        fun `Kotlin bind works with Java Result`() {
            val javaResult: Result<Int, String> = Result.success(42)
            
            val kotlinResult = result {
                javaResult.bind() * 2
            }
            
            assertEquals(84, kotlinResult.getValueOrThrow())
        }
    }
}
