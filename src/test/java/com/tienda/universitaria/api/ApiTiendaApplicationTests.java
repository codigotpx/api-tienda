package com.tienda.universitaria.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.DockerClientFactory;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApiTiendaApplicationTests {

	@BeforeAll
	static void requiresDocker() {
		Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not available");
	}

	@Test
	void contextLoads() {
	}

}
