package com.autos_colombia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutosColombiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutosColombiaApplication.class, args);
	}

}
