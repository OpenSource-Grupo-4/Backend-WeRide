# Backend Fase 1 — Autorización real (P-6) — Design

**Fuente:** Auditoría técnica WeRide (`C:\Users\crama\Desktop\Auditoria-WeRide.pdf`, 2026-07-23), hallazgo P-6 ("Cualquier usuario ve las reservas de todos") y el hallazgo general de que `@EnableMethodSecurity` está activo pero no existe una sola anotación `@PreAuthorize` en el proyecto. Sigue a la Fase 0 (seguridad crítica, ya mergeada a `main`).

## Alcance

Backend únicamente. Los endpoints "por usuario" del backend confían en el ID que manda el cliente en el path o query string en lugar de derivarlo del JWT verificado. Se corrige eso, no se construye un sistema de roles/permisos nuevo.

Endpoints afectados:
- `GET /api/v1/bookings/user/{userId}`
- `GET /api/v1/bookings/user/{userId}/pending`
- `GET /api/v1/bookings/user/{userId}/completed`
- `GET /api/v1/bookings/drafts?customerId={customerId}`
- `GET /api/v1/notifications?userId={userId}`

Fuera de alcance: P-5 (`Page<>` vs array — es un contrato de paginación válido, el ajuste es del frontend), la limpieza del modelo de datos (P-9), validación de entrada (P-10), y la extensión de este patrón de autorización a *todos* los endpoints del sistema (eso es el ítem 16 de Fase 3 de la auditoría — aquí solo se cubren los 5 endpoints de bookings/notifications que la auditoría señaló específicamente en P-6).

## Componentes y cambios

### 1. `AuthenticatedAccountProvider` (nuevo)
Ubicación: `src/main/java/org/example/backendweride/platform/iam/application/internal/outboundservices/security/AuthenticatedAccountProvider.java`

Un único método `Long getCurrentAccountId()`:
1. Lee `SecurityContextHolder.getContext().getAuthentication().getName()` — ya es el username/email, es lo que `UserDetailsImpl.getUsername()` expone y lo que el `BearerAuthRequestFilter` deja en el `SecurityContext` tras validar el JWT.
2. Resuelve la cuenta con `AccountRepository.findByUserName(username)`.
3. Devuelve `account.getId()`; si no existe la cuenta (no debería pasar si el JWT es válido, pero es defensivo), lanza `RuntimeException("Authenticated account not found")`.

Es necesario porque `UserDetailsImpl` (implementación actual de `UserDetails`) no expone el `Account.id`, solo username/password/authorities — no hay atajo sin esta consulta extra.

### 2. `BookingController`
Los tres métodos que hoy leen el ID del path (`getBookingsByUserId`, `getPendingBookingsByUser`, `getCompletedBookingsByUser`) y el que lo lee del query (`getDraftsByCustomer`) dejan de pasarle al query object el valor recibido del cliente; usan `authenticatedAccountProvider.getCurrentAccountId()` en su lugar. El `@PathVariable`/`@RequestParam` se mantiene en la firma del método (no se rompe el contrato de URL que ya usa el frontend) pero su valor ya no se usa para filtrar — se ignora a propósito. `Booking.userId` es `Long` y ya corresponde 1:1 a `Account.id`, no hay conversión de tipos que resolver.

### 3. `NotificationsController.getAllNotificationsByUserId`
Mismo patrón: el `@RequestParam String userId` se mantiene en la firma pero se ignora; la query usa `String.valueOf(authenticatedAccountProvider.getCurrentAccountId())`.

**Riesgo aceptado y documentado:** `Notification.userId` es una columna `String` libre, sin FK a `accounts`, poblada con lo que el cliente mande en `POST /notifications` sin validar contra la tabla real. Es un problema de modelo de datos preexistente (rastreado como P-9, fuera de este plan). Este cambio no lo agrava — cierra un agujero de autorización real — pero como efecto colateral, notificaciones sembradas con IDs no numéricos (ej. `"user-001"` heredado de `db.json`) dejarán de aparecer para ese usuario tras el cambio. La auditoría ya documenta que este endpoint devuelve 400 en producción hoy, así que no hay comportamiento funcional previo que este cambio esté rompiendo.

## Testing

- `AuthenticatedAccountProvider`: test unitario con Mockito — username autenticado con cuenta existente devuelve el ID correcto; username sin cuenta asociada lanza excepción. Sin contexto de Spring, sin BD real (mock de `AccountRepository`).
- `BookingController` / `NotificationsController`: para cada endpoint modificado, un test que monta el `SecurityContextHolder` con un usuario autenticado distinto del `userId` que viaja en el path/query, y verifica que el resultado corresponde al usuario autenticado (no al del path/query) — la prueba concreta de que el bug de autorización está cerrado. Se mockean los command/query services de cada controller; no se levanta contexto Spring completo (no hay infraestructura de test para eso en este proyecto, ver nota de Fase 0).

## Manejo de errores

Si `getCurrentAccountId()` no encuentra la cuenta (JWT válido pero cuenta borrada entre la emisión del token y la request), se propaga la misma `RuntimeException` sin manejo especial — consistente con el resto del proyecto hasta que P-10 (manejo de errores centralizado) se implemente.
