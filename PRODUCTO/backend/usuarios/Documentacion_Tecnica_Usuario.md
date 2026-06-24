# Documentación Técnica: Microservicio de Usuarios

El microservicio de **Usuarios** es el componente central de identidad y gestión territorial de la plataforma Municipalidad Digital. Actúa como un proveedor de identidad (Identity Provider) para el resto del ecosistema, gestionando la autenticación, autorización y perfiles de los ciudadanos y funcionarios.

---

## 1. Stack Tecnológico
- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.3.0
- **Seguridad:** Spring Security 6.3 + JSON Web Tokens (JWT)
- **Base de Datos:** PostgreSQL 15
- **Migraciones de BD:** Flyway
- **Infraestructura:** Docker & Docker Compose
- **Documentación API:** Swagger (OpenAPI 3)

---

## 2. Arquitectura de Seguridad

El sistema utiliza una arquitectura **Stateless** (sin estado) basada en tokens JWT, eliminando la necesidad de manejar sesiones en el servidor.

### 2.1. Sistema de Doble Contraseña (Simulación Clave Única)
Para simular la integración con proveedores externos gubernamentales, el sistema implementa una arquitectura de autenticación dual mediante un `CustomAuthenticationProvider`:

- **Acceso Plataforma (Email):** Si el identificador contiene un `@`, el sistema busca al usuario por `email` y valida contra la columna `password`.
- **Acceso Gubernamental (RUT):** Si el identificador es numérico (RUT), el sistema busca al usuario por `rut` y valida contra la columna `password_clave_unica`.

### 2.2. Control de Acceso Basado en Roles (RBAC)
La seguridad a nivel de métodos se gestiona con la anotación `@PreAuthorize` de Spring Security. Existen 3 niveles de jerarquía:

1. **`ROLE_ADMIN`**: Acceso total. Puede crear, modificar, desactivar y eliminar usuarios y territorios.
2. **`ROLE_FUNCIONARIO`**: Acceso de gestión. Puede visualizar la base de datos completa y asignar/desasignar vecinos a los territorios. No puede realizar eliminaciones físicas (`DELETE`).
3. **`ROLE_VECINO`**: Acceso restringido. Solo puede visualizar el directorio de territorios (Juntas de Vecinos) y gestionar su propio perfil mediante los endpoints de autorreferencia (`/me`).

---

## 3. Modelo de Datos (Entidades)

### Entidad: `Usuarios`
Implementa la interfaz `UserDetails` de Spring Security.
- **Identificadores:** `id`, `rut` (Único), `email` (Único).
- **Seguridad:** `password`, `passwordClaveUnica`, `rol`.
- **Datos Personales:** `nombres`, `apellidoPaterno`, `apellidoMaterno`, `fechaNacimiento`, `genero`, `telefono`.
- **Ubicación:** `direccion`, `comuna`, `region`.
- **Relaciones:** Pertenece a un `Territorio` (Junta de Vecinos).

> **Nota:** Los métodos heredados de `UserDetails` (como `getAuthorities()`) están marcados con `@JsonIgnore` para evitar conflictos en la deserialización de Jackson durante las peticiones `PUT`.

### Entidad: `Territorio`
Representa una entidad territorial (ej. Junta de Vecinos).
- **Datos:** `id`, `nombre`, `tipo`, `comuna`, `region`, `activo`.
- **Relaciones:** Contiene una lista `OneToMany` de vecinos (`Usuarios`).

---

## 4. Endpoints Principales (API REST)

### 4.1. Autenticación (`/auth`) *[Público]*
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/auth/register` | Crea un nuevo usuario. Encripta y guarda ambas contraseñas usando BCrypt. |
| `POST` | `/auth/login` | Inicia sesión (acepta Email o RUT) y retorna un Token JWT válido por 24 horas. |

### 4.2. Perfil Propio (`/usuarios/me`) *[Cualquier Rol]*
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/usuarios/me` | Retorna los datos del usuario dueño del Token (extraído vía `Authentication.getName()`). |
| `PUT` | `/usuarios/me` | Actualiza la información personal del dueño del Token. |

### 4.3. Gestión de Usuarios (`/usuarios`) *[Admin/Funcionario]*
| Método | Endpoint | Roles Permitidos |
| :--- | :--- | :--- |
| `GET` | `/usuarios` | `ADMIN`, `FUNCIONARIO` |
| `GET` | `/usuarios/rut/{rut}` | `ADMIN`, `FUNCIONARIO` |
| `POST` | `/usuarios` | `ADMIN` |
| `PATCH` | `/usuarios/{id}/desactivar` | `ADMIN` |

### 4.4. Gestión de Territorios (`/territorios`)
| Método | Endpoint | Roles Permitidos |
| :--- | :--- | :--- |
| `GET` | `/territorios` | `ADMIN`, `FUNCIONARIO`, `VECINO` |
| `POST` | `/territorios` | `ADMIN` |
| `PATCH` | `/territorios/{t_id}/vecinos/{u_id}` | `ADMIN`, `FUNCIONARIO` |

---

## 5. Infraestructura y Base de Datos

### Docker Compose
El microservicio se despliega junto a su base de datos utilizando `docker-compose.yml`.
- **Contenedor BD:** `postgres:15-alpine` (Puerto 5432).
- **Contenedor App:** Construido mediante un Dockerfile Multi-stage (Maven + Eclipse Temurin 21 JRE). Expuesto en el puerto `8086`.

### Migraciones (Flyway)
El control de versiones del esquema de base de datos se gestiona con Flyway en lugar de `hibernate.ddl-auto`. Los scripts SQL se encuentran en `src/main/resources/db/migration/`:
- `V20260428174500__crear_tabla_territorios.sql`
- `V20260428174600__crear_tabla_usuarios.sql`
- `V20260429000100__add_password_claveunica.sql` (Soporte dual-login).
