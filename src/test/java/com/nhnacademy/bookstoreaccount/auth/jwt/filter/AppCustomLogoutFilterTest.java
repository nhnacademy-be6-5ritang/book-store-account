package com.nhnacademy.bookstoreaccount.auth.jwt.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.nhnacademy.bookstoreaccount.auth.jwt.filter.AppCustomLogoutFilter;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

class AppCustomLogoutFilterTest {
	@InjectMocks
	private AppCustomLogoutFilter logoutFilter;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private FilterChain filterChain;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		logoutFilter = new AppCustomLogoutFilter(redisTemplate, jwtUtils);
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
	}

	@Test
	void doFilter_nonLogoutUri() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some/other/uri");
		MockHttpServletResponse response = new MockHttpServletResponse();

		logoutFilter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_nonPostMethod() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/logout");
		MockHttpServletResponse response = new MockHttpServletResponse();

		logoutFilter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_missingRefreshToken() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
		MockHttpServletResponse response = new MockHttpServletResponse();

		logoutFilter.doFilter(request, response, filterChain);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	@Test
	void doFilter_invalidRefreshToken() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
		request.addHeader("Refresh-Token", "invalidToken");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateToken("invalidToken")).thenReturn("Invalid token");

		logoutFilter.doFilter(request, response, filterChain);

		assertEquals(HttpServletResponse.SC_BAD_GATEWAY, response.getStatus());
	}

	@Test
	void doFilter_invalidTokenType() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
		request.addHeader("Refresh-Token", "refreshToken");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateToken("refreshToken")).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken("refreshToken")).thenReturn("access");

		logoutFilter.doFilter(request, response, filterChain);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	@Test
	void doFilter_refreshTokenNotExists() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
		request.addHeader("Refresh-Token", "refreshToken");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateToken("refreshToken")).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken("refreshToken")).thenReturn("refresh");
		when(jwtUtils.getUserIdFromToken("refreshToken")).thenReturn(1L);
		when(hashOperations.hasKey("RefreshToken:1", "token")).thenReturn(false);

		logoutFilter.doFilter(request, response, filterChain);

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
	}

	@Test
	void doFilter_successfulLogout() throws IOException, ServletException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
		request.addHeader("Refresh-Token", "refreshToken");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtUtils.validateToken("refreshToken")).thenReturn(null);
		when(jwtUtils.getTokenTypeFromToken("refreshToken")).thenReturn("refresh");
		when(jwtUtils.getUserIdFromToken("refreshToken")).thenReturn(1L);
		when(hashOperations.hasKey("RefreshToken:1", "token")).thenReturn(true);

		logoutFilter.doFilter(request, response, filterChain);

		verify(hashOperations).delete("RefreshToken:1", "token");
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertTrue(response.getCookies().length > 0);
		assertEquals(0, response.getCookies()[0].getMaxAge());
	}
}
