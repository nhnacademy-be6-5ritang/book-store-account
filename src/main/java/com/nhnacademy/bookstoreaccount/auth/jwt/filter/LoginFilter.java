package com.nhnacademy.bookstoreaccount.auth.jwt.filter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.LoginRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final RedisTemplate<String, Object> redisTemplate;
	private final Long accessTokenExpiresIn;
	private final Long refreshTokenExpiresIn;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public LoginFilter(
		AuthenticationManager authenticationManager, JwtUtils jwtUtils, RedisTemplate<String, Object> redisTemplate,
		Long accessTokenExpiresIn, Long refreshTokenExpiresIn
	) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.redisTemplate = redisTemplate;
		this.accessTokenExpiresIn = accessTokenExpiresIn;
		this.refreshTokenExpiresIn = refreshTokenExpiresIn;
		setFilterProcessesUrl("/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
		throws AuthenticationException {
		LoginRequest loginRequest;
		try {
			loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
			String userEmail = loginRequest.email();
			String password = loginRequest.password();

			UsernamePasswordAuthenticationToken authToken
				= new UsernamePasswordAuthenticationToken(userEmail, password, null);

			return authenticationManager.authenticate(authToken);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException, ServletException {
		Long userId = Long.parseLong(authentication.getName());
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

		String accessToken = jwtUtils.generateAccessToken("access", userId, roles, accessTokenExpiresIn);
		String refreshToken = jwtUtils.generateRefreshToken("refresh", userId, roles, refreshTokenExpiresIn);

		saveRefreshToken(userId, refreshToken, refreshTokenExpiresIn);

		response.setHeader("Authorization", accessToken);
		response.addCookie(createCookie("Refresh-Token", refreshToken));
		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json");
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
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
		// cookie.setSecure(true);
		cookie.setPath("/");
		cookie.setHttpOnly(true);

		return cookie;
	}
}
