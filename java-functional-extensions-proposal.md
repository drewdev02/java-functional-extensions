# java-functional-extensions

## Port 1:1 de typescript-functional-extensions a Java

---

## 1. Executive Summary

**Objetivo:** Crear una biblioteca Java que sea un port 1:1 de [`typescript-functional-extensions`](https://github.com/seangwright/typescript-functional-extensions), manteniendo nombres de métodos y API lo más fiel posible.

**Valor Principal:**
- Consistencia cross-language (TypeScript ↔ Java)
- Misma API familiar para equipos multiplataforma
- Functional programming patterns accesibles en Java

**Enfoque:** Java-first, priorizando compatibilidad de API sobre "lo idiomatico del lenguaje"

---

## 2. Scope y Alcance

### ✅ In Scope (5 deliverables)

| Mónada | Métodos | Descripción |
|--------|---------|-------------|
| **Maybe** | 23 | Valor opcional (síncrono) |
| **Result** | 34 | Éxito/fallo tipado (síncrono) |
| **MaybeAsync** | 15 | Maybe asíncrono (CompletableFuture) |
| **ResultAsync** | 24 | Result asíncrono (CompletableFuture) |
| **Utilities** | ~10 | Helpers de tipo y utilidades |

### ❌ Out Scope (deferred)

- Colecciones inmutables (usar Java Collections)
- Pattern matching avanzado (Java 21+)
- Otras mónadas no existentes en TS original
- Kotlin wrapper (puede ser fase 2)

---

## 3. API Compatibility Matrix

### Compatibilidad: **90%+**

| TypeScript | Java | Cambio | Razón |
|------------|------|--------|-------|
| `Maybe.from()` | `Maybe.from()` | ✅ Igual | - |
| `Maybe.some()` | `Maybe.some()` | ✅ Igual | - |
| `Maybe.none()` | `Maybe.none()` | ✅ Igual | - |
| `Maybe.tryFirst()` | `Maybe.tryFirst()` | ✅ Igual | - |
| `Result.try()` | `Result.try_()` | ⚠️ `try_` | `try` es keyword reservada |
| `Result.success()` | `Result.success()` | ✅ Igual | - |
| `Result.failure()` | `Result.failure()` | ✅ Igual | - |
| `.map()` | `.map()` | ✅ Igual | - |
| `.bind()` | `.bind()` | ✅ Igual | - |
| `.match()` | `.match()` | ✅ Igual | - |
| `.tap()` | `.tap()` | ✅ Igual | - |
| `.ensure()` | `.ensure()` | ✅ Igual | - |
| `Promise<T>` | `CompletableFuture<T>` | 🔄 Async | Standard Java 8+ |
| Union `T \| null` | `Optional<T>` | 🔄 Interno | Sin null safety en Java |

### Únicos Cambios de Nombres

| Original TS | Java Port | Motivo |
|-------------|-----------|--------|
| `try()` | `try_()` | `try` es keyword en Java |
| `catch()` | `catch_()` | `catch` es keyword en Java |

---

## 4. Fases de Implementación

```
┌─────────────────────────────────────────────────────────────┐
│  FASE 1: Maybe (Core)                    2-3 días          │
│  ├─ Maybe.java con Optional<T> interno                     │
│  ├─ MaybeMatcher<T> interface funcional                    │
│  ├─ Métodos estáticos: from, some, none, tryFirst, etc.    │
│  ├─ Métodos instancia: map, bind, tap, match, or, etc.     │
│  └─ Tests unitarios JUnit 5 (espejo de TS)                 │
├─────────────────────────────────────────────────────────────┤
│  FASE 2: Result (Core)                   3-4 días          │
│  ├─ Result.java con error tipado                           │
│  ├─ ResultMatcher<T, E> interface funcional                │
│  ├─ Métodos estáticos: success, failure, try_, of, etc.    │
│  ├─ Métodos instancia: ensure, bind, map, match, etc.      │
│  └─ Tests unitarios JUnit 5 (espejo de TS)                 │
├─────────────────────────────────────────────────────────────┤
│  FASE 3: MaybeAsync                      2-3 días          │
│  ├─ MaybeAsync.java con CompletableFuture                  │
│  ├─ Mismos métodos que Maybe                               │
│  └─ Tests async con CompletableFuture                      │
├─────────────────────────────────────────────────────────────┤
│  FASE 4: ResultAsync                     3-4 días          │
│  ├─ ResultAsync.java con CompletableFuture                 │
│  ├─ Mismos métodos que Result                              │
│  └─ Tests async con CompletableFuture                      │
├─────────────────────────────────────────────────────────────┤
│  FASE 5: Utils + Docs                    2-3 días          │
│  ├─ Utilities.java (isDefined, isSome, isNone, etc.)       │
│  ├─ Api.java (fetch wrappers si aplica)                    │
│  ├─ README con ejemplos comparativos TS vs Java            │
│  ├─ JavaDoc completo                                       │
│  └─ Setup publicación (Maven Central / JitPack)            │
├─────────────────────────────────────────────────────────────┤
│  TOTAL ESTIMADO:                         12-17 días        │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Estructura del Proyecto

```
java-functional-extensions/
├── src/main/java/com/github/typescriptfunctional/
│   ├── Maybe.java                  # 23 métodos
│   ├── MaybeAsync.java             # 15 métodos (CompletableFuture)
│   ├── Result.java                 # 34 métodos
│   ├── ResultAsync.java            # 24 métodos (CompletableFuture)
│   ├── Unit.java                   # Void replacement
│   ├── Utilities.java              # Helpers
│   ├── Api.java                    # Fetch wrappers (opcional)
│   └── matchers/
│       ├── MaybeMatcher.java       # Functional interface
│       ├── MaybeAsyncMatcher.java  # Functional interface async
│       ├── ResultMatcher.java      # Functional interface
│       └── ResultAsyncMatcher.java # Functional interface async
├── src/test/java/com/github/typescriptfunctional/
│   ├── MaybeTest.java              # Espejo de test/maybe.ts
│   ├── MaybeAsyncTest.java         # Espejo de test/maybeAsync.ts
│   ├── ResultTest.java             # Espejo de test/result.ts
│   ├── ResultAsyncTest.java        # Espejo de test/resultAsync.ts
│   └── UtilitiesTest.java          # Tests de utilidades
├── pom.xml                         # Maven (o build.gradle)
├── settings.xml                    # Maven settings
├── README.md                       # Ejemplos TS vs Java
├── LICENSE                         # MIT (mismo que TS)
└── .github/workflows/
    └── ci.yml                      # GitHub Actions CI/CD
```

---

## 6. Ejemplos de Código

### Maybe

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

### Maybe con Pattern Matching

**TypeScript:**
```typescript
Maybe.tryFirst(employees)
  .tap(({ firstName, lastName, email }) =>
    console.log(`Found: ${firstName} ${lastName}`))
  .bind(employee =>
    Maybe.from(employee.manager)
      .or({ email: 'supervisor@business.com', firstName: 'Company', lastName: 'Supervisor' })
      .map(manager => ({ manager, employee }))
  )
  .match({
    some: attendees => scheduleMeeting(attendees.manager, attendees.employee),
    none: () => console.log('No employees found')
  });
```

**Java:**
```java
Maybe.tryFirst(employees)
  .tap(emp -> System.out.println("Found: " + emp.firstName + " " + emp.lastName))
  .bind(employee ->
    Maybe.from(employee.manager)
      .or(new Employee("supervisor@business.com", "Company", "Supervisor"))
      .map(manager -> new Pair<>(manager, employee))
  )
  .match(
    attendees -> scheduleMeeting(attendees.manager, attendees.employee),
    () -> System.out.println("No employees found")
  );
```

### Result

**TypeScript:**
```typescript
Result.try(
  () => getUser(id),
  error => `Failed: ${error}`
)
  .ensure(user => user.active, 'User inactive')
  .bind(user => Result.success(user.email))
  .match({
    success: email => sendEmail(email),
    failure: error => logError(error)
  });
```

**Java:**
```java
Result.try_(
  () -> getUser(id),
  error -> "Failed: " + error
)
  .ensure(user -> user.active, "User inactive")
  .bind(user -> Result.success(user.email))
  .match(
    email -> sendEmail(email),
    error -> logError(error)
  );
```

### Result con Railway Pattern

**TypeScript:**
```typescript
Result.try(
  () => getEmployee(42),
  (error) => `Retrieving the employee failed: ${error}`
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
  .bind(({ managerId, employeeFullName }) =>
    Result.try(
      () => getEmployee(managerId),
      (error) => `Retrieving the manager failed: ${error}`
    ).map((manager) => ({ manager, employeeFullName }))
  )
  .match({
    success: ({ manager: { email }, employeeFullName }) =>
      sendReminder(email, `Remember to say hello to ${employeeFullName}`),
    failure: (error) => sendSupervisorAlert(error),
  });
```

**Java:**
```java
Result.try_(
  () -> getEmployee(42),
  (error) -> "Retrieving the employee failed: " + error
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
  .bind(pair ->
    Result.try_(
      () -> getEmployee(pair.left),
      (error) -> "Retrieving the manager failed: " + error
    ).map(manager -> new Triple<>(manager, pair.right))
  )
  .match(
    triple -> sendReminder(triple.left.email, "Remember to say hello to " + triple.right),
    error -> sendSupervisorAlert(error)
  );
```

### MaybeAsync

**TypeScript:**
```typescript
const result = await MaybeAsync.from(fetchUser(id))
  .map(user => user.email)
  .toPromise();
```

**Java:**
```java
String email = MaybeAsync.from(fetchUser(id))
  .map(user -> user.email)
  .toCompletableFuture()
  .join();
```

### ResultAsync

**TypeScript:**
```typescript
const resultAsync = ResultAsync.from(async () => {
  try {
    const value = await getLatestInventory();
    return Result.success(value);
  } catch (error) {
    return Result.failure(`Could not retrieve inventory: ${error}`);
  }
});

const result = await resultAsync.toPromise();
```

**Java:**
```java
ResultAsync<Integer> resultAsync = ResultAsync.from(
  CompletableFuture.supplyAsync(() -> {
    try {
      int value = getLatestInventory();
      return Result.success(value);
    } catch (Exception error) {
      return Result.failure("Could not retrieve inventory: " + error.getMessage());
    }
  })
);

Result<Integer> result = resultAsync.toCompletableFuture().join();
```

---

## 7. Decisiones de Diseño Clave

| # | Decisión | Rationale |
|---|----------|-----------|
| 1 | **Nombres 1:1** excepto `try_`, `catch_` | Máxima compatibilidad con TS |
| 2 | **CompletableFuture** para async | Standard de Java 8+, ampliamente adoptado |
| 3 | **`Optional<T>`** interno | Simula null safety, documenta intención |
| 4 | **Functional interfaces** para match | Pattern matching solo Java 21+ |
| 5 | **Java 8+** como target | Máximo alcance, 95%+ del mercado |
| 6 | **Checked exceptions** → RuntimeException | Menos boilerplate, consistente con TS |
| 7 | **Inmutabilidad** por defecto | Functional programming best practice |

---

## 8. Desafíos Específicos de Java

| Desafío | Impacto | Solución |
|---------|---------|----------|
| **Type erasure** | Alto | Documentar limitaciones, usar `@SafeVarargs` |
| **Sin null safety** | Medio | `Optional<T>` interno, JavaDoc claro |
| **CompletableFuture vs Promise** | Medio | Mapear métodos 1:1, aceptar diferencias |
| **Checked exceptions** | Bajo | Envolver en RuntimeException |
| **Verbosidad** | Medio | Aceptar como limitación del lenguaje |
| **Pattern matching (Java 21+)** | Bajo | Soporte dual: lambdas + pattern matching opcional |

---

## 9. Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Type erasure limita API | 🟡 Media | 🔴 Alto | Diseñar generics cuidadosamente, documentar limitaciones |
| CompletableFuture complejo | 🟡 Media | 🟡 Medio | Seguir patrones establecidos de Java, ejemplos claros |
| Boilerplate excesivo | 🔴 Alta | 🟡 Medio | Aceptar como limitación de Java, priorizar claridad |
| Java 8 vs 21 features | 🟡 Media | 🟡 Medio | Soporte Java 8+, features 21 como módulos opcionales |
| Competencia con Vavr | 🟢 Baja | 🟡 Medio | Diferenciar: 1:1 con TS, minimalista, fácil adopción |
| Adopción baja | 🟡 Media | 🟡 Medio | Documentación excepcional, ejemplos comparativos TS |

---

## 10. Criterios de Éxito

- [ ] **90%+** métodos con mismo nombre/signature que TS
- [ ] Tests espejo de TS con **JUnit 5** y coverage equivalente
- [ ] **README** con ejemplos comparativos TS vs Java lado a lado
- [ ] Soporte **Java 8+** (95%+ del mercado)
- [ ] **JavaDoc** completo en todos los métodos públicos
- [ ] Publicación en **Maven Central** o **JitPack**
- [ ] **CI/CD** con GitHub Actions
- [ ] **0 dependencias** externas (solo stdlib de Java)

---

## 11. Configuración de Build

### Maven (Recomendado)

```xml
<project>
  <groupId>com.github.typescriptfunctional</groupId>
  <artifactId>java-functional-extensions</artifactId>
  <version>1.0.0</version>
  <packaging>jar</packaging>

  <properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- Sin dependencias externas -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <version>3.3.0</version>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals><goal>jar</goal></goals>
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
            <goals><goal>jar</goal></goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

### Gradle (Alternativa)

```kotlin
plugins {
    `java-library`
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.typescriptfunctional"
            artifactId = "java-functional-extensions"
            version = "1.0.0"
            from(components["java"])
        }
    }
}
```

---

## 12. Publicación

### Maven Central (Recomendado)

1. Registrar cuenta en [Sonatype OSSRH](https://issues.sonatype.org/)
2. Configurar `settings.xml` con credenciales
3. Firmar artifacts con GPG
4. Publicar vía Maven: `mvn deploy`

### JitPack (Más simple)

1. Crear release en GitHub
2. Usuarios agregan repositorio:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
3. Dependencia:
```xml
<dependency>
    <groupId>com.github.{username}</groupId>
    <artifactId>java-functional-extensions</artifactId>
    <version>v1.0.0</version>
</dependency>
```

---

## 13. Próximos Pasos (si se aprueba)

1. **sdd-spec** → Spec detallado de `Maybe.java`
   - Tipos exactos de cada método
   - Signatures completas
   - Ejemplos de uso
   - Edge cases y comportamientos esperados

2. **sdd-design** → Arquitectura e implementación
   - Estructura de clases
   - Interfaces funcionales
   - Patrones de diseño

3. **sdd-tasks** → Checklist de implementación
   - Tasks desglosados por método
   - Criterios de aceptación

4. **sdd-apply** → Código (por fases)
   - Fase 1: Maybe
   - Fase 2: Result
   - Fase 3: MaybeAsync
   - Fase 4: ResultAsync
   - Fase 5: Utils + Docs

5. **sdd-verify** → Tests y validación
   - Tests espejo de TS
   - Validación de API 1:1

6. **sdd-archive** → Release y publicación
   - Empaquetado
   - Publicación en Maven Central / JitPack

---

## 14. Referencias

- **Biblioteca original:** https://github.com/seangwright/typescript-functional-extensions
- **Documentación TS:** https://github.com/seangwright/typescript-functional-extensions/tree/main/docs
- **Tests TS:** https://github.com/seangwright/typescript-functional-extensions/tree/main/test
- **NPM package:** https://www.npmjs.com/package/typescript-functional-extensions

---

## 15. Historial de Cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0.0 | 2026-03-26 | Propuesta inicial Java-first |

---

**Estado:** ✅ Propuesta aprobada (pendiente de confirmación)

**Próxima fase:** `sdd-spec` - Especificación detallada de Maybe.java
