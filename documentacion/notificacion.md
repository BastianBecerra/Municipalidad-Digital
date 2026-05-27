# Arquitectura y Control de Accesos: Microservicio de Notificaciones (`notificacion`)

El microservicio de **Notificaciones** es un componente modular y puramente **stateless** (sin base de datos) responsable del procesamiento dinámico y despacho de correos electrónicos profesionales con plantillas HTML personalizadas para el Portal Ciudadano.

---

## 1. Topología de Red y Puertos
*   **Puerto del Servicio:** Se ejecuta y escucha peticiones internas en el puerto **`8090`**.

---

## 2. Patrones Arquitectónicos y Aplicación en el Código

Este componente modular ha sido diseñado bajo patrones orientados al rendimiento y la resiliencia:

### Patrón Stateless Service
El microservicio no requiere persistencia física local. Esto significa que no se acopla a base de datos de usuarios ni mantiene variables de sesión en memoria:
*   **Seguridad Autocontenida:** La identidad y permisos de acceso de cada petición se validan al instante analizando criptográficamente la firma del token JWT recibido, reduciendo la carga y dependencias de la red.

### Patrón de Fallback y Proxy de Correo (Mock SMTP)
Dadas las limitaciones típicas de los servidores SMTP reales en entornos locales de desarrollo, el código implementa un proxy de tolerancia a fallos:
*   **Envío Real:** Si se configuran credenciales válidas en la aplicación, se utiliza el gestor oficial de correo de Spring para despachar el mensaje por red.
*   **Desvío a Logs (Simulador):** Si el servidor SMTP no está disponible o rechaza la conexión, el servicio intercepta el error de red, detiene la falla, y dibuja de manera estructurada y estética el HTML resultante directamente en la consola de logs. Esto permite auditorías visuales y validaciones E2E inmediatas sin interrumpir los flujos.

### Objetos de Transferencia de Datos (DTO Pattern)
El canal REST se comunica a través de DTOs específicos que restringen la información expuesta al mínimo indispensable:
*   **Aprobación de Trámite:** Encapsula datos lógicos como el destinatario, el identificador único del documento, la categoría del trámite, el título informativo y la transacción inmutable de la Blockchain para inyectarla en el cuerpo del correo.
*   **Solicitud de Recuperación:** Agrupa el nombre del usuario, su dirección de correo y el token temporal con la URL construida para el reingreso seguro al sistema.

---

## 3. Modelo de Roles y Políticas RBAC (Role-Based Access Control)

Para resguardar que el servicio no sea utilizado para emitir comunicados o correos falsos simulando ser la Municipalidad, Spring Security restringe los accesos:

*   **Ruta de Restablecimiento de Accesos (Pública):** Diseñada para ser accesible sin token de seguridad. Dado que los usuarios que han olvidado su contraseña no han iniciado sesión aún, esta llamada es de acceso libre.
*   **Ruta de Aprobación Documental (Protegida):** Requiere un token JWT legítimo firmado por la municipalidad. El interceptor valida que el claim del token otorgue expresamente los roles de Administrador (`ROLE_ADMIN`) o Funcionario (`ROLE_FUNCIONARIO`). Si la llamada no arrastra estas credenciales de fe pública, es rechazada en la cadena de filtros devolviendo un código de error de acceso prohibido.
*   **Validación Local de JWT:** Descodifica la clave de firma simétrica compartida para autenticar las llamadas REST internas que realiza automáticamente el microservicio de documentos tras completar una firma.
