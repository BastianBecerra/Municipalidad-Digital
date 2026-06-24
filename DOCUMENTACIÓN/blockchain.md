# Adaptador y Firma Criptográfica: Microservicio de Blockchain (`blockchain`)

El microservicio de **Blockchain** funciona como un adaptador o middleware criptográfico que interconecta la lógica de negocio municipal con una red distribuida de contabilidad inmutable. Su propósito es abstraer la complejidad del ecosistema web3 y de las firmas digitales para el resto del backend.

---

## 1. Topología de Red y Puertos
*   **Puerto del Servicio:** Se ejecuta en el puerto **`8087`**.
*   **Nodo Blockchain (Hardhat):** Se conecta a través de llamadas RPC al nodo virtual de Ethereum en ejecución local en el puerto **`8545`**.

---

## 2. Patrones Arquitectónicos y Aplicación en el Código

Este componente implementa patrones adaptadores e integraciones Web3j específicas para dar validez inmutable a los trámites:

### Patrón Adaptador (Web3j Middleware)
El microservicio actúa como un traductor entre el protocolo HTTP REST JSON utilizado por los sistemas internos y el protocolo JSON-RPC nativo de las redes Ethereum:
*   **Beneficio:** Evita que otros microservicios tengan que incorporar librerías de billeteras criptográficas, interactuar de manera directa con gas de transacción, balance de nodos, o gestionar claves privadas. Exponen endpoints tradicionales y devuelven respuestas estandarizadas.

### Patrón Proxy de Contrato Inteligente (Smart Contract Wrappers)
Para invocar los métodos expuestos en el Smart Contract desplegado en la Blockchain local (el cual asocia la relación del identificador de documento con su hash criptográfico SHA-256):
*   **Implementación:** El código utiliza clases generadas automáticamente en base al ABI del contrato. Estas actúan como proxies lógicos, permitiendo invocar funciones remotas del contrato de la red descentralizada como si fuesen métodos locales de Java, simplificando la cohesión del código.

### Sello de Inmutabilidad y Transacción
Cuando el microservicio de documentos aprueba un trámite, se activa el pipeline de registro:
1.  **Firma Local con Clave Privada:** El microservicio utiliza una llave privada municipal preconfigurada para firmar digitalmente la transacción criptográfica de registro.
2.  **Transmisión y Bloque:** Transmite la transacción firmada al nodo de la red en puerto `8545` para su procesamiento por los mineros.
3.  **Transaction Hash:** Tras confirmarse que la transacción ha sido incluida de manera exitosa en un bloque de la Blockchain, el servicio lee y retorna el identificador único de la transacción (`Transaction Hash`), asegurando que la validez e integridad del hash del PDF han quedado grabados de forma permanente, pública y auditable en la red descentralizada.
