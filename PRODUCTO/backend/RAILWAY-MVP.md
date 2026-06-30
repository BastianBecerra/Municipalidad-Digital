# Railway MVP Persistente

Este MVP no despliega Hardhat como nodo. Hardhat solo compila y despliega el contrato `DocumentHash` a Sepolia, que sera la testnet persistente del MVP. Railway ejecuta los microservicios Spring.

## Servicios minimos

- `api-gateway`
- `usuarios`
- `documentos`
- `blockchain`
- `Validacion`
- PostgreSQL para `usuarios`
- PostgreSQL para `documentos`

`notificacion` es opcional para el MVP blockchain; si no existe, `documentos` usa circuit breaker y el flujo blockchain puede seguir.

## Variables compartidas

Configurar en todos los servicios Spring:

```env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<base64-secret-compartido>
MUNI_INTERNAL_TOKEN=<token-interno-largo>
EUREKA_CLIENT_ENABLED=false
```

Railway inyecta `PORT`; los servicios ya lo usan con fallback a su puerto local.

## Servicio blockchain

Despues de desplegar el contrato con Hardhat, configurar:

```env
BLOCKCHAIN_NODE_URL=<rpc-sepolia>
BLOCKCHAIN_PRIVATE_KEY=<wallet-testnet-con-fondos>
BLOCKCHAIN_CONTRACT_ADDRESS=<direccion-del-contrato>
BLOCKCHAIN_CHAIN_ID=11155111
```

Sepolia usa `BLOCKCHAIN_CHAIN_ID=11155111`.

## URLs internas entre servicios

Usar los public URLs HTTPS de Railway o sus dominios privados con el puerto real que Railway exponga para cada servicio. No hardcodear `8087`, `8088`, etc. en Railway si el servicio esta escuchando `PORT`.

En `documentos`:

```env
MUNI_USUARIOS_URL=https://<usuarios>.up.railway.app
MUNI_TERRITORIOS_URL=https://<usuarios>.up.railway.app/territorios
MUNI_BLOCKCHAIN_URL=https://<blockchain>.up.railway.app/api/blockchain
MUNI_NOTIFICACION_URL=https://<notificacion>.up.railway.app/api/notificaciones/public
```

En `Validacion`:

```env
MUNI_DOCUMENTOS_URL=https://<documentos>.up.railway.app/documentos
MUNI_BLOCKCHAIN_URL=https://<blockchain>.up.railway.app/api/blockchain
```

En `api-gateway`:

```env
GATEWAY_USUARIOS_URI=https://<usuarios>.up.railway.app
GATEWAY_BLOCKCHAIN_URI=https://<blockchain>.up.railway.app
GATEWAY_DOCUMENTOS_URI=https://<documentos>.up.railway.app
GATEWAY_VALIDACION_URI=https://<validacion>.up.railway.app
CORS_ALLOWED_ORIGINS=<url-del-frontend>
```

## Comprobacion manual

1. Crear un documento desde el frontend.
2. Confirmar que `documentos` llama a `blockchain` con `X-Internal-Token`.
3. Confirmar que `blockchain` devuelve `transactionHash`.
4. Validar el documento por QR/hash.
5. Reiniciar `blockchain` en Railway.
6. Validar el mismo documento nuevamente. Debe seguir aprobando porque el hash vive en la testnet.
