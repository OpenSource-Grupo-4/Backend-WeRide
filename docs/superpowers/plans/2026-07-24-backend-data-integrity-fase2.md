# Backend Fase 2 — Integridad de datos Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Cerrar P-9 (listas persistidas como blob binario), la tabla basura `BookingEntity`, y los hallazgos B-1, B-2, B-4, B-5 y el healthcheck de Docker Compose (ítem 15) de la auditoría técnica WeRide (2026-07-23). Sigue a Fase 0 (seguridad) y Fase 1 (autorización), ambas ya en `main`.

**Architecture:** 6 cambios puntuales e independientes entre sí (ningún task depende de otro) sobre archivos ya existentes de `Backend-WeRide` (Spring Boot 3.5.7 / Java 24). Ninguno introduce arquitectura nueva.

**Tech Stack:** Spring Boot 3.5.7, Spring Data JPA, JUnit 5 + Mockito.

## Global Constraints

- Java 24 / Spring Boot 3.5.7 — no cambiar versiones (`pom.xml`).
- No agregar dependencias nuevas al `pom.xml`.
- No construir infraestructura de test nueva (H2, perfiles, contexto Spring) — tests unitarios con Mockito, igual que en Fase 0 y Fase 1.
- No tocar archivos de `Frontend-WeRide`.
- **Fuera de alcance, decisión explícita:** no se migra `ddl-auto=update` a `ddl-auto=validate` con Flyway (recomendación completa de P-9) — requiere verificarse contra una base de datos real y Docker no es utilizable en esta máquina. Los `@ElementCollection` de este plan son compatibles con `ddl-auto=update` tal como está.
- P-10 (manejo de errores y validación) NO está en este plan — es un cambio transversal que merece su propio plan.
- Build environment de esta máquina: no hay `mvn` en el PATH ni wrapper en el repo. Usar la distribución de Maven cacheada, vía PowerShell:
  ```powershell
  $env:JAVA_HOME = "C:\Users\crama\.jdks\openjdk-26.0.2"
  & "C:\Users\crama\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd" -q -Dtest=<ClaseDeTest> test
  ```
  Docker no es utilizable en este entorno (`docker run` se cuelga indefinidamente) — no intentar verificación con Docker; para el Task 6 (docker-compose) la verificación es solo lectura/inspección del YAML resultante.
- Los dos tests preexistentes (`BackendWerideApplicationTests`, `WeRidePlatformApplicationTests`) fallan por razones ajenas a este plan (documentado en Fase 0) — no ejecutar `mvn test` sin filtro, usar siempre `-Dtest=<ClaseDeTest>`.

---

### Task 1: `@ElementCollection` en las cinco listas y borrar `BookingEntity` (P-9)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/plan/domain/model/aggregates/Plan.java`
- Modify: `src/main/java/org/example/backendweride/platform/garage/domain/model/aggregates/Vehicle.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java`
- Modify: `src/main/java/org/example/backendweride/platform/location/domain/model/aggregates/Location.java`
- Delete: `src/main/java/org/example/backendweride/platform/booking/infraestructure/persistence/jpa/BookingEntity.java`

**Interfaces:**
- Consumes: nada de otras tareas de este plan.
- Produces: nada consumido por otras tareas de este plan (todas son independientes).

- [x] **Step 1: Anotar `Plan.benefits`**

En `src/main/java/org/example/backendweride/platform/plan/domain/model/aggregates/Plan.java`, reemplazar (línea 41-42):

```java
    @Getter
    List<String> benefits = new ArrayList<>();
```

por:

```java
    @ElementCollection
    @CollectionTable(name = "plan_benefits", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "benefit")
    @Getter
    List<String> benefits = new ArrayList<>();
```

(El archivo ya importa `jakarta.persistence.*`, no hace falta agregar imports. Mismo patrón que ya usa `Booking.issues` en `src/main/java/org/example/backendweride/platform/booking/domain/model/aggregates/Booking.java:72-75`.)

- [x] **Step 2: Anotar `Vehicle.features`**

En `src/main/java/org/example/backendweride/platform/garage/domain/model/aggregates/Vehicle.java`, reemplazar (línea 59-60):

```java
    @Getter
    private List<String> features = new ArrayList<>();
```

por:

```java
    @ElementCollection
    @CollectionTable(name = "vehicle_features", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "feature")
    @Getter
    private List<String> features = new ArrayList<>();
```

- [x] **Step 3: Anotar `Trip.incidentReports` y `Trip.photos`**

En `src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java`, reemplazar (líneas 65-68):

```java
    @Getter
    private List<String> incidentReports = new ArrayList<>();
    @Getter
    private List<String> photos = new ArrayList<>();
```

por:

```java
    @ElementCollection
    @CollectionTable(name = "trip_incident_reports", joinColumns = @JoinColumn(name = "trip_id"))
    @Column(name = "incident_report")
    @Getter
    private List<String> incidentReports = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "trip_photos", joinColumns = @JoinColumn(name = "trip_id"))
    @Column(name = "photo")
    @Getter
    private List<String> photos = new ArrayList<>();
```

(`Trip.routeCoordinates`, líneas 38-40, ya tiene `@ElementCollection` — no tocar ese campo.)

- [x] **Step 4: Anotar `Location.amenities`**

En `src/main/java/org/example/backendweride/platform/location/domain/model/aggregates/Location.java`, reemplazar (línea 43-44):

```java
    @Getter
    private List<String> amenities = new ArrayList<>();
```

por:

```java
    @ElementCollection
    @CollectionTable(name = "location_amenities", joinColumns = @JoinColumn(name = "location_id"))
    @Column(name = "amenity")
    @Getter
    private List<String> amenities = new ArrayList<>();
```

- [x] **Step 5: Borrar la entidad basura**

```bash
rm src/main/java/org/example/backendweride/platform/booking/infraestructure/persistence/jpa/BookingEntity.java
```

Confirmar antes de borrar que sigue sin referencias (ya verificado al escribir este plan, pero repetir por seguridad):

Run: `grep -rl "BookingEntity" src/main/java`
Expected: sin resultados después de borrar el archivo (antes de borrar, el único resultado es el propio archivo).

- [x] **Step 6: Confirmar que compila**

Run: `mvn.cmd -q compile` (con `JAVA_HOME` fijado como se indica en Global Constraints)
Expected: BUILD SUCCESS, sin `[ERROR]`.

- [x] **Step 7: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/plan/domain/model/aggregates/Plan.java src/main/java/org/example/backendweride/platform/garage/domain/model/aggregates/Vehicle.java src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java src/main/java/org/example/backendweride/platform/location/domain/model/aggregates/Location.java
git rm src/main/java/org/example/backendweride/platform/booking/infraestructure/persistence/jpa/BookingEntity.java
git commit -m "fix: map List<String> fields with @ElementCollection instead of blob serialization, remove unused BookingEntity (P-9)"
```

---

### Task 2: Catálogo de vehículos real en `saveDraft` (B-2)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImpl.java`
- Test: `src/test/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImplTest.java` (crear)

**Interfaces:**
- Consumes: `VehicleRepository extends JpaRepository<Vehicle, Long>` (ya existe, `src/main/java/org/example/backendweride/platform/garage/infrastructure/persistence/jpa/VehicleRepository.java`), `Vehicle.getPricePerMinute(): double` (ya existe vía Lombok `@Getter`).
- Produces: nada consumido por otras tareas.

- [x] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImplTest.java`:

```java
package org.example.backendweride.platform.booking.application.commandservice;

import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingCommandServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final BookingCommandServiceImpl service = new BookingCommandServiceImpl(bookingRepository, vehicleRepository);

    private SaveBookingDraftCommand draftCommand(Long vehicleId) {
        return new SaveBookingDraftCommand(
                1L, vehicleId, 2L, 3L,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(1),
                null, null,
                "draft", null, null, null,
                "credit_card", "pending",
                null, 30, null,
                null, null
        );
    }

    @Test
    void saveDraft_returnsFailure_whenVehicleNotInRealRepository() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        var result = service.saveDraft(draftCommand(999L));

        assertFalse(result.success());
        assertEquals("Vehicle not found", result.message());
    }

    @Test
    void saveDraft_succeeds_whenVehicleExistsInRealRepository() {
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getPricePerMinute()).thenReturn(0.5);
        when(vehicleRepository.findById(7L)).thenReturn(Optional.of(vehicle));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.saveDraft(draftCommand(7L));

        assertTrue(result.success());
        assertEquals("Draft saved", result.message());
    }
}
```

- [x] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=BookingCommandServiceImplTest test`
Expected: FAIL — no compila (`BookingCommandServiceImpl` todavía no tiene un constructor de dos argumentos `(BookingRepository, VehicleRepository)`).

- [x] **Step 3: Implementar**

En `src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImpl.java`, reemplazar el import de `VehicleCatalogService` (línea 10) por:

```java
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.VehicleRepository;
```

Reemplazar los campos y el constructor (líneas 23-29):

```java
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;

    public BookingCommandServiceImpl(BookingRepository bookingRepository, VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
    }
```

Reemplazar el cuerpo de `saveDraft` (líneas 31-48):

```java
    @Override
    @Transactional
    public SaveDraftResult saveDraft(SaveBookingDraftCommand command) {
        var vehicleOpt = vehicleRepository.findById(command.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return new SaveDraftResult(null, false, "Vehicle not found");
        }

        Booking booking = Booking.createDraftFrom(command);

        // Calculate cost based on vehicle price per minute
        BigDecimal pricePerMinute = BigDecimal.valueOf(vehicleOpt.get().getPricePerMinute());
        booking.calculateCost(pricePerMinute);

        Booking saved = bookingRepository.save(booking);

        return new SaveDraftResult(saved.getBookingId(), true, "Draft saved");
    }
```

`createBooking` (el resto del archivo) no cambia.

- [x] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=BookingCommandServiceImplTest test`
Expected: PASS — 2 tests, 0 failures.

- [x] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImpl.java src/test/java/org/example/backendweride/platform/booking/application/commandservice/BookingCommandServiceImplTest.java
git commit -m "fix: validate booking vehicle against real VehicleRepository instead of hardcoded catalog (B-2)"
```

---

### Task 3: `deleteDraft` nunca funcionó — usar el usuario autenticado (B-1)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java`
- Modify: `src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java` (ya existe, de Fase 1 — agregar un test)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe, de Fase 1).
- Produces: nada consumido por otras tareas.

- [x] **Step 1: Escribir el test que falla**

En `src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java`, agregar estos imports junto a los existentes:

```java
import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
```

Y agregar este método de test dentro de la clase `BookingControllerTest` (junto a los otros `@Test`):

```java
    @Test
    void deleteDraft_usesAuthenticatedUserId_notParsedFromUsername() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(draftService.deleteDraft(any())).thenReturn(new BookingCommandService.SaveDraftResult(555L, true, "Draft deleted successfully"));

        controller.deleteDraft(555L);

        ArgumentCaptor<DeleteBookingDraftCommand> captor = ArgumentCaptor.forClass(DeleteBookingDraftCommand.class);
        verify(draftService).deleteDraft(captor.capture());
        assertEquals(42L, captor.getValue().userId());
        assertEquals(555L, captor.getValue().draftId());
    }
```

(`draftService`, `authenticatedAccountProvider`, `controller`, y los imports de `verify`/`ArgumentCaptor`/`assertEquals` ya existen en la clase desde Fase 1 — no hace falta duplicarlos.)

- [x] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: FAIL — el test nuevo falla porque hoy `deleteDraft` intenta `Long.parseLong(authentication.getName())` sobre un `SecurityContext` vacío (nadie lo configuró en el test), lanzando `NullPointerException`, no usa `authenticatedAccountProvider`.

- [x] **Step 3: Implementar**

En `src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java`, quitar los imports que dejan de usarse (si no se usan en ningún otro método del archivo):

```java
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
```

Reemplazar el cuerpo de `deleteDraft`:

```java
    public ResponseEntity<String> deleteDraft(@PathVariable Long draftId) {
        DeleteBookingDraftCommand cmd = new DeleteBookingDraftCommand(draftId, authenticatedAccountProvider.getCurrentAccountId());
        var result = draftService.deleteDraft(cmd);

        if (!result.success()) {
            return ResponseEntity.badRequest().body(result.message());
        }

        return ResponseEntity.ok(result.message());
    }
```

- [x] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: PASS — 5 tests (los 4 de Fase 1 más este), 0 failures.

- [x] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java
git commit -m "fix: deleteDraft derives user id from JWT instead of parsing username as Long (B-1)"
```

---

### Task 4: Registro de usuario transaccional (B-4)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java`

**Interfaces:**
- Consumes: nada.
- Produces: nada consumido por otras tareas.

- [x] **Step 1: Agregar `@Transactional`**

En `src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java`, agregar el import junto a los existentes:

```java
import org.springframework.transaction.annotation.Transactional;
```

Y anotar el método `handle(SignUpCommand command)` (línea 31-32):

```java
    @Override
    @Transactional
    public Optional<Account> handle(SignUpCommand command) {
```

El resto del método no cambia — con `@Transactional`, si `profileContextFacade.createProfileForAccount(...)` lanza una excepción, Spring revierte también el `accountRepository.save(account)` anterior, evitando la cuenta huérfana que describe B-4.

No hay test para esta tarea: es una anotación declarativa que depende de un proxy transaccional de Spring (contexto real), y este proyecto no tiene infraestructura de test de integración (ver Global Constraints). Se verifica por compilación y revisión de código.

- [x] **Step 2: Confirmar que compila**

Run: `mvn.cmd -q compile`
Expected: BUILD SUCCESS, sin `[ERROR]`.

- [x] **Step 3: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java
git commit -m "fix: make sign-up transactional so a failed profile creation rolls back the account (B-4)"
```

---

### Task 5: La métrica `carbonSaved` siempre es falsa (B-5)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssembler.java`
- Test: `src/test/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssemblerTest.java` (crear)

**Interfaces:**
- Consumes: `Trip(CreateTripCommand)` constructor (ya existe, `src/main/java/org/example/backendweride/platform/trip/domain/aggregates/Trip.java:74-96`), `CreateTripCommand` record (ya existe, `src/main/java/org/example/backendweride/platform/trip/domain/commands/CreateTripCommand.java`).
- Produces: nada consumido por otras tareas.

- [x] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssemblerTest.java`:

```java
package org.example.backendweride.platform.trip.interfaces.transform;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TripResourceFromEntityAssemblerTest {

    @Test
    void toResource_mapsCarbonSavedFromEntity_notMaxSpeed() {
        CreateTripCommand command = new CreateTripCommand(
                1L, "user-1", 2L, 3L, 4L,
                "route", List.of(),
                new Date(), new Date(),
                30, 5.5f, 20.0f, 45.0f, 10.0f,
                2.5f,
                100, "sunny", 22, "completed",
                List.of(), List.of()
        );
        Trip trip = new Trip(command);

        var resource = TripResourceFromEntityAssembler.toResource(trip);

        assertEquals(2.5f, resource.carbonSaved());
        assertEquals(45.0f, resource.maxSpeed());
    }
}
```

- [x] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=TripResourceFromEntityAssemblerTest test`
Expected: FAIL — `resource.carbonSaved()` da `45.0` (el valor de `maxSpeed`), no `2.5`.

- [x] **Step 3: Implementar**

En `src/main/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssembler.java`, reemplazar la línea 25:

```java
                entity.getMaxSpeed(),
```

por:

```java
                entity.getCarbonSaved(),
```

(Esa es la línea que corresponde al parámetro `carbonSaved` del constructor de `TripResource` — la línea 23, que ya pasa `entity.getMaxSpeed()` correctamente para el parámetro `maxSpeed`, no cambia.)

- [x] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=TripResourceFromEntityAssemblerTest test`
Expected: PASS — 1 test, 0 failures.

- [x] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssembler.java src/test/java/org/example/backendweride/platform/trip/interfaces/transform/TripResourceFromEntityAssemblerTest.java
git commit -m "fix: map carbonSaved from the trip entity instead of duplicating maxSpeed (B-5)"
```

---

### Task 6: Healthcheck de MySQL en Docker Compose (ítem 15)

**Files:**
- Modify: `docker-compose.yml`

**Interfaces:**
- Consumes: nada.
- Produces: nada consumido por otras tareas.

- [x] **Step 1: Agregar `healthcheck` al servicio `mysql` y `depends_on` condicional al `backend`**

En `docker-compose.yml`, agregar `healthcheck:` al final del bloque `mysql:` (después de `networks: - app-net` de ese servicio):

```yaml
  mysql:
    image: mysql:8
    container_name: mysql-db
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: 12345678
      MYSQL_DATABASE: weride-db
      MYSQL_USER: weride_user
      MYSQL_PASSWORD: weride_pass
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - app-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p12345678"]
      interval: 5s
      timeout: 5s
      retries: 10
```

Y cambiar el `depends_on` del servicio `backend` de la forma corta a la forma condicional:

```yaml
  backend:
    build: .
    container_name: backend-weride
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/weride-db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: weride_user
      SPRING_DATASOURCE_PASSWORD: weride_pass
      # Dev-only secret for local docker-compose. Never reuse in a real deployment (Render/Vercel) —
      # production must set its own JWT_SECRET via the platform's env var config.
      JWT_SECRET: BLLZTBmA51ZnZnZ9mwRcmvfZJIru3GL2GqxMcvEtZBtI1m+BkzKLv3dAhqG1XfCx
    ports:
      - "8080:8080"
    networks:
      - app-net
```

El resto del archivo (`networks:`, `volumes:`) no cambia.

- [x] **Step 2: Verificar el YAML por inspección**

Docker no es utilizable en este entorno (`docker run` se cuelga indefinidamente) — no hay forma de correr `docker compose up` para confirmar que el healthcheck funciona de verdad. Verificar en su lugar, leyendo el archivo resultante:
- La indentación de `healthcheck:` está al mismo nivel que `image:`, `environment:`, `ports:`, `volumes:`, `networks:` dentro del servicio `mysql` (2 espacios más que `mysql:`).
- `depends_on:` del `backend` usa la forma de mapa (`mysql:` seguido de `condition: service_healthy` indentado), no la forma de lista (`- mysql`).
- El bloque `environment:` del `backend` sigue teniendo las 4 líneas que ya tenía (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` con su comentario) sin cambios de valor.

Dejar documentado en el commit/reporte que la verificación real (`docker compose up` y observar que el backend espera a que MySQL esté healthy antes de arrancar) queda pendiente para cuando Docker Desktop funcione en esta máquina.

- [x] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "fix: add MySQL healthcheck and make backend wait for it before starting (item 15)"
```
