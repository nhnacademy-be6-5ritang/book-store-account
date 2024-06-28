package com.nhnacademy.bookstoreaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableFeignClients
public class BookStoreAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookStoreAccountApplication.class, args);
	}

}