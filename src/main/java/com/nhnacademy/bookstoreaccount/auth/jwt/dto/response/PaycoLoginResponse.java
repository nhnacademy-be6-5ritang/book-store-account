package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import lombok.Builder;

@Builder
public record PaycoLoginResponse(
	String accessToken,
	String refreshToken
) {
}
