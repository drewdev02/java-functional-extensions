package com.adrewdev.functional.dsl

import com.adrewdev.functional.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.*

/**
 * Tests for the Advanced Railway Pattern DSL.
 * 
 * Tests Arrow Raise-style syntax for railway pattern
 * with bind(), ensure(), and advanced combinators.
 * 
 * @since 1.5.0
 */
@DisplayName("Railway DSL Advanced")
class RailwayDslTest {
    
    @Nested
    @DisplayName("resultScope() with railway scope")
    inner class RailwayScope {
        
        @Test
        fun `bind extracts value on success`() {
            val result: Result<Int, String> = resultScope {
                val x = Result.success<Int, String>(10).bind()
                val y = Result.success<Int, String>(20).bind()
                x + y
            }
            
            assertTrue(result.isSuccessful())
            assertEquals(30, result.getValueOrThrow())
        }
        
        @Test
        fun `bind short-circuits on failure`() {
            val result: Result<Int, String> = resultScope {
                val x = Result.success<Int, String>(10).bind()
                val y = Result.failure<Int, String>("error").bind()
                x + y
            }
            
            assertTrue(result.isFailure())
            assertEquals("error", result.getErrorOrThrow())
        }
        
        @Test
        fun `ensure validates condition`() {
            val result: Result<Int, String> = resultScope {
                val x = Result.success<Int, String>(42).bind()
                ensure(x > 0, "Must be positive")
                x
            }
            
            assertTrue(result.isSuccessful())
            assertEquals(42, result.getValueOrThrow())
        }
        
        @Test
        fun `ensure short-circuits on false`() {
            val result: Result<Int, String> = resultScope {
                val x = Result.success<Int, String>(-5).bind()
                ensure(x > 0, "Must be positive")
                x
            }
            
            assertTrue(result.isFailure())
            assertEquals("Must be positive", result.getErrorOrThrow())
        }
        
        @Test
        fun `ensure with value predicate`() {
            val result: Result<String, String> = resultScope {
                val email = Result.success<String, String>("user@test.com").bind()
                email.ensure({ it.endsWith("@test.com") }) { 
                    "Invalid domain: $it" 
                }
                email
            }
            
            assertTrue(result.isSuccessful())
            assertEquals("user@test.com", result.getValueOrThrow())
        }
        
        @Test
        fun `ensure with failing value predicate`() {
            val result: Result<String, String> = resultScope {
                val email = Result.success<String, String>("user@other.com").bind()
                email.ensure({ it.endsWith("@test.com") }) { 
                    "Invalid domain: $it" 
                }
                email
            }
            
            assertTrue(result.isFailure())
            assertEquals("Invalid domain: user@other.com", result.getErrorOrThrow())
        }
        
        @Test
        fun `multiple binds in sequence`() {
            val result: Result<Int, String> = resultScope {
                val a = Result.success<Int, String>(1).bind()
                val b = Result.success<Int, String>(2).bind()
                val c = Result.success<Int, String>(3).bind()
                a + b + c
            }
            
            assertTrue(result.isSuccessful())
            assertEquals(6, result.getValueOrThrow())
        }
        
        @Test
        fun `first failure short-circuits rest`() {
            var secondBindCalled = false
            
            val result: Result<Int, String> = resultScope {
                val a = Result.success<Int, String>(1).bind()
                val b = Result.failure<Int, String>("first error").bind()
                secondBindCalled = true
                val c = Result.success<Int, String>(3).bind()
                a + b + c
            }
            
            assertTrue(result.isFailure())
            assertEquals("first error", result.getErrorOrThrow())
            assertFalse(secondBindCalled)
        }
    }
    
    @Nested
    @DisplayName("Complete railway examples")
    inner class CompleteExamples {
        
        @Test
        fun `complete user email example with string errors`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            
            val emailResult: Result<String, String> = resultScope {
                val user = Result.success<User, String>(User(1, "user@test.com", true)).bind()
                ensure(user.active, "User not active")
                ensure(user.email.endsWith("@test.com"), "Invalid email domain")
                user.email
            }
            
            assertTrue(emailResult.isSuccessful())
            assertEquals("user@test.com", emailResult.getValueOrThrow())
        }
        
        @Test
        fun `error propagation with inactive user`() {
            data class User(val id: Int, val email: String, val active: Boolean)
            
            val emailResult: Result<String, String> = resultScope {
                val user = Result.success<User, String>(User(2, "inactive@test.com", false)).bind()
                ensure(user.active, "User not active")
                user.email
            }
            
            assertTrue(emailResult.isFailure())
            assertEquals("User not active", emailResult.getErrorOrThrow())
        }
    }
    
    @Nested
    @DisplayName("zip() combinator")
    inner class ZipCombinator {
        
        @Test
        fun `zip combines two successes into pair`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.success(42)
            
            val zipped = r1.zip(r2)
            
            assertTrue(zipped.isSuccessful())
            assertEquals("hello" to 42, zipped.getValueOrThrow())
        }
        
        @Test
        fun `zip with first failure`() {
            val r1: Result<String, String> = Result.failure("error1")
            val r2: Result<Int, String> = Result.success(42)
            
            val zipped = r1.zip(r2)
            
            assertTrue(zipped.isFailure())
            assertEquals("error1", zipped.getErrorOrThrow())
        }
        
        @Test
        fun `zip with second failure`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.failure("error2")
            
            val zipped = r1.zip(r2)
            
            assertTrue(zipped.isFailure())
            assertEquals("error2", zipped.getErrorOrThrow())
        }
        
        @Test
        fun `zip with transform function`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.success(5)
            val result = r1.zip(r2) { s: String, i: Int -> "$s has $i letters" }
            
            assertTrue(result.isSuccessful())
            assertEquals("hello has 5 letters", result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("zip() with three results")
    inner class ZipThreeResults {
        
        @Test
        fun `zip three successes into triple`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.success(42)
            val r3: Result<Boolean, String> = Result.success(true)
            
            val zipped = zip(r1, r2, r3)
            
            assertTrue(zipped.isSuccessful())
            assertEquals(Triple("hello", 42, true), zipped.getValueOrThrow())
        }
        
        @Test
        fun `zip three with first failure`() {
            val r1: Result<String, String> = Result.failure("error1")
            val r2: Result<Int, String> = Result.success(42)
            val r3: Result<Boolean, String> = Result.success(true)
            
            val zipped = zip(r1, r2, r3)
            
            assertTrue(zipped.isFailure())
            assertEquals("error1", zipped.getErrorOrThrow())
        }
        
        @Test
        fun `zip three with transform function`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.success(5)
            val r3: Result<String, String> = Result.success("!")
            val result = zip(r1, r2, r3) { s: String, i: Int, p: String -> "$s has $i letters$p" }
            
            assertTrue(result.isSuccessful())
            assertEquals("hello has 5 letters!", result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("all() combinator")
    inner class AllCombinator {
        
        @Test
        fun `all with all successes`() {
            val results: List<Result<Int, String>> = listOf(
                Result.success(1),
                Result.success(2),
                Result.success(3)
            )
            
            val all = all(results)
            
            assertTrue(all.isSuccessful())
            assertEquals(listOf(1, 2, 3), all.getValueOrThrow())
        }
        
        @Test
        fun `all with one failure`() {
            val results: List<Result<Int, String>> = listOf(
                Result.success(1),
                Result.failure("error"),
                Result.success(3)
            )
            
            val all = all(results)
            
            assertTrue(all.isFailure())
            assertEquals("error", all.getErrorOrThrow())
        }
        
        @Test
        fun `all with empty list`() {
            val results: List<Result<Int, String>> = emptyList()
            
            val all = all(results)
            
            assertTrue(all.isSuccessful())
            assertEquals(emptyList<Int>(), all.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("any() combinator")
    inner class AnyCombinator {
        
        @Test
        fun `any returns first success`() {
            val results: List<Result<Int, String>> = listOf(
                Result.failure("error1"),
                Result.success(42),
                Result.success(100)
            )
            
            val any = any(results)
            
            assertTrue(any.isSuccessful())
            assertEquals(42, any.getValueOrThrow())
        }
        
        @Test
        fun `any returns last failure when all fail`() {
            val results: List<Result<Int, String>> = listOf(
                Result.failure("error1"),
                Result.failure("error2")
            )
            
            val any = any(results)
            
            assertTrue(any.isFailure())
            assertEquals("error2", any.getErrorOrThrow())
        }
        
        @Test
        fun `any with single success`() {
            val results: List<Result<Int, String>> = listOf(
                Result.success(42)
            )
            
            val any = any(results)
            
            assertTrue(any.isSuccessful())
            assertEquals(42, any.getValueOrThrow())
        }
        
        @Test
        fun `any with empty list returns failure`() {
            val results: List<Result<Int, String>> = emptyList()
            
            val any = any(results)
            
            assertTrue(any.isFailure())
        }
    }
    
    @Nested
    @DisplayName("Integration tests")
    inner class IntegrationTests {
        
        @Test
        fun `railway pattern with zip and map`() {
            val r1: Result<String, String> = Result.success("hello")
            val r2: Result<Int, String> = Result.success(5)
            
            val result = r1.zip(r2) { s: String, i: Int -> "$s has $i letters" }
            
            assertTrue(result.isSuccessful())
            assertEquals("hello has 5 letters", result.getValueOrThrow())
        }
        
        @Test
        fun `all with map chain`() {
            val results: List<Result<Int, String>> = listOf(
                Result.success(1),
                Result.success(2),
                Result.success(3)
            )
            
            val result = all(results)
                .map { list -> list.map { it * 2 } }
            
            assertTrue(result.isSuccessful())
            assertEquals(listOf(2, 4, 6), result.getValueOrThrow())
        }
        
        @Test
        fun `any with map chain`() {
            val results: List<Result<Int, String>> = listOf(
                Result.failure("error1"),
                Result.success(42),
                Result.success(100)
            )
            
            val result = any(results)
                .map { it * 2 }
            
            assertTrue(result.isSuccessful())
            assertEquals(84, result.getValueOrThrow())
        }
    }
}
