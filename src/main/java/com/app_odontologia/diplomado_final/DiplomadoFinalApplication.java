package com.app_odontologia.diplomado_final;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiplomadoFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiplomadoFinalApplication.class, args);
	}

}
