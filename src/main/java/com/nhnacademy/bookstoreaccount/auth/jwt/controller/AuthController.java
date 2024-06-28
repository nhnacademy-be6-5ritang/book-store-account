package com.nhnacademy.bookstoreaccount.auth.jwt.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	@GetMapping("/info")
	public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.getUserInfo(request));
	}

	@PostMapping("/reissue")
	public ResponseEntity<?> reissueTokens(HttpServletRequest request, HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();

		Map<String, Object> tokens = authService.reissueTokens(cookies);

		if (tokens == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refresh Token 재발급 중 오류가 발생했습니다.");
		}

		removeCookieWithRefreshToken(cookies, response);

		response.setHeader("Authorization", (String)tokens.get("access"));
		response.addCookie((Cookie)tokens.get("CookieWithRefreshToken"));

		return ResponseEntity.status(HttpStatus.CREATED).body("Refresh Token 재발급 성공");
	}

	@PostMapping("/reissue-with-refresh-token")
	public ResponseEntity<ReissueTokensResponse> reissueTokensWithRefreshToken(@RequestBody String refreshToken){
		ReissueTokensResponse reissuedTokens = authService.reissueTokensWithRefreshToken(refreshToken);
		if(reissuedTokens == null){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(reissuedTokens);
	}

	private void removeCookieWithRefreshToken(Cookie[] cookies, HttpServletResponse response) {
		for (Cookie cookie : cookies) {
			if ("Refresh-Token".equals(cookie.getName())) {
				cookie.setValue(null);
				cookie.setMaxAge(0);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
		}
	}
}
