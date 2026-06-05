package muni.Validacion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ValidacionApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		ValidacionApplication.main(new String[]{"--server.port=0"});
	}
}
