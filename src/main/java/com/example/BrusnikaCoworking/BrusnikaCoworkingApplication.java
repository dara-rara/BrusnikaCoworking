package com.example.BrusnikaCoworking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.BrusnikaCoworking.adapter.repository")
public class BrusnikaCoworkingApplication {
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Yekaterinburg"));
		SpringApplication.run(BrusnikaCoworkingApplication.class, args);
	}
}
