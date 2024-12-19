package com.example.SaludClick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaludClickApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaludClickApplication.class, args);
	}

}
