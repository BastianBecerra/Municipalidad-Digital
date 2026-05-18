package muni.blockchain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class Web3jConfig {

    @Value("${blockchain.node-url}")
    private String nodeUrl;

    @Value("${blockchain.private-key}")
    private String privateKey;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(nodeUrl));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public ContractGasProvider gasProvider() {
        // Valores estándar para redes locales/testnets
        return new StaticGasProvider(
                BigInteger.valueOf(20_000_000_000L), // Gas Price (20 Gwei)
                BigInteger.valueOf(6_721_975L)       // Gas Limit
        );
    }
}
