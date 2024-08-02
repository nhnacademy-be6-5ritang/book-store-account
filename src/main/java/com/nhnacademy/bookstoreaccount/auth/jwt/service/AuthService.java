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
import lombok.Setter;

/**
 * @author 김태환
 * 인증 및 토큰 관리를 담당하는 서비스 클래스입니다.
 * JWT 를 사용하여 액세스 토큰과 리프레시 토큰을 생성, 갱신하고, Redis 를 통해 리프레시 토큰을 관리합니다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtils jwtUtils;
	private final UserInfoClient userInfoClient;

	@Setter
	@Value("${spring.jwt.access-token.expires-in}")
	private Long accessTokenExpiresIn;

	@Setter
	@Value("${spring.jwt.refresh-token.expires-in}")
	private Long refreshTokenExpiresIn;

	/**
	 * HTTP 요청의 액세스 토큰에서 사용자 정보를 추출하여 반환합니다.
	 * 액세스 토큰이 유효하고 올바른 형식일 경우, 사용자 ID와 역할 정보를 포함하는 맵을 반환합니다.
	 *
	 * @param request HTTP 요청 객체.
	 * @return 사용자 ID와 역할 정보를 포함하는 맵. 액세스 토큰이 없거나 유효하지 않은 경우 null 을 반환합니다.
	 */
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

	/**
	 * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
	 * 리프레시 토큰이 유효하지 않거나 Redis 에서 해당 토큰이 존재하지 않으면 null을 반환합니다.
	 *
	 * @param refreshToken 리프레시 토큰.
	 * @return 새로운 액세스 토큰과 리프레시 토큰을 포함하는 {@link ReissueTokensResponse} 객체.
	 *         토큰 발급에 실패한 경우 null 을 반환합니다.
	 */
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

	/**
	 * 리프레시 토큰을 기반으로 새로운 액세스 토큰과 리프레시 토큰을 생성합니다.
	 * 토큰이 유효하지 않거나 리프레시 토큰이 Redis 에 존재하지 않으면 null 을 반환합니다.
	 *
	 * @param refreshToken 리프레시 토큰.
	 * @return 새로운 액세스 토큰과 리프레시 토큰을 포함하는 맵.
	 *         토큰 생성에 실패한 경우 null 을 반환합니다.
	 */
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

	/**
	 * Redis 에서 리프레시 토큰의 존재 여부를 확인합니다.
	 *
	 * @param refreshToken 리프레시 토큰.
	 * @return 리프레시 토큰이 Redis 에 존재하면 true, 그렇지 않으면 false 를 반환합니다.
	 */
	private boolean isRefreshTokenExists(String refreshToken) {
		Long userId = jwtUtils.getUserIdFromToken(refreshToken);
		String redisKey = "RefreshToken:" + userId;
		return redisTemplate.opsForHash().hasKey(redisKey, "token");
	}

	/**
	 * Redis 에 새로운 리프레시 토큰을 저장합니다.
	 * 기존의 리프레시 토큰을 삭제하고 새로운 토큰을 저장한 뒤, 만료 시간을 설정합니다.
	 *
	 * @param userId 사용자 ID.
	 * @param refreshToken 새 리프레시 토큰.
	 * @param expiresIn 리프레시 토큰의 만료 시간 (밀리초 단위).
	 */
	private void saveRefreshToken(Long userId, String refreshToken, Long expiresIn) {
		String redisKey = "RefreshToken:" + userId;
		redisTemplate.delete(redisKey);
		redisTemplate.opsForHash().put(redisKey, "token", refreshToken);
		redisTemplate.expire(redisKey, Duration.ofMillis(expiresIn));
	}

	/**
	 * Payco 사용자 ID를 사용하여 액세스 토큰과 리프레시 토큰을 생성합니다.
	 * Payco 사용자 정보가 유효하고 상태가 "ACTIVE"일 경우, 새로운 토큰을 생성하고 반환합니다.
	 *
	 * @param paycoIdNo Payco 사용자 ID.
	 * @return 액세스 토큰과 리프레시 토큰을 포함하는 {@link PaycoLoginResponse} 객체.
	 *         사용자 정보가 없거나 상태가 "ACTIVE"가 아닌 경우 null 을 반환합니다.
	 */
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