# Inventario Backend

API REST para la gestión de inventario de activos tecnológicos, desarrollada con Spring Boot 3, Spring Security, JWT y MariaDB.

## Tecnologías

- Java 17
- Spring Boot 3.5.16
- Spring Security
- Spring Data JPA
- MariaDB 11.4
- JWT
- OpenAPI / Swagger UI
- Maven

## Requisitos

- Java 17+
- Maven 3.9+
- Docker + Docker Compose

## Inicio rápido

### 1) Levantar la base de datos

```bash
docker compose up -d
```

Esto levantará un contenedor MariaDB en el puerto `3306` con la base de datos `inventario_db`.

### 2) Crear la estructura de la base de datos

Antes de iniciar la aplicación, importa el script SQL del proyecto:

```bash
mysql -h 127.0.0.1 -u inventario -p inventario_db < script.sql
```

O si prefieres conectarte al contenedor:

```bash
docker exec -i inventario-mariadb mariadb -u inventario -pinventario inventario_db < script.sql
```

> El archivo `script.sql` contiene las tablas y objetos necesarios del proyecto. Si no se ejecuta, la app puede arrancar pero no tendrá la estructura requerida para funcionar correctamente.

### 3) Configurar variables de entorno

La aplicación usa valores por defecto en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/inventario_db
spring.datasource.username=inventario
spring.datasource.password=inventario
app.jwt.secret=${JWT_SECRET:inventario-backend-dev-secret-key-2026-change-me}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}
```

Si quieres sobreescribir la clave JWT o el tiempo de expiración:

```bash
export JWT_SECRET=tu_clave_secreta
export JWT_EXPIRATION_MS=3600000
```

### 4) Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La API quedará disponible en:

- http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI docs: http://localhost:8080/v3/api-docs

## Autenticación

La autenticación está basada en JWT y el login público es:

```http
POST /api/auth/login
```

Body de ejemplo:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Usuarios iniciales creados automáticamente por la aplicación:

- `admin` / `admin123` (rol `ADMIN`)
- `user` / `user123` (rol `USER`)

La respuesta devuelve un token JWT y la información del usuario.

```json
{
  "token": "eyJ...",
  "username": "admin",
  "rol": "ADMIN"
}
```

Usa el token en el header `Authorization`:

```http
Authorization: Bearer <token>
```

## Endpoints principales

### Autenticación

- `POST /api/auth/login`

### Activos

- `GET /api/activos`
- `POST /api/activos`
- `GET /api/activos/{id}`
- `PUT /api/activos/{id}`
- `PATCH /api/activos/{id}/estado`

### Categorías

- `GET /api/categorias`

### Reportes

- `GET /api/reportes/activos`

## Roles y permisos

- `ADMIN`: puede crear, actualizar y cambiar el estado de activos.
- `USER`: puede consultar categorías y activos, pero no modificar inventario.

## Ejecutar pruebas

```bash
./mvnw test
```

Para ejecutar una clase de pruebas específica:

```bash
./mvnw test -Dtest=InventarioApiIntegrationTests
```

## Estructura del proyecto

```text
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── docker-compose.yml
├── pom.xml
├── script.sql
├── mvnw
└── README.md
```

## Notas

- El proyecto usa la ruta canónica `/api/auth/login` para la autenticación.
- Si cambias la configuración de MariaDB, ajusta los valores de `spring.datasource.*` en `application.properties`.
