# Municipalidad Digital

Plataforma modular para la digitalización municipal, firma electrónica inmutable y validación de documentos a través de Blockchain. El proyecto cuenta con un frontend en React y microservicios independientes construidos con Java y Spring Boot que se comunican por HTTP y utilizan PostgreSQL.

## Estructura del repositorio

El repositorio está organizado bajo la siguiente estructura para facilitar su despliegue, desarrollo y auditoría:

```text
DOCUMENTACIÓN/
  blockchain.md
  documentacion.md
  documentos.md
  notificacion.md
  usuarios.md
  validacion.md

GESTION/
  .gitkeep
  Integrantes.txt             <-- Información del equipo

PRODUCTO/
  backend/
    blockchain/              <-- Microservicio adaptador de Blockchain
    documentos/              <-- Microservicio de gestión de documentos
    notificacion/            <-- Microservicio de envío de alertas
    usuarios/                <-- Microservicio de autenticación y usuarios
    Validacion/              <-- Microservicio de verificación de firmas
  frontend/                  <-- Portal web ciudadano (Vite + React)
```

---

## Servicios y Componentes

### Frontend
- **frontend**: Interfaz de usuario intuitiva desarrollada con React 19 y Vite 8. Permite a los ciudadanos iniciar sesión, consultar su perfil, solicitar la generación de documentos firmados con tecnología Blockchain y realizar la validación de firmas mediante escaneo de códigos QR o carga de archivos.

### Backend (Microservicios)
- **usuarios**: Gestión de perfiles, autenticación con JWT (JSON Web Tokens), validación de RUT chileno, integración de Clave Única y control de territorios y comunas asociadas.
- **blockchain**: Middleware criptográfico que conecta la lógica municipal con contratos inteligentes en redes Ethereum/Hardhat para asegurar la inmutabilidad de los documentos emitidos.
- **documentos**: Emisión, registro, almacenamiento de metadatos y generación de documentos digitales municipales.
- **notificacion**: Alertas de estados de trámites, envío de notificaciones internas y confirmaciones por correo electrónico.
- **Validacion**: Verificador criptográfico que valida las firmas digitales de los documentos generados contra los registros del microservicio de documentos y la Blockchain.

---

## Requisitos

Para levantar el proyecto de manera local, asegúrate de contar con:
- **Java 21** (JDK compatible como Eclipse Temurin).
- **Node.js 22+** (para el frontend).
- **Maven Wrapper** (incluido como `./mvnw` en cada microservicio).
- **Docker y Docker Compose** (para levantar las bases de datos de PostgreSQL e interfaces locales).

---

## Ejecución Local

### 🖥️ 1. Levantar el Frontend
Accede al directorio del cliente web, instala las dependencias y corre el servidor de desarrollo de Vite:
```bash
cd PRODUCTO/frontend
npm install
npm run dev
```
*El frontend estará disponible en:* `http://localhost:5173/`

### ⚙️ 2. Levantar los Microservicios (Backend)
Puedes levantar cada microservicio por separado usando su propio Maven Wrapper. Por ejemplo, para iniciar el servicio de usuarios:
```bash
cd PRODUCTO/backend/usuarios
./mvnw spring-boot:run
```
*(Repite el mismo comando dentro de la carpeta de los servicios `blockchain`, `documentos`, `notificacion` y `Validacion` según los necesites)*.

### Puertos Usados en Desarrollo

| Componente / Servicio | Puerto local |
| :--- | :---: |
| **frontend** | `5173` |
| **usuarios** | `8086` |
| **blockchain** | `8087` |
| **documentos** | `8088` |
| **Validacion** | `8089` |
| **notificacion** | `8090` |

---

## Configuración y Variables de Entorno

### Bases de Datos y Secretos
- Cada microservicio posee un archivo de configuración [application.properties](file:///c:/Users/marti/coding/Municipalidad-Digital/PRODUCTO/backend/usuarios/src/main/resources/application.properties) donde se definen las conexiones a las bases de datos de PostgreSQL y secretos de firma JWT.
- Las variables locales por defecto apuntan a servicios en `localhost`. Para producción o entornos compartidos, puedes inyectar variables de entorno en el sistema o en tus contenedores de Docker Compose.

---

## Validación y Pruebas

### Ejecutar Tests en el Backend (Java/Spring Boot)
Para correr las pruebas unitarias y de integración de cualquier servicio de backend, ingresa a la carpeta correspondiente y ejecuta:
```bash
./mvnw test
```
Para compilar y empaquetar el microservicio omitiendo los tests:
```bash
./mvnw -DskipTests package
```

### Ejecutar Tests en el Frontend (React/Vitest)
Para correr el set de pruebas del cliente web con Vitest:
```bash
cd PRODUCTO/frontend
npm run test
```

---

## Consideraciones Generales
- **Seguridad**: Nunca incluyas credenciales reales, secretos de JWT, o contraseñas de producción en el archivo `application.properties` al subirlos al repositorio.
- **Base de Datos**: Puedes desplegar bases de datos locales rápidamente usando los archivos `docker-compose.yml` provistos en cada microservicio ejecutando `docker-compose up -d`.
