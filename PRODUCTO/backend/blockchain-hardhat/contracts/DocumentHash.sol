// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title DocumentHash
 * @dev Contrato para registrar y verificar hashes de documentos municipales.
 */
contract DocumentHash {

    struct Documento {
        bytes32 hash;
        uint256 timestamp;
        address registradoPor;
        bool existe;
    }

    mapping(string => Documento) private documentos;
    uint256 public totalDocumentos;

    event DocumentoRegistrado(
        string indexed documentId,
        bytes32 hash,
        address indexed registradoPor,
        uint256 timestamp
    );

    function registrarHash(string calldata documentId, bytes32 hash) external {
        require(hash != bytes32(0), "El hash no puede estar vacio");
        require(!documentos[documentId].existe, "El documento ya fue registrado");

        documentos[documentId] = Documento({
            hash: hash,
            timestamp: block.timestamp,
            registradoPor: msg.sender,
            existe: true
        });

        totalDocumentos++;
        emit DocumentoRegistrado(documentId, hash, msg.sender, block.timestamp);
    }

    function verificarHash(string calldata documentId, bytes32 hash) external view returns (bool coincide) {
        require(documentos[documentId].existe, "Documento no encontrado");
        return documentos[documentId].hash == hash;
    }

    function consultarDocumento(string calldata documentId) external view returns (
        bytes32 hash,
        uint256 timestamp,
        address registradoPor
    ) {
        require(documentos[documentId].existe, "Documento no encontrado");
        Documento storage doc = documentos[documentId];
        return (doc.hash, doc.timestamp, doc.registradoPor);
    }

    function existeDocumento(string calldata documentId) external view returns (bool) {
        return documentos[documentId].existe;
    }
}
