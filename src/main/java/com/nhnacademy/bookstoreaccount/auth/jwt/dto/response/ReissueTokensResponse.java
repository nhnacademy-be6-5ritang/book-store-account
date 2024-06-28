package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import lombok.Builder;

@Builder
public record ReissueTokensResponse(
	String accessToken,
	String refreshToken
) {
}
