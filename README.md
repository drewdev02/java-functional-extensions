# kotlin-functional-extensions

Functional programming extensions for Kotlin, providing a 1:1 API compatibility with [typescript-functional-extensions](https://github.com/your-org/typescript-functional-extensions).

## Features

- **`Maybe<T>`** - Optional value wrapper for null-safe operations
- **`MaybeAsync<T>`** - Async optional values using Kotlin Coroutines
- **`Result<T, E>`** - Success/failure wrapper implementing the Railway Pattern
- **`ResultAsync<T, E>`** - Async result handling with Coroutines

## Installation

```kotlin
dependencies {
    implementation("com.adrewdev:kotlin-functional-extensions:1.0.0")
}
```

## Quick Start

### Maybe

```kotlin
import com.adrewdev.functional.Maybe

val email = Maybe.from(getEmployee())
    .map { it.email }
    .or("default@company.com")
    .getValueOrThrow()
```

### Result

```kotlin
import com.adrewdev.functional.Result

val result = Result.try_({ getEmployee(id) }) { error -> "Failed: $error" }
    .ensure { it.email.endsWith("@company.com") } { "Not company email" }
    .bind { emp -> Result.success(emp.managerId) }
    .match(
        success = { id -> sendNotification(id) },
        failure = { err -> logError(err) }
    )
```

### Async with Coroutines

```kotlin
import com.adrewdev.functional.MaybeAsync
import kotlinx.coroutines.runBlocking

runBlocking {
    val email = MaybeAsync.from(async { fetchUser(id) })
        .map { user -> user.email }
        .getValueOrThrow() // suspend function
}
```

## API Compatibility

This library maintains **1:1 API compatibility** with typescript-functional-extensions:

| TypeScript | Kotlin | Notes |
|------------|--------|-------|
| `Maybe.some(value)` | `Maybe.some(value)` | ✅ |
| `Maybe.none()` | `Maybe.none()` | ✅ |
| `Result.try(fn, errorHandler)` | `Result.try_(fn, errorHandler)` | ⚠️ `try_` due to keyword |
| `Promise<T>` | `Deferred<T>` / suspend | ✅ Coroutines |

## Documentation

- [TypeScript API Reference](./typescript-functional-extensions-analysis.md)
- API docs (coming soon)

## Development

### Build

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

### Publish to Maven Local

```bash
./gradlew publishToMavenLocal
```

## License

MIT
