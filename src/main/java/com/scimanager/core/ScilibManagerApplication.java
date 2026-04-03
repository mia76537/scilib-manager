package com.scimanager.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ScilibManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScilibManagerApplication.class, args);
	}

}
