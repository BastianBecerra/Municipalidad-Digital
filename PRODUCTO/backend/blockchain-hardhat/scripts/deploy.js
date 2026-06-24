const hre = require("hardhat");

async function main() {
  const DocumentHash = await hre.ethers.getContractFactory("DocumentHash");
  const contract = await DocumentHash.deploy();
  await contract.waitForDeployment();

  const address = await contract.getAddress();
  console.log(`✅ DocumentHash desplegado en: ${address}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
