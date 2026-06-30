package muni.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UsuariosApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		UsuariosApplication.main(new String[]{"--server.port=0"});
	}
}
