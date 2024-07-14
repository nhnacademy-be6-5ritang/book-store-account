package com.nhnacademy.bookstoreaccount.auth.jwt.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nhnacademy.bookstoreaccount.auth.jwt.client.UserInfoClient;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetPaycoUserTokenInfoResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.PaycoLoginResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtils jwtUtils;
	private final UserInfoClient userInfoClient;

	@Value("${spring.jwt.access-token.expires-in}")
	private Long accessTokenExpiresIn;
	@Value("${spring.jwt.refresh-token.expires-in}")
	private Long refreshTokenExpiresIn;

	public Map<String, Object> getUserInfo(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");

		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			Long id = jwtUtils.getUserIdFromToken(accessToken);
			List<String> roles = jwtUtils.getRolesFromToken(accessToken);

			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("id", id);
			userInfo.put("roles", roles);

			return userInfo;
		}

		return null;
	}

	public ReissueTokensResponse reissueTokensWithRefreshToken(String refreshToken) {
		Map<String, String> tokens = generateTokens(refreshToken);
		if (tokens == null) {
			return null;
		}
		String newAccessToken = tokens.get("access");
		String newRefreshToken = tokens.get("refresh");

		return ReissueTokensResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

	private Map<String, String> generateTokens(String refreshToken) {
		if (refreshToken == null || jwtUtils.validateToken(refreshToken) != null) {
			return null;
		}

		String tokenType = jwtUtils.getTokenTypeFromToken(refreshToken);
		if (!"refresh".equals(tokenType)) {
			return null;
		}

		if (!isRefreshTokenExists(refreshToken)) {
			return null;
		}

		Long id = jwtUtils.getUserIdFromToken(refreshToken);
		List<String> roles = jwtUtils.getRolesFromToken(refreshToken);

		String newAccessToken = jwtUtils.generateToken("access", id, roles, accessTokenExpiresIn);
		String newRefreshToken = jwtUtils.generateToken("refresh", id, roles, refreshTokenExpiresIn);

		saveRefreshToken(id, newRefreshToken, refreshTokenExpiresIn);

		Map<String, String> tokens = new HashMap<>();
		tokens.put("access", newAccessToken);
		tokens.put("refresh", newRefreshToken);

		return tokens;
	}

	private boolean isRefreshTokenExists(String refreshToken) {
		Long userId = jwtUtils.getUserIdFromToken(refreshToken);
		String redisKey = "RefreshToken:" + userId;
		return redisTemplate.opsForHash().hasKey(redisKey, "token");
	}

	private void saveRefreshToken(Long userId, String refreshToken, Long expiresIn) {
		String redisKey = "RefreshToken:" + userId;
		redisTemplate.delete(redisKey);
		redisTemplate.opsForHash().put(redisKey, "token", refreshToken);
		redisTemplate.expire(redisKey, Duration.ofMillis(expiresIn));
	}

	public PaycoLoginResponse getTokensForPaycoUser(String paycoIdNo) {
		GetPaycoUserTokenInfoResponse paycoUserTokenInfoResponse = userInfoClient.getUserInfoByPaycoId(paycoIdNo)
			.getBody();
		if (paycoUserTokenInfoResponse == null) {
			return null;
		}

		Long userId = paycoUserTokenInfoResponse.id();
		List<String> roles = paycoUserTokenInfoResponse.roles();
		String status = paycoUserTokenInfoResponse.status();

		if (!"ACTIVE".equals(status)) {
			return null;
		}

		String accessToken = jwtUtils.generateToken("access", userId, roles, accessTokenExpiresIn);
		String refreshToken = jwtUtils.generateToken("refresh", userId, roles, refreshTokenExpiresIn);

		saveRefreshToken(userId, refreshToken, refreshTokenExpiresIn);

		return PaycoLoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}
}