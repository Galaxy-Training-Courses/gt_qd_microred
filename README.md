# Microred de Intercambio de Objetos entre Vecinos

Proyecto final del curso **Quarkus Developer** (Galaxy Training, CURS-000638).

Sistema distribuido de microservicios con Quarkus que permite a los vecinos de una comunidad prestarse
objetos de uso ocasional.

---

## 1. Qué resuelve

En una comunidad de vecinos, muchos objetos de uso ocasional (una escalera telescópica, un proyector, una
carpa, herramientas) pasan guardados la mayor parte del año. La Microred permite que los vecinos:

1. **Publiquen** los objetos que están dispuestos a prestar.
2. **Descubran** qué objetos hay disponibles, filtrando por categoría, condición, propietario o estado.
3. **Soliciten** el préstamo de uno o varios objetos durante un periodo determinado.
4. **Gestionen** la solicitud: el propietario la aprueba o rechaza, el solicitante puede cancelarla
   mientras esté pendiente y al final se registra la devolución.
5. **Reciban notificaciones** de cada cambio de estado.

El sistema garantiza que un objeto prestado no se pueda volver a solicitar hasta que se devuelva, que
nadie pueda pedir prestado un objeto propio y que las fechas tengan sentido.

No tiene interfaz gráfica: se consume por API REST (Postman, `curl` o cualquier cliente).

---

## 2. Arquitectura

```text
                    ┌────────────────────────┐
  Cliente ────────► │   community-service    │ ───► community_db (PostgreSQL)
  (REST)            │   :8081  Catálogo      │
                    └───────────▲────────────┘
                                │ consulta y reserva objetos (REST Client reactivo)
                    ┌───────────┴────────────┐
  Cliente ────────► │      loan-service      │ ───► loan_db (PostgreSQL)
  (REST + JWT)      │   :8082  Préstamos     │
                    └───────────┬────────────┘
                                │ publica eventos (tópico loan-events)
                         ┌──────▼──────┐
                         │ Kafka /     │
                         │ Redpanda    │
                         └──────┬──────┘
                    ┌───────────▼────────────┐
                    │ notification-consumer  │ ───► registro (log JSON)
                    │   :8083  Avisos        │
                    └────────────────────────┘
```

| Componente | Puerto | Estilo | Responsabilidad |
|---|---|---|---|
| `community-service` | 8081 | Capas | Catálogo: usuarios y objetos. CRUD con paginación, filtros y ordenamiento. Es la fuente de verdad de *quién existe* y *en qué estado está cada objeto*. |
| `loan-service` | 8082 | Hexagonal simplificada | Proceso de préstamo: valida reglas, controla el ciclo de vida de la solicitud, actualiza el estado de los objetos en `community-service` y publica un evento por cada cambio. |
| `notification-consumer` | 8083 | Consumidor | Escucha los eventos y deja un registro JSON de cada uno. No envía correos: es el punto de enganche para hacerlo. |
| PostgreSQL ×2 | 5432 | — | Una base por servicio (`community_db`, `loan_db`). Los servicios **no comparten tablas** ni tienen claves foráneas entre sí. |
| Kafka / Redpanda | 9092 | — | Mensajería entre `loan-service` y `notification-consumer`. |

**¿Por qué hexagonal solo en `loan-service`?** Ahí están las reglas de negocio que deben sobrevivir a
cambios en HTTP, JPA, Kafka o el cliente REST; su dominio no importa nada de Quarkus, JAX-RS ni JPA.
`community-service` es un catálogo sin reglas propias: añadirle puertos solo sumaría clases.

**¿Por qué no comparten base de datos?** `loan-service` guarda solo los ids de usuarios y objetos, y
pregunta a `community-service` por REST cuando necesita validarlos. Esa separación es lo que hace
necesarios el cliente REST y la tolerancia a fallos.

### Stack

Java 25 · Quarkus 3.38.1 · Maven 3.9 (multi-módulo) · PostgreSQL 17 + Flyway · Hibernate Panache ·
MapStruct 1.6.3 · REST Client reactivo (Mutiny) · SmallRye Fault Tolerance · Kafka (Redpanda en
contenedores) · SmallRye JWT · Datafaker 2.7.0 (datos de ejemplo).

### Estructura del repositorio

```text
.
├── pom.xml                  # POM padre: versiones centralizadas
├── community-service/
├── loan-service/            # domain/ · application/ · adapter/in · adapter/out
├── notification-consumer/
├── postman/collection.json  # flujo de negocio completo (10 pasos)
└── docker-compose.yml       # sistema completo en contenedores
```

---

## 3. Comportamiento

### Ciclo de vida de una solicitud de préstamo

```text
                  ┌──────────► REJECTED   (el propietario rechaza)
                  │
   PENDING ───────┼──────────► CANCELLED  (el solicitante cancela)
   (creada)       │
                  └─ approve ─► APPROVED ── return ──► RETURNED
                                (objetos RESERVED)     (objetos AVAILABLE)
```

Solo son válidas las transiciones del diagrama; cualquier otra devuelve `409 Conflict`. Una solicitud
puede incluir **varios objetos** (maestro/detalle: una cabecera con sus ítems).

### Reglas al crear una solicitud

| Regla | Si falla |
|---|---|
| El solicitante existe | 404 |
| Todos los objetos existen | 404 |
| El solicitante no es propietario de ninguno de los objetos | 422 |
| `requestedFrom` es anterior a `requestedUntil` | 400 |
| Ningún objeto está no disponible | 409 |
| Hay al menos un objeto | 400 |

### Endpoints principales

| Servicio | Endpoint | Uso |
|---|---|---|
| community | `GET/POST /api/users`, `GET/PATCH/DELETE /api/users/{id}` | Usuarios |
| community | `GET /api/users/{id}/objects` | Objetos de un usuario |
| community | `GET/POST /api/objects`, `GET/PUT/PATCH/DELETE /api/objects/{id}` | Objetos (`GET` admite `status`, `category`, `ownerId`, `page`, `size`, `sort`…) |
| loan | `POST /api/loans` 🔒 | Crear solicitud → `PENDING` |
| loan | `GET /api/loans`, `GET /api/loans/{id}` | Consultar (paginado y filtrable) |
| loan | `PATCH /api/loans/{id}/approve` 🔒 · `/reject` 🔒 · `/cancel` · `/return` | Transiciones de estado |
| todos | `GET /q/health/live`, `GET /q/health/ready` | Health checks |

🔒 Requiere `Authorization: Bearer <JWT>`. Sin token → `401`. El resto de endpoints es público. El
sistema **verifica** tokens pero no los emite (no hay servidor de identidad); para pruebas locales la
colección de Postman trae un token de larga duración en la variable `jwtToken`.

Los errores siguen un formato común `ErrorResponse` (`status`, `error`, `message`, …).

### Eventos

Cada cambio de estado publica un evento en el tópico `loan-events`: `LOAN_CREATED`, `LOAN_APPROVED`,
`LOAN_REJECTED`, `LOAN_CANCELLED`, `LOAN_RETURNED`.

### Comportamiento ante fallos

- **`community-service` caído:** `loan-service` no se queda colgado. Reintenta con un límite de tiempo
  por intento y responde `503 Service Unavailable` con un mensaje claro; la solicitud queda sin cambios
  (no hay mutaciones parciales). Mientras tanto su *readiness* pasa a `DOWN`, pero las consultas siguen
  respondiendo.
- **Kafka o `notification-consumer` caídos:** el préstamo se realiza igual; el evento se pierde y queda
  en el log de error, pero la operación de negocio no se revierte.
- **Correlación:** cada petición lleva un `X-Request-Id` que aparece en los logs JSON de los tres
  servicios, lo que permite seguir una operación de extremo a extremo.

### Datos de ejemplo

En los perfiles `%dev` y `%test`, `community-service` arranca con 2 usuarios y 3 objetos de ids fijos (los
objetos 1 y 2 son del usuario 1; el 3, del usuario 2). En `%dev` se suman 10 usuarios y 20 objetos
generados. En `%prod` (contenedores) no se carga ningún dato de ejemplo.

### Perfiles de configuración

| Perfil | Cuándo | Base de datos y Kafka | `community-service` visto desde `loan-service` |
|---|---|---|---|
| `%test` | `mvn verify` | Contenedores efímeros (Dev Services) | Simulado (stub) |
| `%dev` | `mvn quarkus:dev` | Contenedores efímeros (Dev Services) | Real, `http://localhost:8081` |
| `%prod` | jar o contenedor | Variables de entorno (`DATABASE_URL`, `KAFKA_BOOTSTRAP_SERVERS`, …) | Real, `COMMUNITY_SERVICE_URL` |

En `%prod` no hay valores por defecto con credenciales: si falta una variable, el servicio no arranca.

### Fuera de alcance (a propósito)

Interfaz gráfica, servidor de identidad (OAuth/Keycloak), API Gateway, escalado automático, Saga,
métricas con Prometheus/Grafana. Las credenciales de `docker-compose.yml` son **solo para uso local**.

---

## 4. Requisitos

| Herramienta | Versión probada | Para qué |
|---|---|---|
| JDK | 25 | Compilar y ejecutar |
| Maven | 3.9+ | Build multi-módulo |
| Docker Engine | 29.x | Tests, Dev Services, imágenes y Compose |
| Docker Compose | v2+ (plugin) | Sistema completo |
| Node.js + Newman | Node 18+, Newman 6.x | Opcional: flujo de negocio automatizado (`npm install -g newman`) |
| `curl`, `jq` | recientes | Pruebas manuales |

> **Docker es necesario incluso para los tests:** levantan PostgreSQL y Kafka reales en contenedores.
> Sin Docker en ejecución, `mvn verify` falla.

**Máquina:** ~8 GB de RAM libres y ~6 GB de disco (imágenes y repositorio de Maven).

**Puertos libres en el host:** `8081`, `8082`, `8083` (servicios), `5434` (PostgreSQL de
`community-service`) y `5433` (PostgreSQL de `loan-service`).

Comprobación rápida:

```bash
java -version                 # openjdk 25
mvn -v                        # debe mostrar Java 25; si no, ajusta JAVA_HOME
docker info >/dev/null && echo "Docker OK"
docker compose version
newman -v                     # opcional
```

Todos los comandos siguientes se ejecutan **desde la raíz del repositorio**.

---

## 5. Pruebas automatizadas

```bash
mvn clean verify
```

Resultado esperado: **`BUILD SUCCESS`, 85 tests, 0 fallos** (31 en `community-service`, 52 en
`loan-service`, 2 en `notification-consumer`). Tarda unos minutos porque arranca PostgreSQL y Kafka en
contenedores.

| Tipo | Ejemplos | Qué demuestra |
|---|---|---|
| Unitaria pura (sin Quarkus) | `LoanStatusTest`, `LoanDomainServiceTest` | Reglas de negocio y máquina de estados sin framework |
| Unitaria de aplicación (con *fakes*) | `LoanApplicationServiceTest` | Casos de uso aislados de HTTP, BD y Kafka |
| Integración REST (`@QuarkusTest`) | `UserResourceTest`, `LoanResourceTest`, `LoanResourceSecurityTest` | Endpoints, códigos HTTP, paginación, filtros, JWT |
| Integración con mensajería | `LoanEventProducerTest` | El evento llega realmente al tópico |
| Tolerancia a fallos | `CommunityClientAdapterTest` | Con el catálogo inalcanzable se obtiene un error controlado |

---

## 6. Ejecución local

Tres niveles, de menor a mayor parecido a producción. Los tres exponen los mismos puertos y admiten el
mismo flujo de negocio del [§7](#7-probar-el-flujo-de-negocio).

| Nivel | Qué es | Perfil |
|---|---|---|
| 1 — Modo desarrollo | `mvn quarkus:dev`; Quarkus levanta PostgreSQL y Kafka solo | `%dev` |
| 2 — Contenedores individuales | Un `docker run` por componente; red y variables a mano | `%prod` |
| 3 — Docker Compose | Los 6 componentes con un solo comando, orden de arranque por `healthcheck` | `%prod` |

### Nivel 1 — Modo desarrollo

En **tres terminales** (o tres *run configurations* del IDE):

```bash
mvn -pl community-service     quarkus:dev    # http://localhost:8081
mvn -pl loan-service          quarkus:dev    # http://localhost:8082
mvn -pl notification-consumer quarkus:dev    # http://localhost:8083
```

```bash
curl -s localhost:8081/api/objects | jq '.totalElements'     # 23 (3 fijos + 20 generados)
curl -s localhost:8082/q/health/ready | jq .status           # "UP"
```

Recarga en caliente al editar código; `d` abre la Dev UI y `q` sale. Los datos se pierden al parar.

### Empaquetado (necesario para los niveles 2 y 3)

```bash
mvn clean package -DskipTests
docker build -f community-service/src/main/docker/Dockerfile.jvm     -t community-service:jvm     community-service
docker build -f loan-service/src/main/docker/Dockerfile.jvm          -t loan-service:jvm          loan-service
docker build -f notification-consumer/src/main/docker/Dockerfile.jvm -t notification-consumer:jvm notification-consumer
```

(`docker compose up --build` del nivel 3 construye las imágenes por sí solo.)

<details>
<summary>Opcional: imagen nativa de <code>community-service</code></summary>

No requiere GraalVM local (compila dentro de un contenedor); tarda varios minutos y necesita ≥ 6 GB de RAM.

```bash
mvn -pl community-service clean package -Dnative -Dquarkus.native.container-build=true
docker build -f community-service/src/main/docker/Dockerfile.native -t community-service:native community-service
```

Medición de referencia: arranque 1,9 s (JVM) → 0,09 s (nativo); memoria en reposo 285 MiB → 20 MiB;
imagen 705 MB → 296 MB.

</details>

### Nivel 2 — Contenedores individuales (sin Compose)

Muestra a mano lo que Compose automatiza: red, variables de entorno y orden de arranque. Funciona igual
con Podman (`alias docker=podman`).

```bash
docker network create microred-net

docker run -d --name postgres-community --network microred-net -p 5434:5432 \
  -e POSTGRES_DB=community_db -e POSTGRES_USER=community -e POSTGRES_PASSWORD=community postgres:17
docker run -d --name postgres-loan --network microred-net -p 5433:5432 \
  -e POSTGRES_DB=loan_db -e POSTGRES_USER=loan -e POSTGRES_PASSWORD=loan postgres:17
docker run -d --name redpanda --network microred-net \
  redpandadata/redpanda:v26.2.3 redpanda start --mode=dev-container --smp=1 --memory=512M \
  --kafka-addr=internal://0.0.0.0:9092 --advertise-kafka-addr=internal://redpanda:9092
sleep 10    # sin healthcheck: espera a que las bases y el broker estén listos

docker run -d --name community-service --network microred-net -p 8081:8081 \
  -e DATABASE_URL=jdbc:postgresql://postgres-community:5432/community_db \
  -e DATABASE_USER=community -e DATABASE_PASSWORD=community \
  community-service:jvm
docker run -d --name loan-service --network microred-net -p 8082:8082 \
  -e DATABASE_URL=jdbc:postgresql://postgres-loan:5432/loan_db \
  -e DATABASE_USER=loan -e DATABASE_PASSWORD=loan \
  -e COMMUNITY_SERVICE_URL=http://community-service:8081 \
  -e KAFKA_BOOTSTRAP_SERVERS=redpanda:9092 \
  loan-service:jvm
docker run -d --name notification-consumer --network microred-net -p 8083:8083 \
  -e KAFKA_BOOTSTRAP_SERVERS=redpanda:9092 \
  notification-consumer:jvm

for p in 8081 8082 8083; do curl -s localhost:$p/q/health/ready | jq -r .status; done   # UP ×3
```

Si `loan-service` arranca antes de que el catálogo responda, su *readiness* queda `DOWN` y se recupera
sola en cuanto `community-service` contesta.

Limpieza:

```bash
docker rm -f community-service loan-service notification-consumer postgres-community postgres-loan redpanda
docker network rm microred-net
```

### Nivel 3 — Docker Compose

```bash
mvn clean package -DskipTests
docker compose up -d --build
docker compose ps           # esperar ~30-60 s a que los 6 estén (healthy)
```

```text
community-service       Up (healthy)   0.0.0.0:8081->8081
loan-service            Up (healthy)   0.0.0.0:8082->8082
notification-consumer   Up (healthy)   0.0.0.0:8083->8083
postgres-community      Up (healthy)   0.0.0.0:5434->5432
postgres-loan           Up (healthy)   0.0.0.0:5433->5432
redpanda                Up (healthy)
```

Comandos útiles:

```bash
docker compose logs -f loan-service                  # logs JSON
curl -s localhost:8082/q/health | jq                 # liveness + readiness
docker compose exec postgres-community psql -U community -d community_db -c "select id,name,status from objects order by id"
docker compose down                                  # detener (conserva los datos)
docker compose down -v                               # detener y borrar los volúmenes (empezar limpio)
```

---

## 7. Probar el flujo de negocio

Funciona contra cualquiera de los tres niveles.

### Automatizado con Newman

```bash
newman run postman/collection.json
```

Resultado esperado: **10 requests, 20 aserciones, 0 fallos**. Los pasos: crear propietario → crear
solicitante → publicar objeto → consultar disponibles → crear solicitud → consultarla → aprobarla →
verificar objeto `RESERVED` → devolver → verificar objeto `AVAILABLE`. Es re-ejecutable.

### Manual con `curl`

Usa los datos de ejemplo (nivel 1, o una base limpia). Si ya hay datos previos, consulta antes los ids con
`curl -s "$C/api/objects?size=5" | jq '.content[]|{id,ownerId,status}'`.

```bash
C=http://localhost:8081 ; L=http://localhost:8082
TOKEN=$(jq -r '.variable[] | select(.key=="jwtToken").value' postman/collection.json)
J='Content-Type: application/json'

# Catálogo público, paginado y filtrado
curl -s "$C/api/objects?status=AVAILABLE&sort=name&size=5" | jq '{totalElements, totalPages}'

# Sin token → 401
curl -s -o /dev/null -w "%{http_code}\n" -X POST $L/api/loans -H "$J" -d '{}'

# Regla: pedir un objeto propio → 422
curl -s -X POST $L/api/loans -H "Authorization: Bearer $TOKEN" -H "$J" \
  -d '{"requesterId":1,"requestedFrom":"2026-10-01","requestedUntil":"2026-10-05","reason":"x","items":[{"objectId":1}]}' | jq '{status,error}'

# Crear solicitud válida → 201 PENDING
LOAN=$(curl -s -X POST $L/api/loans -H "Authorization: Bearer $TOKEN" -H "$J" -H 'X-Request-Id: demo-001' \
  -d '{"requesterId":2,"requestedFrom":"2026-10-01","requestedUntil":"2026-10-05","reason":"Pintar","items":[{"objectId":1}]}' | jq -r .id)

# Aprobar → APPROVED, y el objeto pasa a RESERVED en el otro servicio
curl -s -X PATCH $L/api/loans/$LOAN/approve -H "Authorization: Bearer $TOKEN" | jq .status
curl -s $C/api/objects/1 | jq .status                     # "RESERVED"

# Aprobar otra vez → 409 (transición inválida)
curl -s -X PATCH $L/api/loans/$LOAN/approve -H "Authorization: Bearer $TOKEN" | jq '{status,message}'

# Devolver → RETURNED, y el objeto vuelve a AVAILABLE
curl -s -X PATCH $L/api/loans/$LOAN/return | jq .status
curl -s $C/api/objects/1 | jq .status                     # "AVAILABLE"
```

### Seguir una operación de extremo a extremo

El `X-Request-Id: demo-001` enviado arriba aparece en los logs de los tres servicios:

```bash
docker compose logs loan-service          | grep demo-001 | jq -r '.message'
docker compose logs community-service     | grep demo-001 | jq -r '.message'
docker compose logs notification-consumer | grep demo-001 | tail -1 | jq '{message, mdc}'
```

(En el nivel 1, búscalo en las tres terminales.)

### Escenarios de resiliencia (con Compose)

**Catálogo caído → 503 controlado, no un cuelgue ni un 500:**

```bash
docker compose stop community-service
curl -s -o /dev/null -w "POST: %{http_code}\n" -X POST $L/api/loans -H "Authorization: Bearer $TOKEN" -H "$J" \
  -d '{"requesterId":2,"requestedFrom":"2026-10-01","requestedUntil":"2026-10-05","reason":"x","items":[{"objectId":1}]}'   # 503
curl -s -o /dev/null -w "ready: %{http_code}\n" $L/q/health/ready    # 503 (deja de recibir tráfico)
curl -s -o /dev/null -w "live:  %{http_code}\n" $L/q/health/live     # 200 (no se reinicia)
docker compose start community-service                                # se recupera solo en ~30 s
```

**Notificaciones caídas → el negocio sigue funcionando:**

```bash
docker compose stop notification-consumer
# crear / aprobar siguen respondiendo 201 / 200
docker compose start notification-consumer
```

---

## 8. Problemas frecuentes

| Síntoma | Causa probable |
|---|---|
| `Could not find a valid Docker environment` | Docker no está en ejecución o tu usuario no está en el grupo `docker` |
| `release version 25 not supported` | Maven usa un JDK anterior: revisa `JAVA_HOME` |
| Tests o `quarkus:dev` parecen colgados al inicio | Primera descarga de las imágenes de PostgreSQL/Redpanda; espera |
| Puerto ocupado al lanzar tests o Compose | Quedó un `quarkus:dev` o un contenedor del nivel 2 abierto |
| El servicio no arranca con `DATABASE_URL` sin definir | Estás en `%prod` sin variables de entorno: es intencional |
| Los ids de los ejemplos `curl` no coinciden | La base tiene datos previos: `docker compose down -v` o consulta los ids |
