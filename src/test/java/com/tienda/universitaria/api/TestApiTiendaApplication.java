package com.tienda.universitaria.api;

import org.springframework.boot.SpringApplication;

public class TestApiTiendaApplication {

	public static void main(String[] args) {
		SpringApplication.from(ApiTiendaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
