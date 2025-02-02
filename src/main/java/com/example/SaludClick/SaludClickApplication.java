package com.example.SaludClick;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class SaludClickApplication {

public static void main(String[] args) {
	SpringApplication.run(SaludClickApplication.class, args);
	}
	 @PostConstruct
	    public void init() {
	        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	    }
}
