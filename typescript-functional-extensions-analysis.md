# Análisis Completo: typescript-functional-extensions → Kotlin/Java

## 1. API COMPLETA POR MÓNADA

### 1.1 Maybe<TValue>

#### Métodos Estáticos

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `some` | `some<TValue>(value: Some<TValue>): Maybe<TValue>` | Crea un Maybe con valor |
| `none` | `none<TValue>(): Maybe<TValue>` | Crea un Maybe sin valor |
| `from` | `from<TValue>(value: Some<TValue> \| None): Maybe<TValue>` | Crea Maybe desde valor opcional (null/undefined → none) |
| `tryFirst` | `tryFirst<TValue>(values: TValue[], predicate?: PredicateOfT<Some<TValue>>): Maybe<TValue>` | Obtiene el primer elemento (opcionalmente con predicado) |
| `tryLast` | `tryLast<TValue>(values: TValue[], predicate?: PredicateOfT<Some<TValue>>): Maybe<TValue>` | Obtiene el último elemento (opcionalmente con predicado) |
| `choose` | `choose<TValue, TNewValue>(maybes: Maybe<TValue>[], projection?: FunctionOfTtoK<TValue, TNewValue>): TNewValue[]` | Extrae valores de Maybes con éxito, opcionalmente transformando |

#### Propiedades

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `hasValue` | `boolean` | true si tiene valor |
| `hasNoValue` | `boolean` | true si no tiene valor |

#### Métodos de Instancia

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `getValueOrDefault` | `(defaultValue: Some<TValue> \| FunctionOfT<Some<TValue>>): TValue` | Obtiene valor o default/factory |
| `getValueOrThrow` | `(): Some<TValue>` | Obtiene valor o lanza Error |
| `map` | `map<TNewValue>(projection: FunctionOfTtoK<TValue, Some<TNewValue>>): Maybe<TNewValue>` | Transforma el valor si existe |
| `mapAsync` | `mapAsync<TNewValue>(projection: FunctionOfTtoK<TValue, Promise<Some<TNewValue>>>): MaybeAsync<TNewValue>` | Transformación asíncrona |
| `tap` | `tap(action: ActionOfT<TValue>): Maybe<TValue>` | Ejecuta acción si tiene valor (sin modificar) |
| `tapAsync` | `tapAsync(asyncAction: FunctionOfTtoK<TValue, Promise<void>>): MaybeAsync<TValue>` | Acción asíncrona si tiene valor |
| `tapNone` | `tapNone(action: Action): Maybe<TValue>` | Ejecuta acción si NO tiene valor |
| `tapNoneAsync` | `tapNoneAsync(action: AsyncAction): MaybeAsync<TValue>` | Acción asíncrona si NO tiene valor |
| `bind` | `bind<TNewValue>(projection: FunctionOfTtoK<TValue, Maybe<Some<TNewValue>>>): Maybe<TNewValue>` | Transforma a otro Maybe (flat map) |
| `bindAsync` | `bindAsync<TNewValue>(projection: FunctionOfTtoK<TValue, MaybeAsync<Some<TNewValue>>>): MaybeAsync<TNewValue>` | Transformación asíncrona a MaybeAsync |
| `match` | `match<TNewValue>(matcher: MaybeMatcher<TValue, TNewValue>): TNewValue` | Pattern matching: some/none |
| `execute` | `execute(action: ActionOfT<TValue>): Unit` | Ejecuta acción, retorna Unit |
| `executeAsync` | `executeAsync(action: AsyncActionOfT<TValue>): Promise<Unit>` | Acción asíncrona, retorna Promise<Unit> |
| `or` | `or(fallback: Some<TValue> \| Maybe<TValue> \| FunctionOfT<...>): Maybe<TValue>` | Valor fallback si no tiene valor |
| `orAsync` | `orAsync(fallback: MaybeAsync<TValue> \| Promise<...> \| FunctionOfT<...>): MaybeAsync<TValue>` | Fallback asíncrono |
| `toResult` | `toResult<TError>(error: Some<TError>): Result<TValue, TError>` | Convierte a Result (none → failure) |
| `pipe` | `pipe(...operations: MaybeOpFn[]): Maybe<any> \| MaybeAsync<any>` | Composición de operaciones |
| `toString` | `(): string` | Representación string |
| `equals` | `equals(maybe: Maybe<TValue>): boolean` | Igualdad estricta |

---

### 1.2 MaybeAsync<TValue>

#### Métodos Estáticos

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `from` | `from<TValue>(value: Maybe<TValue> \| Promise<Maybe<TValue>> \| Promise<Some<TValue> \| None>): MaybeAsync<TValue>` | Crea desde Maybe o Promise |
| `some` | `some<TValue>(value: Some<TValue>): MaybeAsync<TValue>` | Crea MaybeAsync con valor |
| `none` | `none<TValue>(): MaybeAsync<TValue>` | Crea MaybeAsync sin valor |

#### Propiedades (Promise)

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `hasValue` | `Promise<boolean>` | Promise resolviendo a true si tiene valor |
| `hasNoValue` | `Promise<boolean>` | Promise resolviendo a true si no tiene valor |

#### Métodos de Instancia

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `getValueOrDefault` | `(defaultValue: Some<TValue> \| FunctionOfT<Some<TValue>>): Promise<TValue>` | Promise con valor o default |
| `getValueOrThrow` | `(): Promise<Some<TValue>>` | Promise con valor o rejected |
| `map` | `map<TNewValue>(projection: FunctionOfTtoK<TValue, Some<TNewValue> \| Promise<Some<TNewValue>>>): MaybeAsync<TNewValue>` | Transformación (sync o async) |
| `tap` | `tap(action: ActionOfT<TValue> \| AsyncActionOfT<TValue>): MaybeAsync<TValue>` | Acción (sync o async) sin modificar |
| `bind` | `bind<TNewValue>(projection: FunctionOfTtoK<TValue, Maybe<TNewValue> \| MaybeAsync<TNewValue>>): MaybeAsync<TNewValue>` | Flat map (sync o async) |
| `match` | `match<TNewValue>(matcher: MaybeMatcher<TValue, TNewValue>): Promise<TNewValue>` | Pattern matching asíncrono |
| `execute` | `execute(func: ActionOfT<TValue> \| AsyncActionOfT<TValue>): Promise<Unit>` | Ejecuta acción asíncrona |
| `or` | `or(fallback: Some<TValue> \| Maybe<TValue> \| MaybeAsync<TValue> \| FunctionOfT<...>): MaybeAsync<TValue>` | Fallback (múltiples tipos) |
| `toResult` | `toResult<TError>(error: Some<TError>): ResultAsync<TValue, TError>` | Convierte a ResultAsync |
| `pipe` | `pipe(...operations: MaybeAsyncOpFn[]): MaybeAsync<any>` | Composición asíncrona |
| `toPromise` | `toPromise(handleError?: boolean): Promise<Maybe<TValue>>` | Extrae Promise interno |

---

### 1.3 Result<TValue, TError>

#### Métodos Estáticos

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `combine` | `combine<TResultRecord>(results: TResultRecord): Result<ResultRecord<TResultRecord>>` | Combina múltiples Results |
| `combineAsync` | `combineAsync<TResultRecord>(record: TResultRecord): ResultAsync<ResultRecord<TResultRecord>>` | Combina Results asíncronos |
| `combineInOrderAsync` | `combineInOrderAsync<TResultRecord>(record: TResultRecord): ResultAsync<ResultRecord<TResultRecord>>` | Combina en orden secuencial |
| `success` | `success<TValue, TError>(value?: Some<TValue>): Result<TValue, TError>` | Crea Result exitoso |
| `successIf` | `successIf(condition: boolean \| Predicate, state: {value, error}): Result<TValue, TError>` | Success condicional |
| `failure` | `failure<TValue, TError>(error: Some<TError>): Result<TValue, TError>` | Crea Result fallido |
| `failureIf` | `failureIf(condition: boolean \| Predicate, state: {value, error}): Result<TValue, TError>` | Failure condicional |
| `choose` | `choose<TValue, TNewValue, TError>(results: Result<TValue, TError>[], projection?: FunctionOfTtoK<TValue, TNewValue>): TNewValue[]` | Extrae valores exitosos |
| `try` | `try(factory: FunctionOfT<Some<TValue>> \| Action, errorHandler: ErrorHandler<TError>): Result<TValue, TError>` | Ejecuta y captura errores |

#### Propiedades

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `isSuccess` | `boolean` | true si succeeded |
| `isFailure` | `boolean` | true si failed |
| `value` | `TValue` (protected) | Valor interno (si success) |
| `error` | `TError` (protected) | Error interno (si failure) |

#### Métodos de Instancia

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `hasValue` | `(): this is ResultSuccess<TValue>` | Type guard para success |
| `hasError` | `(): this is ResultFailure<TValue, TError>` | Type guard para failure |
| `getValueOrThrow` | `(): Some<TValue>` | Obtiene valor o lanza |
| `getValueOrDefault` | `(defaultOrFactory: Some<TValue> \| FunctionOfT<Some<TValue>>): Some<TValue>` | Valor o default/factory |
| `getErrorOrThrow` | `(): Some<TError>` | Obtiene error o lanza |
| `getErrorOrDefault` | `(errorOrFactory: Some<TError> \| FunctionOfT<Some<TError>>): Some<TError>` | Error o default/factory |
| `ensure` | `ensure(predicate: PredicateOfT<TValue>, errorOrFactory: Some<TError> \| FunctionOfTtoK<TValue, TError>): Result<TValue, TError>` | Valida con predicado |
| `check` | `check<TOtherValue>(projection: FunctionOfTtoK<TValue, Result<TOtherValue, TError>>): Result<TValue, TError>` | Valida sin cambiar valor |
| `checkIf` | `checkIf(condition: boolean \| PredicateOfT<TValue>, projection: ...): Result<TValue, TError>` | Check condicional |
| `map` | `map<TNewValue>(projection: FunctionOfTtoK<TValue, Some<TNewValue>>): Result<TNewValue, TError>` | Transforma valor (success) |
| `mapError` | `mapError<TNewError>(projection: FunctionOfTtoK<TError, Some<TNewError>>): Result<TValue, TNewError>` | Transforma error (failure) |
| `mapFailure` | `mapFailure(projection: FunctionOfTtoK<TError, Some<TValue>>): Result<TValue, TError>` | Convierte error a valor |
| `mapAsync` | `mapAsync<TNewValue>(projection: FunctionOfTtoK<TValue, Promise<Some<TNewValue>>>): ResultAsync<TNewValue, TError>` | Transformación asíncrona |
| `mapFailureAsync` | `mapFailureAsync(projection: FunctionOfTtoK<TError, Promise<Some<TValue>>>): ResultAsync<TValue, TError>` | Error → valor async |
| `bind` | `bind<TNewValue>(projection: FunctionOfTtoK<TValue, Result<TNewValue, TError>>): Result<TNewValue, TError>` | Flat map |
| `bindAsync` | `bindAsync<TNewValue>(projection: FunctionOfTtoK<TValue, Promise<Result<TNewValue, TError>> \| ResultAsync<TNewValue, TError>>): ResultAsync<TNewValue, TError>` | Flat map async |
| `compensate` | `compensate(projection: FunctionOfTtoK<TError, Result<TValue, TError>>): Result<TValue, TError>` | Recupera de failure |
| `compensateAsync` | `compensateAsync(projection: FunctionOfTtoK<TError, Promise<Result<TValue, TError>> \| ResultAsync<TValue, TError>>): ResultAsync<TValue, TError>` | Recuperación async |
| `tap` | `tap(action: ActionOfT<TValue>): Result<TValue, TError>` | Acción si success |
| `tapFailure` | `tapFailure(action: ActionOfT<TError>): Result<TValue, TError>` | Acción si failure |
| `tapAsync` | `tapAsync(action: AsyncActionOfT<TValue>): ResultAsync<TValue, TError>` | Acción async si success |
| `tapIf` | `tapIf(condition: boolean \| PredicateOfT<TValue>, action: ActionOfT<TValue>): Result<TValue, TError>` | Acción condicional |
| `tapEither` | `tapEither(action: Action): Result<TValue, TError>` | Acción siempre |
| `tapEitherAsync` | `tapEitherAsync(action: AsyncAction): ResultAsync<TValue, TError>` | Acción async siempre |
| `match` | `match<TNewValue>(matcher: ResultMatcher<TValue, TError, TNewValue>): TNewValue` | Pattern matching success/failure |
| `finally` | `finally<TNewValue>(projection: FunctionOfTtoK<Result<TValue, TError>, Some<TNewValue>>): Some<TNewValue>` | Ejecuta siempre |
| `onSuccessTry` | `onSuccessTry(action: ActionOfT<TValue>, errorHandler: ErrorHandler<TError>): Result<TValue, TError>` | Try dentro de success |
| `onSuccessTryAsync` | `onSuccessTryAsync(asyncAction: AsyncActionOfT<TValue>, errorHandler: ErrorHandler<TError>): ResultAsync<TValue, TError>` | Try async dentro de success |
| `toString` | `(): string` | Representación string |
| `debug` | `(): string` | Debug string |
| `equals` | `equals(result: Result<TValue, TError>): boolean` | Igualdad |
| `pipe` | `pipe(...operations: ResultOpFn[]): Result<any, any> \| ResultAsync<any, any>` | Composición |

---

### 1.4 ResultAsync<TValue, TError>

#### Métodos Estáticos

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `combine` | `combine<TResultRecord>(results: TResultRecord): ResultAsync<ResultRecord<TResultRecord>>` | Combina Results (paralelo) |
| `combineInOrder` | `combineInOrder<TResultRecord>(results: TResultRecord): ResultAsync<ResultRecord<TResultRecord>>` | Combina en orden |
| `from` | `from<TValue, TError>(value: Result<TValue, TError> \| Promise<Result<TValue, TError>> \| Promise<Some<TValue>>): ResultAsync<TValue, TError>` | Crea desde Result/Promise |
| `try` | `try(promiseOrFunction: Promise<Some<TValue>> \| AsyncFunction, errorHandler: ...): ResultAsync<TValue, TError>` | Ejecuta y captura errores async |
| `success` | `success<TValue, TError>(value?: Some<TValue>): ResultAsync<TValue, TError>` | Crea ResultAsync exitoso |
| `failure` | `failure<TValue, TError>(error: Some<TError>): ResultAsync<TValue, TError>` | Crea ResultAsync fallido |

#### Propiedades (Promise)

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `isSuccess` | `Promise<boolean>` | Promise con estado success |
| `isFailure` | `Promise<boolean>` | Promise con estado failure |

#### Métodos de Instancia

| Método | Firma TypeScript | Descripción |
|--------|-----------------|-------------|
| `getValueOrThrow` | `(): Promise<Some<TValue>>` | Promise con valor o rejected |
| `getValueOrDefault` | `(defaultOrFactory: Some<TValue> \| FunctionOfT<Some<TValue>>): Promise<Some<TValue>>` | Promise con valor o default |
| `getErrorOrThrow` | `(): Promise<Some<TError>>` | Promise con error o rejected |
| `getErrorOrDefault` | `(defaultOrFactory: Some<TError> \| FunctionOfT<Some<TError>>): Promise<Some<TError>>` | Promise con error o default |
| `ensure` | `ensure(predicate: PredicateOfT<TValue>, errorOrFactory: Some<TError> \| FunctionOfTtoK<TValue, Some<TError>> \| AsyncFunctionOfTtoK<...>): ResultAsync<TValue, TError>` | Validación async |
| `map` | `map<TNewValue>(projection: FunctionOfTtoK<TValue, Some<TNewValue>> \| AsyncFunctionOfTtoK<TValue, Some<TNewValue>>): ResultAsync<TNewValue, TError>` | Transformación (sync/async) |
| `mapError` | `mapError<TNewError>(projection: FunctionOfTtoK<TError, Some<TNewError>> \| AsyncFunctionOfTtoK<TError, Some<TNewError>>): ResultAsync<TValue, TNewError>` | Transforma error (sync/async) |
| `mapFailure` | `mapFailure(projection: FunctionOfTtoK<TError, Some<TValue>> \| AsyncFunctionOfTtoK<TError, Some<TValue>>): ResultAsync<TValue, TError>` | Error → valor (sync/async) |
| `bind` | `bind<TNewValue>(projection: FunctionOfTtoK<TValue, Result<TNewValue, TError> \| ResultAsync<TNewValue, TError>>): ResultAsync<TNewValue, TError>` | Flat map (sync/async) |
| `compensate` | `compensate(projection: FunctionOfTtoK<TError, Result<TValue, TError> \| ResultAsync<TValue, TError>>): ResultAsync<TValue, TError>` | Recuperación (sync/async) |
| `tap` | `tap(action: ActionOfT<TValue> \| AsyncActionOfT<TValue>): ResultAsync<TValue, TError>` | Acción si success (sync/async) |
| `tapIf` | `tapIf(condition: boolean \| PredicateOfT<TValue>, action: ActionOfT<TValue>): ResultAsync<TValue, TError>` | Acción condicional |
| `tapEither` | `tapEither(action: Action \| AsyncAction): ResultAsync<TValue, TError>` | Acción siempre (sync/async) |
| `tapFailure` | `tapFailure(action: ActionOfT<TError> \| AsyncActionOfT<TError>): ResultAsync<TValue, TError>` | Acción si failure (sync/async) |
| `match` | `match<TNewValue>(matcher: ResultMatcher<TValue, TError, TNewValue>): Promise<Some<TNewValue>>` | Pattern matching async |
| `finally` | `finally<TNewValue>(projection: FunctionOfTtoK<Result<TValue, TError>, Some<TNewValue>>): Promise<Some<TNewValue>>` | Ejecuta siempre |
| `onSuccessTry` | `onSuccessTry(action: ActionOfT<TValue> \| AsyncActionOfT<TValue>, errorHandler: FunctionOfTtoK<unknown, Some<TError>>): ResultAsync<TValue, TError>` | Try dentro de success |
| `toPromise` | `toPromise(errorHandler?: FunctionOfTtoK<unknown, Some<TError>> \| AsyncFunctionOfTtoK<...>): Promise<Result<TValue, TError>>` | Extrae Promise |
| `pipe` | `pipe(...operations: ResultAsyncOpFn[]): ResultAsync<any, any>` | Composición async |

---

### 1.5 Utilidades

#### maybe.utilities.ts

| Función | Firma | Descripción |
|---------|-------|-------------|
| `emptyStringAsNone` | `(value: string \| null \| undefined): Maybe<string>` | "" → none, string → some |
| `emptyOrWhiteSpaceStringAsNone` | `(value: string \| null \| undefined): Maybe<string>` | "" / whitespace → none |
| `zeroAsNone` | `(value: number \| null \| undefined): Maybe<number>` | 0 → none, number → some |

#### utilities.ts

| Tipo/Función | Descripción |
|--------------|-------------|
| `Unit` | Singleton para operaciones sin valor de retorno |
| `isFunction`, `isDefined`, `isSome`, `isNone`, `isPromise` | Type guards |
| `never()` | Lanza error (nunca debería ejecutarse) |
| `noop()` | Función no-op |

#### api.ts (Fetch wrappers)

| Función | Descripción |
|---------|-------------|
| `fetchResponse` | Fetch → ResultAsync<Response> |
| `fetchJsonResponse<TValue>` | Fetch → ResultAsync<TValue> (JSON) |

---

## 2. MAPEO KOTLIN/JAVA PROPUESTO

### 2.1 Decisiones de Diseño Clave

#### Naming Strategy
- **Mantener nombres 1:1** siempre que sea posible
- Ajustar solo cuando Kotlin/Java lo requiera por:
  - Convenciones del lenguaje (camelCase ya coincide)
  - Colisiones con keywords
  - Type system differences

#### Manejo de Async
- **Kotlin**: Coroutines (suspend functions, Flow)
- **Java**: CompletableFuture, Optional

#### Null Safety
- **Kotlin**: Aprovechar el type system (T?, T)
- **Java**: Optional para null safety explícito

---

### 2.2 Maybe<T> en Kotlin

```kotlin
// Mapeo directo manteniendo API
class Maybe<T : Any> private constructor(private val value: T?) {
    
    // Companion object para métodos estáticos
    companion object {
        fun <T : Any> some(value: T): Maybe<T> = Maybe(value)
        fun <T : Any> none(): Maybe<T> = Maybe(null)
        fun <T : Any> from(value: T?): Maybe<T> = Maybe(value)
        
        fun <T : Any> tryFirst(values: List<T>): Maybe<T> = 
            Maybe(values.firstOrNull())
        
        fun <T : Any> tryFirst(values: List<T>, predicate: (T) -> Boolean): Maybe<T> = 
            Maybe(values.firstOrNull(predicate))
        
        fun <T : Any> tryLast(values: List<T>): Maybe<T> = 
            Maybe(values.lastOrNull())
        
        fun <T : Any> tryLast(values: List<T>, predicate: (T) -> Boolean): Maybe<T> = 
            Maybe(values.lastOrNull(predicate))
        
        fun <T : Any> choose(maybes: List<Maybe<T>>): List<T> = 
            maybes.mapNotNull { it.value }
        
        fun <T : Any, R> choose(maybes: List<Maybe<T>>, projection: (T) -> R): List<R> = 
            maybes.mapNotNull { it.value?.let(projection) }
    }
    
    // Properties
    val hasValue: Boolean get() = value != null
    val hasNoValue: Boolean get() = value == null
    
    // Instance methods
    fun getValueOrDefault(defaultValue: T): T = value ?: defaultValue
    fun getValueOrDefault(factory: () -> T): T = value ?: factory()
    fun getValueOrThrow(): T = value ?: throw NoSuchElementException("No value")
    
    fun <R> map(projection: (T) -> R): Maybe<R> = 
        if (hasValue) Maybe.some(projection(value!!)) else Maybe.none()
    
    fun tap(action: (T) -> Unit): Maybe<T> = apply { 
        if (hasValue) action(value!!) 
    }
    
    fun <R> bind(projection: (T) -> Maybe<R>): Maybe<R> = 
        if (hasValue) projection(value!!) else Maybe.none()
    
    fun <R> match(matcher: MaybeMatcher<T, R>): R = 
        if (hasValue) matcher.some(value!!) else matcher.none()
    
    fun execute(action: (T) -> Unit): Unit = apply { 
        if (hasValue) action(value!!) 
    }
    
    fun or(fallback: T): Maybe<T> = if (hasValue) this else Maybe.some(fallback)
    fun or(fallback: Maybe<T>): Maybe<T> = if (hasValue) this else fallback
    fun or(factory: () -> T): Maybe<T> = if (hasValue) this else Maybe.some(factory())
    fun or(factory: () -> Maybe<T>): Maybe<T> = if (hasValue) this else factory()
    
    fun <E> toResult(error: E): Result<T, E> = 
        if (hasValue) Result.success(value!!) else Result.failure(error)
    
    fun pipe(vararg operations: (Maybe<*>) -> Maybe<*>): Maybe<*> = 
        operations.fold(this as Maybe<*>) { acc, op -> op(acc) }
    
    override fun toString(): String = if (hasValue) "Maybe.some" else "Maybe.none"
    
    fun equals(other: Maybe<T>): Boolean = 
        hasValue && other.hasValue && value == other.value
}

// Functional interfaces para matchers
interface MaybeMatcher<T, R> {
    fun some(value: T): R
    fun none(): R
}
```

---

### 2.3 MaybeAsync<T> en Kotlin (Coroutines)

```kotlin
class MaybeAsync<T : Any> private constructor(private val value: Deferred<Maybe<T>>) {
    
    companion object {
        fun <T : Any> some(value: T): MaybeAsync<T> = 
            MaybeAsync(CompletableDeferred(Maybe.some(value)))
        
        fun <T : Any> none(): MaybeAsync<T> = 
            MaybeAsync(CompletableDeferred(Maybe.none()))
        
        fun <T : Any> from(maybe: Maybe<T>): MaybeAsync<T> = 
            MaybeAsync(CompletableDeferred(maybe))
        
        fun <T : Any> from(deferred: Deferred<Maybe<T>>): MaybeAsync<T> = 
            MaybeAsync(deferred)
        
        fun <T : Any> from(deferred: Deferred<T?>): MaybeAsync<T> = 
            MaybeAsync(deferred.map { Maybe.from(it) })
    }
    
    val hasValue: Deferred<Boolean> get() = value.map { it.hasValue }
    val hasNoValue: Deferred<Boolean> get() = value.map { it.hasNoValue }
    
    suspend fun getValueOrDefault(defaultValue: T): T = 
        value.await().getValueOrDefault(defaultValue)
    
    suspend fun getValueOrDefault(factory: () -> T): T = 
        value.await().getValueOrDefault(factory)
    
    suspend fun getValueOrThrow(): T = value.await().getValueOrThrow()
    
    suspend fun <R> map(projection: suspend (T) -> R): MaybeAsync<R> = 
        MaybeAsync(async { 
            val m = value.await()
            if (m.hasValue) Maybe.some(projection(m.getValueOrThrow())) 
            else Maybe.none()
        })
    
    suspend fun tap(action: suspend (T) -> Unit): MaybeAsync<T> = 
        MaybeAsync(async {
            val m = value.await()
            if (m.hasValue) action(m.getValueOrThrow())
            m
        })
    
    suspend fun <R> bind(projection: suspend (T) -> MaybeAsync<R>): MaybeAsync<R> = 
        MaybeAsync(async {
            val m = value.await()
            if (m.hasValue) projection(m.getValueOrThrow()).toPromise().await()
            else Maybe.none()
        })
    
    suspend fun <R> match(matcher: MaybeMatcher<T, R>): R = 
        value.await().match(matcher)
    
    suspend fun execute(action: suspend (T) -> Unit): Unit = 
        value.await().execute(action)
    
    fun or(fallback: MaybeAsync<T>): MaybeAsync<T> = TODO()
    fun or(fallback: suspend () -> T): MaybeAsync<T> = TODO()
    
    fun <E> toResult(error: E): ResultAsync<T, E> = 
        ResultAsync.from(value.map { it.toResult(error) })
    
    fun toPromise(): Deferred<Maybe<T>> = value
}
```

---

### 2.4 Result<T, E> en Kotlin

```kotlin
class Result<T : Any, E : Any> private constructor(
    private val value: T?,
    private val error: E?
) {
    private val isSuccess: Boolean get() = value != null
    val isFailure: Boolean get() = !isSuccess
    
    companion object {
        fun <T : Any, E : Any> success(value: T): Result<T, E> = 
            Result(value, null)
        
        fun <E : Any> success(): Result<Unit, E> = 
            Result(Unit, null)
        
        fun <T : Any, E : Any> failure(error: E): Result<T, E> = 
            Result(null, error)
        
        fun <T : Any, E : Any> successIf(
            condition: Boolean,
            state: Pair<T, E>
        ): Result<T, E> = 
            if (condition) success(state.first) else failure(state.second)
        
        fun <T : Any, E : Any> failureIf(
            condition: Boolean,
            state: Pair<T, E>
        ): Result<T, E> = 
            if (condition) failure(state.second) else success(state.first)
        
        fun <T : Any, E : Any> try_(
            factory: () -> T,
            errorHandler: (Throwable) -> E
        ): Result<T, E> = 
            try { success(factory()) } 
            catch (e: Throwable) { failure(errorHandler(e)) }
        
        fun <T : Any, E : Any> combine(results: Map<String, Result<T, E>>): Result<Map<String, T>, E> {
            val failures = results.filterValues { it.isFailure }
            return if (failures.isEmpty()) {
                success(results.mapValues { it.value.value!! })
            } else {
                failure(failures.values.first().error!!)
            }
        }
    }
    
    fun hasValue(): Boolean = isSuccess
    fun hasError(): Boolean = isFailure
    
    fun getValueOrThrow(): T = value ?: throw NoSuchElementException("No value")
    fun getValueOrDefault(defaultValue: T): T = value ?: defaultValue
    fun getValueOrDefault(factory: () -> T): T = value ?: factory()
    
    fun getErrorOrThrow(): E = error ?: throw NoSuchElementException("No error")
    fun getErrorOrDefault(defaultError: E): E = error ?: defaultError
    fun getErrorOrDefault(factory: () -> E): E = error ?: factory()
    
    fun ensure(predicate: (T) -> Boolean, error: E): Result<T, E> = 
        if (isFailure) this
        else if (predicate(value!!)) this
        else failure(error)
    
    fun ensure(predicate: (T) -> Boolean, errorFactory: (T) -> E): Result<T, E> = 
        if (isFailure) this
        else if (predicate(value!!)) this
        else failure(errorFactory(value!!))
    
    fun <R> check(projection: (T) -> Result<R, E>): Result<T, E> = 
        bind(projection).map { value!! }
    
    fun <R> map(projection: (T) -> R): Result<R, E> = 
        if (isSuccess) success(projection(value!!)) else failure(error!!)
    
    fun <NewE> mapError(projection: (E) -> NewE): Result<T, NewE> = 
        if (isFailure) failure(projection(error!!)) else success(value!!)
    
    fun mapFailure(projection: (E) -> T): Result<T, E> = 
        if (isSuccess) this else success(projection(error!!))
    
    fun <R> bind(projection: (T) -> Result<R, E>): Result<R, E> = 
        if (isSuccess) projection(value!!) else failure(error!!)
    
    fun compensate(projection: (E) -> Result<T, E>): Result<T, E> = 
        if (isSuccess) this else projection(error!!)
    
    fun tap(action: (T) -> Unit): Result<T, E> = apply {
        if (isSuccess) action(value!!)
    }
    
    fun tapFailure(action: (E) -> Unit): Result<T, E> = apply {
        if (isFailure) action(error!!)
    }
    
    fun tapIf(condition: Boolean, action: (T) -> Unit): Result<T, E> = 
        tapIf({ condition }, action)
    
    fun tapIf(predicate: (T) -> Boolean, action: (T) -> Unit): Result<T, E> = 
        if (isSuccess && predicate(value!!)) {
            action(value!!)
            this
        } else this
    
    fun tapEither(action: () -> Unit): Result<T, E> = apply { action() }
    
    fun <R> match(matcher: ResultMatcher<T, E, R>): R = 
        if (isSuccess) matcher.success(value!!) else matcher.failure(error!!)
    
    fun <R> finally(projection: (Result<T, E>) -> R): R = projection(this)
    
    fun onSuccessTry(action: (T) -> Unit, errorHandler: (Throwable) -> E): Result<T, E> = 
        if (isFailure) this
        else try { 
            action(value!!)
            this
        } catch (e: Throwable) {
            failure(errorHandler(e))
        }
    
    override fun toString(): String = if (isSuccess) "Result.success" else "Result.failure"
    
    fun debug(): String = 
        if (isFailure) "{ Result error: [${getErrorOrThrow()}] }"
        else "{ Result value: [${getValueOrThrow()}] }"
    
    fun equals(other: Result<T, E>): Boolean = 
        when {
            isSuccess && other.isSuccess -> value == other.value
            isFailure && other.isFailure -> error == other.error
            else -> false
        }
}

interface ResultMatcher<T, E, R> {
    fun success(value: T): R
    fun failure(error: E): R
}
```

---

### 2.5 ResultAsync<T, E> en Kotlin (Coroutines)

```kotlin
class ResultAsync<T : Any, E : Any> private constructor(
    private val value: Deferred<kotlin.Result<T, E>>
) {
    companion object {
        fun <T : Any, E : Any> from(result: Result<T, E>): ResultAsync<T, E> = 
            ResultAsync(CompletableDeferred(result))
        
        fun <T : Any, E : Any> from(deferred: Deferred<Result<T, E>>): ResultAsync<T, E> = 
            ResultAsync(deferred)
        
        fun <T : Any, E : Any> success(value: T): ResultAsync<T, E> = 
            ResultAsync(CompletableDeferred(Result.success(value)))
        
        fun <T : Any, E : Any> failure(error: E): ResultAsync<T, E> = 
            ResultAsync(CompletableDeferred(Result.failure(error)))
        
        suspend fun <T : Any, E : Any> try_(
            operation: suspend () -> T,
            errorHandler: suspend (Throwable) -> E
        ): ResultAsync<T, E> = 
            try { 
                success(operation()) 
            } catch (e: Throwable) { 
                failure(errorHandler(e)) 
            }
    }
    
    val isSuccess: Deferred<Boolean> get() = value.map { it.isSuccess }
    val isFailure: Deferred<Boolean> get() = value.map { it.isFailure }
    
    suspend fun getValueOrThrow(): T = value.await().getValueOrThrow()
    suspend fun getValueOrDefault(defaultValue: T): T = 
        value.await().getValueOrDefault(defaultValue)
    
    suspend fun getErrorOrThrow(): E = value.await().getErrorOrThrow()
    suspend fun getErrorOrDefault(defaultError: E): E = 
        value.await().getErrorOrDefault(defaultError)
    
    suspend fun ensure(
        predicate: (T) -> Boolean, 
        error: E
    ): ResultAsync<T, E> = TODO()
    
    suspend fun <R> map(projection: suspend (T) -> R): ResultAsync<R, E> = TODO()
    suspend fun <NewE> mapError(projection: suspend (E) -> NewE): ResultAsync<T, NewE> = TODO()
    suspend fun <R> bind(projection: suspend (T) -> ResultAsync<R, E>): ResultAsync<R, E> = TODO()
    suspend fun compensate(projection: suspend (E) -> ResultAsync<T, E>): ResultAsync<T, E> = TODO()
    
    suspend fun tap(action: suspend (T) -> Unit): ResultAsync<T, E> = TODO()
    suspend fun tapFailure(action: suspend (E) -> Unit): ResultAsync<T, E> = TODO()
    suspend fun tapEither(action: suspend () -> Unit): ResultAsync<T, E> = TODO()
    
    suspend fun <R> match(matcher: ResultMatcher<T, E, R>): R = 
        value.await().match(matcher)
    
    suspend fun toPromise(): Deferred<Result<T, E>> = value
}
```

---

## 3. EJEMPLOS COMPARATIVOS TS vs KOTLIN

### 3.1 Maybe - Ejemplo Básico

**TypeScript:**
```typescript
import { Maybe } from 'typescript-functional-extensions';

const employee = Maybe.from(getEmployee())
  .map(emp => emp.email)
  .or('default@company.com')
  .getValueOrThrow();
```

**Kotlin:**
```kotlin
import com.functional.Maybe

val employee = Maybe.from(getEmployee())
    .map { it.email }
    .or("default@company.com")
    .getValueOrThrow()
```

✅ **1:1 match** - El código se lee idéntico

---

### 3.2 Maybe - Pattern Matching

**TypeScript:**
```typescript
Maybe.tryFirst(employees)
  .match({
    some: (emp) => console.log(`Found: ${emp.name}`),
    none: () => console.log('No employees')
  });
```

**Kotlin:**
```kotlin
Maybe.tryFirst(employees)
    .match(object : MaybeMatcher<Employee, Unit> {
        override fun some(emp: Employee) = println("Found: ${emp.name}")
        override fun none() = println("No employees")
    })

// O con lambdas (más idiomático Kotlin pero menos 1:1)
Maybe.tryFirst(employees).match(
    some = { emp -> println("Found: ${emp.name}") },
    none = { println("No employees") }
)
```

⚠️ **Adaptación necesaria** - Kotlin requiere interfaz o función helper

---

### 3.3 Result - Railway Pattern

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

**Kotlin:**
```kotlin
Result.try_({ getEmployee(id) }) { error -> "Failed: $error" }
    .ensure { it.email.endsWith("@company.com") } { "Not company email" }
    .bind { emp -> Result.success(emp.managerId) }
    .match(object : ResultMatcher<Int, String, Unit> {
        override fun success(id: Int) = sendNotification(id)
        override fun failure(err: String) = logError(err)
    })
```

✅ **Muy cercano** - Solo `try_` por keyword reservada

---

### 3.4 Maybe con Async

**TypeScript:**
```typescript
const maybeAsync = MaybeAsync.from(fetchUser(id))
  .map(user => user.email)
  .tap(email => sendWelcomeEmail(email));

const email = await maybeAsync.getValueOrThrow();
```

**Kotlin:**
```kotlin
val maybeAsync = MaybeAsync.from(async { fetchUser(id) })
    .map { user -> user.email }
    .tap { email -> sendWelcomeEmail(email) }

val email = maybeAsync.getValueOrThrow() // suspend function
```

✅ **Casi 1:1** - `async` wrapper necesario para Coroutines

---

### 3.5 ResultAsync - Combinación

**TypeScript:**
```typescript
const resultAsync = ResultAsync.combine({
  user: getUserAsync(id),
  posts: getPostsAsync(id),
  profile: getProfileAsync(id)
});

const { user, posts, profile } = await resultAsync.getValueOrThrow();
```

**Kotlin:**
```kotlin
val resultAsync = ResultAsync.combine(
    mapOf(
        "user" to getUserAsync(id),
        "posts" to getPostsAsync(id),
        "profile" to getProfileAsync(id)
    )
)

val result = resultAsync.getValueOrThrow()
val user = result["user"]
val posts = result["posts"]
val profile = result["profile"]
```

⚠️ **Diferencia** - Kotlin usa Map en lugar de objeto anónimo

---

## 4. DESAFÍOS Y SOLUCIONES PROPUESTAS

### 4.1 Desafío: Union Types vs Generics

**Problema:**
TypeScript usa union types (`Some<TValue> | None`) que Java/Kotlin no tienen nativamente.

**Solución Kotlin:**
```kotlin
// Usar nullability del type system
class Maybe<T : Any> private constructor(private val value: T?)

// T : Any previene T = Nothing?
// T? representa la posibilidad de none
```

**Solución Java:**
```java
// Usar Optional como wrapper interno
public class Maybe<T> {
    private final Optional<T> value;
    
    private Maybe(Optional<T> value) {
        this.value = value;
    }
    
    public static <T> Maybe<T> some(T value) {
        return new Maybe<>(Optional.of(value));
    }
    
    public static <T> Maybe<T> none() {
        return new Maybe<>(Optional.empty());
    }
}
```

---

### 4.2 Desafío: Overloads con Type Guards

**Problema:**
TypeScript tiene overloads con type guards que Kotlin/Java no pueden replicar exactamente.

**TypeScript:**
```typescript
or(fallbackValue: Some<TValue>): Maybe<TValue>;
or(fallbackMaybe: Maybe<TValue>): Maybe<TValue>;
or(fallbackFactory: FunctionOfT<Some<TValue>>): Maybe<TValue>;
```

**Solución Kotlin:**
```kotlin
// Opción 1: Nombres diferentes (menos 1:1)
fun orValue(fallback: T): Maybe<T>
fun orMaybe(fallback: Maybe<T>): Maybe<T>
fun orFactory(factory: () -> T): Maybe<T>

// Opción 2: Overloads reales (mejor 1:1)
fun or(fallback: T): Maybe<T>
fun or(fallback: Maybe<T>): Maybe<T>
fun or(factory: () -> T): Maybe<T>
fun or(factory: () -> Maybe<T>): Maybe<T>

// Kotlin soporta overloads por tipo de parámetro!
```

✅ **Kotlin soporta overloads** - Se puede mantener 1:1

**Solución Java:**
```java
// Nombres diferentes requeridos
public Maybe<T> orValue(T fallback)
public Maybe<T> orMaybe(Maybe<T> fallback)
public Maybe<T> orFactory(Supplier<T> factory)
public Maybe<T> orMaybeFactory(Supplier<Maybe<T>> factory)
```

---

### 4.3 Desafío: Async Handling

**Problema:**
TypeScript usa `Promise<T>` universalmente. Kotlin tiene Coroutines y Java CompletableFuture.

**Solución Kotlin (Coroutines):**
```kotlin
// MaybeAsync usa Deferred<T> (equivalente a Promise)
class MaybeAsync<T> private constructor(
    private val value: Deferred<Maybe<T>>
)

// Funciones suspend para operaciones async
suspend fun getValueOrThrow(): T
suspend fun <R> map(projection: suspend (T) -> R): MaybeAsync<R>
```

**Ventaja:** Las Coroutines permiten código async que se lee como síncrono.

**Solución Java (CompletableFuture):**
```java
public class MaybeAsync<T> {
    private final CompletableFuture<Maybe<T>> future;
    
    public CompletableFuture<T> getValueOrThrow() {
        return future.thenApply(Maybe::getValueOrThrow);
    }
    
    public <R> MaybeAsync<R> map(Function<T, R> projection) {
        return new MaybeAsync<>(
            future.thenApply(m -> m.map(projection))
        );
    }
}
```

---

### 4.4 Desafío: Pattern Matching

**Problema:**
TypeScript usa objetos con propiedades `some`/`none`. Kotlin tiene `when` pero no es 1:1.

**Solución 1 (1:1 API):**
```kotlin
interface MaybeMatcher<T, R> {
    fun some(value: T): R
    fun none(): R
}

// Uso
maybe.match(object : MaybeMatcher<String, Unit> {
    override fun some(value: String) = println(value)
    override fun none() = println("None")
})
```

**Solución 2 (Más idiomático Kotlin):**
```kotlin
// Función helper para lambdas separadas
fun <T, R> match(
    some: (T) -> R,
    none: () -> R
): R = if (hasValue) some(value!!) else none()

// Uso
maybe.match(
    some = { value -> println(value) },
    none = { println("None") }
)
```

**Recomendación:** Ofrecer AMBAS APIs
- API 1:1 para compatibilidad
- API idiomática Kotlin como extensión

---

### 4.5 Desafío: Unit Type

**Problema:**
TypeScript tiene `Unit` singleton. Kotlin tiene `Unit` built-in pero es diferente.

**Solución:**
```kotlin
// Usar el Unit de Kotlin directamente
fun execute(action: (T) -> Unit): Unit = apply { 
    if (hasValue) action(value!!) 
}

// No se necesita clase Unit custom!
```

✅ **Kotlin ya tiene Unit** - No requiere adaptación

**Java:**
```java
// Crear clase Unit singleton
public final class Unit {
    public static final Unit INSTANCE = new Unit();
    private Unit() {}
}
```

---

### 4.6 Desafío: Pipe Operator

**Problema:**
TypeScript tiene `pipe` con overloads para 1-8 operaciones.

**Solución Kotlin:**
```kotlin
// Versión simple con vararg
fun pipe(vararg operations: (Maybe<*>) -> Maybe<*>): Maybe<*> = 
    operations.fold(this) { acc, op -> op(acc) }

// Versión type-safe (más compleja pero mejor)
inline fun <A> pipe(op1: (Maybe<T>) -> Maybe<A>): Maybe<A> = op1(this)
inline fun <A, B> pipe(op1: (Maybe<T>) -> Maybe<A>, op2: (Maybe<A>) -> Maybe<B>): Maybe<B> = 
    op2(op1(this))
// ... overloads hasta 8
```

---

### 4.7 Desafío: Error Handling en ResultAsync

**Problema:**
TypeScript permite `errorHandler` sincrónico o asíncrono.

**TypeScript:**
```typescript
static try<TValue, TError>(
  promise: Promise<TValue>,
  errorHandler: FunctionOfTtoK<unknown, TError> | AsyncFunctionOfTtoK<unknown, TError>
): ResultAsync<TValue, TError>
```

**Solución Kotlin:**
```kotlin
// Dos funciones separadas
companion object {
    suspend fun <T, E> try_(
        operation: suspend () -> T,
        errorHandler: (Throwable) -> E
    ): ResultAsync<T, E>
    
    suspend fun <T, E> try_(
        operation: suspend () -> T,
        errorHandler: suspend (Throwable) -> E
    ): ResultAsync<T, E>
}
```

---

## 5. ESTRUCTURA DE PROYECTO PROPUESTA

```
kotlin-functional-extensions/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── Maybe.kt
│   │   │   ├── MaybeAsync.kt
│   │   │   ├── Result.kt
│   │   │   ├── ResultAsync.kt
│   │   │   ├── Unit.kt (solo Java)
│   │   │   ├── Utilities.kt
│   │   │   ├── MaybeUtilities.kt
│   │   │   └── Api.kt (fetch wrappers)
│   │   └── java/ (Java-specific implementations)
│   └── test/
│       └── kotlin/
│           ├── MaybeTest.kt
│           ├── MaybeAsyncTest.kt
│           ├── ResultTest.kt
│           └── ResultAsyncTest.kt
├── build.gradle.kts
└── README.md
```

---

## 6. RESUMEN DE COMPATIBILIDAD

| Característica | Compatibilidad | Notas |
|---------------|---------------|-------|
| Nombres de métodos | ✅ 95% | Solo `try` → `try_` por keyword |
| Firma de métodos | ✅ 90% | Overloads posibles en Kotlin |
| Async handling | ✅ 85% | Coroutines/CompletableFuture vs Promise |
| Pattern matching | ⚠️ 70% | Requiere interfaces o helpers |
| Null safety | ✅ Kotlin nativo | `T?` vs `Maybe<T>` |
| Pipe operator | ✅ 100% | Implementación directa |
| Error types | ✅ 100% | Genéricos `TError` |
| Unit type | ✅ Kotlin built-in | Java requiere custom |

---

## 7. RECOMENDACIONES FINALES

### Para Kotlin:
1. **Mantener API 1:1** siempre que sea posible
2. **Aprovechar Coroutines** para async (código más limpio que Promise)
3. **Ofrecer extensiones idiomáticas** además de la API 1:1
4. **Usar nullability** del type system internamente

### Para Java:
1. **Nombres ligeramente diferentes** para overloads (ej: `orValue`, `orMaybe`)
2. **CompletableFuture** para todas las operaciones async
3. **Optional** como wrapper interno de Maybe
4. **Lombok** para reducir boilerplate

### Prioridad de Implementación:
1. **Maybe** (más simple, menos dependencias)
2. **Result** (core del railway pattern)
3. **MaybeAsync** (extensión natural de Maybe)
4. **ResultAsync** (más complejo, combinar async + error handling)

---

## 8. PRÓXIMOS PASOS

1. **Implementar Maybe** con tests 1:1 de TypeScript
2. **Validar API** con ejemplos reales
3. **Implementar Result** manteniendo compatibilidad
4. **Agregar MaybeAsync** con Coroutines
5. **Completar con ResultAsync**
6. **Documentación** comparativa TS → Kotlin
7. **Ejemplos** de migración
