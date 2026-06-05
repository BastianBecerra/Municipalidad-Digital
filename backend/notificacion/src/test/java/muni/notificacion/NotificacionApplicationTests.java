package muni.notificacion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificacionApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain() {
		NotificacionApplication.main(new String[]{"--server.port=0"});
	}
}
