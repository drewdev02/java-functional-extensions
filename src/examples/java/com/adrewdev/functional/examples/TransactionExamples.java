package com.adrewdev.functional.examples;

import com.adrewdev.functional.Result;
import com.adrewdev.functional.ResultTransaction;
import com.adrewdev.functional.Unit;
import com.adrewdev.functional.transaction.Transaction;
import com.adrewdev.functional.transaction.TransactionManager;
import com.adrewdev.functional.transaction.jdbc.JdbcTransactionManager;
import com.adrewdev.functional.transaction.jdbc.JdbcTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Examples of using Transaction Scope with java-functional-extensions.
 * 
 * <p>This class demonstrates various patterns for using transactions with Result monads.</p>
 * 
 * @since 1.2.0
 */
public class TransactionExamples {
    
    // Example domain classes
    static class Customer {
        private final Long id;
        private final String name;
        private final String email;
        private boolean promoted;
        
        public Customer(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.promoted = false;
        }
        
        public void promote() {
            this.promoted = true;
        }
        
        public String getEmail() {
            return email;
        }
        
        public boolean isPromoted() {
            return promoted;
        }
    }
    
    static class EmailGateway {
        public void sendNotification(String email) {
            System.out.println("Sending notification to: " + email);
        }
    }
    
    // Example 1: Basic Transaction Scope
    public static Result<Customer, String> example1_BasicTransaction(
        TransactionManager txManager, 
        Long customerId
    ) {
        return Result.from(() -> getCustomer(customerId))
            .toResult("Customer not found")
            .withTransaction(txManager, customer -> 
                Result.<Customer, String>success(customer)
                    .tap(Customer::promote)
                    .tap(c -> clearAppointments(c))
            )
            .tap(customer -> new EmailGateway().sendNotification(customer.getEmail()));
    }
    
    // Example 2: Multiple Operations in Transaction
    public static Result<Unit, String> example2_MultipleOperations(
        TransactionManager txManager,
        Customer customer1,
        Customer customer2
    ) {
        return ResultTransaction.withTransaction(
            txManager,
            () -> Result.<Unit, String>success(Unit.VALUE)
                .tap(() -> updateCustomer(customer1))
                .tap(() -> updateCustomer(customer2))
                .tap(() -> logAudit("Updated two customers"))
        );
    }
    
    // Example 3: Transaction with Type Transformation
    public static Result<String, String> example3_TypeTransformation(
        TransactionManager txManager,
        Long customerId
    ) {
        return Result.<Long, String>success(customerId)
            .withTransaction(txManager, id -> 
                Result.from(() -> getCustomer(id))
                    .toResult("Customer not found")
                    .map(Customer::getName)
            );
    }
    
    // Example 4: Automatic Rollback on Failure
    public static Result<Unit, String> example4_AutomaticRollback(
        TransactionManager txManager,
        Customer customer
    ) {
        return ResultTransaction.withTransaction(
            txManager,
            () -> Result.<Unit, String>success(Unit.VALUE)
                .tap(() -> updateCustomer(customer))
                .bind(u -> validateCustomer(customer))  // If this fails, updateCustomer is rolled back
                .tap(u -> logAudit("Customer validated and updated"))
        );
    }
    
    // Example 5: JDBC Transaction Manager
    public static Result<Unit, String> example5_JdbcTransaction(
        DataSource dataSource,
        String customerName,
        String email
    ) {
        TransactionManager txManager = new JdbcTransactionManager(dataSource);
        
        return ResultTransaction.withTransaction(
            txManager,
            () -> Result.<Unit, String>success(Unit.VALUE)
                .tap(() -> insertCustomer(dataSource, customerName, email))
                .tap(() -> logAudit("Created customer: " + customerName))
        );
    }
    
    // Example 6: Chained Operations with Early Failure
    public static Result<Customer, String> example6_EarlyFailure(
        TransactionManager txManager,
        Long customerId
    ) {
        return Result.<Long, String>success(customerId)
            .withTransaction(txManager, id -> 
                Result.from(() -> getCustomer(id))
                    .toResult("Customer not found")
                    .bind(customer -> validateCustomerStatus(customer))
                    .tap(Customer::promote)
                    .bind(c -> Result.<Customer, String>failure("Simulated failure"))  // This causes rollback
            );
    }
    
    // Example 7: Kotlin-style Usage (Java)
    public static Result<Unit, String> example7_FunctionalStyle(
        TransactionManager txManager
    ) {
        return ResultTransaction.withTransaction(
            txManager,
            () -> Result.<Unit, String>success(Unit.VALUE)
                .tap(() -> System.out.println("Step 1: Starting transaction"))
                .tap(() -> System.out.println("Step 2: Performing operations"))
                .tap(() -> System.out.println("Step 3: Committing transaction"))
        );
    }
    
    // ========================================================================
    // Helper Methods (simulated)
    // ========================================================================
    
    private static Customer getCustomer(Long id) throws Exception {
        // Simulate database lookup
        if (id == null) {
            throw new Exception("ID is null");
        }
        return new Customer(id, "John Doe", "john@example.com");
    }
    
    private static void clearAppointments(Customer customer) {
        System.out.println("Clearing appointments for: " + customer.name);
    }
    
    private static void updateCustomer(Customer customer) {
        System.out.println("Updating customer: " + customer.name);
    }
    
    private static void logAudit(String message) {
        System.out.println("AUDIT: " + message);
    }
    
    private static Result<Unit, String> validateCustomer(Customer customer) {
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            return Result.failure("Customer email is required");
        }
        return Result.success(Unit.VALUE);
    }
    
    private static Result<Customer, String> validateCustomerStatus(Customer customer) {
        if (!customer.isPromoted()) {
            return Result.failure("Customer must be promoted first");
        }
        return Result.success(customer);
    }
    
    private static void insertCustomer(DataSource dataSource, String name, String email) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO customers (name, email) VALUES (?, ?)"
             )) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert customer", e);
        }
    }
    
    // ========================================================================
    // Main method with runnable examples
    // ========================================================================
    
    public static void main(String[] args) {
        System.out.println("=== Transaction Scope Examples ===\n");
        
        // Create a mock transaction manager for demonstration
        MockTransactionManager mockTxManager = new MockTransactionManager();
        
        // Example 1: Basic Transaction
        System.out.println("Example 1: Basic Transaction");
        Result<Customer, String> result1 = example1_BasicTransaction(mockTxManager, 1L);
        System.out.println("Result: " + (result1.isSuccessful() ? "SUCCESS" : "FAILURE"));
        System.out.println("Transaction committed: " + mockTxManager.isCommitted());
        System.out.println();
        
        // Example 2: Multiple Operations
        System.out.println("Example 2: Multiple Operations");
        Customer c1 = new Customer(1L, "Customer 1", "c1@example.com");
        Customer c2 = new Customer(2L, "Customer 2", "c2@example.com");
        Result<Unit, String> result2 = example2_MultipleOperations(mockTxManager, c1, c2);
        System.out.println("Result: " + (result2.isSuccessful() ? "SUCCESS" : "FAILURE"));
        System.out.println();
        
        // Example 3: Type Transformation
        System.out.println("Example 3: Type Transformation");
        Result<String, String> result3 = example3_TypeTransformation(mockTxManager, 1L);
        System.out.println("Result: " + (result3.isSuccessful() ? "SUCCESS: " + result3.getValueOrThrow() : "FAILURE"));
        System.out.println();
        
        System.out.println("=== All Examples Completed ===");
    }
    
    // Mock Transaction Manager for demonstration
    static class MockTransactionManager implements TransactionManager {
        private boolean committed = false;
        private boolean rolledBack = false;
        
        @Override
        public Transaction begin() {
            committed = false;
            rolledBack = false;
            return new MockTransaction(this);
        }
        
        @Override
        public Transaction getCurrent() {
            return null;
        }
        
        public boolean isCommitted() {
            return committed;
        }
        
        public boolean isRolledBack() {
            return rolledBack;
        }
        
        void commit() {
            committed = true;
        }
        
        void rollback() {
            rolledBack = true;
        }
    }
    
    static class MockTransaction implements Transaction {
        private final MockTransactionManager manager;
        private boolean active = true;
        
        MockTransaction(MockTransactionManager manager) {
            this.manager = manager;
        }
        
        @Override
        public void commit() {
            if (!active) throw new IllegalStateException("Transaction not active");
            manager.commit();
            active = false;
        }
        
        @Override
        public void rollback() {
            if (!active) throw new IllegalStateException("Transaction not active");
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
