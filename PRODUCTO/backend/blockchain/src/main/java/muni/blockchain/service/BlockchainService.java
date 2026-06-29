package muni.blockchain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import muni.blockchain.dto.DocumentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    @Value("${blockchain.chain-id:11155111}")
    private long chainId;

    public String registrarDocumento(String documentId, String content) throws Exception {
        byte[] hashBytes;
        String cleanContent = content.startsWith("0x") ? content.substring(2) : content;
        if (cleanContent.matches("^[0-9a-fA-F]{64}$")) {
            hashBytes = Numeric.hexStringToByteArray(cleanContent);
        } else {
            hashBytes = generarHash(content);
        }
        Bytes32 hashBytes32 = new Bytes32(hashBytes);

        Function function = new Function(
                "registrarHash",
                Arrays.asList(new Utf8String(documentId), hashBytes32),
                Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(function);
        TransactionManager txManager = new RawTransactionManager(web3j, credentials, chainId);
        
        EthSendTransaction ethSendTransaction = txManager.sendTransaction(
                gasProvider.getGasPrice("registrarHash"),
                gasProvider.getGasLimit("registrarHash"),
                contractAddress,
                encodedFunction,
                java.math.BigInteger.ZERO
        );

        if (ethSendTransaction.hasError()) {
            throw new RuntimeException("Error al enviar transacción: " + ethSendTransaction.getError().getMessage());
        }

        String txHash = ethSendTransaction.getTransactionHash();
        log.info("Transacción enviada. Esperando confirmación... Hash: {}", txHash);

        // Esperar por el recibo (máximo 40 segundos, reintentando cada 1 segundo)
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 1000, 40);
        TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(txHash);

        if (!receipt.isStatusOK()) {
            throw new RuntimeException("Error en la ejecución de la transacción en blockchain");
        }

        return receipt.getTransactionHash();
    }

    public DocumentResponse consultarDocumento(String documentId) throws Exception {
        Function function = new Function(
                "consultarDocumento",
                Arrays.asList(new Utf8String(documentId)),
                Arrays.asList(
                        new TypeReference<Bytes32>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Address>() {}
                )
        );

        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(credentials.getAddress(), contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        List<Type> results = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        
        if (results.isEmpty()) {
            throw new RuntimeException("Documento no encontrado en blockchain");
        }

        return DocumentResponse.builder()
                .documentId(documentId)
                .hash(Numeric.toHexString((byte[]) results.get(0).getValue()))
                .timestamp(results.get(1).getValue().toString())
                .registeredBy(results.get(2).getValue().toString())
                .build();
    }

    private byte[] generarHash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(content.getBytes(StandardCharsets.UTF_8));
    }
}
