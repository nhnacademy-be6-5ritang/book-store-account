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

@RequiredArgsConstructor
public class AppCustomLogoutFilter extends GenericFilterBean {
	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtUtils jwtUtils;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {
		doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, filterChain);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {
		String requestUri = request.getRequestURI();
		if (!requestUri.matches("^/logout$")) {
			filterChain.doFilter(request, response);
			return;
		}

		String requestMethod = request.getMethod();
		if (!requestMethod.equals("POST")) {
			filterChain.doFilter(request, response);
			return;
		}

		String refreshToken = null;
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("Refresh-Token")) {
				refreshToken = cookie.getValue();
			}
		}

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

		String email = jwtUtils.getEmailFromToken(refreshToken);
		String redisKey = "RefreshToken:" + email;
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
