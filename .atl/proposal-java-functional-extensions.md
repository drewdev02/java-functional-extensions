# Propuesta: java-functional-extensions

## Cambio de Enfoque: Java-First (anteriormente Kotlin-First)

**Fecha:** 26 de marzo de 2026
**Estado:** Propuesta actualizada
**Versión:** 2.0 (Java-First)

---

## Executive Summary

Esta propuesta actualiza el plan original de **kotlin-functional-extensions** para priorizar **java-functional-extensions** como implementación principal, manteniendo compatibilidad 1:1 con la API de [typescript-functional-extensions](https://github.com/your-org/typescript-functional-extensions).

### Cambios Clave

| Aspecto | Kotlin (Original) | Java (Actualizado) |
|---------|-------------------|-------------------|
| **Async** | Coroutines (suspend functions) | CompletableFuture (Java 8+) |
| **Null Safety** | `T?` nativo del lenguaje | `Optional<T>` interno o documentación |
| **Pattern Matching** | `when` expressions, sealed classes | Interfaces + lambdas |
| **Generics** | Reification parcial | Type erasure (limitación) |
| **Sintaxis** | Lambdas `{ it -> }`, extension functions | Lambdas `() -> {}`, sin extension functions |
| **Build Tool** | Gradle (build.gradle.kts) | Maven o Gradle |
| **Testing** | JUnit 5 + Kotest | JUnit 5 |

### Justificación

- **Java tiene mayor adopción** en enterprise que Kotlin
- **Java 8+ es ubicuo** (2014-presente)
- **CompletableFuture** es estándar maduro para async
- **Kotlin puede ser wrapper** sobre implementación Java

---

## 1. API Compatibility Map

### 1.1 Mapeo TypeScript → Java

| TypeScript | Java | Notas |
|------------|------|-------|
| `Maybe.from(value)` | `Maybe.from(value)` | ✅ Igual |
| `.map(it => ...)` | `.map(v -> ...)` | Similar (lambda) |
| `.match({some, none})` | `.match(new Matcher<>(){...})` | Interface anónima |
| `Promise<T>` | `CompletableFuture<T>` | Async diferente |
| Union `T \| null` | `Optional<T>` o null | Documentar claramente |
| `Result.try(fn, err)` | `Result.try_(fn, err)` | `try_` por keyword |
| `async/await` | `.thenApply()`, `.thenCompose()` | CompletableFuture chain |

### 1.2 Métodos con Cambios de Nombre

| TypeScript | Java | Razón |
|------------|------|-------|
| `try` | `try_` | `try` es keyword en Java |
| `finally` | `finally_` | `finally` es keyword en Java |
| `or(value)` | `orValue(value)` | Overload resolution |
| `or(maybe)` | `orMaybe(maybe)` | Overload resolution |
| `or(factory)` | `orFactory(factory)` | Overload resolution |

---

## 2. Ejemplos de Código Comparativos

### 2.1 Maybe - Ejemplo Básico

**TypeScript:**
```typescript
Maybe.from(getEmployee())
  .map(emp => emp.email)
  .or('default@company.com')
  .getValueOrThrow()
```

**Java:**
```java
Maybe.from(getEmployee())
  .map(emp -> emp.email)
  .or("default@company.com")
  .getValueOrThrow();
```

✅ **1:1 match** - El código se lee idéntico

---

### 2.2 Maybe - Pattern Matching

**TypeScript:**
```typescript
Maybe.tryFirst(employees)
  .match({
    some: (emp) => console.log(`Found: ${emp.name}`),
    none: () => console.log('No employees')
  });
```

**Java:**
```java
Maybe.tryFirst(employees)
  .match(new MaybeMatcher<Employee, Void>() {
    @Override
    public Void some(Employee emp) {
      System.out.println("Found: " + emp.name);
      return null;
    }
    
    @Override
    public Void none() {
      System.out.println("No employees");
      return null;
    }
  });
```

⚠️ **Más verboso** - Requiere clase anónima o lambda con interfaz funcional

**Alternativa con interfaz funcional (Java 8+):**
```java
// Si definimos MaybeMatcher como @FunctionalInterface con método default
Maybe.tryFirst(employees)
  .match(
    emp -> System.out.println("Found: " + emp.name),
    () -> System.out.println("No employees")
  );
```

---

### 2.3 Result - Railway Pattern

**TypeScript:**
```typescript
Result.try(
  () => getEmployee(id),
  (error) => `Failed: ${error}`
)
  .ensure(emp => emp.email.endsWith('@company.com'), 'Not company email')
  .bind(emp => Result.success(emp.managerId))
  .match({
    success: (id) => sendNotification(id),
    failure: (err) => logError(err)
  });
```

**Java:**
```java
Result.try_(() -> getEmployee(id), error -> "Failed: " + error)
  .ensure(emp -> emp.email.endsWith("@company.com"), "Not company email")
  .bind(emp -> Result.success(emp.managerId))
  .match(new ResultMatcher<Integer, String, Void>() {
    @Override
    public Void success(Integer id) {
      sendNotification(id);
      return null;
    }
    
    @Override
    public Void failure(String err) {
      logError(err);
      return null;
    }
  });
```

✅ **Muy cercano** - Solo `try_` y matcher más verboso

---

### 2.4 MaybeAsync con CompletableFuture

**TypeScript:**
```typescript
const maybeAsync = MaybeAsync.from(fetchUser(id))
  .map(user => user.email)
  .tap(email => sendWelcomeEmail(email));

const email = await maybeAsync.getValueOrThrow();
```

**Java:**
```java
MaybeAsync<User> maybeAsync = MaybeAsync.from(fetchUserAsync(id))
  .map(user -> user.email)
  .tap(email -> sendWelcomeEmail(email));

String email = maybeAsync.getValueOrThrow().join(); // o .get()
```

✅ **Casi 1:1** - `CompletableFuture` en lugar de `Promise`

---

### 2.5 ResultAsync - Combinación

**TypeScript:**
```typescript
const resultAsync = ResultAsync.combine({
  user: getUserAsync(id),
  posts: getPostsAsync(id),
  profile: getProfileAsync(id)
});

const { user, posts, profile } = await resultAsync.getValueOrThrow();
```

**Java:**
```java
ResultAsync<Map<String, Object>> resultAsync = ResultAsync.combine(
  Map.of(
    "user", getUserAsync(id),
    "posts", getPostsAsync(id),
    "profile", getProfileAsync(id)
  )
);

Map<String, Object> result = resultAsync.getValueOrThrow().join();
User user = (User) result.get("user");
List<Post> posts = (List<Post>) result.get("posts");
Profile profile = (Profile) result.get("profile");
```

⚠️ **Diferencia** - Java usa `Map` en lugar de objeto anónimo, requiere casting

---

## 3. Estructura del Proyecto Java

```
java-functional-extensions/
├── src/main/java/
│   └── com/github/typescriptfunctional/
│       ├── Maybe.java
│       ├── MaybeAsync.java (CompletableFuture)
│       ├── Result.java
│       ├── Result.java (sealed interface Java 17+)
│       ├── ResultAsync.java (CompletableFuture)
│       ├── Unit.java (singleton)
│       ├── Utilities.java (isDefined, isSome, etc.)
│       ├── MaybeUtilities.java (emptyStringAsNone, etc.)
│       ├── Api.java (HTTP client wrappers)
│       └── matchers/
│           ├── MaybeMatcher.java
│           └── ResultMatcher.java
├── src/test/java/
│   └── com/github/typescriptfunctional/
│       ├── MaybeTest.java
│       ├── MaybeAsyncTest.java
│       ├── ResultTest.java
│       ├── ResultAsyncTest.java
│       └── UtilitiesTest.java
├── pom.xml (Maven) o build.gradle (Gradle)
├── README.md
├── LICENSE
└── docs/
    ├── migration-guide.md
    └── api-reference.md
```

---

## 4. Implementación Core

### 4.1 Maybe.java

```java
package com.github.typescriptfunctional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents an optional value that may be Some(value) or None.
 * Provides 1:1 API compatibility with typescript-functional-extensions.
 * 
 * @param <T> The type of the contained value
 */
public class Maybe<T> {
    
    private final Optional<T> value;
    
    // Constructor privado - usar métodos estáticos
    private Maybe(Optional<T> value) {
        this.value = value;
    }
    
    // ========== STATIC FACTORY METHODS ==========
    
    /**
     * Creates a Maybe with a value (Some).
     */
    public static <T> Maybe<T> some(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Maybe.some() cannot be null. Use Maybe.none() for empty values.");
        }
        return new Maybe<>(Optional.of(value));
    }
    
    /**
     * Creates a Maybe without a value (None).
     */
    public static <T> Maybe<T> none() {
        return new Maybe<>(Optional.empty());
    }
    
    /**
     * Creates a Maybe from a nullable value.
     * null → none, non-null → some
     */
    public static <T> Maybe<T> from(T value) {
        return value != null ? some(value) : none();
    }
    
    /**
     * Gets the first element of a list, or none if empty.
     */
    public static <T> Maybe<T> tryFirst(List<T> values) {
        if (values == null || values.isEmpty()) {
            return none();
        }
        return from(values.get(0));
    }
    
    /**
     * Gets the first element matching predicate, or none if not found.
     */
    public static <T> Maybe<T> tryFirst(List<T> values, Predicate<T> predicate) {
        if (values == null || values.isEmpty()) {
            return none();
        }
        return values.stream()
            .filter(predicate)
            .findFirst()
            .map(Maybe::some)
            .orElse(none());
    }
    
    /**
     * Gets the last element of a list, or none if empty.
     */
    public static <T> Maybe<T> tryLast(List<T> values) {
        if (values == null || values.isEmpty()) {
            return none();
        }
        return from(values.get(values.size() - 1));
    }
    
    /**
     * Gets the last element matching predicate, or none if not found.
     */
    public static <T> Maybe<T> tryLast(List<T> values, Predicate<T> predicate) {
        if (values == null || values.isEmpty()) {
            return none();
        }
        return values.stream()
            .filter(predicate)
            .reduce((first, second) -> second)
            .map(Maybe::some)
            .orElse(none());
    }
    
    /**
     * Extracts values from successful Maybes.
     */
    public static <T> List<T> choose(List<Maybe<T>> maybes) {
        return maybes.stream()
            .filter(Maybe::hasValue)
            .map(m -> m.getValueOrThrow())
            .collect(Collectors.toList());
    }
    
    /**
     * Extracts and transforms values from successful Maybes.
     */
    public static <T, R> List<R> choose(List<Maybe<T>> maybes, Function<T, R> projection) {
        return maybes.stream()
            .filter(Maybe::hasValue)
            .map(m -> projection.apply(m.getValueOrThrow()))
            .collect(Collectors.toList());
    }
    
    // ========== PROPERTIES ==========
    
    public boolean hasValue() {
        return value.isPresent();
    }
    
    public boolean hasNoValue() {
        return value.isEmpty();
    }
    
    // ========== INSTANCE METHODS ==========
    
    /**
     * Gets the value or a default value.
     */
    public T getValueOrDefault(T defaultValue) {
        return value.orElse(defaultValue);
    }
    
    /**
     * Gets the value or the result of a factory function.
     */
    public T getValueOrDefault(Supplier<T> factory) {
        return value.orElseGet(factory);
    }
    
    /**
     * Gets the value or throws NoSuchElementException.
     */
    public T getValueOrThrow() {
        return value.orElseThrow(() -> new NoSuchElementException("No value present"));
    }
    
    /**
     * Transforms the value if present.
     */
    public <R> Maybe<R> map(Function<T, R> projection) {
        return value.isPresent() 
            ? Maybe.some(projection.apply(value.get())) 
            : Maybe.none();
    }
    
    /**
     * Executes an action if value is present (without modifying).
     */
    public Maybe<T> tap(Action<T> action) {
        if (value.isPresent()) {
            action.execute(value.get());
        }
        return this;
    }
    
    /**
     * Transforms to another Maybe (flat map).
     */
    public <R> Maybe<R> bind(Function<T, Maybe<R>> projection) {
        return value.isPresent() 
            ? projection.apply(value.get()) 
            : Maybe.none();
    }
    
    /**
     * Pattern matching with some/none handlers.
     */
    public <R> R match(MaybeMatcher<T, R> matcher) {
        return value.isPresent() 
            ? matcher.some(value.get()) 
            : matcher.none();
    }
    
    /**
     * Executes an action and returns Unit.
     */
    public Unit execute(Action<T> action) {
        if (value.isPresent()) {
            action.execute(value.get());
        }
        return Unit.INSTANCE;
    }
    
    // ========== OR METHODS (Overload Resolution) ==========
    
    /**
     * Returns this Maybe if it has a value, otherwise returns the fallback value.
     */
    public Maybe<T> orValue(T fallback) {
        return value.isPresent() ? this : Maybe.some(fallback);
    }
    
    /**
     * Returns this Maybe if it has a value, otherwise returns the fallback Maybe.
     */
    public Maybe<T> orMaybe(Maybe<T> fallback) {
        return value.isPresent() ? this : fallback;
    }
    
    /**
     * Returns this Maybe if it has a value, otherwise returns Maybe from factory.
     */
    public Maybe<T> orFactory(Supplier<T> factory) {
        return value.isPresent() ? this : Maybe.some(factory.get());
    }
    
    /**
     * Returns this Maybe if it has a value, otherwise returns result of Maybe factory.
     */
    public Maybe<T> orMaybeFactory(Supplier<Maybe<T>> factory) {
        return value.isPresent() ? this : factory.get();
    }
    
    // ========== CONVERSION ==========
    
    /**
     * Converts to Result (none → failure).
     */
    public <E> Result<T, E> toResult(E error) {
        return value.isPresent() 
            ? Result.success(value.get()) 
            : Result.failure(error);
    }
    
    // ========== PIPE ==========
    
    /**
     * Composes multiple operations.
     */
    @SafeVarargs
    public final Maybe<?> pipe(Function<Maybe<?>, Maybe<?>>... operations) {
        Maybe<?> result = this;
        for (Function<Maybe<?>, Maybe<?>> op : operations) {
            result = op.apply(result);
        }
        return result;
    }
    
    // ========== OVERRIDES ==========
    
    @Override
    public String toString() {
        return value.isPresent() ? "Maybe.some" : "Maybe.none";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Maybe)) return false;
        Maybe<?> other = (Maybe<?>) obj;
        return value.equals(other.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    // ========== FUNCTIONAL INTERFACES ==========
    
    @FunctionalInterface
    public interface Action<T> {
        void execute(T value);
    }
}
```

### 4.2 MaybeMatcher.java

```java
package com.github.typescriptfunctional;

/**
 * Matcher for pattern matching on Maybe values.
 * 
 * @param <T> The type of the Maybe value
 * @param <R> The return type of the match
 */
@FunctionalInterface
public interface MaybeMatcher<T, R> {
    R some(T value);
    R none();
    
    /**
     * Helper to create matcher from two lambdas.
     */
    static <T, R> MaybeMatcher<T, R> of(Function<T, R> some, Supplier<R> none) {
        return new MaybeMatcher<T, R>() {
            @Override
            public R some(T value) {
                return some.apply(value);
            }
            
            @Override
            public R none() {
                return none.get();
            }
        };
    }
}
```

### 4.3 MaybeAsync.java (CompletableFuture)

```java
package com.github.typescriptfunctional;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Async version of Maybe using CompletableFuture.
 * Provides 1:1 API compatibility with typescript-functional-extensions MaybeAsync.
 * 
 * @param <T> The type of the contained value
 */
public class MaybeAsync<T> {
    
    private final CompletableFuture<Maybe<T>> future;
    
    private MaybeAsync(CompletableFuture<Maybe<T>> future) {
        this.future = future;
    }
    
    // ========== STATIC FACTORY METHODS ==========
    
    public static <T> MaybeAsync<T> some(T value) {
        return new MaybeAsync<>(CompletableFuture.completedFuture(Maybe.some(value)));
    }
    
    public static <T> MaybeAsync<T> none() {
        return new MaybeAsync<>(CompletableFuture.completedFuture(Maybe.none()));
    }
    
    public static <T> MaybeAsync<T> from(Maybe<T> maybe) {
        return new MaybeAsync<>(CompletableFuture.completedFuture(maybe));
    }
    
    public static <T> MaybeAsync<T> from(CompletableFuture<Maybe<T>> future) {
        return new MaybeAsync<>(future);
    }
    
    public static <T> MaybeAsync<T> from(CompletableFuture<T> future) {
        return new MaybeAsync<>(future.thenApply(Maybe::from));
    }
    
    // ========== PROPERTIES ==========
    
    public CompletableFuture<Boolean> hasValue() {
        return future.thenApply(Maybe::hasValue);
    }
    
    public CompletableFuture<Boolean> hasNoValue() {
        return future.thenApply(Maybe::hasNoValue);
    }
    
    // ========== INSTANCE METHODS ==========
    
    public CompletableFuture<T> getValueOrDefault(T defaultValue) {
        return future.thenApply(m -> m.getValueOrDefault(defaultValue));
    }
    
    public CompletableFuture<T> getValueOrDefault(Supplier<T> factory) {
        return future.thenApply(m -> m.getValueOrDefault(factory));
    }
    
    public CompletableFuture<T> getValueOrThrow() {
        return future.thenApply(Maybe::getValueOrThrow);
    }
    
    public <R> MaybeAsync<R> map(Function<T, R> projection) {
        return new MaybeAsync<>(
            future.thenApply(m -> m.map(projection))
        );
    }
    
    public MaybeAsync<T> tap(Action<T> action) {
        return new MaybeAsync<>(
            future.thenApply(m -> {
                if (m.hasValue()) {
                    action.execute(m.getValueOrThrow());
                }
                return m;
            })
        );
    }
    
    public <R> MaybeAsync<R> bind(Function<T, MaybeAsync<R>> projection) {
        return new MaybeAsync<>(
            future.thenCompose(m -> 
                m.hasValue() 
                    ? projection.apply(m.getValueOrThrow()).future 
                    : CompletableFuture.completedFuture(Maybe.<R>none())
            )
        );
    }
    
    public <R> CompletableFuture<R> match(MaybeMatcher<T, R> matcher) {
        return future.thenApply(m -> m.match(matcher));
    }
    
    public CompletableFuture<Unit> execute(Action<T> action) {
        return future.thenApply(m -> m.execute(action));
    }
    
    // ========== OR METHODS ==========
    
    public MaybeAsync<T> orValue(T fallback) {
        return new MaybeAsync<>(
            future.thenApply(m -> m.orValue(fallback))
        );
    }
    
    public MaybeAsync<T> orMaybe(MaybeAsync<T> fallback) {
        return new MaybeAsync<>(
            future.thenCompose(m -> 
                m.hasValue() 
                    ? CompletableFuture.completedFuture(m) 
                    : fallback.future
            )
        );
    }
    
    // ========== CONVERSION ==========
    
    public <E> ResultAsync<T, E> toResult(E error) {
        return new ResultAsync<>(
            future.thenApply(m -> m.toResult(error))
        );
    }
    
    public CompletableFuture<Maybe<T>> toPromise() {
        return future;
    }
    
    // ========== FUNCTIONAL INTERFACES ==========
    
    @FunctionalInterface
    public interface Action<T> {
        void execute(T value);
    }
}
```

### 4.4 Result.java (esquema)

```java
package com.github.typescriptfunctional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a result that can be Success(value) or Failure(error).
 * Implements the Railway Pattern for error handling.
 * 
 * @param <T> The type of the success value
 * @param <E> The type of the error
 */
public class Result<T, E> {
    
    private final T value;
    private final E error;
    private final boolean isSuccess;
    
    private Result(T value, E error, boolean isSuccess) {
        this.value = value;
        this.error = error;
        this.isSuccess = isSuccess;
    }
    
    // ========== STATIC FACTORY METHODS ==========
    
    public static <T, E> Result<T, E> success(T value) {
        return new Result<>(value, null, true);
    }
    
    public static <T, E> Result<T, E> success() {
        return new Result<>(null, null, true);
    }
    
    public static <T, E> Result<T, E> failure(E error) {
        return new Result<>(null, error, false);
    }
    
    public static <T, E> Result<T, E> successIf(boolean condition, T value, E error) {
        return condition ? success(value) : failure(error);
    }
    
    public static <T, E> Result<T, E> failureIf(boolean condition, T value, E error) {
        return condition ? failure(error) : success(value);
    }
    
    public static <T, E> Result<T, E> try_(Supplier<T> factory, Function<Throwable, E> errorHandler) {
        try {
            return success(factory.get());
        } catch (Throwable e) {
            return failure(errorHandler.apply(e));
        }
    }
    
    // ========== PROPERTIES ==========
    
    public boolean isSuccess() {
        return isSuccess;
    }
    
    public boolean isFailure() {
        return !isSuccess;
    }
    
    // ========== INSTANCE METHODS ==========
    
    public T getValueOrThrow() {
        if (!isSuccess) {
            throw new NoSuchElementException("No value present, error: " + error);
        }
        return value;
    }
    
    public T getValueOrDefault(T defaultValue) {
        return isSuccess ? value : defaultValue;
    }
    
    public T getValueOrDefault(Supplier<T> factory) {
        return isSuccess ? value : factory.get();
    }
    
    public E getErrorOrThrow() {
        if (isSuccess) {
            throw new NoSuchElementException("No error present");
        }
        return error;
    }
    
    public E getErrorOrDefault(E defaultError) {
        return isFailure ? error : defaultError;
    }
    
    public Result<T, E> ensure(Predicate<T> predicate, E error) {
        if (isFailure) return this;
        return predicate.test(value) ? this : failure(error);
    }
    
    public Result<T, E> ensure(Predicate<T> predicate, Function<T, E> errorFactory) {
        if (isFailure) return this;
        return predicate.test(value) ? this : failure(errorFactory.apply(value));
    }
    
    public <R> Result<R, E> map(Function<T, R> projection) {
        return isSuccess ? success(projection.apply(value)) : failure(error);
    }
    
    public <NewE> Result<T, NewE> mapError(Function<E, NewE> projection) {
        return isFailure ? failure(projection.apply(error)) : success(value);
    }
    
    public Result<T, E> mapFailure(Function<E, T> projection) {
        return isFailure ? success(projection.apply(error)) : this;
    }
    
    public <R> Result<R, E> bind(Function<T, Result<R, E>> projection) {
        return isSuccess ? projection.apply(value) : failure(error);
    }
    
    public Result<T, E> compensate(Function<E, Result<T, E>> projection) {
        return isFailure ? projection.apply(error) : this;
    }
    
    public Result<T, E> tap(Action<T> action) {
        if (isSuccess) {
            action.execute(value);
        }
        return this;
    }
    
    public Result<T, E> tapFailure(Action<E> action) {
        if (isFailure) {
            action.execute(error);
        }
        return this;
    }
    
    public Result<T, E> tapEither(Action action) {
        action.execute();
        return this;
    }
    
    public <R> R match(ResultMatcher<T, E, R> matcher) {
        return isSuccess ? matcher.success(value) : matcher.failure(error);
    }
    
    public <R> R finally_(Function<Result<T, E>, R> projection) {
        return projection.apply(this);
    }
    
    // ========== OVERRIDES ==========
    
    @Override
    public String toString() {
        return isSuccess ? "Result.success" : "Result.failure";
    }
    
    public String debug() {
        return isSuccess 
            ? "{ Result value: [" + value + "] }"
            : "{ Result error: [" + error + "] }";
    }
    
    @FunctionalInterface
    public interface Action<T> {
        void execute(T value);
    }
    
    @FunctionalInterface
    public interface Action {
        void execute();
    }
}
```

### 4.5 ResultMatcher.java

```java
package com.github.typescriptfunctional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Matcher for pattern matching on Result values.
 * 
 * @param <T> The type of the success value
 * @param <E> The type of the error
 * @param <R> The return type of the match
 */
@FunctionalInterface
public interface ResultMatcher<T, E, R> {
    R success(T value);
    R failure(E error);
    
    /**
     * Helper to create matcher from two lambdas.
     */
    static <T, E, R> ResultMatcher<T, E, R> of(
        Function<T, R> success, 
        Function<E, R> failure
    ) {
        return new ResultMatcher<T, E, R>() {
            @Override
            public R success(T value) {
                return success.apply(value);
            }
            
            @Override
            public R failure(E error) {
                return failure.apply(error);
            }
        };
    }
}
```

---

## 5. Fases de Implementación

| Fase | Componente | Duración | Complejidad |
|------|-----------|----------|-------------|
| **Fase 1** | Maybe (Java) | 2-3 días | Baja |
| **Fase 2** | Result (Java) | 3-4 días | Media |
| **Fase 3** | MaybeAsync (CompletableFuture) | 2-3 días | Media |
| **Fase 4** | ResultAsync (CompletableFuture) | 3-4 días | Alta |
| **Fase 5** | Utils + Docs + Publicación | 2-3 días | Baja |

**Total: 12-17 días** (ligeramente más que Kotlin por boilerplate de Java)

---

## 6. Desafíos Específicos de Java

| Desafío | Probabilidad | Impacto | Solución |
|---------|-------------|---------|----------|
| **Type erasure** | Media | Alto | Documentar limitaciones, usar @SafeVarargs |
| **Sin null safety** | Alta | Medio | Documentar claramente, usar Optional interno |
| **CompletableFuture vs Promise** | Media | Medio | Mapear métodos 1:1 lo mejor posible |
| **Pattern matching (Java 21+)** | Media | Bajo | Soporte dual: lambdas + pattern matching opcional |
| **Checked exceptions** | Media | Medio | Envolver en RuntimeException o declarar |
| **Boilerplate excesivo** | Alta | Medio | Aceptar como limitación de Java |
| **Java 8 vs 21 features** | Media | Medio | Soporte Java 8+, features opcionales |

---

## 7. Kotlin como Secundario

### Opción A: Kotlin Wrapper sobre Java

```kotlin
// Kotlin usa la implementación Java como base
import com.github.typescriptfunctional.Maybe as JavaMaybe

// Extension functions idiomáticas para Kotlin
fun <T> Maybe<T>.match(some: (T) -> Unit, none: () -> Unit): Unit {
    this.match(object : MaybeMatcher<T, Unit> {
        override fun some(value: T) = some(value)
        override fun none() = none()
    })
}
```

### Opción B: Módulos Separados

```
functional-extensions/
├── java-functional-extensions/   (módulo principal)
├── kotlin-functional-extensions/ (wrapper idiomático)
└── shared-tests/                 (tests compartidos)
```

**Recomendación:** Opción A inicialmente, evaluar Opción B si hay demanda.

---

## 8. Riesgos Actualizados

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Type erasure limita API | Media | Alto | Diseñar cuidadosamente generics, documentar |
| CompletableFuture complejo | Media | Medio | Seguir patrones establecidos de Java |
| Boilerplate excesivo | Alta | Medio | Aceptar como limitación de Java, usar IDE |
| Java 8 vs 21 features | Media | Medio | Soporte Java 8+, features 21 como opcional |
| Adopción menor que TypeScript | Media | Bajo | Enfocar en enterprise Java |
| Kotlin community prefiere Arrow | Baja | Medio | Diferenciar: 1:1 con TS, no funcionalismo puro |

---

## 9. Criterios de Éxito

- ✅ **90%+ métodos** con mismo nombre que TypeScript (Java tiene más limitaciones)
- ✅ **Tests espejo** de TS con JUnit 5
- ✅ **README** con ejemplos TS vs Java lado a lado
- ✅ **Soporte Java 8+** (mercado enterprise)
- ✅ **JavaDoc completo** en todos los métodos públicos
- ✅ **Publicación en Maven Central** (groupId: com.github.typescriptfunctional)
- ✅ **Ejemplos de migración** desde código Java imperativo
- ✅ **Benchmark** de performance vs código imperativo

---

## 10. Maven POM (esqueleto)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.github.typescriptfunctional</groupId>
    <artifactId>java-functional-extensions</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <name>Java Functional Extensions</name>
    <description>1:1 API compatibility with typescript-functional-extensions</description>
    <url>https://github.com/typescriptfunctional/java-functional-extensions</url>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <dependencies>
        <!-- Testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.24.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
            
            <!-- Source and Javadoc -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.3.0</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.6.0</version>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
    
    <!-- Maven Central Publishing -->
    <distributionManagement>
        <snapshotRepository>
            <id>ossrh</id>
            <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
        </snapshotRepository>
        <repository>
            <id>ossrh</id>
            <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
        </repository>
    </distributionManagement>
    
</project>
```

---

## 11. Próximos Pasos Recomendados

1. **Crear repositorio** `java-functional-extensions` en GitHub
2. **Implementar Maybe.java** con tests completos (Fase 1)
3. **Validar API** con ejemplos reales de código enterprise
4. **Implementar Result.java** manteniendo compatibilidad (Fase 2)
5. **Agregar MaybeAsync** con CompletableFuture (Fase 3)
6. **Completar con ResultAsync** (Fase 4)
7. **Documentación** comparativa TS → Java (Fase 5)
8. **Publicar en Maven Central**
9. **Evaluar demanda** para wrapper Kotlin idiomático

---

## 12. Conclusión

El cambio a **Java-first** es estratégico:

- ✅ **Mayor alcance** en enterprise
- ✅ **Estándares maduros** (CompletableFuture, Optional)
- ✅ **Kotlin puede ser wrapper** sobre Java
- ⚠️ **Más boilerplate** pero aceptable
- ⚠️ **Type erasure** requiere diseño cuidadoso

La API mantiene **90%+ compatibilidad** con TypeScript, con ajustes menores donde Java lo requiere (keywords, overload resolution).

---

**Estado:** ✅ Propuesta lista para revisión
**Siguiente:** Crear specs detallados por fase
