package com.file.securepack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecurePackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurePackApplication.class, args);
	}

}
