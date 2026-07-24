# Backend Fase 3 — Autorización real — Design

**Fuente:** Auditoría técnica WeRide (2026-07-23), ítem 16 del "Plan de trabajo recomendado" ("Fase 3 — Autorización y calidad"): *"Comprobación de propiedad en todos los endpoints con id de usuario (P-6)"*. Sigue a Fase 0 (seguridad), Fase 1 (login/JWT real) y Fase 2 (integridad de datos), las tres ya en `main` (Fase 2 en la rama `worktree-fix+backend-data-integrity-fase2`, lista para mergear).

## Contexto

P-6 fase 1 (ya mergeado) resolvió que `booking` y `notifications` **derivaran** el userId del JWT en vez de leerlo de query params. Pero eso solo cubrió los endpoints donde el path/query ya llevaba explícitamente un `userId`. La auditoría es más amplia: *"no existe una sola anotación `@PreAuthorize` en el proyecto"* — el problema real es que ningún endpoint que devuelve o modifica un recurso **perteneciente a un usuario específico** (una reserva, una notificación, un viaje, un perfil, una cuenta) comprueba que el usuario autenticado sea el dueño de ese recurso antes de operar sobre él.

Verificado leyendo el código de la rama `worktree-fix+backend-data-integrity-fase2` (estado post Fase 2):

**Ya protegidos (Fase 1, sin cambios en este plan):** `BookingController.getDraftsByCustomer`, `getBookingsByUserId`, `getPendingBookingsByUser`, `getCompletedBookingsByUser`, `deleteDraft`.

**Sin protección — alcance de este plan:**

| # | Endpoint | Archivo:línea | Problema |
|---|----------|----------------|----------|
| 1 | `GET /bookings/{id}` | `BookingController.java:121` | No compara `booking.userId` con el usuario autenticado. |
| 2 | `GET /bookings?customerId=X&...` | `BookingController.java:143` | `customerId` es un `@RequestParam` arbitrario del cliente. |
| 3 | `GET /bookings/vehicle/{vehicleId}` | `BookingController.java:204` | Sin ningún filtro por usuario — expone reservas de todos. |
| 4 | `GET /bookings/status/{status}` | `BookingController.java:228` | Igual, sin filtro por usuario. |
| 5 | `GET /notifications/{notificationId}` | `NotificationsController.java:68` | No compara `notification.userId`. |
| 6 | `PATCH /notifications/{id}/read` | `NotificationsController.java:77` | Cualquiera marca como leída la notificación de otro. |
| 7 | `GET /trips` | `TripController.java:44` | Lista los viajes de **todos** los usuarios. |
| 8 | `DELETE /trips/{id}` | `TripController.java:52` | Borra el viaje de cualquiera sin comprobar dueño. |
| 9 | `GET /profiles/{id}` | `ProfileController.java:40` | No compara `profile.userId`. |
| 10 | `GET /accounts/{accountId}` | `AccountsController.java:37` | Cualquiera lee los datos de cualquier cuenta. |

## Decisión de alcance: qué SÍ es "propiedad de usuario" y qué NO

`Vehicle`, `Plan` y `Location` (garage, planes, ubicaciones) son catálogo/flota compartidos — ninguna de las tres entidades tiene un campo `userId`/`ownerId` (confirmado por grep). Que cualquier usuario autenticado los lea es el comportamiento esperado de la app (son igual que un catálogo de productos). Restringir sus operaciones de escritura (crear/editar/borrar un vehículo) a un rol "admin" es un problema distinto: **no existe ningún concepto de rol en el proyecto** — `UserDetailsImpl.build()` (`iam/infrastructure/auth/model/UserDetailsImpl.java:41-51`) asigna `ROLE_CLIENT` a todas las cuentas sin excepción, comentado explícitamente como *"For now, WeRide only handles the CLIENT role"*. Introducir RBAC real (roles, quién puede ser admin, cómo se asigna) es una decisión de producto, no un bug — queda fuera de este plan.

## Alcance

Los 10 endpoints de la tabla anterior. Patrón de arreglo, igual en todos: comparar el `userId` del recurso contra `authenticatedAccountProvider.getCurrentAccountId()` (ya existe, de Fase 1) antes de devolver o modificar datos; si no coincide, responder como si el recurso no existiera (`404`), nunca `403`, para no confirmarle a un atacante que el recurso existe pero es ajeno.

**Fuera de alcance, decisión explícita:**
- Autorización por rol (admin) sobre `Vehicle`/`Plan`/`Location` — requiere diseñar RBAC, no existe hoy.
- `TravelHistoryController.getTravelHistoryById` / `updateTravelHistory` — el propio código tiene un problema previo de nombres: `UpdateTravelHistoryCommand.userId()` en realidad contiene el **id del registro** (el path `/travel-history/{id}`, ver `UpdateTravelHistoryCommandFromResourceAssembler.java:13`), no el id del usuario. Añadir una comprobación de propiedad ahí sin antes arreglar ese nombre confuso multiplicaría la confusión en vez de resolverla. Se deja para cuando se aborde ese archivo específicamente.
- B-3 (`updateDraft` ignora el id del draft), B-9 (controllers devuelven entidades JPA en vez de DTOs), B-11 (`POST /profiles` descarta nombre/apellido/email), B-13 (falta `equals`/`hashCode`), B-14 (`DELETE` devuelve 204 sin comprobar existencia), B-15 (`BookingDraftValidationService` inyectado y nunca llamado) — son hallazgos "Alto"/"Medio" reales pero **no** parte del ítem 16 de la Fase 3 del plan de la auditoría (que es específicamente sobre propiedad/autorización). No estaban tampoco agendados en ninguna fase por la propia auditoría. Quedan como candidatos para un plan de "calidad" posterior, junto con P-10 (ya diferido en la Fase 2).
- `TripController.createTrip` — sigue tomando `userId` del cuerpo de la petición (`CreateTripCommandResource`) sin validarlo contra el JWT. No es uno de los 10 endpoints de este plan (es una escritura, no una lectura/borrado ajeno), pero es la misma clase de problema que P-6 fase 1 ya cerró para bookings/notifications. Detectado en la revisión final de esta fase (2026-07-24) — candidato para el mismo plan de "calidad" posterior que P-10 y los hallazgos B-3/9/11/13/14/15.
- Ítems 17-18 de la Fase 3 (Reactive Forms, `takeUntilDestroyed`) son frontend — fuera de alcance por instrucción del usuario (solo backend).
- Ítem 19 (tests con H2) — requiere infraestructura de test nueva, contra la restricción ya establecida en Fase 0/1/2 de no construir infraestructura de test nueva en este entorno.

## Testing

Todos los cambios son lógica de autorización pura (comparar un id contra otro) alcanzable con Mockito puro, sin contexto Spring — mismo patrón que Fase 1 y Fase 2. Cada task agrega un test que prueba el caso "dueño correcto → 200/acción aplicada" y el caso "usuario distinto → 404/sin efecto", siguiendo el estilo ya existente en `BookingControllerTest.java`.

## Manejo de errores

Se usa `ResponseEntity.notFound()` (ya el patrón dominante en el proyecto) para el caso "no es el dueño", igual que para "no existe". No se introduce `@RestControllerAdvice` (eso es P-10, fuera de alcance) ni excepciones nuevas, salvo en `NotificationCommandServiceImpl.handle(MarkNotificationAsReadCommand)`, donde ya existe un `RuntimeException` para "no encontrado" (línea existente) — el caso "pertenece a otro usuario" reutiliza el mismo camino, sin distinguirlo (evita filtrar si el recurso existe).
