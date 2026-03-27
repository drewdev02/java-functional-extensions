package com.adrewdev.functional.dsl

import com.adrewdev.functional.Maybe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.*

/**
 * Tests for the Kotlin Maybe DSL.
 */
@DisplayName("Maybe DSL")
class MaybeDslTest {
    
    @Nested
    @DisplayName("maybe() builder")
    inner class MaybeBuilder {
        
        @Test
        fun `with non-null value returns Some`() {
            val maybe = maybe { "hello" }
            assertTrue(maybe.isSome())
            assertEquals("hello", maybe.getValueOrThrow())
        }
        
        @Test
        fun `with null value returns None`() {
            val maybe = maybe { null }
            assertTrue(maybe.isNone())
        }
        
        @Test
        fun `with direct value returns Some`() {
            val maybe = maybe("hello")
            assertTrue(maybe.isSome())
            assertEquals("hello", maybe.getValueOrThrow())
        }
        
        @Test
        fun `with lambda that returns null returns None`() {
            fun returnsNull(): String? = null
            val maybe = maybe { returnsNull() }
            assertTrue(maybe.isNone())
        }
        
        @Test
        fun `with lambda that returns value returns Some`() {
            fun returnsValue(): String? = "value"
            val maybe = maybe { returnsValue() }
            assertTrue(maybe.isSome())
            assertEquals("value", maybe.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("toMaybe() extension")
    inner class ToMaybeExtension {
        
        @Test
        fun `with non-null returns Some`() {
            val maybe = "hello".toMaybe()
            assertTrue(maybe.isSome())
            assertEquals("hello", maybe.getValueOrThrow())
        }
        
        @Test
        fun `with null returns None`() {
            val maybe: String? = null
            val result = maybe.toMaybe()
            assertTrue(result.isNone())
        }
        
        @Test
        fun `with integer value returns Some`() {
            val maybe = 42.toMaybe()
            assertTrue(maybe.isSome())
            assertEquals(42, maybe.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("map() extension")
    inner class MapExtension {
        
        @Test
        fun `transforms value`() {
            val result = maybe { "hello" }
                .map { str: String -> str.uppercase() }
            
            assertEquals("HELLO", result.getValueOrThrow())
        }
        
        @Test
        fun `with None returns None`() {
            val result: Maybe<String> = maybe { null }
                .map { str: String -> str.uppercase() }
            
            assertTrue(result.isNone())
        }
        
        @Test
        fun `transforms integer to string`() {
            val result = maybe(42)
                .map { num: Int -> "Number: $num" }
            
            assertEquals("Number: 42", result.getValueOrThrow())
        }
        
        @Test
        fun `chains multiple maps`() {
            val result = maybe { "hello" }
                .map { str: String -> str.uppercase() }
                .map { str: String -> str.length }
            
            assertEquals(5, result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("bind() extension")
    inner class BindExtension {
        
        @Test
        fun `chains Maybe-returning functions`() {
            val result = maybe { "hello" }
                .bind { str: String -> maybe { str.length } }
            
            assertEquals(5, result.getValueOrThrow())
        }
        
        @Test
        fun `with None returns None`() {
            val result: Maybe<Int> = maybe { null }
                .bind { str: String -> maybe { str.length } }
            
            assertTrue(result.isNone())
        }
        
        @Test
        fun `chains multiple binds`() {
            val result = maybe { "hello" }
                .bind { str: String -> maybe { str.length } }
                .bind { num: Int -> maybe { num * 2 } }
            
            assertEquals(10, result.getValueOrThrow())
        }
        
        @Test
        fun `bind that returns None propagates None`() {
            val result = maybe { "hello" }
                .bind { _: String -> maybe { null } }
            
            assertTrue(result.isNone())
        }
    }
    
    @Nested
    @DisplayName("tap() extension")
    inner class TapExtension {
        
        @Test
        fun `performs side effect`() {
            var tapped = false
            maybe { "hello" }
                .tap { tapped = true }
            
            assertTrue(tapped)
        }
        
        @Test
        fun `does not perform side effect on None`() {
            var tapped = false
            maybe { null }
                .tap { tapped = true }
            
            assertFalse(tapped)
        }
        
        @Test
        fun `returns original Maybe unchanged`() {
            val original = maybe { "hello" }
            val result = original.tap { }
            
            assertEquals(original, result)
        }
        
        @Test
        fun `captures value in side effect`() {
            var captured: String? = null
            maybe { "hello" }
                .tap { captured = it }
            
            assertEquals("hello", captured)
        }
    }
    
    @Nested
    @DisplayName("or() extension")
    inner class OrExtension {
        
        @Test
        fun `with Some returns original`() {
            val result = maybe { "hello" }
                .or { "world" }
            
            assertEquals("hello", result.getValueOrThrow())
        }
        
        @Test
        fun `with None returns fallback`() {
            val result = Maybe.none<String>()
                .or { "world" }
            
            assertEquals("world", result.getValueOrThrow())
        }
        
        @Test
        fun `with direct value and Some returns original`() {
            val result = maybe { "hello" }
                .or("world")
            
            assertEquals("hello", result.getValueOrThrow())
        }
        
        @Test
        fun `with direct value and None returns fallback`() {
            val result = Maybe.none<String>()
                .or("world")
            
            assertEquals("world", result.getValueOrThrow())
        }
        
        @Test
        fun `lazy evaluation - supplier not called for Some`() {
            var called = false
            val result = maybe { "hello" }
                .or { 
                    called = true
                    "world"
                }
            
            assertFalse(called)
            assertEquals("hello", result.getValueOrThrow())
        }
        
        @Test
        fun `lazy evaluation - supplier called for None`() {
            var called = false
            val result = Maybe.none<String>()
                .or { 
                    called = true
                    "world"
                }
            
            assertTrue(called)
            assertEquals("world", result.getValueOrThrow())
        }
    }
    
    @Nested
    @DisplayName("Pattern matching with when")
    inner class PatternMatching {
        
        @Test
        fun `matches Some`() {
            val maybe = maybe { "hello" }.toMaybeK()
            
            val result = when (maybe) {
                is MaybeK.Some -> maybe.value
                MaybeK.None -> "none"
            }
            
            assertEquals("hello", result)
        }
        
        @Test
        fun `matches None`() {
            val maybe = maybe { null }.toMaybeK()
            
            val result = when (maybe) {
                is MaybeK.Some -> maybe.value
                MaybeK.None -> "none"
            }
            
            assertEquals("none", result)
        }
        
        @Test
        fun `matches Some with integer`() {
            val maybe = maybe(42).toMaybeK()
            
            val result = when (maybe) {
                is MaybeK.Some -> maybe.value * 2
                MaybeK.None -> 0
            }
            
            assertEquals(84, result)
        }
        
        @Test
        fun `matches None in complex expression`() {
            val maybe: Maybe<String> = Maybe.none()
            val maybeK: MaybeK<String> = maybe.toMaybeK()
            
            val result = when (maybeK) {
                is MaybeK.Some -> "Got: ${maybeK.value}"
                MaybeK.None -> "No value"
            }
            
            assertEquals("No value", result)
        }
        
        @Test
        fun `pattern matching preserves type safety`() {
            val maybe: MaybeK<String> = maybe { "hello" }.toMaybeK()
            
            // This should compile without errors
            val length: Int = when (maybe) {
                is MaybeK.Some -> maybe.value.length
                MaybeK.None -> -1
            }
            
            assertEquals(5, length)
        }
        
        @Test
        fun `matches Some - explicit type`() {
            val maybe: MaybeK<String> = maybe { "hello" }.toMaybeK()
            
            val result = when (maybe) {
                is MaybeK.Some -> maybe.value
                MaybeK.None -> "none"
            }
            
            assertEquals("hello", result)
        }
    }
    
    @Nested
    @DisplayName("Integration tests")
    inner class IntegrationTests {
        
        @Test
        fun `complex chain with map and bind`() {
            data class User(val name: String, val age: Int)
            
            fun findUser(id: Int): User? = User("John", 30)
            fun getAge(user: User): Int? = user.age
            
            val result = maybe { findUser(1) }
                .bind { user: User -> maybe { getAge(user) } }
                .map { age: Int -> "Age: $age" }
            
            assertEquals("Age: 30", result.getValueOrThrow())
        }
        
        @Test
        fun `chain with tap for logging`() {
            val log = mutableListOf<String>()
            
            val result = maybe { "test" }
                .tap { log.add("Before map: $it") }
                .map { str: String -> str.uppercase() }
                .tap { log.add("After map: $it") }
            
            assertEquals("TEST", result.getValueOrThrow())
            assertEquals(2, log.size)
            assertEquals("Before map: test", log[0])
            assertEquals("After map: TEST", log[1])
        }
        
        @Test
        fun `chain with or for fallback`() {
            val result = Maybe.none<String>()
                .or { "default" }
                .map { str: String -> str.uppercase() }
            
            assertEquals("DEFAULT", result.getValueOrThrow())
        }
        
        @Test
        fun `None propagates through entire chain`() {
            var tapCalled = false
            var mapCalled = false
            
            val result = Maybe.none<String>()
                .tap { tapCalled = true }
                .map { str: String -> 
                    mapCalled = true
                    str.uppercase() 
                }
                .or("fallback")
            
            assertEquals("fallback", result.getValueOrThrow())
            assertFalse(tapCalled)
            assertFalse(mapCalled)
        }
    }
    
    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {
        
        @Test
        fun `empty string is valid Some`() {
            val maybe = maybe("")
            assertTrue(maybe.isSome())
            assertEquals("", maybe.getValueOrThrow())
        }
        
        @Test
        fun `zero is valid Some`() {
            val maybe = maybe(0)
            assertTrue(maybe.isSome())
            assertEquals(0, maybe.getValueOrThrow())
        }
        
        @Test
        fun `false is valid Some`() {
            val maybe = maybe(false)
            assertTrue(maybe.isSome())
            assertFalse(maybe.getValueOrThrow())
        }
        
        @Test
        fun `nested Maybe can be flattened with bind`() {
            val nested = maybe { maybe { "inner" } }
            val flattened = nested.bind { m: Maybe<String> -> m }
            
            assertEquals("inner", flattened.getValueOrThrow())
        }
    }
}
