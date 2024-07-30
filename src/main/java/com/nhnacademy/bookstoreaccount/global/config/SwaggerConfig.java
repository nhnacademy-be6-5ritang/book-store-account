package com.nhnacademy.bookstoreaccount.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info().title("Auth API").version("1.0").description("Auth API 명세서"));
	}

	@Bean
	public GroupedOpenApi api() {
		String[] paths = {"/api/**"};
		String[] packagesToScan = {"com.nhnacademy.bookstoreaccount"};
		return GroupedOpenApi.builder()
			.group("auth-api")
			.pathsToMatch(paths)
			.packagesToScan(packagesToScan)
			.build();
	}
}


