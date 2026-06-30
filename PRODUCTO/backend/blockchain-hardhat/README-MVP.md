# MVP Blockchain Persistente

Hardhat no se despliega como servicio permanente. Se usa solo para compilar y desplegar el contrato `DocumentHash` a Sepolia, la testnet persistente del MVP.

## Despliegue recomendado

1. Copiar `.env.example` a `.env`.
2. Completar `PRIVATE_KEY` con una wallet de testnet.
3. Completar `SEPOLIA_RPC_URL`.
4. Ejecutar:

```bash
npm ci
npm run compile
npm run deploy:sepolia
```

El script imprimira:

```text
BLOCKCHAIN_CONTRACT_ADDRESS=...
BLOCKCHAIN_CHAIN_ID=...
```

Esos valores se configuran en el servicio Spring `blockchain`, junto con `BLOCKCHAIN_NODE_URL`, `BLOCKCHAIN_PRIVATE_KEY` y `MUNI_INTERNAL_TOKEN`.

## Notas

- Sepolia usa `BLOCKCHAIN_CHAIN_ID=11155111`.
- Si se reinicia Railway, los registros siguen existiendo porque viven en la testnet, no en Hardhat local.
