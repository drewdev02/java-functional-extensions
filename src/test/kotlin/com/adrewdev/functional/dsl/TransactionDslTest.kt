package com.adrewdev.functional.dsl

import com.adrewdev.functional.Result
import com.adrewdev.functional.Unit
import com.adrewdev.functional.transaction.Transaction
import com.adrewdev.functional.transaction.TransactionManager
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for Kotlin Transaction DSL extensions.
 * 
 * @since 1.2.0
 */
@DisplayName("Kotlin Transaction DSL")
class TransactionDslTest {
    
    @Nested
    @DisplayName("withTransaction extension")
    inner class WithTransactionExtension {
        
        @Test
        @DisplayName("commits on success")
        fun commitsOnSuccess() {
            val txManager = MockTransactionManager()
            
            val result = Result.success<String, String>("value")
                .withTransaction(txManager) { value ->
                    Result.success<String, String>("$value modified")
                }
            
            assertTrue(result.isSuccessful())
            assertEquals("value modified", result.getValueOrThrow())
            assertTrue(txManager.isCommitted)
            assertFalse(txManager.isRolledBack)
        }
        
        @Test
        @DisplayName("rolls back on failure")
        fun rollsBackOnFailure() {
            val txManager = MockTransactionManager()
            
            val result = Result.success<String, String>("value")
                .withTransaction(txManager) { _ ->
                    Result.failure<String, String>("operation failed")
                }
            
            assertFalse(result.isSuccessful())
            assertTrue(txManager.isRolledBack)
            assertFalse(txManager.isCommitted)
        }
        
        @Test
        @DisplayName("transforms value type")
        fun transformsValueType() {
            val txManager = MockTransactionManager()
            
            val result: Result<String, String> = Result.success<Int, String>(42)
                .withTransaction(txManager) { number ->
                    Result.success<String, String>("Number: $number")
                }
            
            assertTrue(result.isSuccessful())
            assertEquals("Number: 42", result.getValueOrThrow())
            assertTrue(txManager.isCommitted)
        }
        
        @Test
        @DisplayName("doesn't start transaction on initial failure")
        fun noTransactionOnInitialFailure() {
            val txManager = MockTransactionManager()
            
            val result = Result.failure<String, String>("initial error")
                .withTransaction(txManager) { value ->
                    Result.success<String, String>(value)
                }
            
            assertFalse(result.isSuccessful())
            assertFalse(txManager.isTransactionStarted)
        }
    }
    
    @Nested
    @DisplayName("resultWithTransaction function")
    inner class ResultWithTransactionFunction {
        
        @Test
        @DisplayName("commits standalone operation on success")
        fun commitsStandaloneOperationOnSuccess() {
            val txManager = MockTransactionManager()
            
            val result = resultWithTransaction<String, String>(txManager) {
                Result.success<String, String>("success")
            }
            
            assertTrue(result.isSuccessful())
            assertTrue(txManager.isCommitted)
            assertFalse(txManager.isRolledBack)
        }
        
        @Test
        @DisplayName("rolls back standalone operation on failure")
        fun rollsBackStandaloneOperationOnFailure() {
            val txManager = MockTransactionManager()
            
            val result = resultWithTransaction<String, String>(txManager) {
                Result.failure<String, String>("failed")
            }
            
            assertFalse(result.isSuccessful())
            assertFalse(txManager.isCommitted)
            assertTrue(txManager.isRolledBack)
        }
        
        @Test
        @DisplayName("works with Unit return type")
        fun worksWithUnitReturnType() {
            val txManager = MockTransactionManager()
            val operations = mutableListOf<String>()
            
            val result: Result<Unit, String> = resultWithTransaction<Unit, String>(txManager) {
                Result.success<Unit, String>(Unit.VALUE)
                    .tap { operations.add("operation1") }
                    .tap { operations.add("operation2") }
            }
            
            assertTrue(result.isSuccessful())
            assertTrue(txManager.isCommitted)
            assertEquals(listOf("operation1", "operation2"), operations)
        }
        
        @Test
        @DisplayName("rolls back on exception")
        fun rollsBackOnException() {
            val txManager = MockTransactionManager()
            
            val result = runCatching {
                resultWithTransaction<String, String>(txManager) {
                    throw RuntimeException("unexpected error")
                }
            }
            
            // Exception should be caught and wrapped in Result
            assertTrue(result.isSuccess)
            val resultValue = result.getOrNull()
            if (resultValue != null) {
                assertFalse(resultValue.isSuccessful())
                assertTrue(txManager.isRolledBack)
            }
        }
    }
    
    @Nested
    @DisplayName("Real-world scenarios")
    inner class RealWorldScenarios {
        
        @Test
        @DisplayName("multiple operations in transaction")
        fun multipleOperationsInTransaction() {
            val txManager = MockTransactionManager()
            val operations = mutableListOf<String>()
            
            val result = Result.success<String, String>("start")
                .withTransaction(txManager) { value ->
                    Result.success<String, String>(value)
                        .tap { operations.add("op1") }
                        .tap { operations.add("op2") }
                        .tap { operations.add("op3") }
                }
            
            assertTrue(result.isSuccessful())
            assertTrue(txManager.isCommitted)
            assertEquals(listOf("op1", "op2", "op3"), operations)
        }
        
        @Test
        @DisplayName("chained operations with early failure")
        fun chainedOperationsWithEarlyFailure() {
            val txManager = MockTransactionManager()
            val operations = mutableListOf<String>()
            
            val result = Result.success<String, String>("start")
                .withTransaction(txManager) { value ->
                    Result.success<String, String>(value)
                        .tap { operations.add("op1") }
                        .bind { Result.failure<String, String>("mid-operation failure") }
                        .tap { operations.add("op2") } // Should not execute
                }
            
            assertFalse(result.isSuccessful())
            assertTrue(txManager.isRolledBack)
            assertEquals(listOf("op1"), operations)
        }
        
        @Test
        @DisplayName("railway pattern with transaction")
        fun railwayPatternWithTransaction() {
            val txManager = MockTransactionManager()
            
            data class Customer(val id: Long, val name: String, val promoted: Boolean = false)
            
            fun getCustomer(id: Long): Result<Customer, String> = 
                Result.success(Customer(id, "John"))
            
            fun promoteCustomer(customer: Customer): Result<Customer, String> =
                Result.success(customer.copy(promoted = true))
            
            fun sendNotification(customer: Customer): Result<Customer, String> =
                Result.success(customer)
            
            val result = getCustomer(1L)
                .withTransaction(txManager) { customer ->
                    promoteCustomer(customer)
                        .bind { sendNotification(it) }
                }
            
            assertTrue(result.isSuccessful())
            assertTrue(result.getValueOrThrow().promoted)
            assertTrue(txManager.isCommitted)
        }
    }
    
    // ========================================================================
    // Mock Classes
    // ========================================================================
    
    /**
     * Mock TransactionManager for testing.
     */
    class MockTransactionManager : TransactionManager {
        
        var isTransactionStarted = false
            private set
        
        var isCommitted = false
            private set
        
        var isRolledBack = false
            private set
        
        override fun begin(): Transaction {
            isTransactionStarted = true
            isCommitted = false
            isRolledBack = false
            return MockTransaction(this)
        }
        
        override fun getCurrent(): Transaction? = null
        
        fun commit() {
            isCommitted = true
        }
        
        fun rollback() {
            isRolledBack = true
        }
    }
    
    /**
     * Mock Transaction for testing.
     */
    class MockTransaction(
        private val manager: MockTransactionManager
    ) : Transaction {
        
        private var active = true
        
        override fun commit() {
            check(active) { "Transaction is not active" }
            manager.commit()
            active = false
        }
        
        override fun rollback() {
            check(active) { "Transaction is not active" }
            manager.rollback()
            active = false
        }
        
        override fun isActive(): Boolean = active
        
        override fun close() {
            if (active) {
                rollback()
            }
        }
    }
}
