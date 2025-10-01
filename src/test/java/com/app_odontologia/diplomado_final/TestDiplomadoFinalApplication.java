package com.app_odontologia.diplomado_final;

import org.springframework.boot.SpringApplication;

public class TestDiplomadoFinalApplication {

	public static void main(String[] args) {
		SpringApplication.from(DiplomadoFinalApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
