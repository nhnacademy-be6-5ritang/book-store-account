package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record LoginResponse(
	String accessToken,
	String refreshToken,
	LocalDateTime lastLoginAt
) {
}
