package com.adrewdev.functional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main class with comprehensive examples demonstrating all monads.
 * 
 * Run this to verify that java-functional-extensions works correctly.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== java-functional-extensions Examples ===\n");

        // Maybe examples
        maybeBasicExample();
        maybeWithChaining();
        maybeWithLists();

        // Result examples
        resultBasicExample();
        resultWithRailwayPattern();
        resultWithValidation();

        // MaybeAsync examples
        maybeAsyncExample();

        // ResultAsync examples
        resultAsyncExample();

        // Utilities examples
        utilitiesExample();

        System.out.println("\n=== All examples completed successfully! ===");
    }

    // ==========================================
    // MAYBE EXAMPLES
    // ==========================================

    private static void maybeBasicExample() {
        System.out.println("--- Maybe: Basic Example ---");

        // Create Maybe from value
        Maybe<String> some = Maybe.some("Hello");
        Maybe<String> none = Maybe.none();

        // Pattern matching
        some.match(
            (String value) -> System.out.println("Some: " + value),
            () -> System.out.println("None")
        );

        none.match(
            (String value) -> System.out.println("Some: " + value),
            () -> System.out.println("None: No value")
        );

        // from() handles null gracefully
        Maybe<String> fromNull = Maybe.from(null);
        System.out.println("Maybe.from(null).isNone(): " + fromNull.isNone());

        System.out.println();
    }

    private static void maybeWithChaining() {
        System.out.println("--- Maybe: Chaining Operations ---");

        class Employee {
            String email;
            String firstName;
            String lastName;
            Employee manager;

            Employee(String email, String firstName, String lastName, Employee manager) {
                this.email = email;
                this.firstName = firstName;
                this.lastName = lastName;
                this.manager = manager;
            }
        }

        Employee manager = new Employee("manager@company.com", "John", "Manager", null);
        Employee employee = new Employee("emp@company.com", "Jane", "Doe", manager);

        Maybe.from(employee)
            .tap(emp -> System.out.println("Found employee: " + emp.firstName))
            .bind(emp -> Maybe.from(emp.manager))
            .map(mgr -> mgr.email)
            .or("default@company.com")
            .match(
                (String email) -> System.out.println("Manager email: " + email),
                () -> System.out.println("No manager found")
            );

        System.out.println();
    }

    private static void maybeWithLists() {
        System.out.println("--- Maybe: List Operations ---");

        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        List<String> empty = Arrays.asList();

        // tryFirst
        Maybe.tryFirst(names)
            .match(
                (String first) -> System.out.println("First: " + first),
                () -> System.out.println("Empty list")
            );

        Maybe.tryFirst(empty)
            .match(
                (String first) -> System.out.println("First: " + first),
                () -> System.out.println("Empty list: No first element")
            );

        // tryLast
        Maybe.tryLast(names)
            .match(
                (String last) -> System.out.println("Last: " + last),
                () -> System.out.println("Empty list")
            );

        System.out.println();
    }

    // ==========================================
    // RESULT EXAMPLES
    // ==========================================

    private static void resultBasicExample() {
        System.out.println("--- Result: Basic Example ---");

        // Success
        Result<String, String> success = Result.success("Operation completed");
        success.match(
            (String value) -> System.out.println("Success: " + value),
            (String error) -> System.out.println("Error: " + error)
        );

        // Failure
        Result<String, String> failure = Result.failure("Something went wrong");
        failure.match(
            (String value) -> System.out.println("Success: " + value),
            (String error) -> System.out.println("Error: " + error)
        );

        // try_ with exception handling
        Result<Integer, String> parseResult = Result.try_(
            () -> Integer.parseInt("42"),
            error -> "Parse failed: " + error.getMessage()
        );
        parseResult.match(
            (Integer value) -> System.out.println("Parsed value: " + value),
            (String error) -> System.out.println(error)
        );

        System.out.println();
    }

    private static void resultWithRailwayPattern() {
        System.out.println("--- Result: Railway Pattern ---");

        class User {
            String email;
            boolean active;
            User(String email, boolean active) {
                this.email = email;
                this.active = active;
            }
        }

        User user = new User("user@business.com", true);

        // Railway pattern with bind
        Result<User, String> result = Result.<User, String>success(user)
            .ensure((User u) -> u.active, "User is not active")
            .ensure((User u) -> u.email.endsWith("@business.com"), "Email must be @business.com");

        Result<String, String> emailResult = result.map((User u) -> u.email);

        emailResult.match(
            (String email) -> System.out.println("Success: " + email),
            (String error) -> System.out.println("Error: " + error)
        );

        System.out.println();
    }

    private static void resultWithValidation() {
        System.out.println("--- Result: Validation ---");

        // successIf / failureIf
        Result<String, String> result1 = Result.successIf(
            true, 
            "Valid", 
            "Invalid"
        );
        result1.match(
            (String value) -> System.out.println("successIf (true): " + value),
            (String error) -> System.out.println(error)
        );

        Result<String, String> result2 = Result.failureIf(
            false,
            "Error occurred",
            "Success"
        );
        result2.match(
            (String value) -> System.out.println("failureIf (false): " + value),
            (String error) -> System.out.println(error)
        );

        // zip - combine two results
        Result<String, String> r1 = Result.success("Hello");
        Result<Integer, String> r2 = Result.success(42);
        
        Result<String, String> zipped = r1.zip(r2, (s, i) -> s + " " + i);
        zipped.match(
            (String value) -> System.out.println("Zipped: " + value),
            (String error) -> System.out.println(error)
        );

        System.out.println();
    }

    // ==========================================
    // ASYNC EXAMPLES
    // ==========================================

    private static void maybeAsyncExample() {
        System.out.println("--- MaybeAsync: Async Operations ---");

        // Simulate async fetch
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
                return "Async value";
            } catch (InterruptedException e) {
                return null;
            }
        });

        MaybeAsync<String> maybeAsync = MaybeAsync.from(future);
        
        maybeAsync
            .map(value -> value.toUpperCase())
            .tap(value -> System.out.println("Processing: " + value))
            .toCompletableFuture()
            .join()
            .match(
                (String value) -> System.out.println("Async result: " + value),
                () -> System.out.println("Async: No value")
            );

        System.out.println();
    }

    private static void resultAsyncExample() {
        System.out.println("--- ResultAsync: Async Operations ---");

        // Simulate async operation that can fail
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
                return Integer.parseInt("123");
            } catch (Exception e) {
                throw new RuntimeException("Parse failed");
            }
        });

        ResultAsync<Integer, String> resultAsync = ResultAsync.from(
            future,
            error -> "Error: " + error.getMessage()
        );

        resultAsync
            .map(value -> value * 2)
            .tap(value -> System.out.println("Processed: " + value))
            .toCompletableFuture()
            .join()
            .match(
                (Integer value) -> System.out.println("Async success: " + value),
                (String error) -> System.out.println("Async error: " + error)
            );

        System.out.println();
    }

    // ==========================================
    // UTILITIES EXAMPLES
    // ==========================================

    private static void utilitiesExample() {
        System.out.println("--- Utilities: Helper Functions ---");

        // Type helpers
        System.out.println("isDefined('test'): " + Utilities.isDefined("test"));
        System.out.println("isDefined(null): " + Utilities.isDefined(null));

        System.out.println("isSome(Maybe.some(1)): " + Utilities.isSome(Maybe.some(1)));
        System.out.println("isNone(Maybe.none()): " + Utilities.isNone(Maybe.none()));

        Result<String, String> success = Result.success("test");
        System.out.println("isSuccessful(Result): " + Utilities.isSuccessful(success));

        // Maybe utilities
        System.out.println("zeroAsNone(0): " + Utilities.zeroAsNone(0).isNone());
        System.out.println("zeroAsNone(42): " + Utilities.zeroAsNone(42).isSome());

        System.out.println("emptyStringAsNone(''): " + Utilities.emptyStringAsNone("").isNone());
        System.out.println("emptyStringAsNone('test'): " + Utilities.emptyStringAsNone("test").isSome());

        System.out.println("emptyOrWhiteSpaceStringAsNone('   '): " + 
            Utilities.emptyOrWhiteSpaceStringAsNone("   ").isNone());

        // Utility functions
        System.out.println("identity().apply('test'): " + Utilities.identity().apply("test"));
        
        Utilities.noop();
        System.out.println("noop(): executed without error");

        Utilities.noopConsumer().accept("test");
        System.out.println("noopConsumer(): executed without error");

        System.out.println();
    }
}
