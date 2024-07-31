package com.nhnacademy.bookstoreaccount.global;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nhnacademy.bookstoreaccount.global.controller.SwaggerController;

import jakarta.servlet.http.HttpServletRequest;

class SwaggerControllerTest {

	private final OpenApiResource openApiResource = Mockito.mock(OpenApiResource.class);
	private final SwaggerController swaggerController = new SwaggerController(openApiResource);

	@Test
	void getSwaggerJson_ShouldReturnSwaggerJson() throws JsonProcessingException {
		// Given
		String apiDocsUrl = "/auth-docs/auth-api"; // Swagger 문서 URL
		Locale locale = Locale.ENGLISH; // 테스트용 Locale
		byte[] swaggerJsonBytes = "{\"swagger\":\"2.0\"}".getBytes(StandardCharsets.UTF_8);

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getLocale()).thenReturn(locale);
		when(openApiResource.openapiJson(any(HttpServletRequest.class), eq(apiDocsUrl), any(Locale.class)))
			.thenReturn(swaggerJsonBytes);

		// When
		ResponseEntity<String> responseEntity = swaggerController.getSwaggerJson(request);

		// Then
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo("{\"swagger\":\"2.0\"}");
	}

	@Test
	void getSwaggerJson_ShouldReturnEmptyJsonWhenNoSwaggerJson() throws JsonProcessingException {
		// Given
		String apiDocsUrl = "/back-docs/back-api"; // Swagger 문서 URL
		Locale locale = Locale.ENGLISH; // 테스트용 Locale

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getLocale()).thenReturn(locale);
		when(openApiResource.openapiJson(any(HttpServletRequest.class), eq(apiDocsUrl), any(Locale.class)))
			.thenReturn(null);

		// When
		ResponseEntity<String> responseEntity = swaggerController.getSwaggerJson(request);

		// Then
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo("{}");
	}
}