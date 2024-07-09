package com.nhnacademy.bookstoreaccount.auth.jwt.dto.request;

import lombok.Builder;

@Builder
public record ReissueTokenRequest(
	String refreshToken
) {
}
