package com.nhnacademy.bookstoreaccount.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nhnacademy.bookstoreaccount.global.handler.CustomPageableHandlerMethodArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public CustomPageableHandlerMethodArgumentResolver customPageableHandlerMethodArgumentResolver() {
		return new CustomPageableHandlerMethodArgumentResolver();
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(customPageableHandlerMethodArgumentResolver());
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.parameterName("format")
			.ignoreAcceptHeader(true)
			.favorParameter(true)
			.defaultContentType(MediaType.APPLICATION_JSON)
			.mediaType("json", MediaType.APPLICATION_JSON)
			.mediaType("xml", MediaType.APPLICATION_XML);
	}
}
