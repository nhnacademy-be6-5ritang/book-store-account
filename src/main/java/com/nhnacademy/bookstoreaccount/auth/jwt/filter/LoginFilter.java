package com.nhnacademy.bookstoreaccount.auth.jwt.filter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.LoginRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.LoginResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 김태환
 * 로그인 요청을 처리하는 필터입니다. 이 필터는 사용자의 로그인 요청을 인증하고, 성공적으로 인증된 경우 JWT 를 생성하여 클라이언트에게 반환하며, 리프레시 토큰을 Redis 에 저장합니다.
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final RedisTemplate<String, Object> redisTemplate;
	private final Long accessTokenExpiresIn;
	private final Long refreshTokenExpiresIn;
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 생성자입니다. 로그인 필터를 설정합니다.
	 *
	 * @param authenticationManager 인증을 처리하는 {@link AuthenticationManager} 객체.
	 * @param jwtUtils JWT 관련 유틸리티를 제공하는 {@link JwtUtils} 객체.
	 * @param redisTemplate 리프레시 토큰을 저장하기 위한 {@link RedisTemplate} 객체.
	 * @param accessTokenExpiresIn 액세스 토큰의 만료 시간 (밀리초 단위).
	 * @param refreshTokenExpiresIn 리프레시 토큰의 만료 시간 (밀리초 단위).
	 */
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

	/**
	 * 로그인 요청을 인증합니다.
	 *
	 * @param request  클라이언트 요청을 나타내는 {@link HttpServletRequest} 객체.
	 * @param response 클라이언트 응답을 나타내는 {@link HttpServletResponse} 객체.
	 * @return 인증된 {@link Authentication} 객체.
	 * @throws AuthenticationException 인증 오류가 발생한 경우.
	 */
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

	/**
	 * 인증에 성공한 후 호출됩니다. JWT 를 생성하고 응답에 작성합니다.
	 *
	 * @param request  클라이언트 요청을 나타내는 {@link HttpServletRequest} 객체.
	 * @param response 클라이언트 응답을 나타내는 {@link HttpServletResponse} 객체.
	 * @param chain    필터 체인을 나타내는 {@link FilterChain} 객체.
	 * @param authentication 인증된 {@link Authentication} 객체.
	 * @throws IOException I/O 오류가 발생한 경우.
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException {
		Long userId = Long.parseLong(authentication.getName());
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();

		String accessToken = jwtUtils.generateToken("access", userId, roles, accessTokenExpiresIn);
		String refreshToken = jwtUtils.generateToken("refresh", userId, roles, refreshTokenExpiresIn);

		saveRefreshToken(userId, refreshToken, refreshTokenExpiresIn);

		LoginResponse loginResponse = LoginResponse.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.lastLoginAt(LocalDateTime.now())
			.build();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule()); // Registering the JavaTimeModule
		String loginResponseJson = objectMapper.writeValueAsString(loginResponse);

		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(loginResponseJson);
	}

	/**
	 * 인증에 실패한 후 호출됩니다. 오류 메시지를 응답에 작성합니다.
	 *
	 * @param request  클라이언트 요청을 나타내는 {@link HttpServletRequest} 객체.
	 * @param response 클라이언트 응답을 나타내는 {@link HttpServletResponse} 객체.
	 * @param failed   인증 실패를 나타내는 {@link AuthenticationException} 객체.
	 * @throws IOException I/O 오류가 발생한 경우.
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String errorMessage = "{\"message\": \"비밀번호가 틀렸습니다\"}";
		response.getWriter().write(errorMessage);
	}

	/**
	 * 리프레시 토큰을 Redis 에 저장합니다.
	 *
	 * @param userId          사용자 ID.
	 * @param refreshToken    리프레시 토큰.
	 * @param expiresIn       리프레시 토큰의 만료 시간 (밀리초 단위).
	 */
	private void saveRefreshToken(Long userId, String refreshToken, Long expiresIn) {
		String redisKey = "RefreshToken:" + userId;
		redisTemplate.delete(redisKey);
		redisTemplate.opsForHash().put(redisKey, "token", refreshToken);
		redisTemplate.expire(redisKey, Duration.ofMillis(expiresIn));
	}
}
