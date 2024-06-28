package com.nhnacademy.bookstoreaccount.auth.jwt.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtils jwtUtils;

	@Value("${spring.jwt.access-token.expires-in}")
	private Long accessTokenExpiresIn;
	@Value("${spring.jwt.refresh-token.expires-in}")
	private Long refreshTokenExpiresIn;

	public Map<String, Object> getUserInfo(HttpServletRequest request) {
		String accessToken = request.getHeader("Authorization");

		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			Long id = jwtUtils.getUserIdFromToken(accessToken);
			String email = jwtUtils.getEmailFromToken(accessToken);
			String role = jwtUtils.getRoleFromToken(accessToken);

			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("id", id);
			userInfo.put("email", email);
			userInfo.put("role", role);

			return userInfo;
		}

		return null;
	}

	public Map<String, Object> reissueTokens(Cookie[] cookies) {
		String refreshToken = getRefreshTokenFromCookies(cookies);

		Map<String, String> tokens = generateTokens(refreshToken);
		if (tokens == null) {
			return null;
		}
		String newAccessToken = tokens.get("access");
		String newRefreshToken = tokens.get("refresh");

		Cookie cookieWithRefreshToken = createCookie("Refresh-Token", newRefreshToken);
		cookieWithRefreshToken.setPath("/");
		Map<String, Object> result = new HashMap<>();
		result.put("access", newAccessToken);
		result.put("CookieWithRefreshToken", cookieWithRefreshToken);

		return result;
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
		String role = jwtUtils.getRoleFromToken(refreshToken);

		String newAccessToken = jwtUtils.generateAccessToken("access", id, role, accessTokenExpiresIn);
		String newRefreshToken = jwtUtils.generateRefreshToken("refresh", id, role, refreshTokenExpiresIn);

		saveRefreshToken(id, newRefreshToken, refreshTokenExpiresIn);

		Map<String, String> tokens = new HashMap<>();
		tokens.put("access", newAccessToken);
		tokens.put("refresh", newRefreshToken);

		return tokens;
	}

	private String getRefreshTokenFromCookies(Cookie[] cookies) {
		for (Cookie cookie : cookies) {
			if ("Refresh-Token".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
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

	private Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(24 * 60 * 60);
		cookie.setHttpOnly(true);

		return cookie;
	}
}
