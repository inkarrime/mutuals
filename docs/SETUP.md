# Mutuals Backend: ejecución local

## Requisitos

- Java 21 (en IntelliJ: File → Project Structure → SDK → Temurin 21)
- Maven 3.9+ (o el Maven integrado de IntelliJ)
- Docker Desktop (para PostgreSQL y Mailpit)

## Pasos

1. Copia las variables de entorno: `cp .env.example .env` y cambia `JWT_SECRET` y `QR_SECRET`.
2. Levanta la base de datos y el servidor de correo de prueba: `docker compose up -d db mail`.
3. Ejecuta la API: `mvn spring-boot:run` (o el botón Run de `MutualsApplication` en IntelliJ, cargando el `.env`).
4. Abre la documentación: http://localhost:8080/swagger-ui.html
5. Los correos enviados se ven en Mailpit: http://localhost:8025

Para levantar todo en contenedores (API incluida): `docker compose --profile full up --build`.

## Prueba de punta a punta desde la terminal

Con la API corriendo, `./scripts/smoke-test.sh` recorre el flujo completo (registro, login, errores, roles, mutuals,
racha, interacción, notificaciones, refresh tokens y correos en Mailpit) y muestra ✔ o ✘ por cada paso.
Requiere `curl` y `jq`.

```bash
docker compose --profile full up --build -d
./scripts/smoke-test.sh
```

Si el puerto 8080 está ocupado, usa otro: `APP_PORT=8090 docker compose --profile full up --build -d` y luego
`./scripts/smoke-test.sh http://localhost:8090`.

## Tests

```bash
mvn verify
```

Genera el reporte de cobertura en `target/site/jacoco/index.html`.

## Variables de entorno

| Variable | Descripción |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL (RDS en producción) |
| `JWT_SECRET` | Clave HMAC del JWT, mínimo 32 caracteres |
| `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS` | Duración del access token y del refresh token |
| `QR_SECRET` | Clave para firmar los códigos QR rotativos |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma |
| `MAIL_*`, `FRONTEND_URL` | SMTP y URL usada en los enlaces de los correos |
| `PUSH_PROVIDER`, `FIREBASE_CREDENTIALS_PATH` | `log` en local, `fcm` con credenciales de Firebase |
| `STORAGE_PROVIDER`, `AWS_S3_BUCKET`, `AWS_REGION` | `local` guarda en `uploads/`, `s3` sube a S3 |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Crea un usuario ADMIN al iniciar si no existe |
| `MAX_ACTIVE_STREAKS_FREE` | Límite de rachas activas del plan gratis (4) |

## Estructura

Paquetes por dominio (`auth`, `user`, `social`, `streak`, `economy`, `challenge`, `achievement`, `avatar`, `activity`,
`notification`, `wrapped`, `subscription`, `admin`), cada uno con `entity`, `repository`, `service`, `controller`,
`dto` y `mapper`. Los eventos de dominio viven en `event` y los jobs programados en `scheduler`.
