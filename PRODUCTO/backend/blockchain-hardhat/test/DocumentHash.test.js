const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("DocumentHash", function () {
  let contract;
  let owner;

  const testHash = ethers.keccak256(ethers.toUtf8Bytes("contenido del documento"));
  const documentId = "CERT-2026-001";

  beforeEach(async function () {
    [owner] = await ethers.getSigners();
    const DocumentHash = await ethers.getContractFactory("DocumentHash");
    contract = await DocumentHash.deploy();
    await contract.waitForDeployment();
  });

  describe("Registrar Hash", function () {
    it("Debería registrar un documento exitosamente", async function () {
      await contract.registrarHash(documentId, testHash);
      const existe = await contract.existeDocumento(documentId);
      expect(existe).to.be.true;
    });

    it("Debería incrementar el contador de documentos", async function () {
      await contract.registrarHash(documentId, testHash);
      const total = await contract.totalDocumentos();
      expect(total).to.equal(1);
    });

    it("Debería emitir el evento DocumentoRegistrado", async function () {
      await expect(contract.registrarHash(documentId, testHash))
        .to.emit(contract, "DocumentoRegistrado");
    });

    it("Debería rechazar un hash vacío", async function () {
      await expect(
        contract.registrarHash(documentId, ethers.ZeroHash)
      ).to.be.revertedWith("El hash no puede estar vacio");
    });

    it("Debería rechazar registrar un documento duplicado", async function () {
      await contract.registrarHash(documentId, testHash);
      await expect(
        contract.registrarHash(documentId, testHash)
      ).to.be.revertedWith("El documento ya fue registrado");
    });
  });

  describe("Verificar Hash", function () {
    beforeEach(async function () {
      await contract.registrarHash(documentId, testHash);
    });

    it("Debería retornar true para un hash correcto", async function () {
      const resultado = await contract.verificarHash(documentId, testHash);
      expect(resultado).to.be.true;
    });

    it("Debería retornar false para un hash incorrecto", async function () {
      const hashFalso = ethers.keccak256(ethers.toUtf8Bytes("documento modificado"));
      const resultado = await contract.verificarHash(documentId, hashFalso);
      expect(resultado).to.be.false;
    });

    it("Debería fallar si el documento no existe", async function () {
      await expect(
        contract.verificarHash("DOC-INEXISTENTE", testHash)
      ).to.be.revertedWith("Documento no encontrado");
    });
  });

  describe("Consultar Documento", function () {
    beforeEach(async function () {
      await contract.registrarHash(documentId, testHash);
    });

    it("Debería retornar los datos correctos", async function () {
      const [hash, timestamp, registradoPor] = await contract.consultarDocumento(documentId);
      expect(hash).to.equal(testHash);
      expect(registradoPor).to.equal(owner.address);
      expect(timestamp).to.be.greaterThan(0);
    });
  });
});
