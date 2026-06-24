# Arquitectura y Diseño de Seguridad: Microservicio de Usuarios (`usuarios`)

El microservicio de **Usuarios** es el núcleo de identidad digital y de control de accesos para la plataforma. Su función principal es actuar como la autoridad emisora de credenciales y proveer un entorno de confianza para gestionar los diferentes perfiles del sistema municipal.

---

## 1. Topología de Red y Puertos
*   **Puerto del Servicio:** Se ejecuta en el puerto **`8086`**.
*   **Puerto de Base de Datos:** Se conecta de manera dedicada a una base de datos PostgreSQL en el puerto **`5432`** para asegurar el aislamiento físico de la información de los usuarios.

---

## 2. Patrones Arquitectónicos y Aplicación en el Código

Este componente implementa patrones clásicos de ingeniería de software para mantener un diseño robusto y escalable:

### Capas Lógicas (Layered Architecture)
El código se organiza de forma estricta en tres niveles lógicos desacoplados:
*   **Controladores (Presentation Layer):** Clases encargadas de mapear las rutas de entrada HTTP de autenticación e identidad ciudadana, transformando las solicitudes entrantes en objetos lógicos y controlando las respuestas sin inyectar lógica de base de datos.
*   **Servicios (Business Layer):** Orquestan el comportamiento del negocio. Aquí se realiza la validación de claves, el cifrado seguro con salting y hashing Bcrypt de las contraseñas, la verificación de RUTs únicos y la estructuración del token de seguridad.
*   **Repositorios (Data Access Layer):** Interfaces Spring Data que traducen las consultas lógicas a sentencias seguras contra la base de datos de identidad, automatizando las transacciones y previniendo inyecciones de código malicioso.

### Objetos de Transferencia de Datos (DTO Pattern)
Para evitar la sobreexposición del modelo físico de la base de datos a la red externa, el código utiliza clases de transferencia de datos exclusivas:
*   **Registro Ciudadano:** Agrupa los campos mínimos requeridos por el sistema para dar de alta a un usuario, incluyendo RUT, datos personales y la contraseña que posteriormente se procesará.
*   **Solicitud de Credenciales:** Mapea el RUT del ciudadano y la clave suministrada durante el inicio de sesión.
*   **Respuesta de Autenticación:** Encapsula el token criptográfico firmado junto con datos de utilidad no sensibles como el RUT y el rol asociado, facilitando la navegación en la interfaz de usuario.

### Patrón Singleton
Spring gestiona todas las dependencias lógicas (controladores, gestores de token y repositorios) como Beans del tipo Singleton. Esto asegura que solo exista una instancia en memoria para atender todas las solicitudes del servidor, optimizando la asignación de recursos.

---

## 3. Modelo de Roles y Políticas RBAC (Role-Based Access Control)

La protección del microservicio se rige mediante un esquema estricto de control basado en roles asignados a los usuarios:

*   **Público Ciudadano (`ROLE_VECINO`):** Permiso básico asignado por defecto a los residentes registrados en el portal. Otorga accesos exclusivos a la visualización y emisión de trámites personales y salvoconductos.
*   **Personal de Apoyo (`ROLE_FUNCIONARIO`):** Permiso operativo otorgado al staff interno de la municipalidad. Les faculta para auditar solicitudes, interactuar con los borradores de documentos y aplicar sellos administrativos.
*   **Administrador Global (`ROLE_ADMIN`):** Otorga el control administrativo completo de la plataforma, incluyendo la creación y auditoría de cuentas y accesos directos al panel municipal.

### Integración con JWT (JSON Web Tokens)
Tras comprobar la validez de las credenciales (incluyendo la simulación del portal oficial ClaveÚnica), el servicio de autenticación construye un token JWT. En lugar de almacenar la sesión en el servidor, inyecta la identidad (RUT) y la lista de roles del usuario directamente en el cuerpo (Claims) del token, firmándolo con una clave simétrica compartida. De este modo, cualquier otro servicio satélite puede verificar la firma del token en memoria y aplicar las reglas de RBAC de manera stateless e instantánea.
