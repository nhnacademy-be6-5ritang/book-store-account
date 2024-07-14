package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record GetPaycoUserTokenInfoResponse(
	Long id,
	List<String> roles,
	String status
) {
}
