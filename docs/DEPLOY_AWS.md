# Deployment en AWS (EC2 + RDS)

Arquitectura: la API corre en un contenedor Docker dentro de una instancia EC2 y se conecta a PostgreSQL en RDS.
Solo el EC2 puede hablar con la base de datos; desde internet solo están abiertos HTTP (80) y SSH (22, limitado a tu IP).

```
Internet ──80──▶ [SG mutuals-api] EC2 (Docker: mutuals-backend) ──5432──▶ [SG mutuals-db] RDS PostgreSQL
```

Todo cabe en la capa gratuita (Free Tier) de AWS. Usa la misma región para todo (por ejemplo `us-east-1`).

## 1. Security groups

En **EC2 → Security Groups → Create security group** crea dos grupos en la VPC por defecto:

| Nombre | Regla de entrada | Origen |
| --- | --- | --- |
| `mutuals-api` | HTTP, puerto 80 | `0.0.0.0/0` |
| `mutuals-api` | SSH, puerto 22 | My IP |
| `mutuals-db` | PostgreSQL, puerto 5432 | El security group `mutuals-api` (no una IP) |

## 2. Base de datos en RDS

**RDS → Create database**:

- Standard create → **PostgreSQL 16**, plantilla **Free tier**.
- DB instance identifier: `mutuals-db`. Master username: `mutuals`. Master password: una contraseña fuerte (guárdala).
- Instance class: `db.t3.micro` o `db.t4g.micro`, 20 GB gp2/gp3, sin autoscaling de almacenamiento.
- Connectivity: VPC por defecto, **Public access: No**, security group: `mutuals-db` (quita `default`).
- Additional configuration → **Initial database name: `mutuals`** (si lo dejas vacío la base no se crea).

Cuando el estado sea *Available*, copia el **Endpoint** (algo como `mutuals-db.xxxx.us-east-1.rds.amazonaws.com`).

## 3. Instancia EC2

**EC2 → Launch instance**:

- Nombre `mutuals-api`, AMI **Amazon Linux 2023**, tipo `t2.micro` o `t3.micro`.
- Key pair: crea uno (o usa EC2 Instance Connect desde el navegador).
- Network settings → Select existing security group → `mutuals-api`.
- Advanced details → **User data**: pega el contenido de [`deploy/aws/ec2-user-data.sh`](../deploy/aws/ec2-user-data.sh). Instala Docker, Git y 2 GB de swap.
- Opcional: en **Elastic IPs** asigna una IP fija y asóciala a la instancia para que el enlace no cambie al reiniciar.

## 4. Variables de producción y despliegue

Conéctate a la instancia (**Connect → EC2 Instance Connect**) y ejecuta:

```bash
curl -fsSL https://raw.githubusercontent.com/inkarrime/mutuals/master/deploy/aws/production.env.example -o ~/mutuals.env
nano ~/mutuals.env
```

Completa `DB_URL` con el endpoint de RDS, `DB_PASSWORD`, `JWT_SECRET`, `QR_SECRET` (genera cada secreto con
`openssl rand -base64 48`) y el SMTP. Luego:

```bash
chmod 600 ~/mutuals.env
curl -fsSL https://raw.githubusercontent.com/inkarrime/mutuals/master/deploy/aws/deploy.sh -o ~/deploy.sh
bash ~/deploy.sh
```

El script clona `master`, construye la imagen, arranca el contenedor con `SPRING_PROFILES_ACTIVE=prod`
(ver [`application-prod.yml`](../src/main/resources/application-prod.yml)) y espera a que `/actuator/health` responda.
La primera compilación tarda unos minutos en una instancia micro.

> Si el repositorio es privado, clónalo antes con un token: `git clone https://<token>@github.com/inkarrime/mutuals.git ~/mutuals`.

## 5. Verificación

- `http://<ip-publica>/actuator/health` → `{"status":"UP"}`
- `http://<ip-publica>/swagger-ui.html` → documentación interactiva
- En Postman cambia la variable `baseUrl` a `http://<ip-publica>/api/v1` y ejecuta *Auth → Register* y *Login*.
- Logs: `docker logs -f mutuals-backend`

Para publicar una nueva versión basta con volver a ejecutar `bash ~/deploy.sh`.

## Correo en producción

Mailpit solo sirve en local. En producción usa un SMTP real, por ejemplo Gmail con una
[contraseña de aplicación](https://myaccount.google.com/apppasswords) (`MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`,
`MAIL_SMTP_AUTH=true`, `MAIL_STARTTLS=true`). Si prefieres no enviar correos, usa `MAIL_ENABLED=false`.

## Costos

Detén la instancia EC2 y la base RDS cuando no las uses, y elimínalas al terminar el curso para no recibir cargos
fuera de la capa gratuita.
