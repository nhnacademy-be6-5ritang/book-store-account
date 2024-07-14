package com.nhnacademy.bookstoreaccount.auth.jwt.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetPaycoUserTokenInfoResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserTokenInfoResponse;

@FeignClient(name="UserInfoService", url="http://localhost:8083")
public interface UserInfoClient {
	@GetMapping("/api/internal/users/info")
	GetUserTokenInfoResponse getUserInfoByEmail(@RequestHeader("X-User-Email") String userEmail);

	@GetMapping("/api/internal/users/info-by-payco-id")
	ResponseEntity<GetPaycoUserTokenInfoResponse> getUserInfoByPaycoId(@RequestParam("paycoIdNo") String paycoIdNo);
}
