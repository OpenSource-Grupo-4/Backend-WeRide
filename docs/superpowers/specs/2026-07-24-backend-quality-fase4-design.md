# Backend Fase 4 — Cierre de calidad — Design

**Fuente:** Auditoría técnica WeRide (2026-07-23), sección 4 "Otros hallazgos relevantes — Backend" (B-1..B-15) y P-10, más los dos hallazgos detectados en la revisión final de Fase 3 (2026-07-24): `TripController.createTrip` con `userId` sin validar, y la confusión de nombres en `TravelHistoryController`. Sigue a Fase 0 (seguridad), Fase 1 (login/JWT), Fase 2 (integridad de datos) y Fase 3 (autorización real, P-6) — las cuatro ya en `main`. Solo backend, por instrucción del usuario.

## Contexto y verificación contra el código actual

La auditoría original (`Auditoria-WeRide.pdf`) describe el estado de la rama `main` en el commit `f1e0423`, anterior a las Fases 0-3. Antes de planear esta fase se releyó el código actual (post Fase 3) para confirmar cuáles hallazgos B-1..B-15 y P-10 siguen vivos y cuáles ya quedaron resueltos como efecto colateral de fases anteriores:

**Ya resueltos (verificado, no requieren acción):**
- B-1 (`deleteDraft` con `parseLong` sobre username) — `BookingController.deleteDraft` ya usa `authenticatedAccountProvider.getCurrentAccountId()` (Fase 1/3).
- B-4 (registro no transaccional) — `AccountCommandServiceImpl.handle(SignUpCommand)` ya tiene `@Transactional`.
- B-5 (`carbonSaved` relleno con `getMaxSpeed()`) — `TripResourceFromEntityAssembler` ya usa `entity.getCarbonSaved()` en la posición correcta.
- B-6, B-7, B-12 — resueltos en Fase 0 (según ledger de la sesión anterior).
- P-9 (tablas basura, `BookingEntity` duplicada, `@ElementCollection`) — resuelto en Fase 2.

**Siguen vivos (alcance de este plan):**
- P-10 — ni un `@RestControllerAdvice` ni un `@Valid` en todo el backend. Verificado: `AccountQueryServiceImpl.handle(GetAccountByIdQuery)` lanza `IllegalArgumentException("Account not found")` sin capturar → `GET /accounts/1` con id inexistente sigue devolviendo 500. `AccountCommandServiceImpl.handle(SignUpCommand)` lanza `RuntimeException("User already exists")` sin capturar → sign-up duplicado sigue devolviendo 500. `CreateVehicleResource` no tiene ninguna anotación de validación → `POST /vehicles {}` sigue devolviendo 201 con todo a null.
- B-8 — `BookingQueryServiceImpl.getPendingBookingsByUser` sigue concatenando dos páginas (`pending` + `confirmed`) pedidas con el mismo `Pageable`, duplicando el conteo de elementos por página.
- B-9 — 5 controllers devuelven entidades JPA directamente en vez de los Resource/DTO que el propio proyecto ya define y usa en sus otros endpoints: `TripController.getAllTrips` (`List<Trip>`), `PlanController.findAllPlans` (`List<Plan>`), `TravelHistoryController.getAllTravelHistories` y `getTravelHistoryById` (`List<TravelHistory>`), `ProfileController.getProfileById` (`Profile`), `LocationController.getAllLocation` (`List<Location>`). En los seis casos ya existe un assembler (`TripResourceFromEntityAssembler`, `PlanResourceFromEntity`, `TravelHistoryResourceFromEntityAssembler`, `ProfileResourceFromEntity`, `LocationResourceFromEntityAssembler`) usado en otro endpoint del mismo controller — el arreglo es mecánico, no requiere diseñar DTOs nuevos.
- B-11 — `POST /profiles` sigue forzando `firstName`/`lastName`/`email` a `""` en `Profile(CreateProfileCommand)`, aunque `CreateProfileCommandResource` ya recibe esos tres campos del cliente y simplemente los descarta en el assembler.
- B-13 — ninguna entidad implementa `equals`/`hashCode` (confirmado por búsqueda; el único resultado que parecía uno era un `"confirmed".equals(this.status)` sin relación).
- B-14 — `DELETE /plans/{id}` y `DELETE /trips/{id}` siguen devolviendo 204 sin comprobar que el recurso existiera (o perteneciera al usuario, en el caso de trips).
- B-15 — `BookingDraftValidationService` se sigue inyectando en `BookingDraftServiceImpl` y no se llama nunca en `saveDraft`; toda su lógica de validación (fecha futura, duración 1-1440 min) es código muerto.
- `TripController.createTrip` — sigue tomando `userId` del cuerpo de la petición (`CreateTripCommandResource`) sin compararlo con el JWT (detectado en la revisión final de Fase 3, no incluido en ese plan).
- `TravelHistoryController` — el parámetro de `getTravelHistoryById(@PathVariable Long userId)` y el campo `UpdateTravelHistoryCommand.userId()` en realidad contienen el **id del registro** de historial, no el id del usuario (`GetTravelsHistoryById`, que sí tiene el campo bien nombrado `id`, recibe ese valor). Es solo un problema de nombres — `TravelHistoryCommandServiceImpl.handle(UpdateTravelHistoryCommand)` usa ese valor correctamente como id de búsqueda y `updateFromCommand` no toca el campo `userId` real de la entidad, así que no hay corrupción de datos, pero el nombre confunde a cualquiera que lea o extienda el código.

**Verificado y descartado, no es un bug explotable — fuera de alcance:**
- B-2 (catálogo de vehículos hardcodeado 1..10) y B-3 (`updateDraft` actualiza el primer draft que encuentre, ignorando el id) — `BookingDraftServiceImpl.updateDraft(SaveBookingDraftCommand)` y el registro `UpdateBookingDraftCommand` no están conectados a ningún endpoint de `BookingController` (no existe `PUT`/`PATCH /bookings/draft/{id}`). Es lógica interna inalcanzable desde la API: arreglar el "primer match" o el catálogo de un método que nadie invoca no aporta nada hasta que se decida si el endpoint de actualizar drafts se expone alguna vez. Se deja documentado, no se toca.

## Alcance

Ocho tareas, todas backend, todas con reproducción confirmada contra el código actual del repo (no contra la foto de la auditoría):

1. P-10 mínimo viable: `@RestControllerAdvice` que mapea las excepciones de dominio ya existentes (no se introducen tipos de excepción nuevos) + `@Valid`/anotaciones Jakarta en los tres recursos de creación que la propia auditoría reprodujo en ejecución (`CreateVehicleResource`, `SignUpResource`, `CreateProfileCommandResource`).
2. B-13: `equals`/`hashCode` por id en la superclase común `AuditableAbstractAggregateRoot` (cubre Vehicle, Trip, Plan, Location, TravelHistory, Notification, Account) + los dos agregados que no la extienden (`Booking`, `Profile`), añadidos ahí directamente.
3. B-8: arreglar `getPendingBookingsByUser` para pedir un slice de tamaño mitad por status y no duplicar el conteo de la página.
4. B-9: sustituir las 6 devoluciones de entidad JPA por su Resource ya existente en el proyecto.
5. B-11 + B-15: persistir nombre/apellido/email en `POST /profiles`; conectar `BookingDraftValidationService` a `saveDraft`.
6. B-14: `DELETE /plans/{id}` y `DELETE /trips/{id}` devuelven 404 si el recurso no existe (o no pertenece al usuario, para trips).
7. `TripController.createTrip`: derivar el `userId` del JWT, ignorando el que venga en el cuerpo — mismo patrón que P-6 fase 1.
8. Renombrar `UpdateTravelHistoryCommand.userId` → `id` y el `@PathVariable` de `getTravelHistoryById` de `userId` a `id`, para que el nombre refleje lo que el valor realmente es.

**Fuera de alcance, decisión explícita:**
- B-2/B-3 — código muerto no conectado a ningún endpoint (ver arriba).
- RBAC por rol sobre `Vehicle`/`Plan`/`Location` — sigue sin existir infraestructura de roles; decisión de producto, no bug.
- Ítems 17-19 de la Fase 3 del plan de auditoría (Reactive Forms, `takeUntilDestroyed`, tests con H2) — frontend o requieren infraestructura de test nueva, fuera de las restricciones ya establecidas en fases anteriores.
- Migración completa de IDs `String` → `number` en Angular y las 5 colecciones sin backend (`favorites`, `payments`, etc.) — frontend, decisión de producto pendiente según la propia auditoría.
- `@Valid` en el resto de los ~12 recursos de creación del proyecto que no tienen una reproducción en ejecución documentada por la auditoría — se puede extender el mismo patrón caso por caso más adelante; no se listan todos aquí para no inflar el plan con trabajo mecánico repetido sin un caso de fallo verificado.

## Testing

Mismo patrón que Fases 1-3: Mockito puro, sin contexto Spring. Cada tarea agrega o extiende un test que reproduce el bug (caso que hoy falla) y confirma el arreglo. El `@RestControllerAdvice` se prueba invocando sus métodos `@ExceptionHandler` directamente con la excepción construida a mano — no requiere `@WebMvcTest` ni levantar el contexto.

## Manejo de errores

El `@RestControllerAdvice` no sustituye por completo el manejo de errores del proyecto: solo captura los patrones de excepción que la auditoría reprodujo en ejecución (`IllegalArgumentException` de "no encontrado", `RuntimeException` de credenciales/duplicados en IAM, y `MethodArgumentNotValidException` de `@Valid`). Cualquier otra excepción sigue cayendo al comportamiento por defecto de Spring (500), sin cambios — no se introduce un catch-all de `Exception` que podría ocultar bugs futuros.
