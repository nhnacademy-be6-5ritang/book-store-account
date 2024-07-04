package com.nhnacademy.bookstoreaccount.auth.jwt.dto.request;

public record LoginRequest(
	String email,
	String password
) {
}
