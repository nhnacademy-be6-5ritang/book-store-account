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

@Slf4j
@Component
public class JwtUtils {
	private final SecretKey secretKey;

	public JwtUtils(@Value("${spring.jwt.secret}") String secret) {
		secretKey = new SecretKeySpec(
			secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm()
		);
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token.replace("Bearer ", ""))
			.getPayload();
	}

	public Long getUserIdFromToken(String token) {
		return getClaims(token).get("userId", Long.class);
	}

	public List<String> getRolesFromToken(String token) {
		Claims claims = getClaims(token);
		return ((List<?>)claims.get("roles")).stream()
			.map(Object::toString)
			.collect(Collectors.toList());
	}

	public String getTokenTypeFromToken(String token) {
		return getClaims(token).get("token-type", String.class);
	}

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
