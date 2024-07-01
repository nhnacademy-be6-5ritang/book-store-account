package com.nhnacademy.bookstoreaccount.auth.jwt.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record GetUserTokenInfoResponse(
	Long id,
	String password,
	List<String> roles
) {
}
