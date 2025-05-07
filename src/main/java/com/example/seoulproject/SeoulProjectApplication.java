package com.example.seoulproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SeoulProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeoulProjectApplication.class, args);
	}

}
