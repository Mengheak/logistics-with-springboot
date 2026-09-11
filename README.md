# logistics-apis

Spring Boot 4.1 logistics backend: customers book shipments, dispatchers plan trips, drivers
record scans, and anyone holding a tracking number can follow a parcel.

Built on the same layering as `school-mgnt-sb` (envelope DTOs, `ApplicationConfig` pagination,
JWT security flow), with the exception hierarchy, request/response DTO split, MapStruct mappers
and service interface + `impl` layer the project asked for.

## Running

```bash
./mvnw spring-boot:run
```

H2 in-memory, created fresh on each start. Console at `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:logistics`, user `sa`, empty password).

A bootstrap admin is seeded on startup — `admin@logistics.local` / `Admin@12345`. It is
`config.bootstrap.*` in `application.yaml` and is disabled in the `prod` profile.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@logistics.local","password":"Admin@12345"}'
```

Send the returned `access_token` as `Authorization: Bearer <token>` on every other endpoint.

## Tokens

Login returns a pair: a short-lived JWT access token (60 min) and a long-lived refresh token
(14 days), both configurable under `config.security.jwt`.

```bash
# exchange a refresh token for a new pair
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refresh_token":"<refresh_token>"}'
```

Design, and the reasoning behind it:

- **Refresh tokens are opaque random values, not JWTs.** A refresh token has to be revocable,
  and a self-contained token cannot be taken back before it expires.
- **Only the SHA-256 hash is stored.** A dump of `refresh_tokens` must not hand an attacker
  usable credentials. The raw value appears once, in the response that issued it. SHA-256 rather
  than BCrypt because the token already carries 256 bits of entropy — there is nothing to
  brute-force — and a plain digest is what makes an indexed lookup possible.
- **Rotation on every use.** Refreshing consumes the presented token and returns a new one, so a
  leaked token is only useful until the real client next refreshes.
- **Replay detection.** Tokens from one login share a `family_id`. Presenting a token that was
  already *rotated* means a copy leaked, so the whole family is revoked — the attacker and the
  victim are both signed out, and only the victim can sign in again. A token killed by a logout
  or a disabled account is treated as a merely stale client, not a breach, so ordinary clients
  do not raise false alarms.
- **The revocation outlives the rejected request.** Replay handling both writes (revoke) and
  fails (401). In one transaction the exception would roll the revocation back and leave the
  stolen token alive, so `RefreshTokenRevoker` commits it in its own transaction.
- **Authorities are re-read on refresh**, so a role change or a disabled account takes effect at
  the next refresh rather than lingering until the old access token expires.
- **Changing a password or disabling an account revokes every session** for that user —
  otherwise a stolen refresh token would survive the very action meant to stop it.
- Expired rows are purged nightly by `RefreshTokenJanitor` (kept 7 days past expiry, so replay
  stays detectable for a while).

`config.security.jwt.refresh-token-days` is now live; `TokenRevocationReason` records *why* each
token died (`ROTATED`, `LOGOUT`, `LOGOUT_ALL`, `REUSE_DETECTED`, `ACCOUNT_DISABLED`,
`CREDENTIALS_CHANGED`) for auditing.

## Layout

```
common/enums      ShipmentStatus, TripStatus, ... — each owns its own transition table
common/util       ReferenceCodeGenerator (tracking numbers, trip codes)
config            ApplicationConfig, SecurityConfig, CorsConfig, JpaConfig
security          JwtAuthFilter, CurrentUserProvider, 401/403 JSON handlers
models            AuthUser (the UserDetails principal)
entities          aggregates + embeddable Address + AuditableEntity base
dto/<res>/request Create*/Update* — validated inbound payloads
dto/<res>/response *Response / *SummaryResponse — outbound only
dto/base          Response envelope, PaginatedResponse, PaginationMetadata, Link
mapper            MapStruct interfaces (componentModel = spring)
repositories      Spring Data JPA
services          interfaces
services/impl     implementations (business rules live here)
controllers       thin - validate, delegate, wrap in Response
exception         ApiException + subclasses, GlobalExceptionHandler
bootstrap         DataSeeder (roles + first admin)
```

## Response shape

Every endpoint, success or failure, returns the same envelope. JSON is snake_case in both
directions (`spring.jackson.property-naming-strategy: SNAKE_CASE`).

```json
{ "code": "200", "message": "success", "description": "...", "data": { }, "timestamp": 1789090238842 }
```

Paginated endpoints put `data` + `pagination` (with HATEOAS-style `links`) inside `data`.
Validation failures put a field → message map in `data`, keyed by the snake_case name the
client sent.

## Exceptions

`ApiException` is the base; the handler reads the status off the subclass.

| Exception | Status | Used for |
|---|---|---|
| `BadRequestException` | 400 | malformed or self-contradicting request |
| `UnauthorizedException` | 401 | missing/invalid credentials |
| `ForbiddenException` | 403 | authenticated but not allowed |
| `NotFoundException` | 404 | unknown resource |
| `ConflictException` | 409 | duplicates, double-booked vehicle/driver/shipment |
| `BusinessRuleException` | 422 | illegal status transition, over-capacity trip, empty manifest |

`GlobalExceptionHandler` also maps bean-validation failures, unreadable bodies, type
mismatches, missing params, data-integrity violations, optimistic-lock failures, Spring
Security authentication/authorization failures, unsupported methods and unknown routes.

## Domain rules worth knowing

- **Shipment status** moves only along `ShipmentStatus.allowedNextStatuses()`. All changes go
  through `Shipment.transitionTo(...)`, which validates and appends a `TrackingEvent` — a
  shipment cannot move without leaving an audit trail.
- **Total weight** is derived from the parcel list, never taken from the client.
- **Expected delivery date** comes from `ServiceLevel.getTransitDays()`.
- **Trip manifest** is editable only while `PLANNED`. Loading checks the shipment is
  pre-transit, not already on another open trip, and that the vehicle's `capacity_kg` holds.
- **Dispatch** requires a non-empty manifest and an unexpired licence; it flips vehicle and
  driver to `ON_TRIP` and walks every shipment to `IN_TRANSIT`. **Complete** frees both and
  checks the shipments into the destination hub.
- A vehicle or driver already on an open trip (`PLANNED`/`IN_TRANSIT`) cannot be assigned again.
- Customers, warehouses and vehicles are deactivated/retired, never deleted — history has to
  keep resolving.

## Endpoints

| Method | Path | Roles |
|---|---|---|
| POST | `/api/v1/auth/register` | public (always lands on `CUSTOMER`) |
| POST | `/api/v1/auth/login` | public |
| POST | `/api/v1/auth/refresh` | public (the refresh token is the credential) |
| POST | `/api/v1/auth/logout` | public (revokes the presented token) |
| POST | `/api/v1/auth/logout-all` | public (revokes every session for its owner) |
| GET | `/api/v1/tracking/{trackingNumber}` | public |
| GET | `/api/v1/users/me` | any authenticated |
| CRUD | `/api/v1/users`, `/api/v1/roles` | ADMIN |
| CRUD | `/api/v1/warehouses`, `/api/v1/vehicles`, `/api/v1/drivers` | ADMIN writes, DISPATCHER reads |
| CRUD | `/api/v1/customers` | ADMIN, DISPATCHER |
| CRUD | `/api/v1/shipments` | ADMIN, DISPATCHER |
| PATCH | `/api/v1/shipments/{id}/status` | ADMIN, DISPATCHER, DRIVER |
| CRUD | `/api/v1/trips` | ADMIN, DISPATCHER |
| POST | `/api/v1/trips/{id}/shipments` | ADMIN, DISPATCHER |
| DELETE | `/api/v1/trips/{id}/shipments/{shipmentId}` | ADMIN, DISPATCHER |
| POST | `/api/v1/trips/{id}/dispatch`, `/complete` | ADMIN, DISPATCHER, DRIVER |

List endpoints take `page`, `size`, `sort` plus resource-specific filters
(`status`, `keyword`, `customer_id`, `origin_warehouse_id`, ...).

## Not implemented

- **Row-level ownership.** Authorization is role-based only. The `CUSTOMER` role therefore has
  no access to `/shipments` at all — granting it would expose every customer's shipments, since
  there is no per-customer filtering yet. Customers use the public tracking endpoint.
- **No session listing or per-session revoke by id.** `logout-all` is all-or-nothing; the
  `client_info` column is populated from `User-Agent` and ready for a "your devices" screen, but
  no endpoint exposes it yet.
- **No cap on concurrent sessions per user**, and no rate limiting on `/auth/login` or
  `/auth/refresh`.
- No rating/invoicing, no route optimisation, no proof-of-delivery uploads.
- `ddl-auto: update` against H2. The `prod` profile points at Postgres with
  `ddl-auto: validate`, so real schema migrations (Flyway/Liquibase) still need adding.
