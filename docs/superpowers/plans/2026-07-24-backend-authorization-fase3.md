# Backend Fase 3 — Autorización real Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cerrar el ítem 16 de la Fase 3 de la auditoría técnica WeRide (2026-07-23): comprobación de propiedad en todos los endpoints backend que devuelven o modifican un recurso perteneciente a un usuario específico (bookings, notifications, trips, profiles, accounts). Sigue a Fase 0 (seguridad), Fase 1 (login/JWT) y Fase 2 (integridad de datos), las tres en `main`/lista para mergear.

**Architecture:** 5 tasks independientes entre sí, uno por controller afectado. Mismo patrón en todos: comparar el id de dueño del recurso contra `authenticatedAccountProvider.getCurrentAccountId()` (ya existe, de Fase 1) antes de leer/modificar, devolviendo 404 si no coincide.

**Tech Stack:** Spring Boot 3.5.7, Spring Data JPA, JUnit 5 + Mockito.

## Global Constraints

- Java 24 / Spring Boot 3.5.7 — no cambiar versiones (`pom.xml`).
- No agregar dependencias nuevas al `pom.xml`.
- No construir infraestructura de test nueva (H2, perfiles, contexto Spring) — tests unitarios con Mockito puro, igual que en Fases 0-2.
- No tocar archivos de `Frontend-WeRide`.
- **Fuera de alcance, decisión explícita:** autorización por rol (admin) sobre `Vehicle`/`Plan`/`Location` — no existe ningún concepto de rol en el proyecto hoy (`UserDetailsImpl.build()` asigna `ROLE_CLIENT` a toda cuenta); introducir RBAC real es una decisión de producto, no un bug, y queda fuera de este plan.
- **Fuera de alcance:** `TravelHistoryController` — `UpdateTravelHistoryCommand.userId()` en el código actual en realidad contiene el id del registro de historial (no el id del usuario, ver `UpdateTravelHistoryCommandFromResourceAssembler.java:13`); arreglar la propiedad ahí sin antes resolver ese nombre confuso empeoraría la confusión. Se deja para un plan que trate ese archivo específicamente.
- **Fuera de alcance:** B-3, B-9, B-11, B-13, B-14, B-15, P-10 — hallazgos "Alto"/"Medio" reales pero no agendados por la propia auditoría en ninguna fase con fecha. Candidatos para un plan de "calidad" posterior.
- Respuesta ante "no es el dueño": siempre `404` (nunca `403`), para no confirmarle a un atacante que el recurso existe pero es ajeno — mismo criterio en los 5 tasks.
- Build de esta máquina, vía PowerShell:
  ```powershell
  $env:JAVA_HOME = "C:\Users\crama\.jdks\openjdk-26.0.2"
  & "C:\Users\crama\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd" -q -Dtest=<ClaseDeTest> test
  ```
  No ejecutar `mvn test` sin filtro (los dos tests preexistentes de contexto Spring fallan por razones ajenas a este plan, documentado en Fase 0).

---

### Task 1: `BookingController` — cerrar las 4 fugas de lectura entre usuarios

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/domain/model/queries/GetBookingsByVehicleQuery.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/domain/model/queries/GetBookingsByStatusQuery.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/infraestructure/persistence/jpa/BookingRepository.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImpl.java`
- Test: `src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java` (ya existe, agregar tests)
- Test: `src/test/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImplTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir los tests que fallan en `BookingControllerTest.java`**

Agregar este helper privado y estos 4 tests dentro de la clase `BookingControllerTest` (junto a los existentes):

```java
    private static BookingResource bookingResource(Long id, Long userId) {
        return new BookingResource(id, id, userId, 1L, 1L, 1L, null, null, null, null, null,
                "confirmed", null, null, null, null, null, null, null, null, null, List.of());
    }

    @Test
    void getBookingById_returnsNotFound_whenBookingBelongsToAnotherUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingById(any())).thenReturn(Optional.of(bookingResource(7L, 999L)));

        var response = controller.getBookingById(7L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getBookingById_returnsBooking_whenOwnedByCurrentUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingById(any())).thenReturn(Optional.of(bookingResource(7L, 42L)));

        var response = controller.getBookingById(7L);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void searchBookings_alwaysScopesToAuthenticatedUser_ignoringVehicleAndDateParams() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.searchBookings(any(), any())).thenReturn(EMPTY_PAGE);

        controller.searchBookings(999L, null, null, null, 0, 20);

        ArgumentCaptor<org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery> captor =
                ArgumentCaptor.forClass(org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery.class);
        verify(bookingQueryService).searchBookings(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().customerId());
    }

    @Test
    void getBookingsByVehicle_scopesToAuthenticatedUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingsByVehicle(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getBookingsByVehicle(5L, 0, 20);

        ArgumentCaptor<org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByVehicleQuery> captor =
                ArgumentCaptor.forClass(org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByVehicleQuery.class);
        verify(bookingQueryService).getBookingsByVehicle(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
        assertEquals(5L, captor.getValue().vehicleId());
    }

    @Test
    void getBookingsByStatus_scopesToAuthenticatedUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingsByStatus(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getBookingsByStatus("confirmed", 0, 20);

        ArgumentCaptor<org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByStatusQuery> captor =
                ArgumentCaptor.forClass(org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByStatusQuery.class);
        verify(bookingQueryService).getBookingsByStatus(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
        assertEquals("confirmed", captor.getValue().status());
    }
```

Agregar el import que falta junto a los existentes:

```java
import java.util.Optional;
```

(`List` ya está importado; `BookingResource` ya está importado.)

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: FAIL — no compila todavía (`getBookingsByVehicle`/`getBookingsByStatus` no tienen campo `userId()` en sus queries, `searchBookings` sigue con 7 parámetros incluyendo `customerId`).

- [ ] **Step 3: Implementar — `GetBookingsByVehicleQuery`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by vehicle ID with pagination, scoped to the authenticated user.
 *
 * @summary This query retrieves bookings for a specific vehicle that belong to a specific user, supporting pagination.
 */
public record GetBookingsByVehicleQuery(
    Long userId,
    Long vehicleId,
    int page,
    int size
) {
}
```

- [ ] **Step 4: Implementar — `GetBookingsByStatusQuery`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by status with pagination, scoped to the authenticated user.
 *
 * @summary This query retrieves bookings with a specific status that belong to a specific user, supporting pagination.
 */
public record GetBookingsByStatusQuery(
    Long userId,
    String status,
    int page,
    int size
) {
}
```

- [ ] **Step 5: Implementar — `BookingRepository`**

`findByUserIdAndStatus` (usado por `GetBookingsByStatusQuery`) ya existe, no requiere cambios. Para `GetBookingsByVehicleQuery` agregar un método derivado nuevo que filtre por dueño y vehículo a la vez (evita traer todas las reservas del vehículo y filtrar en memoria). Insertar esta línea después de `Page<Booking> findByVehicleId(Long vehicleId, Pageable pageable);`:

```java
    Page<Booking> findByUserIdAndVehicleId(Long userId, Long vehicleId, Pageable pageable);
```

- [ ] **Step 6: Implementar — `BookingController.getBookingById`**

Reemplazar (líneas 121-124):

```java
    public ResponseEntity<BookingResource> getBookingById(@PathVariable("id") Long id) {
        var opt = bookingQueryService.getBookingById(new GetBookingByIdQuery(id));
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
```

por:

```java
    public ResponseEntity<BookingResource> getBookingById(@PathVariable("id") Long id) {
        var opt = bookingQueryService.getBookingById(new GetBookingByIdQuery(id));
        return opt.filter(booking -> booking.userId().equals(authenticatedAccountProvider.getCurrentAccountId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
```

- [ ] **Step 7: Implementar — `BookingController.searchBookings`**

Reemplazar (líneas 143-165), quitando el parámetro `customerId` del cliente:

```java
    public ResponseEntity<Page<BookingResource>> searchBookings(
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String status,
        @RequestParam(required = false) String startAtFrom,
        @RequestParam(required = false) String startAtTo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate from = null;
        LocalDate to = null;
        try {
            if (startAtFrom != null) from = LocalDate.parse(startAtFrom);
            if (startAtTo != null) to = LocalDate.parse(startAtTo);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }

        SearchBookingsQuery q = new SearchBookingsQuery(authenticatedAccountProvider.getCurrentAccountId(), vehicleId, status, from, to, page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.searchBookings(q, pageable);
        return ResponseEntity.ok(results);
    }
```

(Nota: el test del Step 1 llama `controller.searchBookings(999L, null, null, null, 0, 20)` con 6 argumentos: `vehicleId=999L, status=null, startAtFrom=null, startAtTo=null, page=0, size=20` — ya no hay parámetro `customerId` en la firma.)

- [ ] **Step 8: Implementar — `BookingController.getBookingsByVehicle`**

Reemplazar (líneas 204-213):

```java
    public ResponseEntity<Page<BookingResource>> getBookingsByVehicle(
        @PathVariable Long vehicleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var q = new org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByVehicleQuery(
                authenticatedAccountProvider.getCurrentAccountId(), vehicleId, page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getBookingsByVehicle(q, pageable);
        return ResponseEntity.ok(results);
    }
```

- [ ] **Step 9: Implementar — `BookingController.getBookingsByStatus`**

Reemplazar (líneas 228-237):

```java
    public ResponseEntity<Page<BookingResource>> getBookingsByStatus(
        @PathVariable String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var q = new org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByStatusQuery(
                authenticatedAccountProvider.getCurrentAccountId(), status, page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getBookingsByStatus(q, pageable);
        return ResponseEntity.ok(results);
    }
```

- [ ] **Step 10: Implementar — `BookingQueryServiceImpl`**

Reemplazar `searchBookings` (líneas 58-91) — con `customerId` siempre presente, las ramas de solo-vehículo/solo-fecha/solo-status/todas quedan inalcanzables, se simplifican:

```java
    @Override
    public Page<BookingResource> searchBookings(SearchBookingsQuery q, Pageable pageable) {
        if (q == null || q.customerId() == null) return Page.empty(pageable);

        if (q.status() != null) {
            var page = bookingRepository.findByUserIdAndStatus(q.customerId(), q.status(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        var page = bookingRepository.findByUserId(q.customerId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }
```

Reemplazar `getBookingsByVehicle` (líneas 100-103):

```java
    @Override
    public Page<BookingResource> getBookingsByVehicle(GetBookingsByVehicleQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserIdAndVehicleId(q.userId(), q.vehicleId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }
```

Reemplazar `getBookingsByStatus` (líneas 105-109):

```java
    @Override
    public Page<BookingResource> getBookingsByStatus(GetBookingsByStatusQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserIdAndStatus(q.userId(), q.status(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }
```

- [ ] **Step 11: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: PASS — 9 tests (5 de Fase 1/2 más los 4 nuevos), 0 failures.

- [ ] **Step 12: Escribir y correr el test de `BookingQueryServiceImplTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImplTest.java`:

```java
package org.example.backendweride.platform.booking.application.queryservice;

import org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingQueryServiceImplTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final BookingQueryServiceImpl service = new BookingQueryServiceImpl(bookingRepository);
    private final Pageable pageable = PageRequest.of(0, 20);
    private final Page<org.example.backendweride.platform.booking.domain.model.aggregates.Booking> emptyPage = new PageImpl<>(List.of());

    @Test
    void searchBookings_withoutCustomerId_returnsEmptyPage() {
        var result = service.searchBookings(new SearchBookingsQuery(null, 5L, "confirmed", null, null, 0, 20), pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchBookings_withCustomerIdAndStatus_filtersByBoth() {
        when(bookingRepository.findByUserIdAndStatus(eq(42L), eq("confirmed"), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, 5L, "confirmed", null, null, 0, 20), pageable);

        verify(bookingRepository).findByUserIdAndStatus(42L, "confirmed", pageable);
    }

    @Test
    void searchBookings_withCustomerIdOnly_filtersByUser() {
        when(bookingRepository.findByUserId(eq(42L), any())).thenReturn(emptyPage);

        service.searchBookings(new SearchBookingsQuery(42L, null, null, null, null, 0, 20), pageable);

        verify(bookingRepository).findByUserId(42L, pageable);
    }
}
```

Run: `mvn.cmd -q -Dtest=BookingQueryServiceImplTest test`
Expected: PASS — 3 tests, 0 failures.

- [ ] **Step 13: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java src/main/java/org/example/backendweride/platform/booking/domain/model/queries/GetBookingsByVehicleQuery.java src/main/java/org/example/backendweride/platform/booking/domain/model/queries/GetBookingsByStatusQuery.java src/main/java/org/example/backendweride/platform/booking/infraestructure/persistence/jpa/BookingRepository.java src/main/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImpl.java src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java src/test/java/org/example/backendweride/platform/booking/application/queryservice/BookingQueryServiceImplTest.java
git commit -m "fix: scope booking reads (by id, search, vehicle, status) to the authenticated user (P-6)"
```

---

### Task 2: `NotificationsController` — cerrar lectura y escritura entre usuarios

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/notifications/interfaces/NotificationsController.java`
- Modify: `src/main/java/org/example/backendweride/platform/notifications/domain/model/commands/MarkNotificationAsReadCommand.java`
- Modify: `src/main/java/org/example/backendweride/platform/notifications/application/commandservices/NotificationCommandServiceImpl.java`
- Test: `src/test/java/org/example/backendweride/platform/notifications/interfaces/NotificationsControllerTest.java` (ya existe, agregar tests)
- Test: `src/test/java/org/example/backendweride/platform/notifications/application/commandservices/NotificationCommandServiceImplTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe), `Notification.getUserId(): String` (ya existe, vía Lombok `@Getter`).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir los tests que fallan en `NotificationsControllerTest.java`**

Agregar dentro de la clase:

```java
    @Test
    void getNotificationById_returnsNotFound_whenNotificationBelongsToAnotherUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        var foreignNotification = mock(org.example.backendweride.platform.notifications.domain.model.aggregates.Notification.class);
        when(foreignNotification.getUserId()).thenReturn("999");
        when(notificationQueryService.handle(any(org.example.backendweride.platform.notifications.domain.model.queries.GetNotificationByIdQuery.class)))
                .thenReturn(java.util.Optional.of(foreignNotification));

        var response = controller.getNotificationById("notif-001");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void markAsRead_passesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);

        controller.markAsRead("notif-001");

        ArgumentCaptor<org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand> captor =
                ArgumentCaptor.forClass(org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand.class);
        verify(notificationCommandService).handle(captor.capture());
        assertEquals("42", captor.getValue().userId());
        assertEquals("notif-001", captor.getValue().notificationId());
    }
```

Agregar el import que falta:

```java
import static org.mockito.Mockito.mock;
```

(ya está importado en el archivo actual — solo hace falta si no estuviera. `verify` y `ArgumentCaptor` ya están importados.)

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=NotificationsControllerTest test`
Expected: FAIL — no compila (`MarkNotificationAsReadCommand` no tiene un segundo componente `userId()` todavía).

- [ ] **Step 3: Implementar — `MarkNotificationAsReadCommand`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.notifications.domain.model.commands;

/**
 * Comando para marcar una notificación como leída.
 * Incluye el id del usuario autenticado para comprobar que la notificación le pertenece.
 */
public record MarkNotificationAsReadCommand(String notificationId, String userId) {
}
```

- [ ] **Step 4: Implementar — `NotificationsController`**

Reemplazar `getNotificationById` (líneas 67-74):

```java
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResource> getNotificationById(@PathVariable String notificationId) {
        var query = new GetNotificationByIdQuery(notificationId);
        var notification = notificationQueryService.handle(query);

        return notification
                .filter(entity -> entity.getUserId().equals(String.valueOf(authenticatedAccountProvider.getCurrentAccountId())))
                .map(entity -> ResponseEntity.ok(NotificationResourceFromEntityAssembler.toResourceFromEntity(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
```

Reemplazar `markAsRead` (líneas 76-81):

```java
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable String notificationId) {
        var command = new MarkNotificationAsReadCommand(notificationId, String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
        notificationCommandService.handle(command);
        return ResponseEntity.ok("Notification marked as read");
    }
```

- [ ] **Step 5: Implementar — `NotificationCommandServiceImpl.handle(MarkNotificationAsReadCommand)`**

Reemplazar (líneas correspondientes al método `handle(MarkNotificationAsReadCommand command)`):

```java
    @Override
    public void handle(MarkNotificationAsReadCommand command) {
        notificationRepository.findByPublicId(command.notificationId())
                .filter(notification -> notification.getUserId().equals(command.userId()))
                .map(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                    return notification;
                })
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + command.notificationId()));
    }
```

- [ ] **Step 6: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=NotificationsControllerTest test`
Expected: PASS — 3 tests (1 de Fase 1 más los 2 nuevos), 0 failures.

- [ ] **Step 7: Escribir y correr `NotificationCommandServiceImplTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/notifications/application/commandservices/NotificationCommandServiceImplTest.java`:

```java
package org.example.backendweride.platform.notifications.application.commandservices;

import org.example.backendweride.platform.notifications.domain.model.aggregates.Notification;
import org.example.backendweride.platform.notifications.domain.model.commands.MarkNotificationAsReadCommand;
import org.example.backendweride.platform.notifications.infrastructure.persistence.jpa.NotificationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NotificationCommandServiceImplTest {

    private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
    private final NotificationCommandServiceImpl service = new NotificationCommandServiceImpl(notificationRepository);

    @Test
    void markAsRead_throws_whenNotificationBelongsToAnotherUser() {
        Notification foreign = mock(Notification.class);
        when(foreign.getUserId()).thenReturn("999");
        when(notificationRepository.findByPublicId("notif-001")).thenReturn(Optional.of(foreign));

        assertThrows(RuntimeException.class, () ->
                service.handle(new MarkNotificationAsReadCommand("notif-001", "42")));

        verify(foreign, never()).markAsRead();
    }

    @Test
    void markAsRead_marksAsRead_whenOwnedByCurrentUser() {
        Notification owned = mock(Notification.class);
        when(owned.getUserId()).thenReturn("42");
        when(notificationRepository.findByPublicId("notif-001")).thenReturn(Optional.of(owned));

        service.handle(new MarkNotificationAsReadCommand("notif-001", "42"));

        verify(owned).markAsRead();
        verify(notificationRepository).save(owned);
    }
}
```

Run: `mvn.cmd -q -Dtest=NotificationCommandServiceImplTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/notifications/interfaces/NotificationsController.java src/main/java/org/example/backendweride/platform/notifications/domain/model/commands/MarkNotificationAsReadCommand.java src/main/java/org/example/backendweride/platform/notifications/application/commandservices/NotificationCommandServiceImpl.java src/test/java/org/example/backendweride/platform/notifications/interfaces/NotificationsControllerTest.java src/test/java/org/example/backendweride/platform/notifications/application/commandservices/NotificationCommandServiceImplTest.java
git commit -m "fix: reject reading or marking another user's notification as read (P-6)"
```

---

### Task 3: `TripController` — listar solo mis viajes y no poder borrar el viaje de otro

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/services/queries/TripQueryService.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/application/internal/queries/TripQueryServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/domain/services/commands/TripCommandService.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImpl.java`
- Modify: `src/main/java/org/example/backendweride/platform/trip/infrastructure/persistence/jpa/TripRepository.java`
- Test: `src/test/java/org/example/backendweride/platform/trip/interfaces/TripControllerTest.java` (crear)
- Test: `src/test/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImplTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe), `Trip.getUserId(): String` (ya existe, vía Lombok `@Getter`).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir el test que falla — `TripControllerTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/trip/interfaces/TripControllerTest.java`:

```java
package org.example.backendweride.platform.trip.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.trip.application.internal.commands.TripCommandServiceImpl;
import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripControllerTest {

    private final TripCommandServiceImpl tripCommandService = mock(TripCommandServiceImpl.class);
    private final TripQueryService tripQueryService = mock(TripQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final TripController controller = new TripController(tripCommandService, tripQueryService, authenticatedAccountProvider);

    @Test
    void getAllTrips_usesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(tripQueryService.handle("42")).thenReturn(Optional.of(List.of()));

        controller.getAllTrips();

        verify(tripQueryService).handle("42");
    }

    @Test
    void deleteTripById_passesAuthenticatedUserId() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);

        controller.deleteTripById(7L);

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(tripCommandService).handle(org.mockito.ArgumentMatchers.eq(7L), userIdCaptor.capture());
        assertEquals("42", userIdCaptor.getValue());
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=TripControllerTest test`
Expected: FAIL — no compila (`TripController` no tiene constructor de 3 argumentos, `TripQueryService.handle()` no acepta `String`, `TripCommandService.handle(Long)` no acepta un segundo argumento).

- [ ] **Step 3: Implementar — `TripQueryService` (interfaz)**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.trip.domain.services.queries;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;

import java.util.List;
import java.util.Optional;

public interface TripQueryService {
    Optional<List<Trip>> handle(String userId);
}
```

(Se quita el import no usado de `DeleteTripById` que ya existía.)

- [ ] **Step 4: Implementar — `TripQueryServiceImpl`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.trip.application.internal.queries;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TripQueryServiceImpl implements TripQueryService {
    private final TripRepository tripRepository;

    public TripQueryServiceImpl(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Override
    public Optional<List<Trip>> handle(String userId) {
        var trips = this.tripRepository.findByUserId(userId);
        return Optional.of(trips);
    }
}
```

- [ ] **Step 5: Implementar — `TripRepository`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.trip.infrastructure.persistence.jpa;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByUserId(String userId);
}
```

- [ ] **Step 6: Implementar — `TripCommandService` (interfaz)**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.trip.domain.services.commands;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.commands.CreateTripCommand;

import java.util.Optional;

public interface TripCommandService {
    Optional<Trip> handle(CreateTripCommand command);
    void handle(Long id, String userId);
}
```

(Se quita el import no usado de `DeleteTripById` que ya existía.)

- [ ] **Step 7: Implementar — `TripCommandServiceImpl`**

Reemplazar el método `handle(Long id)`:

```java
    @Override
    public void handle(Long id, String userId) {
        tripRepository.findById(id)
                .filter(trip -> trip.getUserId().equals(userId))
                .ifPresent(tripRepository::delete);
    }
```

- [ ] **Step 8: Implementar — `TripController`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.trip.interfaces;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.trip.application.internal.commands.TripCommandServiceImpl;
import org.example.backendweride.platform.trip.application.internal.queries.TripQueryServiceImpl;
import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.domain.services.commands.TripCommandService;
import org.example.backendweride.platform.trip.domain.services.queries.TripQueryService;
import org.example.backendweride.platform.trip.interfaces.resources.CreateTripCommandResource;
import org.example.backendweride.platform.trip.interfaces.resources.TripResource;
import org.example.backendweride.platform.trip.interfaces.transform.CreateTripCommandFromResourceAssembler;
import org.example.backendweride.platform.trip.interfaces.transform.TripResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/trips", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Trips")
public class TripController {
    private final TripCommandService tripCommandService;
    private final TripQueryService tripQueryService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public TripController(
            TripCommandServiceImpl tripCommandService,
            TripQueryService tripQueryService,
            AuthenticatedAccountProvider authenticatedAccountProvider
    ) {
        this.tripCommandService = tripCommandService;
        this.tripQueryService = tripQueryService;
        this.authenticatedAccountProvider = authenticatedAccountProvider;
    }

    @PostMapping
    public ResponseEntity<TripResource> createTrip(@RequestBody CreateTripCommandResource createTripCommandResource) {
        var result = this.tripCommandService.handle(CreateTripCommandFromResourceAssembler.toCommandFromResource(createTripCommandResource));
        return result.map(response -> new ResponseEntity<>(
                TripResourceFromEntityAssembler.toResource(response), HttpStatus.CREATED
                )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        var result = this.tripQueryService.handle(String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
        return result.map(response ->
                new ResponseEntity<>(response, HttpStatus.OK
        )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTripById(@PathVariable Long id) {
        this.tripCommandService.handle(id, String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
```

- [ ] **Step 9: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=TripControllerTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 10: Escribir y correr `TripCommandServiceImplTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImplTest.java`:

```java
package org.example.backendweride.platform.trip.application.internal.commands;

import org.example.backendweride.platform.trip.domain.aggregates.Trip;
import org.example.backendweride.platform.trip.infrastructure.persistence.jpa.TripRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class TripCommandServiceImplTest {

    private final TripRepository tripRepository = mock(TripRepository.class);
    private final TripCommandServiceImpl service = new TripCommandServiceImpl(tripRepository);

    @Test
    void deleteById_doesNothing_whenTripBelongsToAnotherUser() {
        Trip foreign = mock(Trip.class);
        when(foreign.getUserId()).thenReturn("999");
        when(tripRepository.findById(7L)).thenReturn(Optional.of(foreign));

        service.handle(7L, "42");

        verify(tripRepository, never()).delete(any());
    }

    @Test
    void deleteById_deletes_whenOwnedByCurrentUser() {
        Trip owned = mock(Trip.class);
        when(owned.getUserId()).thenReturn("42");
        when(tripRepository.findById(7L)).thenReturn(Optional.of(owned));

        service.handle(7L, "42");

        verify(tripRepository).delete(owned);
    }
}
```

Run: `mvn.cmd -q -Dtest=TripCommandServiceImplTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 11: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/trip/interfaces/TripController.java src/main/java/org/example/backendweride/platform/trip/domain/services/queries/TripQueryService.java src/main/java/org/example/backendweride/platform/trip/application/internal/queries/TripQueryServiceImpl.java src/main/java/org/example/backendweride/platform/trip/domain/services/commands/TripCommandService.java src/main/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImpl.java src/main/java/org/example/backendweride/platform/trip/infrastructure/persistence/jpa/TripRepository.java src/test/java/org/example/backendweride/platform/trip/interfaces/TripControllerTest.java src/test/java/org/example/backendweride/platform/trip/application/internal/commands/TripCommandServiceImplTest.java
git commit -m "fix: list only the authenticated user's trips and block deleting another user's trip (P-6)"
```

---

### Task 4: `ProfileController` — no exponer el perfil de otro usuario

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java`
- Test: `src/test/java/org/example/backendweride/platform/profile/interfaces/ProfileControllerTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe), `Profile.getUserId(): Long` (ya existe, vía Lombok `@Getter`).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir el test que falla — `ProfileControllerTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/profile/interfaces/ProfileControllerTest.java`:

```java
package org.example.backendweride.platform.profile.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.services.commands.ProfileCommandService;
import org.example.backendweride.platform.profile.domain.services.queries.ProfileQueryService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    private final ProfileCommandService profileCommandService = mock(ProfileCommandService.class);
    private final ProfileQueryService profileQueryService = mock(ProfileQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final ProfileController controller = new ProfileController(
            profileCommandService, profileQueryService, authenticatedAccountProvider);

    @Test
    void getProfileById_returnsNotFound_whenProfileBelongsToAnotherUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        Profile foreign = mock(Profile.class);
        when(foreign.getUserId()).thenReturn(999L);
        when(profileQueryService.handle(3L)).thenReturn(Optional.of(foreign));

        var response = controller.getProfileById(3L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getProfileById_returnsProfile_whenOwnedByCurrentUser() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        Profile owned = mock(Profile.class);
        when(owned.getUserId()).thenReturn(42L);
        when(profileQueryService.handle(3L)).thenReturn(Optional.of(owned));

        var response = controller.getProfileById(3L);

        assertEquals(200, response.getStatusCode().value());
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=ProfileControllerTest test`
Expected: FAIL — no compila (`ProfileController` no tiene un constructor de 3 argumentos).

- [ ] **Step 3: Implementar — `ProfileController`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.profile.interfaces;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.services.commands.ProfileCommandService;
import org.example.backendweride.platform.profile.domain.services.queries.ProfileQueryService;
import org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource;
import org.example.backendweride.platform.profile.interfaces.resources.ProfileResource;
import org.example.backendweride.platform.profile.interfaces.transform.CreateProfileCommandFromResourceAssembler;
import org.example.backendweride.platform.profile.interfaces.transform.ProfileResourceFromEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "profiles")
public class ProfileController {
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public ProfileController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService, AuthenticatedAccountProvider authenticatedAccountProvider) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService = profileQueryService;
        this.authenticatedAccountProvider = authenticatedAccountProvider;
    }

    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileCommandResource profileResource) {
        var result = this.profileCommandService.handle(CreateProfileCommandFromResourceAssembler.toCommandFromResource(profileResource));

        return result.map(response -> new ResponseEntity<>(
                ProfileResourceFromEntity.tpProfileResourceFromEntity(response), CREATED
        )).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        var result = this.profileQueryService.handle(id);
        return result.filter(profile -> profile.getUserId().equals(authenticatedAccountProvider.getCurrentAccountId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
```

(De paso corrige un bug adyacente en la misma línea: `getProfileById` devolvía `201 CREATED` en vez de `200 OK` para un `GET` exitoso.)

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=ProfileControllerTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java src/test/java/org/example/backendweride/platform/profile/interfaces/ProfileControllerTest.java
git commit -m "fix: reject reading another user's profile, return 200 instead of 201 on GET (P-6)"
```

---

### Task 5: `AccountsController` — no exponer la cuenta de otro usuario

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/iam/interfaces/rest/AccountsController.java`
- Test: `src/test/java/org/example/backendweride/platform/iam/interfaces/rest/AccountsControllerTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (ya existe).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir el test que falla — `AccountsControllerTest` (nuevo)**

Crear `src/test/java/org/example/backendweride/platform/iam/interfaces/rest/AccountsControllerTest.java`:

```java
package org.example.backendweride.platform.iam.interfaces.rest;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.iam.domain.services.AccountQueryService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AccountsControllerTest {

    private final AccountQueryService accountQueryService = mock(AccountQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final AccountsController controller = new AccountsController(accountQueryService, authenticatedAccountProvider);

    @Test
    void getAccountById_returnsNotFound_whenRequestingAnotherAccount() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);

        var response = controller.getAccountById(999L);

        assertEquals(404, response.getStatusCode().value());
        verifyNoInteractions(accountQueryService);
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=AccountsControllerTest test`
Expected: FAIL — no compila (`AccountsController` no tiene un constructor de 2 argumentos).

- [ ] **Step 3: Implementar — `AccountsController`**

Reemplazar el archivo completo:

```java
package org.example.backendweride.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.iam.domain.model.queries.GetAccountByIdQuery;
import org.example.backendweride.platform.iam.domain.services.AccountQueryService;
import org.example.backendweride.platform.iam.interfaces.rest.resources.AccountResource;
import org.example.backendweride.platform.iam.interfaces.rest.transform.AccountResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Accounts Controller
 *
 * @summary Handles HTTP requests related to account retrieval.
 */
@RestController
@RequestMapping(value = "api/v1/accounts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Accounts", description = "Manage User Accounts")
public class AccountsController {
    private final AccountQueryService accountQueryService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public AccountsController(AccountQueryService accountQueryService, AuthenticatedAccountProvider authenticatedAccountProvider) {
        this.accountQueryService = accountQueryService;
        this.authenticatedAccountProvider = authenticatedAccountProvider;
    }

    /**
     * Get account by ID.
     * @param accountId The ID of the account to retrieve.
     * @return ResponseEntity containing the account resource or a not found status.
     */
    @GetMapping("/{accountId}")
    @Operation (summary = "Get account by ID", description = "Retrieve account details using the account ID.")
    @ApiResponses (value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountResource> getAccountById(@PathVariable Long accountId) {
        if (!accountId.equals(authenticatedAccountProvider.getCurrentAccountId())) {
            return ResponseEntity.notFound().build();
        }

        var getAccountByIdQuery = new GetAccountByIdQuery(accountId);
        var account = accountQueryService.handle(getAccountByIdQuery);
        if(account.isEmpty()) return ResponseEntity.notFound().build();

        var userResource = AccountResourceFromEntityAssembler.toResourceFromEntity(account.get());
        return ResponseEntity.ok(userResource);
    }
}
```

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=AccountsControllerTest test`
Expected: PASS — 1 test, 0 failures.

- [ ] **Step 5: Confirmar que compila todo el proyecto**

Run: `mvn.cmd -q compile`
Expected: BUILD SUCCESS, sin `[ERROR]`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/interfaces/rest/AccountsController.java src/test/java/org/example/backendweride/platform/iam/interfaces/rest/AccountsControllerTest.java
git commit -m "fix: reject reading another account's data by id (P-6)"
```
