package com.nhnacademy.bookstoreaccount.auth.jwt.controller;

import java.util.Map;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.ReissueTokenRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.PaycoLoginResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * @author 김태환
 * 인증 인가 관련 API 를 제공하는 컨트롤러입니다.
 */
@Tag(name = "Auth", description = "인증 인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	/**
	 * 토큰으로부터 사용자 정보 조회
	 * @param request HttpServletRequest
	 * @return 사용자 정보 응답
	 */
	@Operation(
			summary = "토큰으로부터 사용자 정보 조회",
			description = "Access Token으로부터 사용자의 id와 권한 목록을 조회합니다."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "토큰으로부터 사용자 정보를 성공적으로 조회했습니다."),
	})
	@GetMapping("/info")
	public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
		return ResponseEntity.status(HttpStatus.OK).body(authService.getUserInfo(request));
	}

	/**
	 * 토큰 재발급
	 * @param reissueTokenRequest Refresh Token
	 * @return 토큰 재발급 응답
	 */
	@Operation(
			summary = "토큰 재발급",
			description = "Refresh Token을 이용하여 Access Token과 Refresh Token을 재발급합니다."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "토큰 재발급을 성공적으로 수행했습니다."),
			@ApiResponse(responseCode = "400", description = "Refresh Token이 유효하지 않습니다."),
	})
	@PostMapping("/reissue-with-refresh-token")
	public ResponseEntity<ReissueTokensResponse> reissueTokensWithRefreshToken(
		@RequestBody ReissueTokenRequest reissueTokenRequest) {
		ReissueTokensResponse reissuedTokens = authService.reissueTokensWithRefreshToken(
			reissueTokenRequest.refreshToken()
		);
		if (reissuedTokens == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(reissuedTokens);
	}

	/**
	 * Payco 사용자 토큰 발급
	 * @param paycoIdNo Payco 사용자 ID
	 * @return Payco 사용자 토큰 발급 응답
	 */
	@Operation(
			summary = "Payco 사용자 토큰 발급",
			description = "Payco 로그인 시 Access Token과 Refresh Token을 발급합니다."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Payco 사용자 토큰 발급을 성공적으로 수행했습니다."),
			@ApiResponse(responseCode = "400", description = "Payco 사용자 정보가 없거나 유효하지 않습니다."),
	})
	@PostMapping("/tokens-for-payco-user")
	public ResponseEntity<PaycoLoginResponse> getTokensForPaycoUser(@RequestParam String paycoIdNo) {
		PaycoLoginResponse paycoUserTokens = authService.getTokensForPaycoUser(paycoIdNo);
		if (paycoUserTokens == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(paycoUserTokens);
	}

	/**
	 * 로그인
	 * @param loginRequest 로그인 요청
	 */
	@Operation(
			summary = "로그인",
			description = "이메일과 비밀번호로 로그인합니다."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "로그인을 성공적으로 수행했습니다."),
			@ApiResponse(responseCode = "401", description = "로그인에 실패했습니다."),
	})
	@PostMapping("/login")
	public void login(@RequestBody LoginRequest loginRequest){
		// LoginFilter Swagger 용
	}

	/**
	 * 로그아웃
	 * @header Refresh-Token Refresh Token
	 * @return 로그아웃 성공
	 */
	@Operation(
			summary = "로그아웃",
			description = "Refresh Token을 폐기하여 로그아웃 처리합니다."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "로그아웃을 성공적으로 수행했습니다."),
			@ApiResponse(responseCode = "400", description = "Refresh Token이 없습니다."),
			@ApiResponse(responseCode = "502", description = "Refresh Token이 유효하지 않습니다."),
	})
	@PostMapping("/logout")
	public void logout(){
		// AppCustomLogoutFilter Swagger 용
	}
}
