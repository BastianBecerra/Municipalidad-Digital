const hre = require("hardhat");

async function main() {
  const contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const DocumentHash = await hre.ethers.getContractFactory("DocumentHash");
  const contract = await DocumentHash.attach(contractAddress);

  const documentId = "DOC-TEST-" + Date.now();
  const testHash = hre.ethers.keccak256(hre.ethers.toUtf8Bytes("Este es un documento de prueba municipal"));

  console.log(`\nIniciando verificación`);
  console.log(`-----------------------------------`);
  console.log(`ID Documento: ${documentId}`);
  console.log(`Hash a registrar: ${testHash}`);

  // 1. Registrar el hash
  console.log(`\nRegistrando hash en la blockchain...`);
  const tx = await contract.registrarHash(documentId, testHash);
  await tx.wait();
  console.log(`Hash guardado en el bloque.`);

  // 2. Consultar el hash
  console.log(`\nConsultando datos desde la blockchain...`);
  const [storedHash, timestamp, registradoPor] = await contract.consultarDocumento(documentId);

  console.log(`-----------------------------------`);
  console.log(`DATOS RECUPERADOS:`);
  console.log(`Hash almacenado: ${storedHash}`);
  console.log(`Fecha registro: ${new Date(Number(timestamp) * 1000).toLocaleString()}`);
  console.log(`Registrado por: ${registradoPor}`);
  console.log(`-----------------------------------`);

  if (storedHash === testHash) {
    console.log(`VERIFICACIÓN EXITOSA: Los datos coinciden perfectamente.`);
  } else {
    console.log(`ERROR: Los datos no coinciden.`);
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
