package com.nhnacademy.bookstoreaccount.auth.jwt.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserTokenInfoResponse;

@FeignClient(name="UserInfoService", url="http://localhost:8083")
public interface UserInfoClient {
	@GetMapping("/internal/users/info")
	GetUserTokenInfoResponse getUserInfoByEmail(@RequestHeader("X-User-Email") String userEmail);
}
