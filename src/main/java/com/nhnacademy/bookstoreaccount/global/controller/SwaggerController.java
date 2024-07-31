package com.nhnacademy.bookstoreaccount.global.controller;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SwaggerController {
	private final OpenApiResource openApiResource;

	@GetMapping()
	public ResponseEntity<String> getSwaggerJson(HttpServletRequest request) throws JsonProcessingException {
		String apiDocsUrl = "/auth-docs/auth-api"; // Swagger 문서 URL
		Locale locale = request.getLocale(); // 현재 요청의 Locale

		byte[] swaggerJsonBytes = openApiResource.openapiJson(request, apiDocsUrl, locale);
		if (swaggerJsonBytes == null) {
			return ResponseEntity.ok("{}"); // 빈 JSON 반환
		}

		String swaggerJson = new String(swaggerJsonBytes, StandardCharsets.UTF_8);
		return ResponseEntity.ok(swaggerJson);
	}
}



