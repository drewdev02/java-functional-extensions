package com.adrewdev.functional.dsl.examples

import com.adrewdev.functional.Maybe
import com.adrewdev.functional.Result
import com.adrewdev.functional.dsl.*
import kotlinx.coroutines.*

/**
 * Complete examples of Kotlin DSL usage.
 *
 * This file demonstrates all features of the Kotlin DSL
 * with real-world examples.
 *
 * @since 1.1.0
 */
object KotlinExamples {

    /**
     * Domain model for examples.
     */
    data class User(
        val id: Int,
        val email: String,
        val active: Boolean,
        val managerId: Int? = null
    )

    /**
     * Error types for railway pattern examples.
     */
    sealed class Error {
        data class NotFound(val id: Int) : Error()
        data class Inactive(val id: Int) : Error()
        data class InvalidEmail(val email: String) : Error()
        data class DatabaseError(val message: String) : Error()
    }

    // ========================================================================
    // Example 1: Basic Maybe Usage
    // ========================================================================

    /**
     * Demonstrates basic Maybe DSL operations.
     *
     * Shows:
     * - Creating Maybe with builder function
     * - Extension functions (map, or, tap)
     * - Pattern matching with when
     */
    fun maybeExample() {
        // Create Maybe from nullable value
        val maybeUser = maybe { getUserNullable(1) }

        // Chain operations
        val email = maybeUser
            .map { it.email }
            .or("default@email.com")
            .getValueOrThrow()

        println("Email: $email")

        // Pattern matching
        when (val result = maybeUser.toMaybeK()) {
            is MaybeK.Some -> println("User: ${result.value.email}")
            MaybeK.None -> println("No user found")
        }

        // Match function
        maybeUser.match(
            { user -> println("Found user: ${user.id}") },
            { println("User not found") }
        )
    }

    // ========================================================================
    // Example 2: Railway Pattern with Result DSL
    // ========================================================================

    /**
     * Demonstrates railway pattern with Result DSL.
     *
     * Shows:
     * - Result builder with exception handling
     * - bind() for chaining operations
     * - ensure() for validation
     * - Pattern matching
     */
    fun resultExample() {
        val emailResult = resultScope<String, Error> {
            val user = getUser(1).bind()
            ensure(user.active, Error.Inactive(user.id))
            ensure(user.email.endsWith("@test.com"), Error.InvalidEmail(user.email))
            user.email
        }

        // Pattern matching
        when (val result = emailResult.toResultK()) {
            is ResultK.Success -> println("Email: ${result.value}")
            is ResultK.Failure -> {
                when (val error = result.error) {
                    is Error.Inactive -> println("Inactive user: ${error.id}")
                    is Error.InvalidEmail -> println("Invalid email: ${error.email}")
                    else -> println("Error: $error")
                }
            }
        }

        // Match function
        emailResult.match(
            { println("Success: $it") },
            { println("Failure: $it") }
        )
    }

    // ========================================================================
    // Example 3: Coroutines with Async Operations
    // ========================================================================

    /**
     * Demonstrates coroutines support with async operations.
     *
     * Shows:
     * - maybeAsync builder
     * - resultAsync builder
     * - await() for suspending extraction
     * - Async railway pattern
     */
    suspend fun coroutinesExample() = runBlocking {
        // Async Maybe
        val maybeUser = maybeAsync { fetchUser(1) }
        val user = maybeUser.awaitGetValue()
        println("Fetched user: ${user.id}")

        // Async Result with railway pattern
        val emailResult = resultAsync(
            { e: Throwable ->
                when (e) {
                    is IllegalStateException -> Error.Inactive(0)
                    else -> Error.DatabaseError(e.message ?: "Unknown error")
                }
            },
            {
                val user = fetchUser(1)
                if (!user.active) throw IllegalStateException("Inactive user")
                user.email
            }
        )

        // Await result
        val email = emailResult.awaitGetValue()
        println("Email: $email")

        // Pattern matching on async result
        when (val result = emailResult.await().toResultK()) {
            is ResultK.Success -> println("Success: ${result.value}")
            is ResultK.Failure -> println("Error: ${result.error}")
        }
    }

    // ========================================================================
    // Example 4: Railway DSL with resultScope
    // ========================================================================

    /**
     * Demonstrates Arrow Raise-style syntax with resultScope.
     *
     * Shows:
     * - resultScope builder
     * - bind() for short-circuiting
     * - ensure() for validation
     * - Type-safe error handling
     */
    fun railwayExample(): Result<String, Error> {
        // Arrow Raise-style syntax
        val email = resultScope<String, Error> {
            val user = getUser(1).bind()
            ensure(user.active, Error.Inactive(user.id))
            ensure(user.email.endsWith("@test.com"), Error.InvalidEmail(user.email))
            user.email
        }

        // Pattern matching
        when (val result = email.toResultK()) {
            is ResultK.Success -> println("Email: ${result.value}")
            is ResultK.Failure -> println("Error: ${result.error}")
        }

        return email
    }

    // ========================================================================
    // Example 5: Combinators
    // ========================================================================

    /**
     * Demonstrates Result combinators.
     *
     * Shows:
     * - zip() for combining two results
     * - all() for combining list of results
     * - any() for first success
     */
    fun combinatorExample() {
        val result1 = result { "hello" }
        val result2 = result { "world" }
        val result3: Result<String, String> = Result.failure("error")

        // zip - combine two results
        val zipped = result1.zip(result2) { a, b -> "$a $b" }
        zipped.match(
            { println("Zipped: $it") },
            { println("Zip failed: $it") }
        )

        // all - all must succeed
        val allResults = com.adrewdev.functional.dsl.all(listOf(result1, result2))
        allResults.match(
            { println("All: $it") },
            { println("All failed: $it") }
        )

        // any - first success wins
        val anyResult = com.adrewdev.functional.dsl.any(listOf(result3, result1, result2))
        anyResult.match(
            { println("Any: $it") },
            { println("Any failed: $it") }
        )

        // Triple zip
        val r1 = result { "hello" }
        val r2 = result { "42" }
        val r3 = result { "true" }
        val triple = zip(r1, r2, r3) { a, b, c -> "$a $b $c" }
        triple.match(
            { println("Triple: $it") },
            { println("Triple failed: $it") }
        )
    }

    // ========================================================================
    // Example 6: Complete Real-World Example
    // ========================================================================

    /**
     * Complete real-world example combining all features.
     *
     * Shows:
     * - Full railway pattern with error types
     * - Coroutines integration
     * - Error handling and recovery
     * - Pattern matching
     */
    suspend fun completeExample() = runBlocking {
        val emailResult = resultAsync(
            { e: Throwable ->
                when (e) {
                    is IllegalArgumentException -> Error.InvalidEmail(e.message ?: "")
                    is IllegalStateException -> Error.Inactive(0)
                    else -> Error.DatabaseError(e.message ?: "Unknown error")
                }
            },
            {
                val user = fetchUser(1)
                if (!user.active) throw IllegalStateException("Inactive user: ${user.id}")
                validateEmail(user.email)
            }
        )

        when (val result = emailResult.await().toResultK()) {
            is ResultK.Success -> println("Email: ${result.value}")
            is ResultK.Failure -> when (val error = result.error) {
                is Error.NotFound -> println("Not found: ${error.id}")
                is Error.Inactive -> println("Inactive: ${error.id}")
                is Error.InvalidEmail -> println("Invalid: ${error.email}")
                is Error.DatabaseError -> println("Database: ${error.message}")
            }
        }
    }

    // ========================================================================
    // Example 7: Maybe Extensions
    // ========================================================================

    /**
     * Demonstrates Maybe extension functions.
     *
     * Shows:
     * - toMaybe() extension
     * - tap() for side effects
     * - or() with lambda
     * - bind() for chaining
     */
    fun maybeExtensionsExample() {
        // toMaybe extension
        val nullableValue: String? = "hello"
        val maybe = nullableValue.toMaybe()
        maybe.match(
            { println("Value: $it") },
            { println("No value") }
        )

        // tap for side effects
        maybe
            .tap { println("Processing: $it") }
            .map { it.uppercase() }
            .tap { println("Result: $it") }

        // or with lazy evaluation
        val result = maybe.or { "default" }
        println("Result: ${result.getValueOrThrow()}")

        // bind for chaining
        val chained = maybe.bind { value ->
            maybe { value.length }
        }
        println("Length: ${chained.getValueOrThrow()}")
    }

    // ========================================================================
    // Example 8: Error Handling Strategies
    // ========================================================================

    /**
     * Demonstrates different error handling strategies.
     *
     * Shows:
     * - recover() for fallback values
     * - recoverWith() for fallback Results
     * - mapError() for error transformation
     */
    fun errorHandlingExample() {
        val failure = Result.failure<String, Error>(Error.NotFound(1))

        // recover - provide fallback value
        val recovered = failure.recover { error ->
            when (error) {
                is Error.NotFound -> "default@email.com"
                else -> "error@email.com"
            }
        }
        println("Recovered: ${recovered.getValueOrThrow()}")

        // recoverWith - provide fallback Result
        val recoveredWith = failure.recoverWith { error ->
            when (error) {
                is Error.NotFound -> Result.success<String, Error>("fallback@email.com")
                else -> Result.failure(Error.DatabaseError("Unknown"))
            }
        }
        println("RecoveredWith: ${recoveredWith.getValueOrThrow()}")

        // mapError - transform error type
        val mappedError = failure.mapError { error ->
            "Error: ${error::class.simpleName}"
        }
        println("MappedError: ${mappedError.getErrorOrThrow()}")
    }

    // ========================================================================
    // Example 9: Java Interoperability
    // ========================================================================

    /**
     * Demonstrates Java interoperability.
     *
     * Shows:
     * - Converting between Java and Kotlin types
     * - Using Java Maybe/Result from Kotlin
     * - Pattern matching on Java types
     */
    fun javaInteropExample() {
        // Java Maybe from Kotlin
        val javaMaybe: Maybe<String> = Maybe.from("hello")
        val kotlinMaybe = javaMaybe.toMaybeK()
        when (kotlinMaybe) {
            is MaybeK.Some -> println("Java Maybe value: ${kotlinMaybe.value}")
            MaybeK.None -> println("Java Maybe is None")
        }

        // Java Result from Kotlin
        val javaResult: Result<String, String> = Result.success("world")
        val kotlinResult = javaResult.toResultK()
        when (kotlinResult) {
            is ResultK.Success -> println("Java Result value: ${kotlinResult.value}")
            is ResultK.Failure -> println("Java Result error: ${kotlinResult.error}")
        }
    }

    // ========================================================================
    // Helper Functions
    // ========================================================================

    private fun getUser(id: Int): Result<User, Error> = Result.success(User(id, "user@test.com", true))

    private fun getUserNullable(id: Int): User? {
        return if (id > 0) User(id, "user@test.com", true) else null
    }

    private suspend fun fetchUser(id: Int): User = withContext(Dispatchers.IO) {
        delay(100) // Simulate network delay
        getUser(id).bind()
    }

    private fun validateEmail(email: String): String {
        require(email.endsWith("@test.com")) { "Invalid domain" }
        return email
    }
}

/**
 * Main function to run all examples.
 */
fun main() = runBlocking {
    println("=== Kotlin DSL Examples ===\n")

    println("1. Maybe Example:")
    KotlinExamples.maybeExample()
    println()

    println("2. Result Example:")
    KotlinExamples.resultExample()
    println()

    println("3. Coroutines Example:")
    KotlinExamples.coroutinesExample()
    println()

    println("4. Railway Example:")
    KotlinExamples.railwayExample()
    println()

    println("5. Combinator Example:")
    KotlinExamples.combinatorExample()
    println()

    println("6. Complete Example:")
    KotlinExamples.completeExample()
    println()

    println("7. Maybe Extensions Example:")
    KotlinExamples.maybeExtensionsExample()
    println()

    println("8. Error Handling Example:")
    KotlinExamples.errorHandlingExample()
    println()

    println("9. Java Interop Example:")
    KotlinExamples.javaInteropExample()
    println()

    println("=== All Examples Complete ===")
}
