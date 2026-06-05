package muni.blockchain.service;

import muni.blockchain.dto.DocumentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private Credentials credentials;

    @Mock
    private ContractGasProvider gasProvider;

    @InjectMocks
    private BlockchainService blockchainService;

    private final String contractAddress = "0xE1A7Ba537F7711Ced5F8Ed44445cF974CB49441a";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(blockchainService, "contractAddress", contractAddress);
    }

    @Test
    void testRegistrarDocumento_Success_Hex64Chars() throws Exception {
        String docId = "doc123";
        String contentHex = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
        String expectedTxHash = "0xmockedtxhash";

        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(expectedTxHash);

        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.isStatusOK()).thenReturn(true);
        when(receipt.getTransactionHash()).thenReturn(expectedTxHash);

        try (MockedConstruction<RawTransactionManager> mockedTx = mockConstruction(RawTransactionManager.class,
                (mock, context) -> {
                    when(mock.sendTransaction(any(), any(), anyString(), anyString(), any()))
                            .thenReturn(ethSendTransaction);
                });
             MockedConstruction<PollingTransactionReceiptProcessor> mockedReceipt = mockConstruction(PollingTransactionReceiptProcessor.class,
                (mock, context) -> {
                    when(mock.waitForTransactionReceipt(anyString()))
                            .thenReturn(receipt);
                })) {

            String result = blockchainService.registrarDocumento(docId, contentHex);
            assertThat(result).isEqualTo(expectedTxHash);
        }
    }

    @Test
    void testRegistrarDocumento_Success_HexWith0xPrefix() throws Exception {
        String docId = "doc123";
        String contentHex = "0xa1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
        String expectedTxHash = "0xmockedtxhash";

        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(expectedTxHash);

        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.isStatusOK()).thenReturn(true);
        when(receipt.getTransactionHash()).thenReturn(expectedTxHash);

        try (MockedConstruction<RawTransactionManager> mockedTx = mockConstruction(RawTransactionManager.class,
                (mock, context) -> {
                    when(mock.sendTransaction(any(), any(), anyString(), anyString(), any()))
                            .thenReturn(ethSendTransaction);
                });
             MockedConstruction<PollingTransactionReceiptProcessor> mockedReceipt = mockConstruction(PollingTransactionReceiptProcessor.class,
                (mock, context) -> {
                    when(mock.waitForTransactionReceipt(anyString()))
                            .thenReturn(receipt);
                })) {

            String result = blockchainService.registrarDocumento(docId, contentHex);
            assertThat(result).isEqualTo(expectedTxHash);
        }
    }

    @Test
    void testRegistrarDocumento_Success_PlainText() throws Exception {
        String docId = "doc123";
        String contentText = "Hola Mundo";
        String expectedTxHash = "0xmockedtxhash";

        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(expectedTxHash);

        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.isStatusOK()).thenReturn(true);
        when(receipt.getTransactionHash()).thenReturn(expectedTxHash);

        try (MockedConstruction<RawTransactionManager> mockedTx = mockConstruction(RawTransactionManager.class,
                (mock, context) -> {
                    when(mock.sendTransaction(any(), any(), anyString(), anyString(), any()))
                            .thenReturn(ethSendTransaction);
                });
             MockedConstruction<PollingTransactionReceiptProcessor> mockedReceipt = mockConstruction(PollingTransactionReceiptProcessor.class,
                (mock, context) -> {
                    when(mock.waitForTransactionReceipt(anyString()))
                            .thenReturn(receipt);
                })) {

            String result = blockchainService.registrarDocumento(docId, contentText);
            assertThat(result).isEqualTo(expectedTxHash);
        }
    }

    @Test
    void testRegistrarDocumento_Failure_SendTransactionHasError() throws Exception {
        String docId = "doc123";
        String contentText = "Hola Mundo";

        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        when(ethSendTransaction.hasError()).thenReturn(true);
        
        Response.Error error = mock(Response.Error.class);
        when(error.getMessage()).thenReturn("Simulated Send Error");
        when(ethSendTransaction.getError()).thenReturn(error);

        try (MockedConstruction<RawTransactionManager> mockedTx = mockConstruction(RawTransactionManager.class,
                (mock, context) -> {
                    when(mock.sendTransaction(any(), any(), anyString(), anyString(), any()))
                            .thenReturn(ethSendTransaction);
                })) {

            assertThatThrownBy(() -> blockchainService.registrarDocumento(docId, contentText))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al enviar transacción: Simulated Send Error");
        }
    }

    @Test
    void testRegistrarDocumento_Failure_ReceiptStatusNotOK() throws Exception {
        String docId = "doc123";
        String contentText = "Hola Mundo";
        String expectedTxHash = "0xmockedtxhash";

        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        when(ethSendTransaction.hasError()).thenReturn(false);
        when(ethSendTransaction.getTransactionHash()).thenReturn(expectedTxHash);

        TransactionReceipt receipt = mock(TransactionReceipt.class);
        when(receipt.isStatusOK()).thenReturn(false);

        try (MockedConstruction<RawTransactionManager> mockedTx = mockConstruction(RawTransactionManager.class,
                (mock, context) -> {
                    when(mock.sendTransaction(any(), any(), anyString(), anyString(), any()))
                            .thenReturn(ethSendTransaction);
                });
             MockedConstruction<PollingTransactionReceiptProcessor> mockedReceipt = mockConstruction(PollingTransactionReceiptProcessor.class,
                (mock, context) -> {
                    when(mock.waitForTransactionReceipt(anyString()))
                            .thenReturn(receipt);
                })) {

            assertThatThrownBy(() -> blockchainService.registrarDocumento(docId, contentText))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error en la ejecución de la transacción en blockchain");
        }
    }

    @Test
    void testConsultarDocumento_Success() throws Exception {
        String docId = "doc123";
        String expectedAddress = "0xe1a7ba537f7711ced5f8ed44445cf974cb49441a";

        // Codificar retornos
        byte[] expectedHashBytes = new byte[32];
        expectedHashBytes[0] = 1;
        expectedHashBytes[31] = 9;
        
        String encodedHash = TypeEncoder.encode(new Bytes32(expectedHashBytes));
        String encodedTime = TypeEncoder.encode(new Uint256(BigInteger.valueOf(1717387200L)));
        String encodedAddr = TypeEncoder.encode(new Address(expectedAddress));
        
        String responseValue = "0x" + encodedHash + encodedTime + encodedAddr;

        Request<?, EthCall> request = mock(Request.class);
        EthCall response = mock(EthCall.class);
        when(response.getValue()).thenReturn(responseValue);
        when(request.send()).thenReturn(response);

        when(web3j.ethCall(any(), any())).thenReturn((Request) request);
        when(credentials.getAddress()).thenReturn("0xsomeaddress");

        DocumentResponse res = blockchainService.consultarDocumento(docId);
        
        assertThat(res.getDocumentId()).isEqualTo(docId);
        assertThat(res.getTimestamp()).isEqualTo("1717387200");
        assertThat(res.getRegisteredBy()).isEqualTo(expectedAddress);
        assertThat(res.getHash()).startsWith("0x01");
    }

    @Test
    void testConsultarDocumento_NotFound() throws Exception {
        String docId = "doc123";

        Request<?, EthCall> request = mock(Request.class);
        EthCall response = mock(EthCall.class);
        // Retornar vacío para que no decodifique nada
        when(response.getValue()).thenReturn("0x");
        when(request.send()).thenReturn(response);

        when(web3j.ethCall(any(), any())).thenReturn((Request) request);
        when(credentials.getAddress()).thenReturn("0xsomeaddress");

        assertThatThrownBy(() -> blockchainService.consultarDocumento(docId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Documento no encontrado en blockchain");
    }
}
