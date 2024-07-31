package com.nhnacademy.bookstoreaccount.auth.jwt.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;

import com.nhnacademy.bookstoreaccount.auth.jwt.client.UserInfoClient;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetPaycoUserTokenInfoResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.PaycoLoginResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AuthService;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;

class AuthServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserInfoClient userInfoClient;

	@InjectMocks
	private AuthService authService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);

		// Set values for accessTokenExpiresIn and refreshTokenExpiresIn
		authService.setAccessTokenExpiresIn(3600000L); // 1 hour in milliseconds
		authService.setRefreshTokenExpiresIn(7200000L); // 2 hours in milliseconds
	}

	@Test
	void testGetUserInfo() {
		String token = "Bearer testToken";
		when(request.getHeader("Authorization")).thenReturn(token);
		when(jwtUtils.getUserIdFromToken(token)).thenReturn(1L);
		when(jwtUtils.getRolesFromToken(token)).thenReturn(List.of("ROLE_USER"));

		Map<String, Object> userInfo = authService.getUserInfo(request);

		assertThat(userInfo).isNotNull();
		assertThat(userInfo.get("id")).isEqualTo(1L);
		assertThat(userInfo.get("roles")).isEqualTo(List.of("ROLE_USER"));
	}

	@Test
	void testGetUserInfo_WithValidToken() {
		String accessToken = "Bearer validAccessToken";
		Long userId = 1L;
		List<String> roles = List.of("ROLE_USER");

		when(request.getHeader("Authorization")).thenReturn(accessToken);
		when(jwtUtils.getUserIdFromToken(accessToken)).thenReturn(userId);
		when(jwtUtils.getRolesFromToken(accessToken)).thenReturn(roles);

		Map<String, Object> userInfo = authService.getUserInfo(request);

		assertThat(userInfo).isNotNull();
		assertThat(userInfo.get("id")).isEqualTo(userId);
		assertThat(userInfo.get("roles")).isEqualTo(roles);
	}

	@Test
	void testGetUserInfo_WithInvalidToken() {
		when(request.getHeader("Authorization")).thenReturn(null);

		Map<String, Object> userInfo = authService.getUserInfo(request);

		assertThat(userInfo).isNull();
	}

	@Test
	void testGetUserInfo_WithNonBearerToken() {
		when(request.getHeader("Authorization")).thenReturn("InvalidToken");

		Map<String, Object> userInfo = authService.getUserInfo(request);

		assertThat(userInfo).isNull();
	}

	@Test
	void testReissueTokensWithRefreshToken() {
		String refreshToken = "testRefreshToken";
		String newAccessToken = "newAccessToken";
		String newRefreshToken = "newRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken(refreshToken)).thenReturn("refresh");
		when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(1L);
		when(jwtUtils.getRolesFromToken(refreshToken)).thenReturn(List.of("ROLE_USER"));
		when(hashOperations.hasKey(anyString(), eq("token"))).thenReturn(true);
		when(jwtUtils.generateToken(anyString(), anyLong(), anyList(), anyLong())).thenReturn(newAccessToken,
			newRefreshToken);

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNotNull();
		assertThat(response.accessToken()).isEqualTo(newAccessToken);
		assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

		verify(redisTemplate, times(1)).delete("RefreshToken:1");
		verify(hashOperations, times(1)).put(eq("RefreshToken:1"), eq("token"), eq(newRefreshToken));
	}

	@Test
	void testReissueTokensWithRefreshToken_Success() {
		String refreshToken = "validRefreshToken";
		String newAccessToken = "newAccessToken";
		String newRefreshToken = "newRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken(refreshToken)).thenReturn("refresh");
		when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(1L);
		when(jwtUtils.getRolesFromToken(refreshToken)).thenReturn(List.of("ROLE_USER"));
		when(redisTemplate.opsForHash().hasKey(anyString(), eq("token"))).thenReturn(true);
		when(jwtUtils.generateToken(anyString(), anyLong(), anyList(), anyLong())).thenReturn(newAccessToken,
			newRefreshToken);

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNotNull();
		assertThat(response.accessToken()).isEqualTo(newAccessToken);
		assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

		verify(redisTemplate, times(1)).delete("RefreshToken:1");
		verify(hashOperations, times(1)).put(eq("RefreshToken:1"), eq("token"), eq(newRefreshToken));
	}

	@Test
	void testReissueTokensWithRefreshToken_InvalidRefreshToken() {
		String refreshToken = "invalidRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn("Invalid token");

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNull();
		verify(redisTemplate, never()).delete(anyString());
		verify(redisTemplate.opsForValue(), never()).set(anyString(), any(), anyLong(), any());
	}

	@Test
	void testReissueTokensWithRefreshToken_NonRefreshTokenType() {
		String refreshToken = "validRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken(refreshToken)).thenReturn("access");

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNull();
		verify(redisTemplate, never()).delete(anyString());
		verify(redisTemplate.opsForValue(), never()).set(anyString(), any(), anyLong(), any());
	}

	@Test
	void testReissueTokensWithRefreshToken_TokenNotExistsInRedis() {
		String refreshToken = "validRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken(refreshToken)).thenReturn("refresh");
		when(jwtUtils.getUserIdFromToken(refreshToken)).thenReturn(1L);
		when(redisTemplate.opsForHash().hasKey(anyString(), eq("token"))).thenReturn(false);

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNull();
		verify(redisTemplate, never()).delete(anyString());
		verify(redisTemplate.opsForValue(), never()).set(anyString(), any(), anyLong(), any());
	}

	@Test
	void testReissueTokensWithNullRefreshToken() {
		String refreshToken = null;

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNull();
		verify(jwtUtils, never()).validateToken(anyString());
		verify(jwtUtils, never()).getTokenTypeFromToken(anyString());
		verify(redisTemplate, never()).opsForHash();
	}

	@Test
	void testReissueTokensWithInvalidRefreshToken() {
		String refreshToken = "invalidRefreshToken";

		when(jwtUtils.validateToken(refreshToken)).thenReturn("Invalid Token");

		ReissueTokensResponse response = authService.reissueTokensWithRefreshToken(refreshToken);

		assertThat(response).isNull();
	}

	@Test
	void testGetTokensForPaycoUser() {
		String paycoIdNo = "paycoIdNo";
		GetPaycoUserTokenInfoResponse paycoUserTokenInfoResponse = GetPaycoUserTokenInfoResponse.builder()
			.id(1L)
			.roles(List.of("ROLE_USER"))
			.status("ACTIVE")
			.build();

		when(userInfoClient.getUserInfoByPaycoId(anyString())).thenReturn(
			ResponseEntity.ok(paycoUserTokenInfoResponse));
		when(jwtUtils.generateToken(anyString(), anyLong(), anyList(), anyLong())).thenReturn("accessToken",
			"refreshToken");

		PaycoLoginResponse response = authService.getTokensForPaycoUser(paycoIdNo);

		assertThat(response).isNotNull();
		assertThat(response.accessToken()).isEqualTo("accessToken");
		assertThat(response.refreshToken()).isEqualTo("refreshToken");

		verify(redisTemplate, times(1)).delete("RefreshToken:1");
		verify(hashOperations, times(1)).put(eq("RefreshToken:1"), eq("token"), eq("refreshToken"));
	}

	@Test
	void testGetTokensForInactivePaycoUser() {
		String paycoIdNo = "paycoIdNo";
		GetPaycoUserTokenInfoResponse paycoUserTokenInfoResponse = GetPaycoUserTokenInfoResponse.builder()
			.id(1L)
			.roles(List.of("ROLE_USER"))
			.status("INACTIVE")
			.build();

		when(userInfoClient.getUserInfoByPaycoId(anyString())).thenReturn(
			ResponseEntity.ok(paycoUserTokenInfoResponse));

		PaycoLoginResponse response = authService.getTokensForPaycoUser(paycoIdNo);

		assertThat(response).isNull();
	}
}
