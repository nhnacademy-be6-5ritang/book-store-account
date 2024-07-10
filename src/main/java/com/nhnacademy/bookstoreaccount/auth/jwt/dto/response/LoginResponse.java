package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import lombok.Builder;

@Builder
public record LoginResponse(
	String accessToken,
	String refreshToken
) {
}
