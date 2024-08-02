package com.nhnacademy.bookstoreaccount.auth.jwt.filter;

import java.io.IOException;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author 김태환
 * 사용자 로그아웃 요청을 처리하는 커스텀 필터입니다.
 */
@RequiredArgsConstructor
public class AppCustomLogoutFilter extends GenericFilterBean {
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtils jwtUtils;

	/**
	 * 요청이 로그아웃 요청인지 확인하고 처리합니다.
	 *
	 * @param servletRequest  클라이언트 요청을 나타내는 {@link ServletRequest} 객체.
	 * @param servletResponse 클라이언트 응답을 나타내는 {@link ServletResponse} 객체.
	 * @param filterChain     필터 체인을 나타내는 {@link FilterChain} 객체.
	 * @throws IOException      I/O 오류가 발생한 경우.
	 * @throws ServletException 서블릿 관련 오류가 발생한 경우.
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {
		doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, filterChain);
	}

	/**
	 * HTTP 요청을 필터링하고 로그아웃 처리를 수행합니다.
	 *
	 * @param request  HTTP 요청을 나타내는 {@link HttpServletRequest} 객체.
	 * @param response HTTP 응답을 나타내는 {@link HttpServletResponse} 객체.
	 * @param filterChain 필터 체인을 나타내는 {@link FilterChain} 객체.
	 * @throws IOException      I/O 오류가 발생한 경우.
	 * @throws ServletException 서블릿 관련 오류가 발생한 경우.
	 */
	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {
		String requestUri = request.getRequestURI();
		if (!requestUri.matches("^/auth/logout$")) {
			filterChain.doFilter(request, response);
			return;
		}

		String requestMethod = request.getMethod();
		if (!requestMethod.equals("POST")) {
			filterChain.doFilter(request, response);
			return;
		}

		String refreshToken = request.getHeader("Refresh-Token");

		if (Objects.isNull(refreshToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (jwtUtils.validateToken(refreshToken) != null) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		}

		String tokenType = jwtUtils.getTokenTypeFromToken(refreshToken);
		if (!"refresh".equals(tokenType)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Long userId = jwtUtils.getUserIdFromToken(refreshToken);
		String redisKey = "RefreshToken:" + userId;
		boolean refreshTokenExists = redisTemplate.opsForHash().hasKey(redisKey, "token");
		if (!refreshTokenExists) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		redisTemplate.opsForHash().delete(redisKey, "token");

		Cookie cookie = new Cookie("Refresh-Token", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");

		response.addCookie(cookie);
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
