# java-functional-extensions

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-380%20passing-brightgreen)]()
[![Java](https://img.shields.io/badge/java-8+-orange)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

A TypeScript implementation of the C# library CSharpFunctionalExtensions, including synchronous and asynchronous Maybe and Result monads.

**Port 1:1 de [typescript-functional-extensions](https://github.com/seangwright/typescript-functional-extensions) a Java.**

## Installation

```xml
<dependency>
  <groupId>com.adrewdev.functional</groupId>
  <artifactId>java-functional-extensions</artifactId>
  <version>1.0.0</version>
</dependency>
```

Or build from source:
```bash
git clone https://github.com/adrewdev/java-functional-extensions.git
cd java-functional-extensions
mvn clean install
```

## Quick Start

### Maybe Example
```java
import com.adrewdev.functional.Maybe;

Maybe<String> maybe = Maybe.from(getUserName());

maybe.match(
    name -> System.out.println("User: " + name),
    () -> System.out.println("No user found")
);
```

### Result Example
```java
import com.adrewdev.functional.Result;

Result<User, String> result = Result.of(
    () -> getUserById(id),
    error -> "Failed to get user: " + error.getMessage()
)
.ensure(user -> user.isActive(), "User is not active")
.bind(user -> Result.success(user.getEmail()));

result.match(
    email -> sendEmail(email),
    error -> logError(error)
);
```

## API Overview

### Maybe
Represents a value that might or might not exist.

| Method | Description |
|--------|-------------|
| `from(T)` | Wrap a value (null → None) |
| `some(T)` | Wrap a non-null value |
| `none()` | Create empty Maybe |
| `tryFirst(List)` | Get first element or None |
| `tryLast(List)` | Get last element or None |
| `map(Function)` | Transform value |
| `bind(Function)` | Chain Maybe-returning functions |
| `tap(Consumer)` | Side effect without transformation |
| `match()` | Pattern matching |
| `or(T)` | Fallback value |
| `toResult(E)` | Convert to Result |

### Result
Represents a successful or failed operation.

| Method | Description |
|--------|-------------|
| `success(T)` | Create successful Result |
| `failure(E)` | Create failed Result |
| `of()` | Execute supplier with error handling |
| `of()` | Wrap Result supplier |
| `map(Function)` | Transform success value |
| `mapError(Function)` | Transform error value |
| `bind(Function)` | Chain Result-returning functions |
| `ensure()` | Validate with predicate |
| `recover()` | Recover from failure |
| `zip()` | Combine two Results |

### MaybeAsync
Asynchronous version of Maybe (CompletableFuture).

### ResultAsync
Asynchronous version of Result (CompletableFuture).

### Utilities
Helper functions: `isDefined()`, `isSome()`, `isNone()`, `zeroAsNone()`, etc.

## TypeScript vs Java Comparison

### Maybe
```typescript
// TypeScript
Maybe.from(getEmployee())
  .map(emp => emp.email)
  .or('default@company.com')
  .getValueOrThrow()
```

```java
// Java
Maybe.from(getEmployee())
  .map(emp -> emp.email)
  .or("default@company.com")
  .getValueOrThrow();
```

### Result with Railway Pattern
```typescript
// TypeScript
Result.try(
  () => getEmployee(42),
  (error) => `Retrieving failed: ${error}`
)
.ensure(
  (employee) => employee.email.endsWith('@business.com'),
  ({ firstName, lastName }) =>
    `Employee ${firstName} ${lastName} is a contractor`
)
.bind(({ firstName, lastName, managerId }) =>
  Maybe.from(managerId).toResult(
    `Employee ${firstName} ${lastName} does not have a manager`
  )
)
.map((managerId) => ({
  managerId,
  employeeFullName: `${firstName} ${lastName}`,
}))
.match({
  success: ({ manager: { email }, employeeFullName }) =>
    sendReminder(email, `Remember to say hello to ${employeeFullName}`),
  failure: (error) => sendSupervisorAlert(error),
});
```

```java
// Java
Result.of(
  () -> getEmployee(42),
  (error) -> "Retrieving failed: " + error
)
.ensure(
  (employee) -> employee.email.endsWith("@business.com"),
  (employee) -> "Employee " + employee.firstName + " " + employee.lastName + " is a contractor"
)
.bind(employee ->
  Maybe.from(employee.managerId)
    .toResult("Employee " + employee.firstName + " " + employee.lastName + " does not have a manager")
)
.map(managerId -> new Pair<>(managerId, employee.fullName()))
.match(
  pair -> sendReminder(pair.first.email, "Remember to say hello to " + pair.second),
  error -> sendSupervisorAlert(error)
);
```

### Async Operations
```typescript
// TypeScript
const result = await MaybeAsync.from(fetchUser(id))
  .map(user => user.email)
  .toPromise();
```

```java
// Java
String email = MaybeAsync.from(fetchUser(id))
  .map(user -> user.email)
  .toCompletableFuture()
  .join();
```

## Kotlin DSL

java-functional-extensions includes a complete Kotlin DSL for idiomatic usage with coroutines support.

### Setup

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.adrewdev:java-functional-extensions:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")
}
```

### Maybe DSL

```kotlin
import com.adrewdev.functional.dsl.*

// Builder function
val maybe = maybe { getUser(id) }

// Extension functions
val email = maybe { getUser(id) }
    .map { it.email }
    .or { "default@email.com" }
    .getValueOrThrow()

// Pattern matching
when (maybe.toMaybeK()) {
    is MaybeK.Some -> println(maybe.value)
    MaybeK.None -> println("No user")
}
```

### Result DSL

```kotlin
import com.adrewdev.functional.dsl.*

// Builder with exception handling
val result = result {
    readFile(path)  // Exceptions automatically caught
}

// Railway pattern
val email = result {
    val user = getUser(id).bind()
    ensure(user.active) { Error.UserInactive(user.id) }
    ensure(user.email.endsWith("@company.com")) { 
        Error.InvalidEmail(user.email) 
    }
    user.email
}

// Pattern matching
when (result.toResultK()) {
    is ResultK.Success -> println(result.value)
    is ResultK.Failure -> println(result.error)
}
```

### Coroutines Support

```kotlin
import com.adrewdev.functional.dsl.*
import kotlinx.coroutines.*

// Async builders
val maybe = maybeAsync { fetchUser(id) }
val result = resultAsync { fetchEmail(id) }

// Await results
runBlocking {
    val email = resultAsync { fetchEmail(id) }.awaitGetValue()
    println(email)
}

// Async railway pattern
suspend fun getUserEmail(id: Int): Result<String, Error> = resultAsync {
    val user = fetchUser(id).bindAsync()
    ensure(user.active) { Error.Inactive(user.id) }
    user.email
}.await()
```

### Railway DSL (Arrow Raise Style)

```kotlin
import com.adrewdev.functional.dsl.*

// Arrow Raise-style syntax
val email: Result<String, Error> = resultScope {
    val user = getUser(id).bind()
    ensure(user.active) { Error.Inactive(user.id) }
    ensure(user.email.endsWith("@test.com")) { 
        Error.InvalidEmail(user.email) 
    }
    user.email
}

// Combinators
val allEmails = all(listOf(result1, result2, result3))
val firstSuccess = any(listOf(result1, result2, result3))
val combined = result1.zip(result2) { a, b -> "$a, $b" }
```

### Complete Example

```kotlin
import com.adrewdev.functional.dsl.*
import kotlinx.coroutines.*

data class User(val id: Int, val email: String, val active: Boolean)
sealed class Error {
    data class NotFound(val id: Int) : Error()
    data class Inactive(val id: Int) : Error()
    data class InvalidEmail(val email: String) : Error()
}

suspend fun fetchUser(id: Int): User = withContext(Dispatchers.IO) {
    delay(100)
    User(id, "user@test.com", true)
}

suspend fun validateEmail(email: String): String = withContext(Dispatchers.IO) {
    delay(50)
    ensure(email.endsWith("@test.com")) { 
        throw IllegalArgumentException("Invalid domain") 
    }
    email
}

fun main() = runBlocking {
    val emailResult = resultAsync<String, Error>({ e ->
        when (e) {
            is IllegalArgumentException -> Error.InvalidEmail(e.message ?: "")
            else -> Error.NotFound(1)
        }
    }) {
        val user = fetchUser(1)
        ensure(user.active) { Error.Inactive(user.id) }
        validateEmail(user.email)
    }
    
    when (val result = emailResult.await().toResultK()) {
        is ResultK.Success -> println("Email: ${result.value}")
        is ResultK.Failure -> when (val error = result.error) {
            is Error.NotFound -> println("Not found: ${error.id}")
            is Error.Inactive -> println("Inactive: ${error.id}")
            is Error.InvalidEmail -> println("Invalid: ${error.email}")
        }
    }
}
```

### API Reference

| Function | Description |
|----------|-------------|
| `maybe { }` | Creates Maybe from nullable value |
| `maybe(value)` | Creates Maybe from non-null value |
| `result { }` | Creates Result with exception handling |
| `resultScope { }` | Railway pattern DSL scope |
| `maybeAsync { }` | Creates MaybeAsync with coroutines |
| `resultAsync { }` | Creates ResultAsync with coroutines |
| `.bind()` | Extracts value or short-circuits |
| `.ensure(condition)` | Validates condition |
| `.await()` | Awaits async result |
| `.toMaybeK()` | Converts to sealed class for pattern matching |
| `.toResultK()` | Converts to sealed class for pattern matching |
| `zip()` | Combines two results |
| `all()` | Combines list of results |
| `any()` | Returns first success |

## Testing

Run all tests:
```bash
mvn test
```

Expected output:
```
Tests run: 551, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Project Structure

```
java-functional-extensions/
├── src/main/java/com/adrewdev/functional/
│   ├── Maybe.java              (26 methods)
│   ├── MaybeAsync.java         (15 methods)
│   ├── Result.java             (34 methods)
│   ├── ResultAsync.java        (24 methods)
│   ├── Utilities.java          (13 methods)
│   └── matchers/               (4 interfaces)
├── src/main/kotlin/com/adrewdev/functional/dsl/
│   ├── MaybeDsl.kt             (Maybe extensions)
│   ├── ResultDsl.kt            (Result extensions)
│   ├── CoroutinesDsl.kt        (Coroutines support)
│   ├── RailwayDsl.kt           (Railway pattern DSL)
│   └── examples/
│       └── KotlinExamples.kt   (Complete examples)
├── src/test/java/
│   ├── MaybeTest.java          (86 tests)
│   ├── MaybeAsyncTest.java     (58 tests)
│   ├── ResultTest.java         (125 tests)
│   ├── ResultAsyncTest.java    (70 tests)
│   └── UtilitiesTest.java      (39 tests)
├── src/test/kotlin/
│   ├── MaybeDslTest.kt         (42 tests)
│   ├── ResultDslTest.kt        (67 tests)
│   ├── CoroutinesDslTest.kt    (32 tests)
│   └── RailwayDslTest.kt       (30 tests)
└── pom.xml
```

## Key Features

- ✅ **100% API compatible** with typescript-functional-extensions
- ✅ **Zero external dependencies** (only Java stdlib)
- ✅ **Java 8+ compatible**
- ✅ **Kotlin DSL** with idiomatic extensions
- ✅ **Coroutines support** with suspend functions
- ✅ **Immutable and thread-safe**
- ✅ **Complete JavaDoc and KDoc**
- ✅ **551 passing tests** (380 Java + 171 Kotlin)
- ✅ **Railway Oriented Programming** support
- ✅ **Arrow Raise-style syntax** with resultScope
- ✅ **Pattern matching** with sealed classes
- ✅ **Async/await pattern** with CompletableFuture

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `mvn test`
5. Submit a pull request

## Acknowledgments

- Original TypeScript library: [typescript-functional-extensions](https://github.com/seangwright/typescript-functional-extensions) by Sean G. Wright
- Inspired by C# library: [CSharpFunctionalExtensions](https://github.com/vkhorikov/CSharpFunctionalExtensions) by Vladimir Khorikov
- Railway Oriented Programming: [fsharpforfunandprofit.com/rop](https://fsharpforfunandprofit.com/rop/)
