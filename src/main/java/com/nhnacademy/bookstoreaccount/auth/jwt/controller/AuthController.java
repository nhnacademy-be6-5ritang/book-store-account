package com.nhnacademy.bookstoreaccount.auth.jwt.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.ReissueTokenRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
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

	@PostMapping("/reissue-with-refresh-token")
	public ResponseEntity<ReissueTokensResponse> reissueTokensWithRefreshToken(@RequestBody ReissueTokenRequest reissueTokenRequest){
		ReissueTokensResponse reissuedTokens = authService.reissueTokensWithRefreshToken(
			reissueTokenRequest.refreshToken()
		);
		if(reissuedTokens == null){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(reissuedTokens);
	}
}
