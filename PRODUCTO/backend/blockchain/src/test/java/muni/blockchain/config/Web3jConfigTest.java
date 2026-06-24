package muni.blockchain.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Web3jConfigTest {

    @Autowired
    private Web3jConfig web3jConfig;

    @Autowired
    private Web3j web3j;

    @Autowired
    private Credentials credentials;

    @Autowired
    private ContractGasProvider gasProvider;

    @Test
    void testConfigBeans() {
        assertThat(web3jConfig).isNotNull();
        assertThat(web3j).isNotNull();
        
        assertThat(credentials).isNotNull();
        assertThat(credentials.getAddress()).isNotEmpty();
        
        assertThat(gasProvider).isNotNull();
        assertThat(gasProvider.getGasPrice("anyMethod")).isNotNull();
        assertThat(gasProvider.getGasLimit("anyMethod")).isNotNull();
    }
}
