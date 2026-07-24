# Backend Fase 4 — Cierre de calidad — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cerrar los hallazgos B-8, B-9, B-11, B-13, B-14, B-15 y P-10 de la auditoría técnica WeRide que siguen vivos en el código actual (verificado contra `main` post Fase 3), más dos hallazgos detectados en la revisión final de Fase 3 (`TripController.createTrip` sin validar `userId`, nombres confusos en `TravelHistoryController`).

**Architecture:** Ocho cambios independientes de corrección de bugs y limpieza, sin nuevas dependencias ni abstracciones. Un `@RestControllerAdvice` nuevo captura las excepciones de dominio que ya existen; el resto son ediciones puntuales en controllers, services y entidades.

**Tech Stack:** Spring Boot 3.5.7, Java 24, Jakarta Validation (`spring-boot-starter-validation`, ya en el `pom.xml` — usado por las anotaciones `@NotNull`/`@NotBlank` que ya existen en las entidades), JUnit 5 + Mockito (sin contexto Spring).

## Global Constraints

- Solo backend. No tocar `Frontend-WeRide`.
- Build de verificación (nunca `mvn test` sin filtrar — dos tests de contexto Spring pre-existentes fallan por razones no relacionadas):
  ```powershell
  $env:JAVA_HOME = "C:\Users\crama\.jdks\openjdk-26.0.2"
  & "C:\Users\crama\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd" -q -Dtest=<ClassName> test
  ```
  En PowerShell, una lista de varias clases separadas por coma falla por cómo PowerShell parsea `,` — usar el Bash tool para ese comando exacto si se necesita filtrar varias clases a la vez.
- Docker no es usable en esta máquina (`docker run` se cuelga) — no hay verificación basada en contenedores, solo Mockito puro.
- No introducir tipos de excepción nuevos para P-10 — el `@RestControllerAdvice` mapea las excepciones de dominio que el código ya lanza (`IllegalArgumentException`, `RuntimeException` con mensajes concretos, `MethodArgumentNotValidException` de `@Valid`). No se toca ningún servicio para lanzar excepciones distintas de las que ya lanza hoy.
- No tocar `BookingDraftServiceImpl.updateDraft` ni `UpdateBookingDraftCommand` (B-3) ni `VehicleCatalogService` (B-2) — confirmado que no están conectados a ningún endpoint; ver el spec para el razonamiento completo.
- Cada tarea termina con al menos un test que falla si la corrección no está (reproduce el bug) y pasa una vez aplicada.

---

### Task 1: P-10 mínimo viable — `@RestControllerAdvice` + `@Valid` en los 3 recursos reproducidos

**Files:**
- Create: `src/main/java/org/example/backendweride/platform/shared/interfaces/rest/GlobalExceptionHandler.java`
- Create: `src/test/java/org/example/backendweride/platform/shared/interfaces/rest/GlobalExceptionHandlerTest.java`
- Modify: `src/main/java/org/example/backendweride/platform/garage/interfaces/rest/resources/CreateVehicleResource.java`
- Modify: `src/main/java/org/example/backendweride/platform/garage/interfaces/rest/VehiclesController.java` (añadir `@Valid`)
- Modify: `src/main/java/org/example/backendweride/platform/iam/interfaces/rest/resources/SignUpResource.java`
- Modify: `src/main/java/org/example/backendweride/platform/iam/interfaces/rest/IamAuthController.java` (añadir `@Valid` en `signUp`)
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/resources/CreateProfileCommandResource.java`
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java` (añadir `@Valid` en `createProfile`)

**Interfaces:**
- Produces: `GlobalExceptionHandler` con métodos `@ExceptionHandler` para `IllegalArgumentException` (404), `MethodArgumentNotValidException` (400), y `RuntimeException` (mapeo por mensaje: contiene "already exists" → 409, "not found" → 404, "invalid credentials" → 401, cualquier otro caso → deja propagar re-lanzando la misma excepción para no ocultar bugs no contemplados — ver Step 3).

- [ ] **Step 1: Escribir el test que reproduce los 3 casos verificados en ejecución por la auditoría**

```java
package org.example.backendweride.platform.shared.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIllegalArgumentExceptionToNotFound() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("Account not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Account not found", response.getBody());
    }

    @Test
    void mapsDuplicateRuntimeExceptionToConflict() {
        var response = handler.handleRuntimeException(new RuntimeException("User already exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void mapsNotFoundRuntimeExceptionToNotFound() {
        var response = handler.handleRuntimeException(new RuntimeException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void mapsInvalidCredentialsRuntimeExceptionToUnauthorized() {
        var response = handler.handleRuntimeException(new RuntimeException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void unrecognizedRuntimeExceptionFallsBackToInternalServerError() {
        var response = handler.handleRuntimeException(new RuntimeException("Something unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void mapsValidationExceptionToBadRequestWithFieldErrors() throws NoSuchMethodException {
        var target = new Object();
        var bindingResult = new MapBindingResult(new HashMap<>(), "resource");
        bindingResult.addError(new org.springframework.validation.FieldError("resource", "brand", "must not be blank"));
        MethodParameter param = new MethodParameter(this.getClass().getDeclaredMethod("mapsValidationExceptionToBadRequestWithFieldErrors"), -1);
        var ex = new MethodArgumentNotValidException(param, bindingResult);

        var response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(true, response.getBody().contains("brand"));
    }
}
```

- [ ] **Step 2: Ejecutar el test y confirmar que falla (la clase no existe)**

Run: `mvn -Dtest=GlobalExceptionHandlerTest test`
Expected: FAIL — `GlobalExceptionHandler` no existe.

- [ ] **Step 3: Crear `GlobalExceptionHandler`**

```java
package org.example.backendweride.platform.shared.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Mapea a códigos HTTP concretos las excepciones de dominio que el proyecto ya lanza
 * (no introduce tipos de excepción nuevos, ver Fase 4 design spec — P-10 mínimo viable).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (message.contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
        if (message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        if (message.contains("invalid credentials")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail);
    }
}
```

- [ ] **Step 4: Ejecutar el test y confirmar que pasa**

Run: `mvn -Dtest=GlobalExceptionHandlerTest test`
Expected: PASS (6/6)

- [ ] **Step 5: Añadir validación a `CreateVehicleResource` (repro exacta de la auditoría: `POST /vehicles {}` → 201 con todo a null)**

```java
package org.example.backendweride.platform.garage.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

public record CreateVehicleResource(
        @NotBlank String brand,
        @NotBlank String model,
        Integer year,
        Integer battery,
        Integer maxSpeed,
        Integer range,
        double weight,
        String color,
        String licensePlate,
        String location,
        String status,
        @NotBlank String type,
        String companyId,
        double pricePerMinute,
        String image,
        List<String> features,
        String maintenanceStatus,
        Date lastMaintenance,
        Date nextMaintenance,
        double totalKilometers,
        double rating
) {}
```

Y en `VehiclesController.createVehicle`, añadir `@Valid`:

```java
import jakarta.validation.Valid;
// ...
public ResponseEntity<VehicleResource> createVehicle(@Valid @RequestBody CreateVehicleResource resource) {
```

- [ ] **Step 6: Añadir validación a `SignUpResource`**

```java
package org.example.backendweride.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

/**
 * Sign Up Resource
 *
 * @summary Represents the data required for signing up, including username and password.
 */
public record SignUpResource(@NotBlank String username, @NotBlank String password) {}
```

En `IamAuthController`, añadir `@Valid` al parámetro de `signUp`:

```java
public ResponseEntity<AccountResource> signUp(@Valid @RequestBody SignUpResource signUpResource) {
```

- [ ] **Step 7: Añadir validación a `CreateProfileCommandResource`**

```java
package org.example.backendweride.platform.profile.interfaces.resources;

import jakarta.validation.constraints.NotNull;

public record CreateProfileCommandResource(
        @NotNull Long userId,
        String firstName,
        String lastName,
        String email
) {
}
```

En `ProfileController`, añadir `@Valid` al parámetro de `createProfile`:

```java
import jakarta.validation.Valid;
// ...
public ResponseEntity<ProfileResource> createProfile(@Valid @RequestBody CreateProfileCommandResource profileResource) {
```

- [ ] **Step 8: Compilar y correr el test de GlobalExceptionHandler de nuevo (confirma que las anotaciones no rompen nada)**

Run: `mvn -Dtest=GlobalExceptionHandlerTest test`
Expected: PASS (6/6)

- [ ] **Step 9: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/shared/interfaces/rest/GlobalExceptionHandler.java \
        src/test/java/org/example/backendweride/platform/shared/interfaces/rest/GlobalExceptionHandlerTest.java \
        src/main/java/org/example/backendweride/platform/garage/interfaces/rest/resources/CreateVehicleResource.java \
        src/main/java/org/example/backendweride/platform/garage/interfaces/rest/VehiclesController.java \
        src/main/java/org/example/backendweride/platform/iam/interfaces/rest/resources/SignUpResource.java \
        src/main/java/org/example/backendweride/platform/iam/interfaces/rest/IamAuthController.java \
        src/main/java/org/example/backendweride/platform/profile/interfaces/resources/CreateProfileCommandResource.java \
        src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java
git commit -m "fix: map domain exceptions to HTTP status and validate creation payloads (P-10)"
```

---

### Task 2: B-13 — `equals`/`hashCode` en las 9 entidades

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/shared/domain/model/aggregates/AuditableAbstractAggregateRoot.java`
- Modify: `src/main/java/org/example/backendweride/platform/garage/domain/model/aggregates/Vehicle.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/domain/model/aggregates/Plan.java`
- Modify: `src/main/java/org/example/backendweride/platform/location/domain/model/aggregates/Location.java`
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/domain/model/aggregates/TravelHistory.java`
- Modify: `src/main/java/org/example/backendweride/platform/notifications/domain/model/aggregates/Notification.java`
- Modify: `src/main/java/org/example/backendweride/platform/iam/domain/model/aggregates/Account.java`
- Modify: `src/main/java/org/example/backendweride/platform/profile/domain/model/aggregates/Profile.java`
- Test: `src/test/java/org/example/backendweride/platform/shared/domain/model/aggregates/AuditableAbstractAggregateRootTest.java`

**Interfaces:**
- Patrón usado en las 9 clases (id-based, seguro con proxies JPA — el patrón estándar de Vlad Mihalcea para JPA): dos entidades son iguales si son de la misma clase (usando `getClass()`, no `instanceof`, porque un proxy Hibernate es de una subclase) y su `id` no-null coincide; `hashCode()` es una constante por clase (`getClass().hashCode()`) para que el hash no cambie cuando Hibernate asigna el id tras persistir.

- [ ] **Step 1: Escribir el test para el patrón compartido (usando `Booking`, la única clase que ya extiende `AuditableAbstractAggregateRoot`)**

```java
package org.example.backendweride.platform.shared.domain.model.aggregates;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AuditableAbstractAggregateRootTest {

    @Test
    void entitiesWithSameIdAreEqual() throws Exception {
        Booking a = new Booking();
        Booking b = new Booking();
        setId(a, 1L);
        setId(b, 1L);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void entitiesWithDifferentIdAreNotEqual() throws Exception {
        Booking a = new Booking();
        Booking b = new Booking();
        setId(a, 1L);
        setId(b, 2L);
        assertNotEquals(a, b);
    }

    @Test
    void entitiesWithNullIdAreNeverEqualToAnother() {
        Booking a = new Booking();
        Booking b = new Booking();
        assertNotEquals(a, b);
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = AuditableAbstractAggregateRoot.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
```

- [ ] **Step 2: Ejecutar el test y confirmar que falla**

Run: `mvn -Dtest=AuditableAbstractAggregateRootTest test`
Expected: FAIL — `Booking` no tiene `equals`/`hashCode`, así que se usa la identidad de objeto por defecto.

- [ ] **Step 3: Añadir el patrón a `AuditableAbstractAggregateRoot` (cubre `Booking`)**

```java
package org.example.backendweride.platform.shared.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.Objects;

/**
 * Auditable Abstract Aggregate Root
 * Base class for aggregate roots that require auditing.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractAggregateRoot<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditableAbstractAggregateRoot<?> other = (AuditableAbstractAggregateRoot<?>) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
```

- [ ] **Step 4: Ejecutar el test y confirmar que pasa**

Run: `mvn -Dtest=AuditableAbstractAggregateRootTest test`
Expected: PASS (3/3)

- [ ] **Step 5: Añadir el mismo patrón directamente a las 8 entidades que no extienden la clase base**

`Vehicle.java` (añadir al final de la clase, antes de la última llave de cierre):

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle other = (Vehicle) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Trip.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip other = (Trip) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Plan.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan other = (Plan) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Location.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location other = (Location) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`TravelHistory.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TravelHistory other = (TravelHistory) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Notification.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification other = (Notification) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Account.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account other = (Account) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

`Profile.java`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile other = (Profile) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
```

- [ ] **Step 6: Compilar todo el proyecto para confirmar que ninguna entidad rompe (algunas dependen de `Lombok @Getter`, no de `equals`/`hashCode` generado, así que no hay colisión)**

Run: `mvn -Dtest=AuditableAbstractAggregateRootTest test`
Expected: PASS — y `mvn compile` (sin `-Dtest`) para confirmar que las 8 clases compilan.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/shared/domain/model/aggregates/AuditableAbstractAggregateRoot.java \
        src/main/java/org/example/backendweride/platform/garage/domain/model/aggregates/Vehicle.java \
        src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java \
        src/main/java/org/example/backendweride/platform/plan/domain/model/aggregates/Plan.java \
        src/main/java/org/example/backendweride/platform/location/domain/model/aggregates/Location.java \
        src/main/java/org/example/backendweride/platform/travelhistory/domain/model/aggregates/TravelHistory.java \
        src/main/java/org/example/backendweride/platform/notifications/domain/model/aggregates/Notification.java \
        src/main/java/org/example/backendweride/platform/iam/domain/model/aggregates/Account.java \
        src/main/java/org/example/backendweride/platform/profile/domain/model/aggregates/Profile.java \
        src/test/java/org/example/backendweride/platform/shared/domain/model/aggregates/AuditableAbstractAggregateRootTest.java
git commit -m "fix: implement id-based equals/hashCode on all JPA entities (B-13)"
```

---

### Task 3: B-8 — `getPendingBookingsByUser` duplica el conteo de página

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImpl.java`
- Test: `src/test/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImplTest.java` (extender el existente)

**Interfaces:**
- Consumes: `BookingRepository.findByUserIdAndStatus(Long, String, Pageable)` (ya existe).
- No cambia la firma pública de `getPendingBookingsByUser`.

- [ ] **Step 1: Escribir el test que reproduce el bug (pide `size=10`, espera como máximo 10 elementos combinados, no 20)**

```java
@Test
void getPendingBookingsByUser_doesNotDoubleCountAcrossStatuses() {
    var pageable = PageRequest.of(0, 10);
    var userId = 7L;
    var pendingBooking = bookingWithStatus(1L, userId, "pending");
    var confirmedBooking = bookingWithStatus(2L, userId, "confirmed");

    when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("pending"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(pendingBooking), pageable, 1));
    when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("confirmed"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(confirmedBooking), pageable, 1));

    var result = service.getPendingBookingsByUser(new GetPendingBookingsByUserQuery(userId), pageable);

    assertEquals(2, result.getTotalElements());
    assertTrue(result.getContent().size() <= pageable.getPageSize());
}

private Booking bookingWithStatus(Long bookingId, Long userId, String status) {
    Booking booking = new Booking();
    booking.setUserId(userId);
    booking.setStatus(status);
    booking.setBookingId(bookingId);
    return booking;
}
```

(Este test ya pasaría con el bug actual para `size=10` porque 1+1=2 no excede el tamaño de página — el bug real se manifiesta con volumen. Se ajusta el test para reproducirlo de forma determinista pidiendo `size=1` y confirmando que cada sub-consulta pide `size/2` en vez de `size` completo:)

```java
@Test
void getPendingBookingsByUser_splitsPageSizeBetweenTheTwoStatuses() {
    var pageable = PageRequest.of(0, 10);
    var userId = 7L;

    when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("pending"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));
    when(bookingRepository.findByUserIdAndStatus(eq(userId), eq("confirmed"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    service.getPendingBookingsByUser(new GetPendingBookingsByUserQuery(userId), pageable);

    var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(bookingRepository, times(2)).findByUserIdAndStatus(eq(userId), anyString(), pageableCaptor.capture());
    for (Pageable used : pageableCaptor.getAllValues()) {
        assertEquals(5, used.getPageSize());
    }
}
```

- [ ] **Step 2: Ejecutar el test y confirmar que falla**

Run: `mvn -Dtest=BookingQueryServiceImplTest test`
Expected: FAIL en `getPendingBookingsByUser_splitsPageSizeBetweenTheTwoStatuses` — hoy ambas llamadas usan el `Pageable` completo (`size=10`), no `size=5`.

- [ ] **Step 3: Arreglar `getPendingBookingsByUser`**

```java
@Override
public Page<BookingResource> getPendingBookingsByUser(GetPendingBookingsByUserQuery q, Pageable pageable) {
    int halfSize = Math.max(1, pageable.getPageSize() / 2);
    Pageable halfPageable = PageRequest.of(pageable.getPageNumber(), halfSize, pageable.getSort());

    var pendingPage = bookingRepository.findByUserIdAndStatus(q.userId(), "pending", halfPageable);
    var confirmedPage = bookingRepository.findByUserIdAndStatus(q.userId(), "confirmed", halfPageable);

    var combined = new java.util.ArrayList<>(pendingPage.getContent());
    combined.addAll(confirmedPage.getContent());

    return new PageImpl<>(
        combined.stream()
            .map(BookingResourceFromEntityAssembler::toResource)
            .collect(Collectors.toList()),
        pageable,
        pendingPage.getTotalElements() + confirmedPage.getTotalElements()
    );
}
```

- [ ] **Step 4: Ejecutar el test y confirmar que pasa**

Run: `mvn -Dtest=BookingQueryServiceImplTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImpl.java \
        src/test/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImplTest.java
git commit -m "fix: split page size between pending/confirmed sub-queries to stop over-fetching (B-8)"
```

---

### Task 4: B-9 — sustituir entidades JPA por Resources ya existentes en 5 controllers

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/services/queries/TripQueryService.java` (firma de retorno)
- Modify: `src/main/java/org/example/backendweride/platform/trip/application/internal/queries/TripQueryServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/interfaces/PlanController.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/domain/services/queries/PlanQueryService.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/application/internal/queries/PlanQueryServiceImpl.java` (o equivalente — localizar la implementación de `PlanQueryService`)
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/interfaces/TravelHistoryController.java`
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/domain/services/queryservices/TravelHistoryQueryService.java`
- Modify: la implementación de `TravelHistoryQueryService`
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java`
- Modify: `src/main/java/org/example/backendweride/platform/location/interfaces/LocationController.java`
- Modify: `src/main/java/org/example/backendweride/platform/location/domain/services/queryservices/LocationQueryService.java`
- Modify: la implementación de `LocationQueryService`
- Test: extender los tests de controller existentes para los 5 controllers, o crear uno nuevo si no existe.

**Interfaces:**
- Consumes assemblers ya existentes: `TripResourceFromEntityAssembler.toResource(Trip)`, `PlanResourceFromEntity.toPlanResource(Plan)`, `TravelHistoryResourceFromEntityAssembler.toTravelHistoryFromEntity(TravelHistory)`, `ProfileResourceFromEntity.tpProfileResourceFromEntity(Profile)`, `LocationResourceFromEntityAssembler.toResourceFromEntity(Location)`.
- Decisión de diseño: mover el mapeo a entidad→Resource dentro de cada `*QueryServiceImpl` (para que el controller nunca vea la entidad JPA), en vez de mapear en el controller — sigue el patrón que ya usan `BookingQueryServiceImpl` y `NotificationQueryServiceImpl` (ambos devuelven `Resource`, no entidad, desde el query service).

- [ ] **Step 1: `TripController` + `TripQueryService` — escribir el test que confirma el tipo de retorno**

```java
// En TripControllerTest.java (extender el existente)
@Test
void getAllTrips_returnsTripResourceNotEntity() {
    when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(1L);
    when(tripQueryService.handle("1")).thenReturn(Optional.of(List.of()));

    var response = controller.getAllTrips();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    // El tipo del cuerpo ya no es List<Trip>, es List<TripResource> — lo verifica el compilador,
    // este test solo confirma que la ruta feliz sigue devolviendo 200 con el mock reescrito.
}
```

- [ ] **Step 2: Ejecutar y confirmar que falla la compilación (el mock de `tripQueryService.handle` ya no coincide con la firma vieja una vez cambiemos la interfaz)**

Run: `mvn -Dtest=TripControllerTest test`
Expected: FAIL (no compila hasta el Step 3).

- [ ] **Step 3: Cambiar `TripQueryService` para devolver `TripResource`**

```java
// TripQueryService.java
package org.example.backendweride.platform.trip.domain.services.queries;

import org.example.backendweride.platform.trip.interfaces.resources.TripResource;

import java.util.List;
import java.util.Optional;

public interface TripQueryService {
    Optional<List<TripResource>> handle(String userId);
}
```

```java
// TripQueryServiceImpl.java
package org.example.backendweride.platform.trip.application.internal.queries;

import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.example.backendweride.platform.trip.interfaces.resources.TripResource;
import org.example.backendweride.platform.trip.interfaces.transform.TripResourceFromEntityAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripQueryServiceImpl implements TripQueryService {
    private final TripRepository tripRepository;

    public TripQueryServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public Optional<List<TripResource>> handle(String userId) {
        var trips = tripRepository.findByUserId(userId).stream()
                .map(TripResourceFromEntityAssembler::toResource)
                .collect(Collectors.toList());
        return Optional.of(trips);
    }
}
```

`TripController.getAllTrips`:

```java
@GetMapping
public ResponseEntity<List<TripResource>> getAllTrips() {
    var result = this.tripQueryService.handle(String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
    return result.map(response ->
            new ResponseEntity<>(response, HttpStatus.OK
    )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

(Quitar el import de `org.example.backendweride.platform.trip.domain.aggregates.Trip` que ya no se usa en el controller.)

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=TripControllerTest test`
Expected: PASS

- [ ] **Step 5: Repetir el mismo patrón para `PlanController.findAllPlans`**

Localizar la implementación de `PlanQueryService` (interfaz con métodos `handle(GetPlanById)` y `handle()` — el segundo es el que alimenta `findAllPlans`) y cambiar su retorno de `Optional<List<Plan>>` a `Optional<List<PlanResource>>`, mapeando con `PlanResourceFromEntity.toPlanResource`. Actualizar `PlanController.findAllPlans`:

```java
@GetMapping
public ResponseEntity<List<PlanResource>> findAllPlans() {
    var result = this.planQueryService.handle();
    return result.map(response -> new ResponseEntity<>(
            response, HttpStatus.OK
    )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

Añadir/ajustar el test de `PlanControllerTest` que confirme que el mock de `planQueryService.handle()` ahora devuelve `Optional<List<PlanResource>>`.

Run: `mvn -Dtest=PlanControllerTest test` → PASS.

- [ ] **Step 6: Repetir para `TravelHistoryController.getAllTravelHistories` y `getTravelHistoryById`**

Cambiar `TravelHistoryQueryService` (métodos que atienden `GetAllTravelsHistory` y `GetTravelsHistoryById`) para devolver `Optional<List<TravelHistoryResource>>`, mapeando con `TravelHistoryResourceFromEntityAssembler.toTravelHistoryFromEntity`. Actualizar ambos métodos del controller:

```java
@GetMapping
public ResponseEntity<List<TravelHistoryResource>> getAllTravelHistories() {
    var result = travelHistoryQueryService.handle(new org.example.backendweride.platform.travelhistory.domain.model.queries.GetAllTravelsHistory());
    return result.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}

@GetMapping("{id}")
public ResponseEntity<List<TravelHistoryResource>> getTravelHistoryById(@PathVariable Long id) {
    var result = travelHistoryQueryService.handle(new GetTravelsHistoryById(id));
    return result.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

(El `@PathVariable` se renombra de `userId` a `id` aquí mismo — coincide con la Task 8, no hace falta repetirlo allí si se hace en este paso; marcar la Task 8 como ya cubierta para este método si se ejecuta en este orden.)

Run: `mvn -Dtest=TravelHistoryControllerTest test` → PASS (crear el test si no existe, siguiendo el patrón de `TripControllerTest`).

- [ ] **Step 7: Repetir para `ProfileController.getProfileById`**

```java
@GetMapping("/{id}")
public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long id) {
    var result = this.profileQueryService.handle(id);
    return result.filter(profile -> authenticatedAccountProvider.getCurrentAccountId().equals(profile.getUserId()))
            .map(profile -> ResponseEntity.ok(ProfileResourceFromEntity.tpProfileResourceFromEntity(profile)))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

(`profileQueryService.handle(id)` sigue devolviendo `Optional<Profile>` — el mapeo a `ProfileResource` ocurre en el controller, igual que ya hace `createProfile`, para no tocar la firma de `ProfileQueryService` innecesariamente ya que aquí no hay lista de por medio.)

Run: `mvn -Dtest=ProfileControllerTest test` → PASS.

- [ ] **Step 8: Repetir para `LocationController.getAllLocation`**

Cambiar `LocationQueryService.handle()` para devolver `Optional<List<LocationResource>>`, mapeando con `LocationResourceFromEntityAssembler.toResourceFromEntity`:

```java
@GetMapping
public ResponseEntity<List<LocationResource>> getAllLocation() {
    var result = this.locationQueryService.handle();
    return result.map(response ->
            new ResponseEntity<>(response, HttpStatus.OK
            )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

Run: `mvn -Dtest=LocationControllerTest test` → PASS (crear el test si no existe).

- [ ] **Step 9: Commit**

```bash
git add -A -- '*/trip/*' '*/plan/*' '*/travelhistory/*' '*/profile/*' '*/location/*'
git commit -m "fix: return DTOs instead of JPA entities from 5 read endpoints (B-9)"
```

---

### Task 5: B-11 + B-15 — persistir datos de perfil y conectar la validación de drafts

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/profile/domain/model/aggregates/Profile.java`
- Modify: `src/main/java/org/example/backendweride/platform/profile/domain/model/commands/CreateProfileCommand.java`
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/transform/CreateProfileCommandFromResourceAssembler.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingDraftServiceImpl.java`
- Test: `src/test/java/org/example/backendweride/platform/profile/**` (nuevo o extendido)
- Test: `src/test/java/org/example/backendweride/platform/booking/application/commandservice/BookingDraftServiceImplTest.java` (nuevo o extendido)

**Interfaces:**
- `CreateProfileCommand` gana los campos `firstName`, `lastName`, `email` (hoy solo tiene `userId` — verificar antes de editar cuál es la firma exacta actual).

- [ ] **Step 1: Test que reproduce B-11 (los datos enviados deben persistirse, no descartarse)**

```java
@Test
void profileConstructorPersistsProvidedNameLastNameAndEmail() {
    var command = new CreateProfileCommand(1L, "Ada", "Lovelace", "ada@weride.com");
    var profile = new Profile(command);

    assertEquals("Ada", profile.getFirstName());
    assertEquals("Lovelace", profile.getLastName());
    assertEquals("ada@weride.com", profile.getEmail());
}
```

- [ ] **Step 2: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=ProfileTest test`
Expected: FAIL — `CreateProfileCommand` hoy solo tiene `userId`, no compila con 4 argumentos.

- [ ] **Step 3: Ampliar `CreateProfileCommand`, el assembler y `Profile`**

```java
// CreateProfileCommand.java
package org.example.backendweride.platform.profile.domain.model.commands;

public record CreateProfileCommand(Long userId, String firstName, String lastName, String email) {
}
```

```java
// CreateProfileCommandFromResourceAssembler.java
package org.example.backendweride.platform.profile.interfaces.transform;

import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource;

public class CreateProfileCommandFromResourceAssembler {
    public static CreateProfileCommand toCommandFromResource(CreateProfileCommandResource resource) {
        return new CreateProfileCommand(
                resource.userId(),
                resource.firstName(),
                resource.lastName(),
                resource.email()
        );
    }
}
```

```java
// Profile.java — constructor
public Profile(CreateProfileCommand profileCommand) {
    this.userId = profileCommand.userId();
    this.firstName = profileCommand.firstName();
    this.lastName = profileCommand.lastName();
    this.email = profileCommand.email();
}
```

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=ProfileTest test`
Expected: PASS

- [ ] **Step 5: Test que reproduce B-15 (la validación debe rechazar una fecha pasada)**

```java
@Test
void saveDraft_rejectsPastStartDate() {
    var pastCommand = new SaveBookingDraftCommand(1L, 2L, 3L, 4L, LocalDateTime.now(),
            LocalDateTime.now().minusDays(1), LocalDateTime.now(), null, null,
            "draft", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "card", "pending",
            0f, 30, 0f, null, null);

    var result = service.saveDraft(pastCommand);

    assertFalse(result.success());
    assertEquals("Start date cannot be in the past", result.message());
}
```

(Ajustar el orden/tipos exactos de los argumentos de `SaveBookingDraftCommand` al leer el record actual antes de escribir el test — el ejemplo asume `startDate` es el 6º argumento posicional, igual que en `BookingDraftValidationServiceImpl.validateForSave`.)

- [ ] **Step 6: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=BookingDraftServiceImplTest test`
Expected: FAIL — hoy `saveDraft` no llama a `validationService`, así que una fecha pasada se guarda igual (`result.success()` es `true`).

- [ ] **Step 7: Conectar `validationService` en `saveDraft`**

```java
@Override
@Transactional
public BookingCommandService.SaveDraftResult saveDraft(SaveBookingDraftCommand command) {
    var validation = validationService.validateForSave(command);
    if (!validation.valid()) {
        return new BookingCommandService.SaveDraftResult(null, false, validation.message());
    }

    Booking booking = Booking.createDraftFrom(command);
    Booking saved = bookingRepository.save(booking);

    return new BookingCommandService.SaveDraftResult(saved.getBookingId(), true, "Draft saved successfully");
}
```

(Verificar el nombre exacto de los accessors de `ValidationResult` — `valid()`/`message()` o `isValid()`/`getMessage()` — leyendo `BookingDraftValidationService.ValidationResult` antes de escribir este código.)

- [ ] **Step 8: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=BookingDraftServiceImplTest test`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/profile/domain/model/aggregates/Profile.java \
        src/main/java/org/example/backendweride/platform/profile/domain/model/commands/CreateProfileCommand.java \
        src/main/java/org/example/backendweride/platform/profile/interfaces/transform/CreateProfileCommandFromResourceAssembler.java \
        src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingDraftServiceImpl.java \
        src/test -- '*Profile*' '*BookingDraftServiceImpl*'
git commit -m "fix: persist profile name/lastname/email and enforce draft validation (B-11, B-15)"
```

---

### Task 6: B-14 — `DELETE /plans/{id}` y `DELETE /trips/{id}` devuelven 404 si no hay nada que borrar

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/plan/domain/services/commands/PlanCommandService.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/application/internal/commands/PlanCommandServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/plan/interfaces/PlanController.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/services/commands/TripCommandService.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java`
- Test: `src/test/java/org/example/backendweride/platform/plan/application/internal/commands/PlanCommandServiceImplTest.java` (nuevo)
- Test: `src/test/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImplTest.java` (extender el existente de Fase 3)

**Interfaces:**
- `PlanCommandService.handle(Long id)` y `TripCommandService.handle(Long id, String userId)` pasan de `void` a `boolean` (`true` si se borró algo).

- [ ] **Step 1: Test que reproduce B-14 para Plan**

```java
@Test
void handle_returnsFalseWhenPlanDoesNotExist() {
    when(planRepository.existsById(99L)).thenReturn(false);

    boolean deleted = service.handle(99L);

    assertFalse(deleted);
    verify(planRepository, never()).deleteById(anyLong());
}

@Test
void handle_returnsTrueAndDeletesWhenPlanExists() {
    when(planRepository.existsById(1L)).thenReturn(true);

    boolean deleted = service.handle(1L);

    assertTrue(deleted);
    verify(planRepository).deleteById(1L);
}
```

- [ ] **Step 2: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=PlanCommandServiceImplTest test`
Expected: FAIL — `handle(Long)` es `void` hoy, no compila contra un `boolean`.

- [ ] **Step 3: Cambiar `PlanCommandService`/`Impl`/`Controller`**

```java
// PlanCommandService.java
public interface PlanCommandService {
    Optional<Plan> handle(CreatePlanCommand command);
    boolean handle(Long id);
}
```

```java
// PlanCommandServiceImpl.java
@Override
public boolean handle(Long id) {
    if (!this.planRepository.existsById(id)) {
        return false;
    }
    this.planRepository.deleteById(id);
    return true;
}
```

```java
// PlanController.java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deletePlanById(@PathVariable Long id) {
    boolean deleted = this.planCommandService.handle(id);
    return deleted
            ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}
```

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=PlanCommandServiceImplTest test`
Expected: PASS

- [ ] **Step 5: Repetir el mismo patrón para Trip (aprovechando que ya filtra por dueño)**

```java
// TripCommandService.java
public interface TripCommandService {
    Optional<Trip> handle(CreateTripCommand command);
    boolean handle(Long id, String userId);
}
```

```java
// TripCommandServiceImpl.java
@Override
public boolean handle(Long id, String userId) {
    var trip = tripRepository.findById(id).filter(t -> userId.equals(t.getUserId()));
    trip.ifPresent(tripRepository::delete);
    return trip.isPresent();
}
```

```java
// TripController.java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTripById(@PathVariable Long id) {
    boolean deleted = this.tripCommandService.handle(id, String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
    return deleted
            ? ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}
```

Añadir a `TripCommandServiceImplTest` (ya existe desde Fase 3) un caso que confirme el `boolean` de retorno:

```java
@Test
void handle_returnsFalseWhenTripDoesNotBelongToUser() {
    var trip = new Trip();
    trip.setUserId("other-user");
    when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

    boolean deleted = service.handle(1L, "current-user");

    assertFalse(deleted);
    verify(tripRepository, never()).delete(any());
}

@Test
void handle_returnsTrueWhenTripBelongsToUser() {
    var trip = new Trip();
    trip.setUserId("current-user");
    when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

    boolean deleted = service.handle(1L, "current-user");

    assertTrue(deleted);
    verify(tripRepository).delete(trip);
}
```

- [ ] **Step 6: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=TripCommandServiceImplTest test`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/plan/domain/services/commands/PlanCommandService.java \
        src/main/java/org/example/backendweride/platform/plan/application/internal/commands/PlanCommandServiceImpl.java \
        src/main/java/org/example/backendweride/platform/plan/interfaces/PlanController.java \
        src/main/java/org/example/backendweride/platform/trip/domain/services/commands/TripCommandService.java \
        src/main/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImpl.java \
        src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java \
        src/test -- '*PlanCommandServiceImpl*' '*TripCommandServiceImpl*'
git commit -m "fix: return 404 instead of 204 when deleting a non-existent plan or trip (B-14)"
```

---

### Task 7: `TripController.createTrip` — derivar `userId` del JWT

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/commands/CreateTripCommand.java` (verificar si `userId` ya es un campo posicional a reemplazar, no a añadir)
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/transform/CreateTripCommandFromResourceAssembler.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java`
- Test: `src/test/java/org/example/backendweride/platform/trip/interfaces/TripControllerTest.java` (extender)

**Interfaces:**
- `CreateTripCommandFromResourceAssembler.toCommandFromResource` gana un segundo parámetro `String userId` (el del JWT), que sustituye al `resource.userId()` que hoy se usa.

- [ ] **Step 1: Test que reproduce el problema**

```java
@Test
void createTrip_usesAuthenticatedUserIdNotResourceBody() {
    when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
    var resource = new CreateTripCommandResource(
            1L, "someone-else-id", 2L, 3L, 4L, "route", List.of(),
            new Date(), new Date(), 10, 5f, 20f, 25f, 0f, 0f, 0,
            "sunny", 20, "active", List.of(), List.of()
    );
    ArgumentCaptor<CreateTripCommand> captor = ArgumentCaptor.forClass(CreateTripCommand.class);
    when(tripCommandService.handle(captor.capture())).thenReturn(Optional.of(new Trip()));

    controller.createTrip(resource);

    assertEquals("42", captor.getValue().userId());
}
```

(Ajustar los argumentos posicionales de `CreateTripCommandResource` al leer el record actual — el orden exacto no es crítico para la intención del test, solo que `userId` del body sea distinto al que finalmente se usa.)

- [ ] **Step 2: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=TripControllerTest test`
Expected: FAIL — hoy el comando usa `resource.userId()` ("someone-else-id"), no "42".

- [ ] **Step 3: Cambiar el assembler y el controller**

```java
// CreateTripCommandFromResourceAssembler.java
public class CreateTripCommandFromResourceAssembler {
    public static CreateTripCommand toCommandFromResource(CreateTripCommandResource resource, String userId) {
        return new CreateTripCommand(
                resource.bookingId(),
                userId,
                resource.vehicleId(),
                resource.startLocationId(),
                resource.endLocationId(),
                resource.route(),
                resource.routeCoordinates(),
                resource.startDate(),
                resource.endDate(),
                resource.duration(),
                resource.distance(),
                resource.averageSpeed(),
                resource.maxSpeed(),
                resource.totalCost(),
                resource.carbonSaved(),
                resource.caloriesBurned(),
                resource.weather(),
                resource.temperature(),
                resource.status(),
                resource.incidentReports(),
                resource.photos()
        );
    }
}
```

(Verificar el orden exacto de campos de `CreateTripCommand`/`CreateTripCommandResource` leyendo ambos records antes de escribir esta versión — debe conservar el orden actual, solo sustituyendo `resource.userId()` por el parámetro `userId`.)

```java
// TripController.createTrip
@PostMapping
public ResponseEntity<TripResource> createTrip(@RequestBody CreateTripCommandResource createTripCommandResource) {
    var userId = String.valueOf(authenticatedAccountProvider.getCurrentAccountId());
    var result = this.tripCommandService.handle(
            CreateTripCommandFromResourceAssembler.toCommandFromResource(createTripCommandResource, userId));
    return result.map(response -> new ResponseEntity<>(
            TripResourceFromEntityAssembler.toResource(response), HttpStatus.CREATED
            )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}
```

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=TripControllerTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/trip/interfaces/transform/CreateTripCommandFromResourceAssembler.java \
        src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java \
        src/test/java/org/example/backendweride/platform/trip/interfaces/TripControllerTest.java
git commit -m "fix: derive trip creator id from JWT instead of trusting the request body"
```

---

### Task 8: Renombrar `UpdateTravelHistoryCommand.userId` → `id`

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/domain/model/commands/UpdateTravelHistoryCommand.java`
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/interfaces/transform/UpdateTravelHistoryCommandFromResourceAssembler.java`
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/application/internal/commandservices/TravelHistoryCommandServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/travelhistory/interfaces/TravelHistoryController.java` (si el `@PathVariable Long userId` de `getTravelHistoryById` no se renombró ya en la Task 4, Step 6 — renombrarlo aquí)

**Nota:** si la Task 4 ya se ejecutó y renombró el `@PathVariable` de `getTravelHistoryById`, este task solo cubre el renombrado de `UpdateTravelHistoryCommand` (que es un archivo distinto, en `updateTravelHistory`, no en `getTravelHistoryById`).

- [ ] **Step 1: Test que confirma el nombre correcto del campo (falla en compilación si el campo se sigue llamando `userId`)**

```java
@Test
void updateTravelHistoryCommandExposesRecordIdNotUserId() {
    var command = new UpdateTravelHistoryCommand(5L, "loc", "vehicle", "img", "10m", "2km");
    assertEquals(5L, command.id());
}
```

- [ ] **Step 2: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=UpdateTravelHistoryCommandTest test`
Expected: FAIL — el record no tiene un accessor `id()`, solo `userId()`.

- [ ] **Step 3: Renombrar el campo**

```java
// UpdateTravelHistoryCommand.java
package org.example.backendweride.platform.travelhistory.domain.model.commands;

public record UpdateTravelHistoryCommand(
        Long id,
        String location,
        String vehicle,
        String image,
        String tripDuration,
        String travelDistance
) {
}
```

```java
// UpdateTravelHistoryCommandFromResourceAssembler.java
public class UpdateTravelHistoryCommandFromResourceAssembler {
    public static UpdateTravelHistoryCommand toCommandFromResource(Long id, UpdateTravelHistoryResource resource) {
        return new UpdateTravelHistoryCommand(
                id,
                resource.location(),
                resource.vehicle(),
                resource.image(),
                resource.tripDuration(),
                resource.travelDistance()
        );
    }
}
```

```java
// TravelHistoryCommandServiceImpl.java — dentro de handle(UpdateTravelHistoryCommand)
var existingTravelHistory = travelHistoryRepository.findById(travelHistoryCommand.id());
```

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=UpdateTravelHistoryCommandTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/travelhistory/domain/model/commands/UpdateTravelHistoryCommand.java \
        src/main/java/org/example/backendweride/platform/travelhistory/interfaces/transform/UpdateTravelHistoryCommandFromResourceAssembler.java \
        src/main/java/org/example/backendweride/platform/travelhistory/application/internal/commandservices/TravelHistoryCommandServiceImpl.java \
        src/test -- '*UpdateTravelHistoryCommand*'
git commit -m "refactor: rename UpdateTravelHistoryCommand.userId to id — it always held the record id"
```
