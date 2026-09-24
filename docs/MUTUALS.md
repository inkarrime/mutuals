# Mutuals: contexto del proyecto

Documento de referencia para el equipo (Oriana, Ashly, Jhanpiere) y para cualquier agente de IA que continúe el trabajo. Resume el producto, las reglas de negocio, la arquitectura y el estado del backend al 24 de septiembre de 2026.

Documentos relacionados:

- `docs/ESTADO_ENTREGA.md`: rúbrica de la Semana 7 y estado de cada requerimiento.
- `docs/SETUP.md`: cómo levantar el proyecto en local.
- `Signatures/signature_1.md`: enunciado y rúbrica oficiales del curso.
- `postman_collection.json`: 115 requests con tests y ejemplos.

## Índice

1. [Producto](#1-producto)
2. [Secciones de la app](#2-secciones-de-la-app)
3. [Reglas de negocio](#3-reglas-de-negocio)
4. [Arquitectura del backend](#4-arquitectura-del-backend)
5. [Modelo de datos (28 entidades)](#5-modelo-de-datos-28-entidades)
6. [Endpoints (87)](#6-endpoints-87)
7. [Eventos, asincronía y jobs](#7-eventos-asincronía-y-jobs)
8. [Seguridad y privacidad](#8-seguridad-y-privacidad)
9. [Manejo de errores](#9-manejo-de-errores)
10. [Configuración](#10-configuración)
11. [Tests y verificación](#11-tests-y-verificación)
12. [Decisiones tomadas](#12-decisiones-tomadas)
13. [Pendientes y roadmap](#13-pendientes-y-roadmap)
14. [Guía para agentes y nuevos colaboradores](#14-guía-para-agentes-y-nuevos-colaboradores)

---

## 1. Producto

**Mutuals** es una app móvil y web gamificada que mantiene vivas las amistades mediante **rachas diarias verificadas** entre dos personas.

- **Problema:** las amistades cercanas se debilitan sin que nadie lo note; no hay una señal clara de qué tan activo está un vínculo ni un motivo concreto para escribirle a alguien hoy.
- **Diferencial:** Snapchat mide que *mandaste algo*; Mutuals mide que **de verdad se vieron o hablaron** (QR, proximidad, confirmación mutua) y usa desafíos de revelación mutua para generar conversación real.
- **Público:** jóvenes de 17 a 25 años.
- **Inspiración:** Duolingo (rachas y Friend Streak), BeReal, Snapchat, Strava y Spotify Wrapped.
- **Estrategia:** building in public en TikTok para conseguir early adopters y llegar a la exposición final con usuarios y métricas reales.
- **Hitos:** backend completo el viernes 25/09/2026 (Semana 7); exposición del producto completo a fin de año (objetivo: premio Berners-Lee).

## 2. Secciones de la app

| Sección | Contenido |
| --- | --- |
| **Home (/mutuals)** | Topbar: gemas a la izquierda y racha personal a la derecha. Grid de mutuals con racha: avatar del amigo y fueguito con los días. Orden: primero las rachas en riesgo (no contadas hoy), luego por días |
| **Desafíos** | Un desafío semanal por pareja, tomado de un catálogo que administra el ADMIN. Revelación mutua: la respuesta del otro se ve cuando ambos cumplen |
| **Novedades** | Logros de mutuals y de perfiles seguidos. Likes a todo; comentarios solo entre mutuals. Notificaciones HIGH (push) y LOW (punto rojo) |
| **Perfil** | Público: avatar personalizable, racha personal, métricas, logros y medallas, contadores de seguidores y seguidos. Las rachas y con quién son privadas |
| **Onboarding** | Registro y pantalla propia que explica la ubicación antes del permiso del sistema. Si no la activa, puede hacerlo luego en Preferences |
| **Preferences** | Ubicación (con consentimiento), push, correos, idioma y silenciar destacados |

## 3. Reglas de negocio

### 3.1 Modelo social

| Relación | Cómo se forma | Qué permite |
| --- | --- | --- |
| Seguidor | Follow a un perfil, sin aprobación | Ver sus logros en Novedades (LOW) y dar like |
| Mutual | El otro devuelve el follow | Comentar, regalar escudos, notificaciones HIGH, invitar a racha |
| Racha | Un mutual invita y el otro acepta | Racha diaria, gemas y desafíos |

- Las listas de seguidores y seguidos son públicas; lo privado son las rachas (con quién, cuántos días y desafíos).
- **Unfollow con racha activa:** responde 409 con los datos de la racha; con `confirmStreakLoss=true` se rompe la racha (motivo `UNFOLLOW`). Al otro no se le notifica.
- **Bloquear:** borra follows en ambas direcciones, termina el mutual y rompe la racha (motivo `BLOCK`), e impide volver a seguir.
- **Reportar:** SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, IMPERSONATION u OTHER; el ADMIN revisa y puede suspender.
- Nada se borra: `Mutual` pasa a `ENDED` y se reactiva si vuelven a ser mutuals, conservando el historial.

### 3.2 Rachas

- La racha es **diaria**: cada día calendario (zona `America/Lima`) necesita al menos una interacción confirmada.
- Varias interacciones el mismo día cuentan como una.
- Una racha recién aceptada tiene un día de gracia para su primera interacción.
- **Job 00:05:** si ayer no hubo interacción, consume el escudo activo o rompe la racha (motivo `MISSED_DAY`).
- **Job 20:00:** avisa de las rachas en riesgo (no contadas hoy).
- **Racha pura:** la que nunca usó escudo.
- **Racha personal:** días seguidos con al menos una interacción con cualquier mutual; se reinicia si pasa un día sin actividad.
- Cada 7 días completados, ambos ganan 10 gemas. Hitos de 7, 30, 100 y 365 días generan logros y novedades.
- **Plan gratis:** hasta 4 rachas activas (`MAX_ACTIVE_STREAKS_FREE`); invitar o aceptar una quinta responde 403. Mutuals y seguidores son ilimitados.

### 3.3 Registro de interacciones

El GPS solo detecta y sugiere; la interacción se verifica con uno de tres métodos. Los tres terminan en el evento `InteractionConfirmed`.

| Método | Cómo funciona | Reglas |
| --- | --- | --- |
| `MANUAL` | Uno marca y el otro confirma o rechaza | Vence a las 24 h; no se confirma la propia; una pendiente por pareja; si hoy ya contó, 409 |
| `QR` | Uno escanea el QR del otro | Token HMAC que rota cada 30 s (acepta la ventana anterior); confirma al instante |
| `PROXIMITY` | Ambos envían su ubicación | Menos de 100 m y menos de 2 min entre pings; el primero recibe 202 (esperando) y el segundo 201 |

**Detección automática (opt-in):** `POST /locations` con la ubicación activada guarda la última posición y, si un mutual con ubicación activa está a menos de 150 m y su racha no contó hoy, dispara `ProximityDetected` (máximo un push por racha por día). Solo se guarda la última ubicación y se borra a los 10 minutos.

### 3.4 Gamificación

| Elemento | Se obtiene | Sirve para |
| --- | --- | --- |
| Gemas | Semanas de racha, desafíos completados y logros | Comprar escudos (30 gemas) e ítems de avatar |
| Escudos | Compra con gemas, regalo de un mutual o beneficio de Plus | Proteger un día sin interacción; máximo 1 por racha por semana |
| Desafíos | Uno por pareja por semana (lunes 00:10) | Contacto real; dan gemas a ambos |
| Avatar | Tienda con gemas o desbloqueo por logro | Personalizar lo que ven tus mutuals en su Home |

- Los escudos nunca se venden con dinero real.
- Al activar un escudo se descuenta de inmediato; si la racha termina por unfollow o bloqueo con el escudo activo, se devuelve.
- Los desafíos con presencia exigen un check-in QR o de proximidad ese día; los de foto exigen imagen (JPEG, PNG o WEBP de hasta 5 MB).

### 3.5 Mutuals Plus (suscripción)

| Beneficio | Gratis | Plus |
| --- | --- | --- |
| Rachas activas | Hasta 4 | Ilimitadas |
| Escudo mensual | No | 1 al mes, más 1 de bienvenida |
| Wrapped histórico | Resumen y 3 amigos | Completo, con sección por amigo |
| Personalización | Tema por defecto | Temas, banner, estilo del fueguito, ícono de la app, widgets |
| Avatar | Tienda con gemas | Además, ítems exclusivos |

El pago es simulado. Activar Plus agrega el rol `PREMIUM`; al vencer (job diario 01:00) se quita. Cancelar mantiene los beneficios hasta el vencimiento.

### 3.6 Novedades y notificaciones

| Prioridad | Origen | Entrega |
| --- | --- | --- |
| HIGH | Mutuals: confirmaciones, invitaciones, rachas en riesgo, escudos, desafíos, logros | Push (FCM), aviso en pantalla y punto rojo |
| LOW | Perfiles seguidos y destacados: rachas de 30 días o más | Solo punto rojo en Novedades |

Los logros públicos nunca revelan con quién es la racha ("Carla alcanzó 200 días de racha").

### 3.7 Wrapped

| Tipo | Contenido | Generación |
| --- | --- | --- |
| Mensual o anual | Interacciones, días activos, racha más larga, top mutual, métodos, desafíos, gemas, escudos regalados, nuevos mutuals | Job asíncrono el día 1 a las 03:00 (anual el 1 de enero); correo con Thymeleaf |
| Histórico | Resumen total y sección por amigo o ex amigo | Al pedirlo; completo solo en Plus |
| De Mutuals | Estadísticas agregadas y anónimas de la plataforma | Endpoint ADMIN; lectura pública |

## 4. Arquitectura del backend

- **Stack:** Java 21, Spring Boot 3.5.6, Spring Security, Spring Data JPA (Hibernate 6), PostgreSQL 16, jjwt 0.12.6, Thymeleaf, springdoc 2.8.9, AWS SDK v2 (S3), Firebase Admin (FCM), Lombok, JUnit 5 y H2 para tests.
- **Organización:** paquetes por dominio y, dentro de cada uno, capas: `entity`, `repository`, `service`, `controller`, `dto`, `mapper` y `listener`.

```
com.mutuals
├── auth           registro, login, refresh tokens, recuperación de contraseña
├── user           perfil, preferencias, personalización, dispositivos, ubicación, cuenta
├── social         follows, mutuals, bloqueos, reportes
├── streak         invitaciones, rachas, interacciones, QR, proximidad, jobs de mantenimiento
├── economy        billetera, gemas, escudos
├── challenge      plantillas, desafíos semanales, respuestas
├── achievement    logros y evaluación automática
├── avatar         tienda e inventario por capas
├── activity       feed de novedades, likes, comentarios
├── notification   notificaciones y push (PushSender: log | fcm)
├── wrapped        cálculo y generación de Wrapped
├── subscription   Mutuals Plus y límite del plan gratis
├── admin          métricas y moderación
├── email          EmailService (Thymeleaf) y listener de correos
├── storage        StorageService: local | s3
├── event          22 eventos de dominio (records)
├── scheduler      jobs con @Scheduled
├── security       JWT, filtro, UserDetailsService, SecurityConfig
├── config         propiedades, async, OpenAPI, seed de datos
└── common         BaseEntity, excepciones, ErrorResponse, PageResponse, utilidades
```

**Principios aplicados:**

- Los controllers solo reciben la petición, validan con `@Valid` y delegan en un servicio; nunca acceden a repositorios.
- Los servicios devuelven DTOs; el mapeo ocurre dentro de la transacción porque `open-in-view` está desactivado.
- La inyección es siempre por constructor (`@RequiredArgsConstructor`).
- Hay interfaces donde existen implementaciones alternativas: `PushSender` y `StorageService`.
- `Clock` se inyecta como bean para que las reglas de fecha se puedan testear.

## 5. Modelo de datos (28 entidades)

Todas extienden `BaseEntity` (`id`, `createdAt`, `updatedAt`). Las relaciones son LAZY. Los nombres de tablas y columnas están en inglés y snake_case.

| Dominio | Entidad | Campos y relaciones clave |
| --- | --- | --- |
| Cuenta | `User` | email y username únicos, displayName, passwordHash, bio, avatarUrl, roles (`USER`, `PREMIUM`, `ADMIN` en `user_roles`), status (`ACTIVE`, `SUSPENDED`, `DELETED`), gems, shields, personalStreak, longestPersonalStreak |
| Cuenta | `UserPreferences` | OneToOne User; ubicación activada, fecha y versión del consentimiento, onboarding, idioma, push, correos, silenciar destacados |
| Cuenta | `ProfileCustomization` | OneToOne User; theme, fireStyle, appIcon (opciones premium), bannerUrl, logros fijados (máx. 3), widgetConfig |
| Cuenta | `Device` | ManyToOne User; fcmToken único, platform |
| Cuenta | `UserLocation` | OneToOne User; última latitud y longitud, recordedAt (TTL de 10 min) |
| Auth | `RefreshToken` | ManyToOne User; hash SHA-256 del token, expiresAt, revokedAt |
| Auth | `PasswordResetToken` | ManyToOne User; hash, expiresAt (30 min), usedAt |
| Social | `Follow` | follower y followed (ManyToOne User); par único |
| Social | `Mutual` | userA (id menor) y userB; status (`ACTIVE`, `ENDED`), firstMutualAt, currentSince, endedAt, endReason |
| Social | `Block` | blocker y blocked; par único |
| Social | `Report` | reporter, reported, reason, description, status (`PENDING`, `REVIEWED`, `DISMISSED`), reviewedBy, resolutionNote |
| Rachas | `StreakInvitation` | mutual, inviter, invitee, status (`PENDING`, `ACCEPTED`, `REJECTED`, `CANCELED`) |
| Rachas | `Streak` | ManyToOne Mutual (una pareja puede tener varias en el tiempo); status (`ACTIVE`, `BROKEN`), currentLength, start/end/lastActiveDate, shieldsUsed, shieldArmed, shieldArmedBy, lastShieldUsedOn, weeksRewarded, endReason (`MISSED_DAY`, `UNFOLLOW`, `BLOCK`, `ACCOUNT_DELETED`) |
| Rachas | `Interaction` | streak, initiator, confirmer, method (`MANUAL`, `PROXIMITY`, `QR`), status (`PENDING`, `CONFIRMED`, `EXPIRED`, `REJECTED`), note, expiresAt, confirmedAt, interactionDate |
| Economía | `WalletTransaction` | user, currency (`GEM`, `SHIELD`), amount, type (`EARN`, `SPEND`, `GIFT_SENT`, `GIFT_RECEIVED`, `USED`, `GRANT`), reason, counterparty |
| Desafíos | `ChallengeTemplate` | title, prompt, type (`REMOTE`, `IN_PERSON`, `CREATIVE`), rewardGems, requiresPresence, requiresPhoto, active |
| Desafíos | `Challenge` | template, mutual, weekStart (único por mutual y semana), status (`OPEN`, `COMPLETED`, `EXPIRED`); OneToMany submissions (cascade ALL + orphanRemoval) |
| Desafíos | `ChallengeSubmission` | challenge, user (único por desafío), content, photoUrl, submittedAt |
| Logros | `Achievement` | code único, name, type (`STREAK`, `PERSONAL_STREAK`, `MONTHLY_MEDAL`, `CHALLENGE`, `SOCIAL`), threshold, rewardGems |
| Logros | `UserAchievement` | user, achievement (par único), unlockedAt |
| Avatar | `AvatarItem` | code único, layer (`BASE`, `HAIR`, `OUTFIT`, `ACCESSORY`), priceGems, premiumOnly, unlockAchievement |
| Avatar | `UserAvatarItem` | user, item (par único), equipped |
| Novedades | `Activity` | actor, type, title (sin revelar al amigo), value, likeCount, commentCount |
| Novedades | `ActivityLike` | user, activity (par único) |
| Novedades | `ActivityComment` | user, activity, content, hidden (moderación) |
| Sistema | `Notification` | recipient, type, priority (`HIGH`, `LOW`), title, body, read, referenceType, referenceId |
| Sistema | `WrappedSummary` | scope (`USER`, `PLATFORM`), user, periodType (`MONTHLY`, `YEARLY`, `ALL_TIME`), periodKey, statsJson |
| Plan | `Subscription` | user, plan (`FREE`, `PLUS`), status (`ACTIVE`, `CANCELED`, `EXPIRED`), startedAt, expiresAt, paymentReference |

**Cascades:** `User` → `UserPreferences` y `ProfileCustomization` (ALL + orphanRemoval); `Challenge` → `ChallengeSubmission` (ALL + orphanRemoval). Rachas, interacciones y mutuals no se borran en cascada porque son historial.

**Datos iniciales (`DataSeeder`):** 10 plantillas de desafíos, 10 logros, 7 ítems de avatar y un ADMIN si existen `ADMIN_EMAIL` y `ADMIN_PASSWORD`.

## 6. Endpoints (87)

Base: `/api/v1`. Salvo `auth/**` y `GET /wrapped/platform/**`, todos requieren `Authorization: Bearer <accessToken>`. `/admin/**` requiere el rol `ADMIN`. La documentación interactiva está en `/swagger-ui.html`.

| Dominio | Endpoints |
| --- | --- |
| Auth | `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/password-reset`, `/auth/password-reset/confirm` |
| Usuario | `GET, PATCH, DELETE /users/me`; `GET, PATCH /users/me/preferences`; `GET, PATCH /users/me/customization`; `POST, GET /users/me/devices`; `DELETE /users/me/devices/{id}`; `GET /users/search?q=`; `GET /users/{id}`; `GET /users/by-username/{username}`; `GET /users/{id}/followers`; `GET /users/{id}/following` |
| Social | `POST, DELETE /users/{id}/follow` (`?confirmStreakLoss=true`); `GET /mutuals`; `POST, DELETE /users/{id}/block`; `GET /blocks`; `POST /reports` |
| Rachas | `POST, GET /streak-invitations` (`?box=received|sent`); `PATCH, DELETE /streak-invitations/{id}`; `GET /streaks/history`; `GET /streaks/{id}`; `POST /streaks/{id}/shield` |
| Interacciones | `POST /interactions`; `POST /interactions/{id}/confirm`; `POST /interactions/{id}/reject`; `GET /interactions/pending`; `GET /interactions/timeline` |
| Check-ins | `GET /qr-codes/me`; `POST /check-ins/qr`; `POST /check-ins/proximity`; `POST /locations` |
| Billetera | `GET /wallet`; `GET /wallet/transactions`; `POST /shields/purchase`; `POST /shields/gifts` |
| Desafíos | `GET /challenges`; `GET /challenges/{id}`; `POST /challenges/{id}/submissions` (multipart: `content`, `photo`) |
| Logros y avatar | `GET /achievements`; `GET /users/{id}/achievements`; `GET /avatar-items`; `POST /avatar-items/{id}/purchase`; `GET /users/me/avatar`; `PUT /users/me/avatar/{itemId}` |
| Novedades | `GET /activities`; `POST, DELETE /activities/{id}/likes`; `GET, POST /activities/{id}/comments` |
| Notificaciones | `GET /notifications` (`?priority=`); `GET /notifications/unread-count`; `PATCH /notifications/{id}/read`; `PATCH /notifications/read-all` |
| Wrapped | `GET /wrapped`; `GET /wrapped/{YYYY-MM o YYYY}`; `GET /wrapped/all-time`; `GET /wrapped/all-time/mutuals/{friendId}` (PREMIUM); `GET /wrapped/platform/{period}` (público) |
| Plus | `GET /subscriptions/me`; `POST /subscriptions`; `DELETE /subscriptions/me` |
| Admin | `GET /admin/metrics`; `GET /admin/reports`; `PATCH /admin/reports/{id}`; `PATCH /admin/users/{id}/status`; `DELETE /admin/comments/{id}`; `GET, POST /admin/challenge-templates`; `PUT, DELETE /admin/challenge-templates/{id}`; `POST /admin/challenges/assign`; `POST /admin/wrapped/platform/{period}` |

## 7. Eventos, asincronía y jobs

Los eventos se publican con `ApplicationEventPublisher`. Los listeners usan `@Async` y `@TransactionalEventListener(fallbackExecution = true)` con `@Transactional(REQUIRES_NEW)`, así que solo actúan sobre datos ya confirmados y un fallo de push o de correo no revierte la operación principal. El pool es `ThreadPoolTaskExecutor` (core 4, max 8, cola 100).

| Evento | Qué lo dispara | Qué hacen los listeners |
| --- | --- | --- |
| `UserRegistered` | Registro | Correo de bienvenida |
| `UserFollowed` | Follow | Notificación LOW |
| `MutualCreated` | Follow devuelto | Notificación HIGH a ambos; logro SOCIAL |
| `StreakInvitationCreated` | Invitación | Notificación HIGH al invitado |
| `StreakStarted` | Invitación aceptada | Notificación a quien invitó |
| `InteractionPending` | Interacción manual marcada | Notificación HIGH para confirmar |
| `InteractionConfirmed` | Cualquier método confirmado | Notificaciones y logros de racha |
| `ProximityDetected` | Ubicaciones cercanas | Push a ambos (una vez por día) |
| `StreakMilestoneReached` | 7, 30, 100 o 365 días | Novedad pública; destacado LOW a seguidores si son 30 días o más |
| `PersonalStreakUpdated` | Racha personal cambia | Logro PERSONAL_STREAK; novedad en hitos |
| `StreakAtRisk` | Job 20:00 | Push HIGH |
| `StreakBroken` | Job, unfollow o bloqueo | Notificación solo si fue `MISSED_DAY` |
| `ShieldUsed` | Job de medianoche | Notificación a ambos |
| `ShieldGifted` | Regalo de escudo | Notificación al receptor |
| `ChallengeAssigned` / `ChallengeCompleted` | Job del lunes / ambos respondieron | Notificaciones; logro CHALLENGE |
| `AchievementUnlocked` | Logro obtenido | Notificación y novedad pública |
| `ActivityLiked` / `ActivityCommented` | Like o comentario | Notificación al autor (HIGH si es mutual) |
| `PasswordResetRequested` | Olvidé mi contraseña | Correo con enlace de un solo uso |
| `WrappedGenerated` | Job mensual o anual | Push y correo HTML |
| `SubscriptionActivated` | Suscripción a Plus | Correo y notificación |

| Job (`America/Lima`) | Horario | Acción |
| --- | --- | --- |
| Cierre del día | 00:05 | Consume escudos, rompe rachas y reinicia rachas personales |
| Rachas en riesgo | 20:00 | Dispara `StreakAtRisk` |
| Expirar interacciones | Cada 15 min | Manuales pendientes con más de 24 h pasan a `EXPIRED` |
| Desafíos semanales | Lunes 00:10 | Expira los de la semana anterior y asigna nuevos |
| Limpiar ubicaciones | Cada 5 min | Borra ubicaciones con más de 10 min |
| Suscripciones | 01:00 diario | Expira y quita `PREMIUM` |
| Escudo mensual Plus | Día 1, 02:00 | +1 escudo a suscriptores activos |
| Wrapped | Día 1, 03:00 y 1 de enero, 04:00 | Genera los Wrapped (asíncrono) |

## 8. Seguridad y privacidad

- **Autenticación:** access token JWT HS256 de 15 min con claims `userId` y `roles`, y refresh token de 30 días guardado como hash SHA-256. El refresh rota en cada uso; si se reusa uno revocado, se revocan todas las sesiones del usuario.
- **Contraseñas:** BCrypt (strength 12) y validación `@StrongPassword` (8 a 72 caracteres, mayúscula, minúscula, número y símbolo).
- **Autorización:** reglas por ruta en `SecurityConfig` (`/admin/**` exige `ADMIN`) y `@PreAuthorize` en métodos (`hasRole('ADMIN')`, `hasRole('PREMIUM')`). Los servicios además verifican pertenencia: solo los miembros de una racha, interacción o desafío pueden verla o actuar sobre ella.
- **Filtro JWT:** carga el usuario desde la base en cada request, así que una cuenta suspendida pierde acceso al instante.
- **Otras medidas:** API stateless con CSRF desactivado (no usa cookies), CORS con orígenes configurables, cabeceras CSP y `frame-options: deny`. Las consultas JPA son parametrizadas (previenen inyección SQL), los uploads se validan por tipo y tamaño, y los secretos viven en variables de entorno.
- **Privacidad:** la ubicación es opt-in con fecha y versión del consentimiento (Ley 29733), solo se guarda la última y se borra a los 10 min o al revocar el permiso. Los DTOs públicos nunca exponen email ni con quién es una racha. Eliminar la cuenta anonimiza los datos ("Usuario eliminado").

## 9. Manejo de errores

`GlobalExceptionHandler` (`@RestControllerAdvice`) devuelve siempre un `ErrorResponse` con `timestamp`, `status`, `error`, `message`, `path` y, cuando corresponde, `fieldErrors` o `details`.

| Excepción | HTTP |
| --- | --- |
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException`, `ConflictException`, `ActiveStreakConflictException`, `InsufficientBalanceException` | 409 |
| `InvalidOperationException`, `InvalidCheckInException`, `InvalidQrTokenException`, `InteractionExpiredException` | 400 |
| `UnauthorizedException`, `InvalidCredentialsException`, `InvalidTokenException` | 401 |
| `ForbiddenOperationException`, `PlanLimitExceededException`, `UserBlockedException`, `AccountSuspendedException` | 403 |
| `StorageException` | 500 |

También se manejan excepciones de Spring: validación de body y parámetros, JSON mal formado, acceso denegado, integridad de datos, método no permitido, ruta inexistente y cualquier otra (500).

## 10. Configuración

Todas las variables tienen valores por defecto para desarrollo en `application.yml`; en producción se definen como variables de entorno (ver `.env.example` y `docs/SETUP.md`).

- **Base de datos:** `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. En local PostgreSQL corre en Docker en el puerto **5433**, porque 5432 lo ocupa un PostgreSQL instalado en Windows.
- **Tokens:** `JWT_SECRET` y `QR_SECRET` (mínimo 32 caracteres), `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS`.
- **Proveedores:** `PUSH_PROVIDER` (`log` o `fcm`, con `FIREBASE_CREDENTIALS_PATH`) y `STORAGE_PROVIDER` (`local` o `s3`, con `AWS_S3_BUCKET` y `AWS_REGION`).
- **Correo:** `MAIL_*`, `MAIL_ENABLED` y `FRONTEND_URL`. En local, Mailpit muestra los correos en `localhost:8025`.
- **Otros:** `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `MAX_ACTIVE_STREAKS_FREE`, `CORS_ALLOWED_ORIGINS`.

## 11. Tests y verificación

- **JUnit (`src/test`), 13 tests:**
  - reglas de `StreakProgressService` (suma diaria, idempotencia, gemas semanales, hitos);
  - `StreakMaintenanceService` (escudo que cubre el día, ruptura);
  - `QrTokenService` (válido, vencido, adulterado);
  - `GeoUtils`;
  - una integración con MockMvc y H2 (registro, validaciones, 401 y 403, follow mutuo, invitación, QR, unfollow con 409).
- **Postman:** `postman_collection.json` con 115 requests en 12 carpetas, en orden de flujo. Cada uno tiene tests, variables automáticas y un ejemplo de respuesta. Se corre con Run collection y requiere el ADMIN configurado.
- **Validación hecha:** compilación, los 13 tests, 87 llamadas manuales y la colección completa, contra PostgreSQL real.

## 12. Decisiones tomadas

| Decisión | Motivo |
| --- | --- |
| Racha diaria, no semanal | Público 17-25 acostumbrado a Snapchat; los escudos reducen la presión |
| El GPS solo sugiere; verifican QR, proximidad o confirmación | El GPS falla bajo techo; así se evitan rachas falsas y se protege la privacidad |
| Follow mutuo = mutual | Un solo mecanismo simple y coherente con el nombre de la app |
| Listas de seguidores públicas y rachas privadas | Lo íntimo es la racha, no la lista |
| No notificar el unfollow | Evita conflicto entre usuarios |
| Nada se borra, solo cambia de estado | Permite historial y Wrapped |
| Limitar rachas activas, no amigos | No frena el crecimiento de la red |
| Escudos sin venta con dinero | Ético: no se "compra" una amistad |
| Pago de Plus simulado | Alcance del curso |
| Mappers manuales y no MapStruct | Menos riesgo de configuración; la rúbrica acepta métodos de conversión |
| `ddl-auto: update` sin Flyway | Velocidad para la entrega; Flyway queda en el roadmap |
| Email y contraseña como login base; Google después | La rúbrica exige registro con contraseña; Apple ID requiere cuenta de pago |

## 13. Pendientes y roadmap

**Para el viernes (Semana 7):**

1. Deployment en AWS con EC2 o ECS, RDS, security groups y variables de producción (2 pts).
2. Informe en `README.md` de 1000 a 2000 palabras, con las secciones del enunciado.
3. Git: subir por ramas de feature con PRs revisados por otro integrante.
4. GitHub Projects con issues, milestones y labels.
5. Verificar los correos en Mailpit y configurar el bucket de S3.

**Después:**

- Subir la cobertura de tests a más de 80 %.
- Integrar FCM real con las credenciales de Firebase.
- Migraciones con Flyway y rate limiting en login.
- Login con Google.
- Frontend web (React) y móvil (React Native), con widgets y detección GPS en segundo plano mediante geofencing.
- Aniversario de mutuals, Wrapped de pareja, recuperar racha (Plus), tarjetas compartibles y rachas en espacios UTEC.

## 14. Guía para agentes y nuevos colaboradores

**Antes de cambiar algo:**

1. Lee este documento, `docs/ESTADO_ENTREGA.md` y `Signatures/signature_1.md` (la rúbrica manda).
2. Levanta el entorno: `docker compose up -d db mail` y ejecuta `MutualsApplication` con Java 21.
3. Revisa Swagger (`/swagger-ui.html`) y corre la colección de Postman para ver el comportamiento real.

**Convenciones:**

- Código, nombres, tablas y mensajes de error en inglés; los textos al usuario (notificaciones y correos) en español.
- Cada funcionalidad va en su paquete de dominio y respeta Controller → Service → Repository. Nada de lógica en controllers.
- Los DTOs son `record`, con validaciones de Jakarta. Nunca se devuelven entidades.
- Los errores se lanzan como excepciones de `common/exception`; nunca se construyen respuestas de error a mano.
- Los efectos secundarios (push, correo, logros, novedades) van en listeners de eventos, no dentro del servicio principal.
- Las fechas de negocio se calculan con el `Clock` inyectado (`LocalDate.now(clock)`), nunca con `LocalDate.now()`.
- Todo endpoint nuevo lleva `@Operation`, se agrega a la colección de Postman y se actualiza `docs/ESTADO_ENTREGA.md`.
- No se commitean `.env` ni credenciales.

**Cómo agregar una funcionalidad:**

1. Entidad y repositorio.
2. DTOs de request y response.
3. Mapper.
4. Servicio con `@Transactional` y validación de pertenencia.
5. Controller.
6. Evento y listener, si hay efectos secundarios.
7. Tests.
8. Postman.
9. Actualizar este documento.

**Cuidado con:**

- Spring Boot 3.5 requiere Java 21 en IntelliJ. JDK 26 da problemas con Lombok y JaCoCo.
- El PostgreSQL de Docker está en el puerto 5433, no en 5432.
- Una racha pertenece a un `Mutual`: para operar sobre la racha de dos usuarios, busca primero el mutual activo (`MutualService.getActiveBetween`).
- `Mutual.userA` siempre es el id menor (`PairKey`).
- Los listeners corren en otro hilo: recarga las entidades por id dentro del listener y no pases entidades en los eventos.
