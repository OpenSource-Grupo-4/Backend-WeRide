# Backend Fase 5 — Cierre de pendientes — Design

**Fuente:** balance de la auditoría técnica WeRide tras las Fases 0-4 (ver `docs/superpowers/specs/2026-07-24-backend-quality-fase4-design.md`). Lo que queda del lado backend, ya acotado a lo que tiene un arreglo de una sola línea o es borrar código muerto — sin construir infraestructura nueva (ponytail: la solución más chica que sirve).

## Qué queda y qué se hace con cada cosa

1. **`POST /profiles` confía en el `userId` del body** — mismo patrón que ya se cerró para `createTrip` en Fase 4 (P-6). Arreglo: derivar el `userId` del JWT, un parámetro más en el assembler, igual que `CreateTripCommandFromResourceAssembler`.

2. **B-2/B-3 — código muerto en borradores de reserva.** Verificado en Fase 4: `BookingDraftServiceImpl.updateDraft`, el record `UpdateBookingDraftCommand` y `VehicleCatalogService` no están conectados a ningún endpoint de `BookingController` ni a nada más en el proyecto (`grep` sin resultados fuera de sus propios archivos y de un test que solo mockea el constructor). No hay nada que "arreglar" en su lógica porque nadie la ejecuta — la corrección real es borrarlos: ninguna feature futura depende de código que no se llama, y dejarlos invita a que alguien los conecte sin darse cuenta de que el catálogo es falso y el filtro de `updateDraft` está roto. Ponytail: deletion over addition.

3. **B-10 (cobertura de tests 0%, `Dockerfile` con `-DskipTests`).** Investigado a fondo: **no tiene arreglo de una línea.** `WeRidePlatformApplicationTests.java` (paquete `com.weride.platform`, que no es el paquete real de la app) es un test huérfano/duplicado — no encuentra `@SpringBootConfiguration` porque está en el paquete equivocado. Se borra: es puro ruido, cero valor, cero riesgo.
   `BackendWerideApplicationTests.contextLoads`, en cambio, necesita una base de datos MySQL real (usa `${DB_USER}`/`${DB_PASSWORD}` sin default desde la rotación de secretos de Fase 0). El `Dockerfile` construye la imagen con `mvn clean package` en una etapa de build que **no tiene acceso a ningún contenedor de MySQL** (docker-compose no está activo durante `docker build`, solo durante `docker compose up`). Quitar `-DskipTests` ahí rompería el build de la imagen siempre, no solo en este entorno de verificación — no es una limitación de esta máquina, es una limitación real del Dockerfile actual. Arreglarlo de verdad requiere una base de datos en memoria (H2) o mocks para el contexto de test, que es exactamente la infraestructura de test nueva que every fase anterior (0, 1, 2, 3, 4) dejó fuera de alcance por la misma razón. Se mantiene fuera de alcance aquí también — no se toca `-DskipTests`.

## Alcance

Dos tareas: el fix de `POST /profiles`, y el borrado de código muerto (B-2/B-3 + el test huérfano de B-10).

**Fuera de alcance, decisión explícita (repetida de fases anteriores, sigue vigente):**
- RBAC sobre `Vehicle`/`Plan`/`Location` — no existe infraestructura de roles, decisión de producto.
- Suite de tests con H2/mocks para poder quitar `-DskipTests` del Dockerfile — requiere construir infraestructura de test nueva, fuera de las restricciones de este entorno.
- `@Valid` en los ~12 recursos de creación restantes sin una reproducción en ejecución documentada por la auditoría (mismo criterio ya aplicado en Fase 4: solo se valida lo que tiene un caso de fallo verificado, no todo por prevención).
- Todo lo de frontend/integración — fuera de alcance por instrucción del usuario en toda esta serie de fases.

## Testing

Mismo patrón Mockito puro que las fases anteriores, sin contexto Spring.
