package muni.documentos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DocumentosApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		DocumentosApplication.main(new String[]{"--server.port=0"});
	}

}
