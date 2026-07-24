# Backend Fase 1 — Autorización real (P-6) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cerrar el hallazgo P-6 de la auditoría técnica WeRide (2026-07-23): 5 endpoints backend que hoy confían en el ID de usuario que manda el cliente (path/query param) en lugar de derivarlo del JWT verificado, permitiendo que cualquier usuario autenticado lea las reservas o notificaciones de cualquier otro.

**Architecture:** Un helper nuevo (`AuthenticatedAccountProvider`) resuelve el `Account.id` del usuario autenticado a partir del `SecurityContext` (que ya contiene el username validado por el filtro JWT existente). Los controllers de `booking` y `notifications` dejan de usar el id que llega en la URL para filtrar y usan el que devuelve este helper. Sigue a la Fase 0 (seguridad crítica, ya mergeada a `main`).

**Tech Stack:** Spring Boot 3.5.7, Spring Security, JUnit 5 + Mockito (`spring-boot-starter-test`).

## Global Constraints

- Java 24 / Spring Boot 3.5.7 — no cambiar versiones (`pom.xml`).
- No agregar dependencias nuevas al `pom.xml`.
- No construir infraestructura de test nueva (H2, perfiles, MockMvc con contexto Spring) — los tests de este plan son unitarios puros con Mockito, igual que en Fase 0.
- No tocar archivos de `Frontend-WeRide`. Las firmas de los endpoints (verbo HTTP, ruta, `@PathVariable`/`@RequestParam` en la firma del método) no cambian — solo deja de usarse el valor recibido para filtrar. Esto evita romper el contrato de URL que el frontend ya usa.
- Build environment de esta máquina: no hay `mvn` en el PATH ni wrapper en el repo. Usar la distribución de Maven cacheada, vía PowerShell (no bash, para que la variable de entorno se fije bien):
  ```powershell
  $env:JAVA_HOME = "C:\Users\crama\.jdks\openjdk-26.0.2"
  & "C:\Users\crama\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd" -q -Dtest=<ClaseDeTest> test
  ```
  Docker no es utilizable en este entorno (`docker run` se cuelga indefinidamente) — no intentar verificación con Docker.
- Los dos tests preexistentes (`BackendWerideApplicationTests`, `WeRidePlatformApplicationTests`) fallan por razones ajenas a este plan (documentado en Fase 0) — no ejecutar la suite completa con `mvn test`, ejecutar solo las clases de test nuevas con `-Dtest=<ClaseDeTest>`.

---

### Task 1: `AuthenticatedAccountProvider`

**Files:**
- Create: `src/main/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProvider.java`
- Test: `src/test/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProviderTest.java`

**Interfaces:**
- Consumes: `AccountRepository.findByUserName(String): Optional<Account>` (ya existe, `src/main/java/org/example/backendweride/platform/iam/infrastructure/persistence/jpa/repositories/AccountRepository.java`), `Account.getId(): Long` (ya existe vía Lombok `@Getter`), `SecurityContextHolder.getContext().getAuthentication().getName(): String` (ya es como el resto del proyecto obtiene el username autenticado — ver `BookingController.deleteDraft`).
- Produces: `AuthenticatedAccountProvider.getCurrentAccountId(): Long`. Las Tareas 2 y 3 inyectan este componente por constructor y llaman a este método.

- [ ] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProviderTest.java`:

```java
package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticatedAccountProviderTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AuthenticatedAccountProvider provider = new AuthenticatedAccountProvider(accountRepository);

    private void authenticateAs(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAccountIdForAuthenticatedUsername() {
        authenticateAs("auditor@weride.com");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(42L);
        when(accountRepository.findByUserName("auditor@weride.com")).thenReturn(Optional.of(account));

        Long id = provider.getCurrentAccountId();

        assertEquals(42L, id);
    }

    @Test
    void throwsWhenAuthenticatedUsernameHasNoAccount() {
        authenticateAs("ghost@weride.com");
        when(accountRepository.findByUserName("ghost@weride.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, provider::getCurrentAccountId);
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=AuthenticatedAccountProviderTest test` (con `JAVA_HOME` fijado como se indica en Global Constraints)
Expected: FAIL con "cannot find symbol: class AuthenticatedAccountProvider" — la clase todavía no existe.

- [ ] **Step 3: Implementar**

Crear `src/main/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProvider.java`:

```java
package org.example.backendweride.platform.iam.application.internal.outboundservices.security;

import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the Account.id of the currently authenticated user from the SecurityContext.
 * UserDetailsImpl only carries the username, so this requires one repository lookup.
 */
@Component
public class AuthenticatedAccountProvider {

    private final AccountRepository accountRepository;

    public AuthenticatedAccountProvider(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Long getCurrentAccountId() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountRepository.findByUserName(username)
                .map(org.example.backendweride.platform.iam.domain.model.aggregates.Account::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated account not found"));
    }
}
```

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=AuthenticatedAccountProviderTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProvider.java src/test/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProviderTest.java
git commit -m "feat: add AuthenticatedAccountProvider to resolve account id from JWT (P-6)"
```

---

### Task 2: `BookingController` deja de confiar en el userId del cliente

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java`
- Test: `src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (Tarea 1). `BookingQueryService.getBookingsByUserId(GetBookingsByUserIdQuery, Pageable): Page<BookingResource>`, `.getPendingBookingsByUser(GetPendingBookingsByUserQuery, Pageable): Page<BookingResource>`, `.getCompletedBookingsByUser(GetCompletedBookingsByUserQuery, Pageable): Page<BookingResource>`, `.getBookingDraftsByCustomer(GetBookingDraftsByCustomerQuery, Pageable): Page<BookingResource>` (todas ya existen en `src/main/java/org/example/backendweride/platform/booking/domain/services/BookingQueryService.java`). `GetBookingsByUserIdQuery(Long userId)`, `GetPendingBookingsByUserQuery(Long userId)`, `GetCompletedBookingsByUserQuery(Long userId)`, `GetBookingDraftsByCustomerQuery(Long customerId, int page, int size)` (records ya existentes).
- Produces: nada consumido por otras tareas — Task 2 y Task 3 son independientes entre sí, ambas dependen solo de Task 1.

- [ ] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java`:

```java
package org.example.backendweride.platform.booking.interfaces;

import org.example.backendweride.platform.booking.domain.model.queries.GetBookingDraftsByCustomerQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByUserIdQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetCompletedBookingsByUserQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetPendingBookingsByUserQuery;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.services.BookingDraftService;
import org.example.backendweride.platform.booking.domain.services.BookingQueryService;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    private final BookingCommandService commandService = mock(BookingCommandService.class);
    private final BookingQueryService bookingQueryService = mock(BookingQueryService.class);
    private final BookingDraftService draftService = mock(BookingDraftService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final BookingController controller = new BookingController(
            commandService, bookingQueryService, draftService, authenticatedAccountProvider);

    private static final Page<BookingResource> EMPTY_PAGE = new PageImpl<>(List.of());

    @Test
    void getBookingsByUserId_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingsByUserId(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getBookingsByUserId(999L, 0, 20);

        ArgumentCaptor<GetBookingsByUserIdQuery> captor = ArgumentCaptor.forClass(GetBookingsByUserIdQuery.class);
        verify(bookingQueryService).getBookingsByUserId(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getPendingBookingsByUser_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getPendingBookingsByUser(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getPendingBookingsByUser(999L, 0, 20);

        ArgumentCaptor<GetPendingBookingsByUserQuery> captor = ArgumentCaptor.forClass(GetPendingBookingsByUserQuery.class);
        verify(bookingQueryService).getPendingBookingsByUser(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getCompletedBookingsByUser_usesAuthenticatedUserId_notPathVariable() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getCompletedBookingsByUser(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getCompletedBookingsByUser(999L, 0, 20);

        ArgumentCaptor<GetCompletedBookingsByUserQuery> captor = ArgumentCaptor.forClass(GetCompletedBookingsByUserQuery.class);
        verify(bookingQueryService).getCompletedBookingsByUser(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().userId());
    }

    @Test
    void getDraftsByCustomer_usesAuthenticatedUserId_notQueryParam() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(bookingQueryService.getBookingDraftsByCustomer(any(), any())).thenReturn(EMPTY_PAGE);

        controller.getDraftsByCustomer(999L, 0, 20);

        ArgumentCaptor<GetBookingDraftsByCustomerQuery> captor = ArgumentCaptor.forClass(GetBookingDraftsByCustomerQuery.class);
        verify(bookingQueryService).getBookingDraftsByCustomer(captor.capture(), any(Pageable.class));
        assertEquals(42L, captor.getValue().customerId());
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: FAIL — no compila (`BookingController` no tiene un constructor de 4 argumentos que acepte `AuthenticatedAccountProvider`), o si compila por casualidad, los 4 tests fallan porque hoy la query se construye con `999L`/el valor recibido, no con `42L`.

- [ ] **Step 3: Implementar**

En `src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java`:

Agregar el import (junto a los demás imports del paquete `booking`):

```java
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
```

Reemplazar los campos y el constructor (líneas 54-65 del archivo original):

```java
    private final BookingCommandService commandService;
    private final BookingQueryService bookingQueryService;
    private final BookingDraftService draftService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public BookingController(BookingCommandService commandService,
                           BookingQueryService bookingQueryService,
                           BookingDraftService draftService,
                           AuthenticatedAccountProvider authenticatedAccountProvider) {
        this.commandService = commandService;
        this.bookingQueryService = bookingQueryService;
        this.draftService = draftService;
        this.authenticatedAccountProvider = authenticatedAccountProvider;
    }
```

Reemplazar el cuerpo de `getDraftsByCustomer` (la línea que construye `GetBookingDraftsByCustomerQuery`) — el método sigue recibiendo `@RequestParam Long customerId` en su firma (no se rompe la URL), pero deja de usar ese valor:

```java
    public ResponseEntity<Page<BookingResource>> getDraftsByCustomer(
        @RequestParam Long customerId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        GetBookingDraftsByCustomerQuery q = new GetBookingDraftsByCustomerQuery(authenticatedAccountProvider.getCurrentAccountId(), page, size);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getBookingDraftsByCustomer(q, pageable);
        return ResponseEntity.ok(results);
    }
```

Reemplazar el cuerpo de `getBookingsByUserId` (misma idea, sigue recibiendo `@PathVariable Long userId`):

```java
    public ResponseEntity<Page<BookingResource>> getBookingsByUserId(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var q = new org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByUserIdQuery(authenticatedAccountProvider.getCurrentAccountId());
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getBookingsByUserId(q, pageable);
        return ResponseEntity.ok(results);
    }
```

Reemplazar el cuerpo de `getPendingBookingsByUser`:

```java
    public ResponseEntity<Page<BookingResource>> getPendingBookingsByUser(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var q = new org.example.backendweride.platform.booking.domain.model.queries.GetPendingBookingsByUserQuery(authenticatedAccountProvider.getCurrentAccountId());
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getPendingBookingsByUser(q, pageable);
        return ResponseEntity.ok(results);
    }
```

Reemplazar el cuerpo de `getCompletedBookingsByUser`:

```java
    public ResponseEntity<Page<BookingResource>> getCompletedBookingsByUser(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var q = new org.example.backendweride.platform.booking.domain.model.queries.GetCompletedBookingsByUserQuery(authenticatedAccountProvider.getCurrentAccountId());
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var results = bookingQueryService.getCompletedBookingsByUser(q, pageable);
        return ResponseEntity.ok(results);
    }
```

Ningún otro método de `BookingController` cambia (`saveDraft`, `createBooking`, `getBookingById`, `searchBookings`, `getBookingsByVehicle`, `getBookingsByStatus`, `deleteDraft` quedan igual — `searchBookings` toma `customerId` como filtro opcional de búsqueda general, no es un endpoint "mis reservas", está fuera del alcance de P-6 según la auditoría).

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=BookingControllerTest test`
Expected: PASS — 4 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/booking/interfaces/BookingController.java src/test/java/org/example/backendweride/platform/booking/interfaces/BookingControllerTest.java
git commit -m "fix: derive booking user id from JWT instead of client-supplied path/query param (P-6)"
```

---

### Task 3: `NotificationsController` deja de confiar en el userId del cliente

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/notifications/interfaces/NotificationsController.java`
- Test: `src/test/java/org/example/backendweride/platform/notifications/interfaces/NotificationsControllerTest.java` (crear)

**Interfaces:**
- Consumes: `AuthenticatedAccountProvider.getCurrentAccountId(): Long` (Tarea 1). `NotificationQueryService.handle(GetAllNotificationsByUserIdQuery): List<Notification>` (ya existe, `src/main/java/org/example/backendweride/platform/notifications/domain/services/NotificationQueryService.java`). `GetAllNotificationsByUserIdQuery(String userId)` (record ya existente).
- Produces: nada consumido por otras tareas.

- [ ] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/notifications/interfaces/NotificationsControllerTest.java`:

```java
package org.example.backendweride.platform.notifications.interfaces;

import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
import org.example.backendweride.platform.notifications.domain.model.queries.GetAllNotificationsByUserIdQuery;
import org.example.backendweride.platform.notifications.domain.services.NotificationCommandService;
import org.example.backendweride.platform.notifications.domain.services.NotificationQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationsControllerTest {

    private final NotificationCommandService notificationCommandService = mock(NotificationCommandService.class);
    private final NotificationQueryService notificationQueryService = mock(NotificationQueryService.class);
    private final AuthenticatedAccountProvider authenticatedAccountProvider = mock(AuthenticatedAccountProvider.class);

    private final NotificationsController controller = new NotificationsController(
            notificationCommandService, notificationQueryService, authenticatedAccountProvider);

    @Test
    void getAllNotificationsByUserId_usesAuthenticatedUserId_notQueryParam() {
        when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
        when(notificationQueryService.handle(any())).thenReturn(List.of());

        controller.getAllNotificationsByUserId("999");

        ArgumentCaptor<GetAllNotificationsByUserIdQuery> captor = ArgumentCaptor.forClass(GetAllNotificationsByUserIdQuery.class);
        verify(notificationQueryService).handle(captor.capture());
        assertEquals("42", captor.getValue().userId());
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn.cmd -q -Dtest=NotificationsControllerTest test`
Expected: FAIL — `NotificationsController` no tiene un constructor de 3 argumentos que acepte `AuthenticatedAccountProvider`.

- [ ] **Step 3: Implementar**

En `src/main/java/org/example/backendweride/platform/notifications/interfaces/NotificationsController.java`:

Agregar el import:

```java
import org.example.backendweride.platform.iam.application.internal.outboundservices.security.AuthenticatedAccountProvider;
```

Reemplazar los campos y el constructor (líneas 27-33 del archivo original):

```java
    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final AuthenticatedAccountProvider authenticatedAccountProvider;

    public NotificationsController(NotificationCommandService notificationCommandService, NotificationQueryService notificationQueryService, AuthenticatedAccountProvider authenticatedAccountProvider) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
        this.authenticatedAccountProvider = authenticatedAccountProvider;
    }
```

Reemplazar el cuerpo de `getAllNotificationsByUserId` (sigue recibiendo `@RequestParam String userId` en su firma, no se rompe la URL, pero deja de usar ese valor):

```java
    @GetMapping
    public ResponseEntity<List<NotificationResource>> getAllNotificationsByUserId(@RequestParam String userId) {
        var query = new GetAllNotificationsByUserIdQuery(String.valueOf(authenticatedAccountProvider.getCurrentAccountId()));
        var notifications = notificationQueryService.handle(query);

        var resources = notifications.stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
```

Ningún otro método (`createNotification`, `getNotificationById`, `markAsRead`) cambia.

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn.cmd -q -Dtest=NotificationsControllerTest test`
Expected: PASS — 1 test, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/notifications/interfaces/NotificationsController.java src/test/java/org/example/backendweride/platform/notifications/interfaces/NotificationsControllerTest.java
git commit -m "fix: derive notification user id from JWT instead of client-supplied query param (P-6)"
```
