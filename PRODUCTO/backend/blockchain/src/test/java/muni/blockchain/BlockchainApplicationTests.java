package muni.blockchain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BlockchainApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		BlockchainApplication.main(new String[]{"--server.port=0"});
	}

}
