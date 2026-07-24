# Backend Fase 5 — Cierre de pendientes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cerrar los dos pendientes de backend que quedaron tras la Fase 4: `POST /profiles` sin validar el `userId` contra el JWT, y código muerto (B-2/B-3 + un test huérfano) que no aporta nada y confunde a quien lea el proyecto.

**Architecture:** Un fix de una línea (mismo patrón ya usado en Fase 4 para `createTrip`) y un borrado de archivos/métodos sin ningún caller. Sin abstracciones nuevas, sin infraestructura nueva.

**Tech Stack:** Spring Boot 3.5.7, Java 24, JUnit 5 + Mockito (sin contexto Spring).

## Global Constraints

- Solo backend.
- Build de verificación (nunca `mvn test` sin filtrar — 2 tests de contexto Spring pre-existentes fallan por razones no relacionadas, documentado desde Fase 0):
  ```powershell
  $env:JAVA_HOME = "C:\Users\crama\.jdks\openjdk-26.0.2"
  & "C:\Users\crama\.m2\wrapper\dists\apache-maven-3.9.11-bin\6mqf5t809d9geo83kj4ttckcbc\apache-maven-3.9.11\bin\mvn.cmd" -q -Dtest=<ClassName> test
  ```
- No se toca `-DskipTests` en el `Dockerfile` — ver el design spec de esta fase para el porqué (el build de la imagen no tiene MySQL disponible, arreglarlo de verdad requiere infraestructura de test nueva, fuera de alcance).
- No se construye RBAC, ni tests con H2, ni nada de frontend.

---

### Task 1: `POST /profiles` deriva el `userId` del JWT

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/transform/CreateProfileCommandFromResourceAssembler.java`
- Modify: `src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java`
- Test: `src/test/java/org/example/backendweride/platform/profile/interfaces/ProfileControllerTest.java` (extender)

**Interfaces:**
- `CreateProfileCommandFromResourceAssembler.toCommandFromResource` gana un segundo parámetro `Long userId` que sustituye a `resource.userId()`.

- [ ] **Step 1: Escribir el test que reproduce el problema**

```java
@Test
void createProfile_usesAuthenticatedUserIdNotResourceBody() {
    when(authenticatedAccountProvider.getCurrentAccountId()).thenReturn(42L);
    var resource = new CreateProfileCommandResource(999L, "Ada", "Lovelace", "ada@weride.com");
    ArgumentCaptor<CreateProfileCommand> captor = ArgumentCaptor.forClass(CreateProfileCommand.class);
    when(profileCommandService.handle(captor.capture())).thenReturn(Optional.empty());

    controller.createProfile(resource);

    assertEquals(42L, captor.getValue().userId());
}
```

(Añadir los imports que falten: `org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand`, `org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource`, `org.mockito.ArgumentCaptor`.)

- [ ] **Step 2: Ejecutar y confirmar que falla**

Run: `mvn -Dtest=ProfileControllerTest test`
Expected: FAIL — hoy el comando usa `resource.userId()` (999L), no el `42L` del JWT.

- [ ] **Step 3: Cambiar el assembler y el controller**

```java
// CreateProfileCommandFromResourceAssembler.java
package org.example.backendweride.platform.profile.interfaces.transform;

import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource;

public class CreateProfileCommandFromResourceAssembler {
    public static CreateProfileCommand toCommandFromResource(CreateProfileCommandResource resource, Long userId) {
        return new CreateProfileCommand(
                userId,
                resource.firstName(),
                resource.lastName(),
                resource.email()
        );
    }
}
```

```java
// ProfileController.java — createProfile
@PostMapping
public ResponseEntity<ProfileResource> createProfile(@Valid @RequestBody CreateProfileCommandResource profileResource) {
    var userId = authenticatedAccountProvider.getCurrentAccountId();
    var result = this.profileCommandService.handle(
            CreateProfileCommandFromResourceAssembler.toCommandFromResource(profileResource, userId));

    return result.map(response -> new ResponseEntity<>(
            ProfileResourceFromEntity.tpProfileResourceFromEntity(response), CREATED
    )).orElseGet(() -> ResponseEntity.notFound().build());
}
```

- [ ] **Step 4: Ejecutar y confirmar que pasa**

Run: `mvn -Dtest=ProfileControllerTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/profile/interfaces/transform/CreateProfileCommandFromResourceAssembler.java \
        src/main/java/org/example/backendweride/platform/profile/interfaces/ProfileController.java \
        src/test/java/org/example/backendweride/platform/profile/interfaces/ProfileControllerTest.java
git commit -m "fix: derive profile owner id from JWT instead of trusting the request body"
```

---

### Task 2: Borrar código muerto (B-2/B-3 + test huérfano de B-10)

**Files:**
- Delete: `src/main/java/org/example/backendweride/platform/booking/domain/model/commands/UpdateBookingDraftCommand.java`
- Delete: `src/main/java/org/example/backendweride/platform/booking/domain/services/VehicleCatalogService.java`
- Delete: `src/test/java/com/weride/platform/WeRidePlatformApplicationTests.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/domain/services/BookingDraftService.java`
- Modify: `src/main/java/org/example/backendweride/platform/booking/application/commandservice/BookingDraftServiceImpl.java`
- Modify: `src/test/java/org/example/backendweride/platform/booking/application/commandservice/BookingDraftServiceImplTest.java`

**Interfaces:**
- `BookingDraftService` pierde el método `updateDraft(SaveBookingDraftCommand)`.
- `BookingDraftServiceImpl` pierde el constructor de 3 argumentos (`bookingRepository, vehicleCatalogService, validationService`) a favor de uno de 2 (`bookingRepository, validationService`).

Confirmado por `grep` (Fase 4) que ninguno de estos símbolos tiene caller fuera de estos mismos archivos y del test que se está actualizando.

- [ ] **Step 1: Borrar los 2 archivos muertos y el test huérfano**

```bash
git rm src/main/java/org/example/backendweride/platform/booking/domain/model/commands/UpdateBookingDraftCommand.java
git rm src/main/java/org/example/backendweride/platform/booking/domain/services/VehicleCatalogService.java
git rm src/test/java/com/weride/platform/WeRidePlatformApplicationTests.java
```

- [ ] **Step 2: Quitar `updateDraft` de la interfaz `BookingDraftService`**

```java
// BookingDraftService.java — queda así, sin el método updateDraft
package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;

import java.util.Optional;

public interface BookingDraftService {
    BookingCommandService.SaveDraftResult saveDraft(SaveBookingDraftCommand command);
    BookingCommandService.SaveDraftResult deleteDraft(DeleteBookingDraftCommand command);
    Optional<SaveBookingDraftCommand> getDraftById(Long draftId);
}
```

(Verificar el archivo real antes de sobrescribir — mantener exactamente los mismos métodos que ya existen hoy salvo `updateDraft`, que se quita.)

- [ ] **Step 3: Quitar `updateDraft`, el campo `vehicleCatalogService` y su import de `BookingDraftServiceImpl`**

```java
// BookingDraftServiceImpl.java
package org.example.backendweride.platform.booking.application.commandservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.backendweride.platform.booking.domain.services.BookingDraftService;
import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.services.BookingDraftValidationService;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.DeleteBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;

import java.util.Optional;

/**
 * Implementation of BookingDraftService to handle booking draft operations.
 *
 * @summary This service processes commands for saving and deleting booking drafts.
 */
@Service
public class BookingDraftServiceImpl implements BookingDraftService {

    private final BookingRepository bookingRepository;
    private final BookingDraftValidationService validationService;

    public BookingDraftServiceImpl(
        BookingRepository bookingRepository,
        BookingDraftValidationService validationService
    ) {
        this.bookingRepository = bookingRepository;
        this.validationService = validationService;
    }

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

    @Override
    @Transactional
    public BookingCommandService.SaveDraftResult deleteDraft(DeleteBookingDraftCommand command) {
        if (command.draftId() == null) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft ID is required");
        }

        var existingOpt = bookingRepository.findByBookingId(command.draftId());
        if (existingOpt.isEmpty()) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft not found");
        }

        Booking existing = existingOpt.get();

        if (!existing.getUserId().equals(command.userId())) {
            return new BookingCommandService.SaveDraftResult(null, false, "Draft does not belong to this user");
        }

        if (!"draft".equals(existing.getStatus())) {
            return new BookingCommandService.SaveDraftResult(null, false, "Cannot delete a non-draft booking");
        }

        bookingRepository.delete(existing);

        return new BookingCommandService.SaveDraftResult(command.draftId(), true, "Draft deleted successfully");
    }

    @Override
    public Optional<SaveBookingDraftCommand> getDraftById(Long draftId) {
        if (draftId == null) {
            return Optional.empty();
        }

        var opt = bookingRepository.findByBookingId(draftId);
        if (opt.isEmpty() || !"draft".equals(opt.get().getStatus())) {
            return Optional.empty();
        }

        Booking booking = opt.get();
        return Optional.of(new SaveBookingDraftCommand(
            booking.getUserId(),
            booking.getVehicleId(),
            booking.getStartLocationId(),
            booking.getEndLocationId(),
            booking.getReservedAt(),
            booking.getStartDate(),
            booking.getEndDate(),
            booking.getActualStartDate(),
            booking.getActualEndDate(),
            booking.getStatus(),
            booking.getTotalCost(),
            booking.getDiscount(),
            booking.getFinalCost(),
            booking.getPaymentMethod(),
            booking.getPaymentStatus(),
            booking.getDistance(),
            booking.getDuration(),
            booking.getAverageSpeed(),
            booking.getRating() != null ? booking.getRating().getScore() : null,
            booking.getRating() != null ? booking.getRating().getComment() : null
        ));
    }
}
```

(Este bloque es el archivo actual — que ya incluye el fix de B-15 de Fase 4 — con `updateDraft` y el parámetro/campo `vehicleCatalogService` quitados. Verificar contra el archivo real antes de sobrescribir, por si algo cambió, y preservar cualquier diferencia que no sea `updateDraft`/`vehicleCatalogService`.)

- [ ] **Step 4: Actualizar el test para el constructor de 2 argumentos**

En `BookingDraftServiceImplTest.java`:
- Quitar el import de `org.example.backendweride.platform.booking.domain.services.VehicleCatalogService`.
- Quitar la línea `private final VehicleCatalogService vehicleCatalogService = mock(VehicleCatalogService.class);`.
- Cambiar `new BookingDraftServiceImpl(bookingRepository, vehicleCatalogService, validationService)` por `new BookingDraftServiceImpl(bookingRepository, validationService)`.

- [ ] **Step 5: Compilar y correr los tests afectados**

Run: `mvn -Dtest=BookingDraftServiceImplTest test`
Expected: PASS

Run: `mvn -q compile` y `mvn -q test-compile` (sin `-Dtest`) para confirmar que no quedó ningún caller roto por los símbolos borrados.
Expected: BUILD SUCCESS en ambos.

- [ ] **Step 6: Commit**

```bash
git add -A -- 'src/main/java/org/example/backendweride/platform/booking/*' 'src/test/java/org/example/backendweride/platform/booking/*' 'src/test/java/com/weride/platform/*'
git commit -m "chore: delete unreachable booking-draft update logic and orphaned WeRidePlatformApplicationTests (B-2, B-3, B-10 cleanup)"
```
