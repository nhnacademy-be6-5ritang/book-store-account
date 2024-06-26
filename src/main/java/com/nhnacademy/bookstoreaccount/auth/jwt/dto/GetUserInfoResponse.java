package com.nhnacademy.bookstoreaccount.auth.jwt.dto;

import lombok.Builder;

@Builder
public record GetUserInfoResponse(
	Long id,
	String password,
	String role
) {
}

