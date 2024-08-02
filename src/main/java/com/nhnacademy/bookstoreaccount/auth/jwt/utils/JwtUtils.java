package com.nhnacademy.bookstoreaccount.auth.jwt.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 김태환
 * JWT 토큰의 생성 및 검증을 담당합니다.
 */
@Slf4j
@Component
public class JwtUtils {
	private final SecretKey secretKey;

	/**
	 * JWT 유틸리티를 초기화합니다.
	 * @param secret JWT 서명에 사용할 비밀 키.
	 */
	public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
		secretKey = new SecretKeySpec(
			secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm()
		);
	}

	/**
	 * JWT 토큰에서 클레임을 추출합니다.
	 * @param token JWT 토큰.
	 * @return 토큰에서 추출한 클레임.
	 */
	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token.replace("Bearer ", ""))
			.getPayload();
	}

	/**
	 * JWT 토큰에서 사용자 ID를 추출합니다.
	 * @param token JWT 토큰.
	 * @return 토큰에서 추출한 사용자 ID.
	 */
	public Long getUserIdFromToken(String token) {
		return getClaims(token).get("userId", Long.class);
	}

	/**
	 * JWT 토큰에서 역할 정보를 추출합니다.
	 * @param token JWT 토큰.
	 * @return 토큰에서 추출한 역할 리스트.
	 */
	public List<String> getRolesFromToken(String token) {
		Claims claims = getClaims(token);
		return ((List<?>)claims.get("roles")).stream()
			.map(Object::toString)
			.toList();
	}

	/**
	 * JWT 토큰에서 토큰 타입을 추출합니다.
	 * @param token JWT 토큰.
	 * @return 토큰에서 추출한 토큰 타입.
	 */
	public String getTokenTypeFromToken(String token) {
		return getClaims(token).get("token-type", String.class);
	}

	/**
	 * JWT 토큰의 유효성을 검증합니다.
	 * @param token JWT 토큰.
	 * @return 토큰이 유효하지 않을 경우의 에러 메시지. 유효한 경우 null을 반환합니다.
	 */
	public String validateToken(String token) {
		String errorMessage = null;
		try {
			Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token.replace("Bearer ", ""));
		} catch (SecurityException | MalformedJwtException e) {
			errorMessage = "유효하지 않은 토큰입니다.";
			log.info(errorMessage, e);
		} catch (ExpiredJwtException e) {
			errorMessage = "만료된 토큰입니다.";
			log.info(errorMessage, e);
		} catch (UnsupportedJwtException e) {
			errorMessage = "지원하지 않는 토큰입니다.";
			log.info(errorMessage, e);
		} catch (IllegalArgumentException e) {
			errorMessage = "토큰 값이 비어있습니다.";
			log.info(errorMessage, e);
		}
		return errorMessage;
	}

	/**
	 * 새 JWT 토큰을 생성합니다.
	 * @param tokenType 토큰의 타입("access" 또는 "refresh").
	 * @param userId 사용자 ID.
	 * @param roles 사용자 역할 리스트.
	 * @param expiresIn 토큰의 만료 시간(밀리초 단위).
	 * @return 생성된 JWT 토큰.
	 */
	public String generateToken(String tokenType, Long userId, List<String> roles, Long expiresIn) {
		String tokenTypePrefix = "access".equals(tokenType) ? "Bearer " : "";
		return tokenTypePrefix + Jwts.builder()
			.claim("token-type", tokenType)
			.claim("userId", userId)
			.claim("roles", roles)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiresIn))
			.signWith(secretKey)
			.compact();
	}
}
