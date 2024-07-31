package com.nhnacademy.bookstoreaccount.auth.jwt.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

class JwtUtilsTest {
	private JwtUtils jwtUtils;
	private SecretKey secretKey;

	@BeforeEach
	void setUp() {
		String secret = "MySuperSecretKeyForJwt12345MySuperSecretKeyForJwt12345"; // Ensure this key is at least 32 bytes long
		jwtUtils = new JwtUtils(secret);
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void generateToken() {
		String token = jwtUtils.generateToken("access", 1L, List.of("ROLE_USER"), 3600000L);
		assertNotNull(token);
		assertTrue(token.startsWith("Bearer "));
	}

	@Test
	void getUserIdFromToken() {
		String token = generateTestToken("access", 1L, List.of("ROLE_USER"), 3600000L);
		Long userId = jwtUtils.getUserIdFromToken(token);
		assertEquals(1L, userId);
	}

	@Test
	void getRolesFromToken() {
		String token = generateTestToken("access", 1L, List.of("ROLE_USER"), 3600000L);
		List<String> roles = jwtUtils.getRolesFromToken(token);
		assertNotNull(roles);
		assertEquals(1, roles.size());
		assertEquals("ROLE_USER", roles.get(0));
	}

	@Test
	void getTokenTypeFromToken() {
		String token = generateTestToken("access", 1L, List.of("ROLE_USER"), 3600000L);
		String tokenType = jwtUtils.getTokenTypeFromToken(token);
		assertEquals("access", tokenType);
	}

	@Test
	void validateToken() {
		String token = generateTestToken("access", 1L, List.of("ROLE_USER"), 3600000L);
		String validationResult = jwtUtils.validateToken(token);
		assertNull(validationResult);

		String expiredToken = generateTestToken("access", 1L, List.of("ROLE_USER"), -3600000L);
		validationResult = jwtUtils.validateToken(expiredToken);
		assertEquals("만료된 토큰입니다.", validationResult);
	}

	@Test
	void validateToken_invalidToken() {
		String invalidToken = "invalidToken";
		String validationResult = jwtUtils.validateToken(invalidToken);
		assertEquals("유효하지 않은 토큰입니다.", validationResult);
	}

	@Test
	void validateToken_unsupportedToken() {
		String unsupportedToken = Jwts.builder()
			.claim("token-type", "access")
			.claim("userId", 1L)
			.claim("roles", List.of("ROLE_USER"))
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + 3600000L))
			.compact(); // Create a token without signing it
		String validationResult = jwtUtils.validateToken(unsupportedToken);
		assertEquals("지원하지 않는 토큰입니다.", validationResult);
	}

	@Test
	void validateToken_emptyToken() {
		String validationResult = jwtUtils.validateToken("");
		assertEquals("토큰 값이 비어있습니다.", validationResult);
	}

	private String generateTestToken(String tokenType, Long userId, List<String> roles, Long expiresIn) {
		return Jwts.builder()
			.claim("token-type", tokenType)
			.claim("userId", userId)
			.claim("roles", roles)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expiresIn))
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}
}
