const hre = require("hardhat");
const fs = require("fs");
const path = require("path");

async function main() {
  const network = hre.network.name;
  if (network === "hardhat" || network === "localhost") {
    console.warn("Deploying to a non-persistent local network. Use --network amoy or --network sepolia for the MVP.");
  }

  const DocumentHash = await hre.ethers.getContractFactory("DocumentHash");
  const contract = await DocumentHash.deploy();
  await contract.waitForDeployment();

  const address = await contract.getAddress();
  const chainId = Number((await hre.ethers.provider.getNetwork()).chainId);
  const deployer = (await hre.ethers.getSigners())[0].address;
  const deployment = {
    contract: "DocumentHash",
    address,
    network,
    chainId,
    deployer,
    deployedAt: new Date().toISOString(),
  };

  const deploymentsDir = path.join(__dirname, "..", "deployments");
  fs.mkdirSync(deploymentsDir, { recursive: true });
  fs.writeFileSync(
    path.join(deploymentsDir, `${network}.json`),
    `${JSON.stringify(deployment, null, 2)}\n`
  );

  console.log(`DocumentHash deployed: ${address}`);
  console.log(`Network: ${network} (chainId ${chainId})`);
  console.log("");
  console.log("Set these variables in the blockchain service:");
  console.log(`BLOCKCHAIN_CONTRACT_ADDRESS=${address}`);
  console.log(`BLOCKCHAIN_CHAIN_ID=${chainId}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
