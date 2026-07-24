# Backend Fase 0 — Seguridad crítica — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Cerrar los 6 hallazgos "no negociables" de la auditoría técnica WeRide (2026-07-23) que son responsabilidad exclusiva del backend: login que no verifica contraseña, API completamente abierta por un bean de seguridad duplicado, secretos versionados en Git, el JWT completo en los logs, un 401 que en realidad responde 200, y un puerto de Docker mal mapeado.

**Architecture:** Son 6 cambios puntuales y aislados sobre archivos ya existentes de `Backend-WeRide` (Spring Boot 3.5.7 / Java 24), sin nuevas capas ni dependencias. Cada tarea es independiente salvo la 2 (que debe ir antes de la 5, porque el 401 real solo se puede verificar de punta a punta una vez que `SecurityConfig.java` deja de anular el filtro JWT) y la 6, que hace la verificación manual integrada de todo lo anterior.

**Tech Stack:** Spring Boot 3.5.7, Spring Security, JUnit 5 + Mockito (ya en `spring-boot-starter-test`), Docker Compose, MySQL 8.

## Global Constraints

- Java 24 / Spring Boot 3.5.7 — no cambiar versiones (`pom.xml`).
- No agregar dependencias nuevas al `pom.xml`. Mockito ya está disponible vía `spring-boot-starter-test`.
- No construir infraestructura de test nueva (H2, `application-test.properties`, perfiles) — eso es explícitamente Fase 3, fuera de este plan.
- No tocar ningún archivo de `Frontend-WeRide`.
- El único mecanismo de verificación end-to-end disponible es `docker compose up --build` + `curl` manual, porque el proyecto no tiene tests de integración.
- Cada archivo de configuración de seguridad debe seguir compilando (`mvn -q compile` sin errores) después de cada tarea.

---

### Task 1: Verificar la contraseña en el login (P-1)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java:54-63`
- Test: `src/test/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImplTest.java` (crear)

**Interfaces:**
- Consumes: `HashingService.matches(CharSequence rawPassword, String encodedPassword): boolean` (ya existe en `src/main/java/org/example/backendweride/platform/iam/application/internal/outboundservices/hashing/HashingService.java`), `TokenService.generateToken(String username): String`, `AccountRepository.existsByUserName(String): boolean`, `AccountRepository.findByUserName(String): Optional<Account>`.
- Produces: `AccountCommandServiceImpl.handle(SignInCommand): Optional<ImmutablePair<Account, String>>` sigue con la misma firma; ahora lanza `RuntimeException("Invalid credentials")` cuando la contraseña no coincide.

- [ ] **Step 1: Escribir el test que falla**

Crear `src/test/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImplTest.java`:

```java
package org.example.backendweride.platform.iam.application.internal.commandservices;

import org.example.backendweride.platform.iam.application.internal.outboundservices.hashing.HashingService;
import org.example.backendweride.platform.iam.application.internal.outboundservices.tokens.TokenService;
import org.example.backendweride.platform.iam.domain.model.aggregates.Account;
import org.example.backendweride.platform.iam.domain.model.commands.SignInCommand;
import org.example.backendweride.platform.iam.infrastructure.persistence.jpa.repositories.AccountRepository;
import org.example.backendweride.platform.profile.interfaces.acl.ProfileContextFacade;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountCommandServiceImplTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final HashingService hashingService = mock(HashingService.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final ProfileContextFacade profileContextFacade = mock(ProfileContextFacade.class);

    private final AccountCommandServiceImpl service = new AccountCommandServiceImpl(
            accountRepository, hashingService, tokenService, profileContextFacade);

    @Test
    void signInWithWrongPassword_throwsAndNeverGeneratesToken() {
        var account = new Account("auditor@weride.com", "hashed-password");
        when(accountRepository.existsByUserName("auditor@weride.com")).thenReturn(true);
        when(accountRepository.findByUserName("auditor@weride.com")).thenReturn(Optional.of(account));
        when(hashingService.matches("malo", "hashed-password")).thenReturn(false);

        var command = new SignInCommand("auditor@weride.com", "malo");

        var exception = assertThrows(RuntimeException.class, () -> service.handle(command));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void signInWithCorrectPassword_returnsAccountAndToken() {
        var account = new Account("auditor@weride.com", "hashed-password");
        when(accountRepository.existsByUserName("auditor@weride.com")).thenReturn(true);
        when(accountRepository.findByUserName("auditor@weride.com")).thenReturn(Optional.of(account));
        when(hashingService.matches("correct-password", "hashed-password")).thenReturn(true);
        when(tokenService.generateToken("auditor@weride.com")).thenReturn("jwt-token-123");

        var command = new SignInCommand("auditor@weride.com", "correct-password");

        var result = service.handle(command);

        assertEquals(true, result.isPresent());
        assertEquals("jwt-token-123", result.get().getRight());
    }
}
```

- [ ] **Step 2: Confirmar que falla**

Run: `mvn -q -Dtest=AccountCommandServiceImplTest test`
Expected: FAIL — `signInWithWrongPassword_throwsAndNeverGeneratesToken` falla porque hoy no se lanza ninguna excepción (el login actual ignora la contraseña y siempre genera un token).

- [ ] **Step 3: Implementar el fix mínimo**

En `src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java`, reemplazar el método `handle(SignInCommand command)` (líneas 54-63) por:

```java
    @Override
    public Optional<ImmutablePair<Account, String>> handle(SignInCommand command) {
        var accountExists = accountRepository.existsByUserName(command.username());
        if(accountExists) {
            var account = accountRepository.findByUserName(command.username());
            if (!hashingService.matches(command.password(), account.get().getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }
            var token = tokenService.generateToken(account.get().getUserName());
            return Optional.of(ImmutablePair.of(account.get(), token));
        }
        throw new RuntimeException("User not found");
    }
```

- [ ] **Step 4: Confirmar que pasa**

Run: `mvn -q -Dtest=AccountCommandServiceImplTest test`
Expected: PASS — 2 tests, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImpl.java src/test/java/org/example/backendweride/platform/iam/application/internal/commandservices/AccountCommandServiceImplTest.java
git commit -m "fix: verify password hash before issuing JWT on sign-in (P-1)"
```

---

### Task 2: Unificar los `SecurityFilterChain` duplicados (P-2)

**Files:**
- Delete: `src/main/java/org/example/backendweride/securityconfig/SecurityConfig.java`
- Modify: `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/configuration/WebSecurityConfig.java`
- Modify: `src/main/resources/application.properties:14`

**Interfaces:**
- Consumes: nada de tareas anteriores.
- Produces: un único bean `CorsConfigurationSource corsConfigurationSource()` y un único bean `SecurityFilterChain filterChain(HttpSecurity)`, ambos en `WebSecurityConfig`. La Tarea 6 depende de que este bean único exista para poder verificar que las rutas sin token devuelven 401.

- [ ] **Step 1: Borrar el archivo con el bean duplicado**

```bash
rm src/main/java/org/example/backendweride/securityconfig/SecurityConfig.java
```

- [ ] **Step 2: Mover el CORS real a `WebSecurityConfig.java`**

En `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/configuration/WebSecurityConfig.java`, agregar los imports que faltan (junto a los existentes, línea 21 `import org.springframework.web.cors.CorsConfiguration;`):

```java
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
```

Reemplazar el bloque `http.cors(...)` dentro de `filterChain` (líneas 70-78) para que use un bean nuevo en lugar de la config inline con `allowedOrigins("*")`:

```java
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://frontend-we-ride.vercel.app",
                "https://backend-weride.onrender.com"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(customizer ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(
                                "/api/v1/authentication/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/v1/professional-profiles"
                        ).permitAll()
                        .anyRequest().authenticated());
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
```

Esto reemplaza tanto el bloque `http.cors(...)` original como el método `filterChain` completo (líneas 69-98 del archivo original); el resto de la clase (constructor, `authRequestFilter`, `authenticationManager`, `authenticationProvider`, `passwordEncoder`) queda igual.

- [ ] **Step 3: Quitar el interruptor que permitía el choque de beans en silencio**

En `src/main/resources/application.properties`, borrar la línea 14:

```
spring.main.allow-bean-definition-overriding=true
```

- [ ] **Step 4: Confirmar que compila**

Run: `mvn -q compile`
Expected: BUILD SUCCESS. Si en cambio falla con `ConflictingBeanDefinitionException` o similar, significa que quedó algún bean `filterChain` o `corsConfigurationSource` duplicado — revisar que `SecurityConfig.java` se haya borrado.

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/org/example/backendweride/securityconfig src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/configuration/WebSecurityConfig.java src/main/resources/application.properties
git commit -m "fix: remove duplicate SecurityFilterChain bean that left the API open (P-2)"
```

---

### Task 3: Sacar secretos de Git (P-8)

**Files:**
- Modify: `src/main/resources/application.properties:7-9, 23`
- Modify: `docker-compose.yml`

**Interfaces:**
- Consumes: nada de tareas anteriores.
- Produces: `spring.datasource.username`, `spring.datasource.password` y `authorization.jwt.secret` ya no tienen valores hardcodeados en `application.properties`; se resuelven vía variables de entorno `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (o las variables `SPRING_DATASOURCE_*` que ya inyecta Docker Compose, que tienen prioridad). La Tarea 6 depende de que `docker-compose.yml` defina `JWT_SECRET` para que el backend arranque.

- [ ] **Step 1: Generar un secreto JWT nuevo (rotación real)**

Run: `openssl rand -base64 48`
Expected: una cadena base64 de ~64 caracteres, distinta de `aVeryLongAndSecureSecretKeyForYourWeRideApplication` (ese valor quedó expuesto en el historial de Git y no debe reutilizarse). Guardar el resultado para el Step 3.

- [ ] **Step 2: Quitar los valores hardcodeados de `application.properties`**

En `src/main/resources/application.properties`, reemplazar las líneas 7-9:

```
spring.datasource.url=jdbc:mysql://localhost:3306/weride-db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Y la línea 23:

```
authorization.jwt.secret=${JWT_SECRET}
```

- [ ] **Step 3: Inyectar `JWT_SECRET` en Docker Compose**

En `docker-compose.yml`, dentro de `services.backend.environment`, agregar la línea con el valor generado en el Step 1 (reemplazar `<valor-generado-en-step-1>` por la cadena real):

```yaml
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/weride-db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: weride_user
      SPRING_DATASOURCE_PASSWORD: weride_pass
      JWT_SECRET: <valor-generado-en-step-1>
```

`SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` ya estaban y siguen cubriendo `DB_USER`/`DB_PASSWORD` porque Spring Boot mapea `SPRING_DATASOURCE_USERNAME` directamente a la propiedad `spring.datasource.username` sin pasar por el placeholder `${DB_USER}` — solo hace falta exportar `DB_USER`/`DB_PASSWORD` a mano si se corre el backend fuera de Docker Compose (`mvn spring-boot:run`).

- [ ] **Step 4: Confirmar que arranca fuera de Docker (variables exportadas a mano)**

Run (PowerShell):
```powershell
$env:DB_USER="root"; $env:DB_PASSWORD="1234"; $env:JWT_SECRET="test-only-secret-not-for-prod-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; mvn -q spring-boot:run
```
Expected: el log muestra `Started BackendWerideApplication` sin `PlaceholderResolutionException`. Detener con Ctrl+C. (Requiere un MySQL local escuchando en `localhost:3306` con esas credenciales; si no lo tienes, basta con confirmar que el arranque falla por *conexión a MySQL* y no por *placeholder sin resolver* — eso ya confirma que la propiedad se resuelve.)

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/application.properties docker-compose.yml
git commit -m "fix: move DB credentials and JWT secret to env vars, rotate JWT secret (P-8)"
```

---

### Task 4: Dejar de loguear el JWT completo (B-6)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/BearerAuthRequestFilter.java:42`

**Interfaces:**
- Consumes: nada.
- Produces: nada que otras tareas consuman.

- [ ] **Step 1: Borrar la línea que loguea el token**

En `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/BearerAuthRequestFilter.java`, borrar la línea 42:

```java
            LOGGER.info("Token: {}", token);
```

El método `doFilterInternal` queda así (líneas 39-56):

```java
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            var token = tokenService.getBearerTokenFrom(request);
            if(Objects.nonNull(token) && tokenService.validateToken(token)) {
                var username = tokenService.getUsernameFromToken(token);
                var userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                SecurityContextHolder.getContext()
                        .setAuthentication(UsernamePasswordAuthTokenBuilder
                                .build(userDetails, request));
            } else {
                LOGGER.warn("Token is not valid");
            }
        } catch(Exception e) {
            LOGGER.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
```

- [ ] **Step 2: Confirmar que compila**

Run: `mvn -q compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/BearerAuthRequestFilter.java
git commit -m "fix: stop logging the full JWT on every request (B-6)"
```

---

### Task 5: Responder 401 real cuando falta el token (B-7)

**Files:**
- Modify: `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/UnauthorizedRequestHandleEntryPoint.java`

**Interfaces:**
- Consumes: nada.
- Produces: `UnauthorizedRequestHandleEntryPoint.commence(...)` ahora escribe el status HTTP en la respuesta. La Tarea 6 verifica esto manualmente contra el stack levantado con Docker.

- [ ] **Step 1: Escribir el status en la respuesta**

En `src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/UnauthorizedRequestHandleEntryPoint.java`, reemplazar el método `commence` (líneas 23-26):

```java
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException, ServletException {
        LOGGER.error("Unauthorized request: {}", authenticationException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
```

- [ ] **Step 2: Confirmar que compila**

Run: `mvn -q compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/example/backendweride/platform/iam/infrastructure/auth/pipeline/UnauthorizedRequestHandleEntryPoint.java
git commit -m "fix: return a real 401 instead of an empty 200 for unauthenticated requests (B-7)"
```

---

### Task 6: Arreglar el puerto de MySQL en Docker y verificar todo el stack (B-12 + verificación integral)

**Files:**
- Modify: `docker-compose.yml`

**Interfaces:**
- Consumes: los cambios de las Tareas 1-5 (verifica su efecto end-to-end).
- Produces: nada consumido por tareas posteriores — es la última del plan.

- [ ] **Step 1: Corregir el mapeo de puertos de MySQL**

En `docker-compose.yml`, dentro de `services.mysql.ports`, cambiar:

```yaml
    ports:
      - "3307:3307"
```

por:

```yaml
    ports:
      - "3307:3306"
```

(MySQL escucha en el puerto `3306` dentro del contenedor; el host sigue usando `3307` para no chocar con un MySQL local en `3306`.)

- [ ] **Step 2: Levantar el stack completo**

Run: `docker compose up --build -d`
Expected: `BUILD SUCCESS` en el log de Maven dentro del build de la imagen, y `docker compose ps` muestra `mysql-db` y `backend-weride` en estado `running`/`healthy`.

- [ ] **Step 3: Verificar P-1 — login rechaza contraseña incorrecta**

Primero crear una cuenta de prueba:

Run:
```bash
curl -s -X POST http://localhost:8080/api/v1/authentication/sign-up -H "Content-Type: application/json" -d "{\"username\":\"auditor@weride.com\",\"password\":\"correct-password\"}"
```
Expected: `201` con el recurso de la cuenta creada.

Luego probar login con password incorrecta:

Run:
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/v1/authentication/sign-in -H "Content-Type: application/json" -d "{\"username\":\"auditor@weride.com\",\"password\":\"malo\"}"
```
Expected: `500` (la excepción `RuntimeException("Invalid credentials")` no tiene un `@ControllerAdvice` que la traduzca a 400 — eso es P-10, Fase 2 — pero ya **no** devuelve `200` con un JWT). Confirma que el flujo cambió: antes devolvía `200` + token con cualquier contraseña.

Login con la contraseña correcta:

Run:
```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/v1/authentication/sign-in -H "Content-Type: application/json" -d "{\"username\":\"auditor@weride.com\",\"password\":\"correct-password\"}"
```
Expected: `200` + JWT.

- [ ] **Step 4: Verificar P-2 y B-7 — la API exige token y responde 401 real**

Run:
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/vehicles
```
Expected: `401` (antes de este plan devolvía `200` con todos los vehículos, sin token).

- [ ] **Step 5: Verificar P-8 — no hay secretos hardcodeados en el jar**

Run: `docker compose exec backend-weride sh -c "printenv JWT_SECRET"`
Expected: imprime el secreto rotado del Step 3 de la Tarea 3, confirmando que se está leyendo de la variable de entorno y no de un default en el código.

- [ ] **Step 6: Verificar B-6 — el JWT no aparece en los logs**

Run: `docker compose logs backend-weride | grep "Token:"`
Expected: sin resultados (exit code 1 de `grep`, ninguna línea coincide).

- [ ] **Step 7: Verificar B-12 — el puerto de MySQL expone el contenedor correcto**

Run: `docker compose port mysql 3306`
Expected: imprime `0.0.0.0:3307`, confirmando que el mapeo `3307:3306` está activo (antes `3307:3307` no exponía nada porque MySQL escucha en `3306`).

- [ ] **Step 8: Apagar el stack**

Run: `docker compose down`

- [ ] **Step 9: Commit**

```bash
git add docker-compose.yml
git commit -m "fix: correct MySQL port mapping in docker-compose (B-12)"
```
