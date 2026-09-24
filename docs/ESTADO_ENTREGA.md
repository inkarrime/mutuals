# Estado de la entrega · Semana 7 (Backend)

CS2031 Desarrollo Basado en Plataformas · Mutuals
Fecha límite: viernes 25 de septiembre de 2026, 11:59 p. m.
Última actualización: 24 de septiembre de 2026

Leyenda: ✅ Hecho · ⚠️ Parcial o por verificar · ❌ Pendiente

## Rúbrica

| # | Requerimiento | Pts | Estado | Detalle |
| --- | --- | --- | --- | --- |
| 1.1 | Más de 6 entidades | 1.5 | ✅ | 28 entidades con `@Entity`, `@Table`, `@Column` |
| 1.2 | Relaciones JPA | 1.0 | ✅ | ManyToOne, OneToOne y OneToMany; todas LAZY, cascade donde corresponde |
| 1.3 | Constraints y validaciones | 0.5 | ✅ | Unique, índices, `@Valid`, `@Email`, `@Pattern`, `@Min`, `@Size`, `@StrongPassword` |
| 2.1 | Más de 10 DTOs | 1.2 | ✅ | ~60 records de request y response |
| 2.2 | Mapeo entidad-DTO | 0.8 | ✅ | Mappers `@Component`, sin fuga de datos sensibles |
| 3.1 | Separación de capas | 0.8 | ✅ | Controller → Service → Repository |
| 3.2 | Principio de responsabilidad única | 0.6 | ✅ | Servicios separados por responsabilidad |
| 3.3 | Inyección de dependencias | 0.6 | ✅ | Por constructor; interfaces `PushSender` y `StorageService` |
| 4.1 | Más de 7 excepciones personalizadas | 0.8 | ✅ | 17 excepciones con jerarquía (`ApiException`) |
| 4.2 | Global Exception Handler | 1.2 | ✅ | `ErrorResponse` uniforme: timestamp, status, error, message, path |
| 5.1 | Spring Security y CORS | 1.0 | ✅ | SecurityContext usado en los servicios |
| 5.2 | Sistema JWT | 1.5 | ✅ | Filtro, refresh tokens con rotación, secret en variable de entorno |
| 5.3 | Roles y autorización | 1.0 | ✅ | USER, PREMIUM, ADMIN con `@PreAuthorize` |
| 5.4 | Registro y login | 0.5 | ✅ | BCrypt, email único, contraseña fuerte |
| 6.1 | Diseño RESTful | 0.8 | ✅ | `/api/v1`, recursos en plural (HATEOAS no considerado) |
| 6.2 | Códigos de estado HTTP | 0.7 | ✅ | 200, 201, 202, 204, 400, 401, 403, 404, 409, 500 |
| 6.3 | Estructura de controladores | 0.5 | ✅ | Controllers delgados, `ResponseEntity`, `@Valid` |
| 7.1 | Eventos | 1.0 | ✅ | 22 eventos con `@TransactionalEventListener` |
| 7.2 | Procesamiento asíncrono | 0.5 | ✅ | `@Async` con `ThreadPoolTaskExecutor` en varios servicios |
| 7.3 | Correo con plantillas | 0.5 | ⚠️ | 4 plantillas Thymeleaf; verificar la llegada en Mailpit (`localhost:8025`) |
| 8 | Deployment en AWS | 2.0 | ❌ | EC2/ECS + RDS, security groups, variables de producción |
| 9.1 | README y documentación | 0.4 | ❌ | Existe `docs/SETUP.md`; falta el README completo |
| 9.2 | Control de versiones | 0.4 | ❌ | Faltan commits, ramas por feature, PRs con code review |
| 9.3 | Gestión de proyecto | 0.2 | ❌ | Faltan GitHub Issues/Projects, milestones y labels |

## Entregables

| Entregable | Estado | Detalle |
| --- | --- | --- |
| Colección de Postman en la raíz (`postman_collection.json`) | ✅ | 115 requests con variables, auth, tests `pm.test` y ejemplos |
| Informe en `README.md` (1000–2000 palabras) | ❌ | Portada, índice, introducción, problema, solución, entidades, errores, seguridad, eventos, GitHub, conclusión, apéndices |

## Bonus

| Bonus | Estado | Detalle |
| --- | --- | --- |
| Swagger / OpenAPI | ✅ | `/swagger-ui.html` |
| Docker Compose | ✅ | PostgreSQL (puerto 5433) y Mailpit |
| Paginación | ✅ | `PageResponse` en listados |
| Filtros y búsquedas | ✅ | Búsqueda de usuarios, filtros de notificaciones y reportes |
| CI/CD con GitHub Actions | ✅ | `.github/workflows/ci.yml`, corre al hacer push |
| Logging con SLF4J | ✅ | Jobs, eventos y errores |
| Upload de archivos a S3 | ⚠️ | `S3StorageService` listo; falta configurar el bucket |
| Cobertura de tests mayor a 80% | ❌ | 13 tests (unitarios e integración) |

## Resumen

Puntaje asegurado aproximado: 15.5 / 20. Pendientes con mayor impacto: deployment en AWS (2 pts), README (0.4), Git (0.4) y GitHub Projects (0.2).
