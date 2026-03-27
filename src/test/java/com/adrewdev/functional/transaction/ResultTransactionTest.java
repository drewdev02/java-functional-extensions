package com.adrewdev.functional.transaction;

import com.adrewdev.functional.Result;
import com.adrewdev.functional.ResultTransaction;
import com.adrewdev.functional.Unit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ResultTransaction} and transaction scope functionality.
 * 
 * @since 1.2.0
 */
@DisplayName("Transaction Scope")
class ResultTransactionTest {
    
    @Nested
    @DisplayName("withTransaction - Basic")
    class WithTransactionBasic {
        
        @Test
        @DisplayName("commits on success")
        void commitsOnSuccess() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<String, String>success("value")
                .withTransaction(txManager, value -> 
                    Result.<String, String>success(value + " modified")
                );
            
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("value modified");
            assertThat(txManager.isCommitted()).isTrue();
            assertThat(txManager.isRolledBack()).isFalse();
        }
        
        @Test
        @DisplayName("rolls back on failure")
        void rollsBackOnFailure() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<String, String>success("value")
                .withTransaction(txManager, value -> 
                    Result.<String, String>failure("operation failed")
                );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("operation failed");
            assertThat(txManager.isCommitted()).isFalse();
            assertThat(txManager.isRolledBack()).isTrue();
        }
        
        @Test
        @DisplayName("rolls back on exception")
        void rollsBackOnException() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<String, String>success("value")
                .withTransaction(txManager, value -> {
                    throw new RuntimeException("unexpected error");
                });
            
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).contains("Transaction failed");
            assertThat(txManager.isRolledBack()).isTrue();
        }
        
        @Test
        @DisplayName("doesn't start transaction if initial result is failure")
        void noTransactionOnInitialFailure() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<String, String>failure("initial error")
                .withTransaction(txManager, value -> 
                    Result.<String, String>success(value)
                );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo("initial error");
            assertThat(txManager.isTransactionStarted()).isFalse();
            assertThat(txManager.isCommitted()).isFalse();
            assertThat(txManager.isRolledBack()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("withTransaction - Type Transformation")
    class WithTransactionTypeTransformation {
        
        @Test
        @DisplayName("transforms value type on success")
        void transformsValueTypeOnSuccess() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<Integer, String>success(42)
                .withTransaction(txManager, number -> 
                    Result.<String, String>success("Number: " + number)
                );
            
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("Number: 42");
            assertThat(txManager.isCommitted()).isTrue();
        }
        
        @Test
        @DisplayName("preserves error type on failure")
        void preservesErrorTypeOnFailure() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, Integer> result = Result.<String, Integer>failure(404)
                .withTransaction(txManager, value -> 
                    Result.<String, Integer>success("transformed")
                );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).isEqualTo(404);
            assertThat(txManager.isTransactionStarted()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("withTransaction - Supplier Overload")
    class WithTransactionSupplier {
        
        @Test
        @DisplayName("commits supplier operation on success")
        void commitsSupplierOperationOnSuccess() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = ResultTransaction.withTransaction(
                txManager,
                () -> Result.success("success")
            );
            
            assertThat(result.isSuccessful()).isTrue();
            assertThat(txManager.isCommitted()).isTrue();
            assertThat(txManager.isRolledBack()).isFalse();
        }
        
        @Test
        @DisplayName("rolls back supplier operation on failure")
        void rollsBackSupplierOperationOnFailure() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = ResultTransaction.withTransaction(
                txManager,
                () -> Result.failure("failed")
            );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(txManager.isCommitted()).isFalse();
            assertThat(txManager.isRolledBack()).isTrue();
        }
        
        @Test
        @DisplayName("rolls back supplier operation on exception")
        void rollsBackSupplierOperationOnException() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = ResultTransaction.withTransaction(
                txManager,
                () -> {
                    throw new RuntimeException("supplier error");
                }
            );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorOrThrow()).contains("Transaction failed");
            assertThat(txManager.isRolledBack()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Transaction - AutoCloseable")
    class TransactionAutoCloseable {
        
        @Test
        @DisplayName("automatically rolls back on try-with-resources exit without commit")
        void autoRollbackOnTryWithResources() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            try (Transaction tx = txManager.begin()) {
                // Simulate some work
                txManager.recordOperation();
                // No commit - should auto rollback
            }
            
            assertThat(txManager.isRolledBack()).isTrue();
            assertThat(txManager.isCommitted()).isFalse();
        }
        
        @Test
        @DisplayName("does not rollback after explicit commit")
        void noRollbackAfterCommit() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            try (Transaction tx = txManager.begin()) {
                txManager.recordOperation();
                tx.commit();
            }
            
            assertThat(txManager.isCommitted()).isTrue();
            assertThat(txManager.isRolledBack()).isFalse();
        }
        
        @Test
        @DisplayName("rolls back on exception in try block")
        void rollbackOnException() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            try {
                try (Transaction tx = txManager.begin()) {
                    txManager.recordOperation();
                    throw new RuntimeException("unexpected error");
                }
            } catch (RuntimeException e) {
                // Expected
            }
            
            assertThat(txManager.isRolledBack()).isTrue();
            assertThat(txManager.isCommitted()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("Real-world Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("multiple operations in transaction")
        void multipleOperationsInTransaction() {
            MockTransactionManager txManager = new MockTransactionManager();
            List<String> operations = new ArrayList<>();
            
            Result<String, String> result = Result.<String, String>success("start")
                .withTransaction(txManager, value -> 
                    Result.<String, String>success(value)
                        .tap(v -> operations.add("operation1"))
                        .tap(v -> operations.add("operation2"))
                        .tap(v -> operations.add("operation3"))
                );
            
            assertThat(result.isSuccessful()).isTrue();
            assertThat(txManager.isCommitted()).isTrue();
            assertThat(operations).containsExactly("operation1", "operation2", "operation3");
        }
        
        @Test
        @DisplayName("chained operations with early failure")
        void chainedOperationsWithEarlyFailure() {
            MockTransactionManager txManager = new MockTransactionManager();
            List<String> operations = new ArrayList<>();
            
            Result<String, String> result = Result.<String, String>success("start")
                .withTransaction(txManager, value -> 
                    Result.<String, String>success(value)
                        .tap(v -> operations.add("operation1"))
                        .bind(v -> Result.<String, String>failure("mid-operation failure"))
                        .tap(v -> operations.add("operation2")) // Should not execute
                );
            
            assertThat(result.isFailure()).isTrue();
            assertThat(txManager.isRolledBack()).isTrue();
            assertThat(operations).containsExactly("operation1");
        }
        
        @Test
        @DisplayName("nested Result operations")
        void nestedResultOperations() {
            MockTransactionManager txManager = new MockTransactionManager();
            
            Result<String, String> result = Result.<String, String>success("outer")
                .withTransaction(txManager, outerValue -> 
                    Result.<String, String>success("inner")
                        .bind(inner -> Result.<String, String>success(outerValue + " + " + inner))
                );
            
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.getValueOrThrow()).isEqualTo("outer + inner");
            assertThat(txManager.isCommitted()).isTrue();
        }
    }
    
    // ========================================================================
    // Mock Classes
    // ========================================================================
    
    /**
     * Mock TransactionManager for testing.
     */
    static class MockTransactionManager implements TransactionManager {
        
        private boolean transactionStarted = false;
        private boolean committed = false;
        private boolean rolledBack = false;
        private int operationCount = 0;
        
        @Override
        public Transaction begin() {
            transactionStarted = true;
            committed = false;
            rolledBack = false;
            return new MockTransaction(this);
        }
        
        @Override
        public Transaction getCurrent() {
            return null;
        }
        
        public boolean isTransactionStarted() { 
            return transactionStarted; 
        }
        
        public boolean isCommitted() { 
            return committed; 
        }
        
        public boolean isRolledBack() { 
            return rolledBack; 
        }
        
        public int getOperationCount() {
            return operationCount;
        }
        
        public void recordOperation() {
            operationCount++;
        }
        
        void commit() { 
            committed = true; 
        }
        
        void rollback() { 
            rolledBack = true; 
        }
    }
    
    /**
     * Mock Transaction for testing.
     */
    static class MockTransaction implements Transaction {
        
        private final MockTransactionManager manager;
        private boolean active = true;
        
        MockTransaction(MockTransactionManager manager) {
            this.manager = manager;
        }
        
        @Override
        public void commit() {
            if (!active) {
                throw new IllegalStateException("Transaction is not active");
            }
            manager.commit();
            active = false;
        }
        
        @Override
        public void rollback() {
            if (!active) {
                throw new IllegalStateException("Transaction is not active");
            }
            manager.rollback();
            active = false;
        }
        
        @Override
        public boolean isActive() {
            return active;
        }
        
        @Override
        public void close() {
            if (active) {
                rollback();
            }
        }
    }
}
