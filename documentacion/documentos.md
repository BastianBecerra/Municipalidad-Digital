# Arquitectura y Pipeline Criptográfico: Microservicio de Documentos (`documentos`)

El microservicio de **Documentos** es el motor operativo responsable del ciclo de vida legal de los trámites digitales del portal municipal. Su misión es la maquetación de archivos, la inyección de sellos de seguridad interactivos y la coordinación de sellados criptográficos en la Blockchain.

---

## 1. Topología de Red y Puertos
*   **Puerto del Servicio:** Se ejecuta en el puerto **`8088`**.
*   **Puerto de Base de Datos:** Se conecta de forma exclusiva a su propia base de datos PostgreSQL en el puerto **`5433`**, garantizando que el almacenamiento de documentos esté segregado del de identidades ciudadanas.

---

## 2. Patrones Arquitectónicos y Aplicación en el Código

Este microservicio implementa múltiples patrones avanzados para gestionar la complejidad y la integración entre sistemas:

### Polimorfismo en Base de Datos (Joined-Table Inheritance)
Para modelar los distintos tipos de documentos municipales (Licitaciones, Contratos, Certificados de Residencia, Salvoconductos, Juntas de Vecinos) que comparten atributos comunes pero contienen propiedades particulares, se aplica el patrón de **Herencia de Tablas Unidas**.
*   **Implementación:** En el modelo de datos, una tabla principal unifica los metadatos comunes a todo documento (como título, fecha de creación, estado, QR, firma y hash criptográfico), mientras que tablas independientes e hijas almacenan los atributos exclusivos de cada trámite. Hibernate vincula lógicamente las tablas secundarias con la base mediante llaves foráneas automáticas.
*   **Ventaja:** Optimiza el almacenamiento físico y previene la dispersión o redundancia de tablas de documentos.

### Capas Lógicas y Inyección de Dependencias
*   **Capa Controladores:** Expone APIs REST para que la interfaz ciudadana o los administradores consulten, aprueben o descarguen PDF.
*   **Capa Servicios:** Alberga la lógica de negocio y encapsula las integraciones con servicios web externos.
*   **Capa Repositorios:** Mapea el acceso físico polimórfico a las base de datos de manera automatizada.

### Patrón Proxy y Clientes de Integración (RestTemplate)
Para recopilar datos e invocar procesos en otros componentes del ecosistema municipal, se implementa el patrón Proxy a través de clientes de servicios web:
*   **Integración con Usuarios:** Resuelve la identidad y datos de contacto de contratistas o ciudadanos consumiendo el perfil de usuarios.
*   **Integración con Blockchain:** Dispara la inmutabilidad criptográfica enviando el hash único al middleware web3.
*   **Integración con Notificaciones:** Invoca el envío del correo electrónico interactivo enviando los datos lógicos de la transacción.
*   **Propagación de Identidad:** El cliente web incluye interceptores que copian y propagan de manera automática las cabeceras de autorización de la petición original, manteniendo el hilo de seguridad sin requerir almacenamiento de estado.

### Pipeline Criptográfico (Firma, QR y PDF)
Al aprobarse administrativamente un borrador de documento, se activa un pipeline de maquetación:
1.  **Cálculo SHA-256:** Genera una huella digital única e inalterable basada en los metadatos concatenados del documento.
2.  **Sello de Firma:** Simula un certificado digital en base al hash que valida la autenticidad e integridad del trámite.
3.  **Matriz QR (ZXing):** Genera una imagen codificando el enlace de verificación pública correspondiente a la huella digital.
4.  **Maquetador PDF (OpenPDF):** Integra los textos del formulario municipal e imprime el QR interactivo y el sello digital en el pie de página de un documento final.

---

## 3. Modelo de Roles y Políticas RBAC (Role-Based Access Control)

La seguridad del microservicio está regida por Spring Security stateless:
*   **Vecino (`ROLE_VECINO`):** Autorizado para la auto-emisión de documentos inmediatos como salvoconductos y actas de residencia.
*   **Funcionario y Administrador (`ROLE_FUNCIONARIO` / `ROLE_ADMIN`):** Autorización exclusiva para acceder a la lógica crítica de negocio, incluyendo la aprobación administrativa de borradores de contratos y licitaciones, y la posterior sincronización con la red Blockchain municipal.
*   **Intercepción JWT:** El filtro de seguridad extrae y valida la firma del token en cada llamada. Si es legítimo, descodifica los roles inyectados en la solicitud y otorga o deniega el acceso a los métodos de negocio antes de su ejecución.
