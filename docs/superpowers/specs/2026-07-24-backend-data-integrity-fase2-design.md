# Backend Fase 2 — Integridad de datos — Design

**Fuente:** Auditoría técnica WeRide (2026-07-23), sección "Fase 2 — Integridad de datos (2-3 días)" (ítems 11 y 13 del plan de trabajo) más los hallazgos "Otros hallazgos relevantes — Backend" B-1, B-2, B-4, B-5. Sigue a Fase 0 (seguridad crítica) y Fase 1 (autorización real, P-6), ambas ya mergeadas a `main`.

## Alcance

Backend únicamente, verificado leyendo el código real:

1. **P-9 — listas persistidas como blob binario.** `Plan.benefits`, `Vehicle.features`, `Trip.incidentReports`, `Trip.photos`, `Location.amenities` son `List<String>` sin `@ElementCollection`. Hibernate las serializa como `varbinary(255)`: no son consultables por SQL y revientan con `MysqlDataTruncation` al superar 255 bytes (reproducido en la auditoría con `POST /vehicles` de 40 features → 500). Se anota cada campo con `@ElementCollection` + `@CollectionTable`, igual que ya hace `Booking.issues` en el propio código (patrón existente, no inventado).
2. **`BookingEntity.java` — tabla basura.** Clase `@Entity` sin repositorio, sin ningún uso en el código (confirmado por grep), que Hibernate sigue creando y manteniendo (`bookingses` vs `bookingseses`). Se borra.
3. **B-2 — catálogo de vehículos hardcodeado.** `BookingCommandServiceImpl.saveDraft` valida el vehículo contra `VehicleCatalogService`, una lista en memoria con IDs `"1".."10"` fijos e instanciada manualmente con `new VehicleCatalogService()` (bypass de la inyección de Spring). Un vehículo creado por la API real da "Vehicle not found". Se reemplaza por `VehicleRepository.findById(Long)` contra la tabla real.
4. **B-1 — `deleteDraft` nunca funcionó.** `BookingController.deleteDraft` hace `Long.parseLong(authentication.getName())` sobre el username (no es un número) → `NumberFormatException` → 500 siempre. Se reemplaza por `AuthenticatedAccountProvider.getCurrentAccountId()` (ya existe, de Fase 1), consistente con el resto del controller.
5. **B-4 — registro no transaccional.** `AccountCommandServiceImpl.handle(SignUpCommand)` guarda la cuenta y luego crea el perfil en dos pasos sin `@Transactional`; si falla la creación del perfil queda una cuenta huérfana y el reintento da "User already exists". Se agrega `@Transactional` al método.
6. **B-5 — métrica ecológica siempre falsa.** `TripResourceFromEntityAssembler.toResource` pasa `entity.getMaxSpeed()` dos veces (una para el campo `maxSpeed`, otra por error para el campo `carbonSaved`) en vez de `entity.getCarbonSaved()`. Una línea.
7. **Ítem 15 — healthcheck de Docker Compose.** El contenedor del backend falla en el primer intento porque MySQL aún no acepta conexiones; solo arranca por `restart: always`. Se agrega `healthcheck` al servicio `mysql` y `depends_on: condition: service_healthy` al `backend`. (El puerto `3307:3306`, la otra mitad del ítem 15, ya se corrigió en Fase 0 / B-12.)

**Fuera de alcance, decisión explícita:** la recomendación completa de P-9 incluye pasar `ddl-auto=update` a `ddl-auto=validate` con Flyway. No se hace en este plan: es una migración de infraestructura de build (nueva dependencia, migraciones versionadas) que requiere verificarse contra una base de datos real, y Docker no es utilizable en esta máquina (ver Fase 0). Los `@ElementCollection` de este plan son compatibles con `ddl-auto=update` tal como está hoy — Hibernate crea las tablas de colección nuevas igual que ya crea `booking_issues` para `Booking.issues`. Migrar a Flyway queda para cuando haya un entorno donde se pueda verificar de punta a punta.

P-10 (manejo de errores y validación) queda para un plan posterior (Fase "calidad"), es un cambio transversal a todos los controllers y merece su propio plan.

## Testing

- Los 5 cambios de `@ElementCollection` son mapeo declarativo de JPA, no lógica de negocio — no hay nada que testear con Mockito, y una prueba de persistencia real requiere Hibernate + una base de datos (no disponible en este entorno, igual que en Fase 0). Se verifica por compilación limpia; la verificación funcional (`POST /vehicles` con muchas features → 201, no 500) queda documentada como paso manual pendiente, mismo patrón que la Tarea 6 de Fase 0.
- B-2, B-1, B-5 sí tienen lógica testeable con Mockito puro (sin contexto Spring), igual que en Fase 0 y Fase 1.
- B-4 (`@Transactional`) es una anotación declarativa; verificar su efecto real (rollback ante fallo) requiere un contexto Spring transaccional real, fuera de alcance de este plan — se verifica por compilación y revisión de código, sin test dedicado.

## Manejo de errores

Ninguno de estos cambios introduce manejo de errores nuevo (eso es P-10, fuera de alcance). B-2 mantiene el mismo patrón de retorno (`SaveDraftResult(null, false, "Vehicle not found")`) que ya existía, solo cambia la fuente de datos.
