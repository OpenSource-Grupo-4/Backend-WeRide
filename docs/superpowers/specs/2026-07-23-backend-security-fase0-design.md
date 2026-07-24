# Backend Fase 0 — Seguridad crítica — Design

**Fuente:** Auditoría técnica WeRide (`C:\Users\crama\Desktop\Auditoria-WeRide.pdf`, 2026-07-23), sección "Fase 0 — Seguridad (1 día, no negociable)", restringida a los ítems que son responsabilidad exclusiva del backend.

## Alcance

6 arreglos, todos en `Backend-WeRide`, ninguno requiere cambios en el frontend:

1. **P-1** — `AccountCommandServiceImpl.handle(SignInCommand)` nunca llama a `hashingService.matches()`; cualquier password (incluida vacía) recibe un JWT válido.
2. **P-2** — Dos beans `SecurityFilterChain` llamados `filterChain` (`securityconfig/SecurityConfig.java` y `WebSecurityConfig.java`) compiten por `spring.main.allow-bean-definition-overriding=true`. Gana el que hace `permitAll()` de todo.
3. **P-8** — `spring.datasource.username`, `spring.datasource.password` y `authorization.jwt.secret` están hardcodeados en texto plano en `application.properties`, versionado en Git.
4. **B-6** — `BearerAuthRequestFilter` loguea el JWT completo a nivel INFO en cada request.
5. **B-7** — `UnauthorizedRequestHandleEntryPoint` solo loguea la excepción; nunca escribe el status en el `HttpServletResponse`, así que una request sin token recibe 200 con cuerpo vacío en lugar de 401.
6. **B-12** — `docker-compose.yml` publica `3307:3307`, pero MySQL escucha en `3306` dentro del contenedor; el puerto publicado no expone nada.

Fuera de alcance (quedan para un plan posterior): P-5, P-6, P-9, P-10, y todo lo de frontend (P-3, P-4, P-7, F1-F17).

## Componentes y cambios

### 1. `AccountCommandServiceImpl.java`
Antes de emitir el token en `handle(SignInCommand)`, verificar `hashingService.matches(command.password(), account.getPassword())`. Si no coincide, lanzar `RuntimeException("Invalid credentials")` — mismo patrón de excepción que ya usa el resto de la clase (`"User not found"`, `"User already exists"`); no se introduce un tipo de excepción nuevo en esta fase.

### 2. `securityconfig/SecurityConfig.java` → eliminado
Se borra el archivo completo. Su `CorsConfigurationSource` (con la lista real de orígenes: `localhost:4200`, `frontend-we-ride.vercel.app`, `backend-weride.onrender.com`) se traslada a `WebSecurityConfig.java`, reemplazando el CORS inline que hoy usa `allowedOrigins("*")`. `spring.main.allow-bean-definition-overriding=true` se elimina de `application.properties` — con dos beans iguales, el arranque debe fallar ruidosamente, no elegir uno en silencio.

### 3. `application.properties` + `docker-compose.yml`
`spring.datasource.username=${DB_USER}`, `spring.datasource.password=${DB_PASSWORD}`, `authorization.jwt.secret=${JWT_SECRET}` — sin valores por defecto. `docker-compose.yml` ya inyecta `SPRING_DATASOURCE_USERNAME/PASSWORD` para el servicio `backend`; se añade `JWT_SECRET` con un valor nuevo generado para este cambio (rotación real, no mover el mismo secreto de sitio).

### 4. `BearerAuthRequestFilter.java`
Eliminar la línea `LOGGER.info("Token: {}", token)`.

### 5. `UnauthorizedRequestHandleEntryPoint.java`
Además de loguear, llamar a `response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")` para que el cliente reciba un 401 real.

### 6. `docker-compose.yml`
Cambiar el mapeo de puertos del servicio `mysql` de `"3307:3307"` a `"3307:3306"`.

## Testing

El proyecto no tiene infraestructura de test (BD en memoria, perfiles de test) — construirla es trabajo de Fase 3 y queda fuera de este plan.

- **P-1** es testeable con un test unitario puro (Mockito, ya incluido vía `spring-boot-starter-test`): mock de `AccountRepository`, `HashingService`, `TokenService`, sin tocar base de datos real. Cubre el caso "password incorrecta → excepción, sin token" y "password correcta → token".
- **P-2, P-8, B-6, B-7, B-12** se verifican manualmente con `docker compose up --build` + `curl`, replicando los checks que ya hizo la auditoría: login con password incorrecta debe fallar; un endpoint sin token debe dar 401; los logs no deben contener el JWT.

## Manejo de errores

El rechazo de login por password incorrecta reutiliza el patrón de excepción existente en `AccountCommandServiceImpl` (`RuntimeException` con mensaje). No se introduce `@ControllerAdvice` en esta fase — ese es el alcance de P-10 (Fase 2).
