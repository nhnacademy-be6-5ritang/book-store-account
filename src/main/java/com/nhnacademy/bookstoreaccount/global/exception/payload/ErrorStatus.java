package com.nhnacademy.bookstoreaccount.global.exception.payload;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorStatus {
	private String message;
	private HttpStatus status;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime timestamp;

	public static ErrorStatus from(String errorMessage, HttpStatus status, LocalDateTime timestamp) {
		return ErrorStatus.builder()
			.message(errorMessage)
			.status(status)
			.timestamp(timestamp)
			.build();
	}
}
