package com.nhnacademy.bookstoreaccount.auth.jwt.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.LoginRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import jakarta.servlet.FilterChain;

class LoginFilterTest {
	@InjectMocks
	private LoginFilter loginFilter;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		loginFilter = new LoginFilter(authenticationManager, jwtUtils, redisTemplate, 3600000L, 7200000L);
		when(redisTemplate.opsForHash()).thenReturn(hashOperations);
	}

	@Test
	void attemptAuthentication_success() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setContent(objectMapper.writeValueAsBytes(new LoginRequest("test@test.com", "password")));

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("test@test.com",
			"password", null);
		Authentication authentication = new UsernamePasswordAuthenticationToken("1", "password",
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
			authentication);

		Authentication result = loginFilter.attemptAuthentication(request, response);

		assertNotNull(result);
		assertEquals("1", result.getName());
	}

	@Test
	void successfulAuthentication() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);
		Authentication authentication = new UsernamePasswordAuthenticationToken("1", "password",
			List.of(new SimpleGrantedAuthority("ROLE_USER")));

		when(jwtUtils.generateToken(eq("access"), anyLong(), anyList(), anyLong())).thenReturn("accessToken");
		when(jwtUtils.generateToken(eq("refresh"), anyLong(), anyList(), anyLong())).thenReturn("refreshToken");

		loginFilter.successfulAuthentication(request, response, chain, authentication);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("application/json;charset=UTF-8", response.getContentType());
		assertTrue(response.getContentAsString().contains("accessToken"));
		assertTrue(response.getContentAsString().contains("refreshToken"));
		assertTrue(response.getContentAsString().contains(LocalDateTime.now().getYear() + ""));

		verify(redisTemplate).delete("RefreshToken:1");
		verify(hashOperations).put("RefreshToken:1", "token", "refreshToken");
		verify(redisTemplate).expire("RefreshToken:1", Duration.ofMillis(7200000L));
	}

	@Test
	void unsuccessfulAuthentication() throws IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AuthenticationException exception = mock(AuthenticationException.class);

		loginFilter.unsuccessfulAuthentication(request, response, exception);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
		assertEquals("application/json;charset=UTF-8", response.getContentType());
		assertTrue(response.getContentAsString().contains("비밀번호가 틀렸습니다"));
	}

	@Test
	void attemptAuthentication_ioException() throws IOException {
		MockHttpServletRequest request = mock(MockHttpServletRequest.class);
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(request.getInputStream()).thenThrow(new IOException("Test IOException"));

		Exception exception = assertThrows(RuntimeException.class, () -> {
			loginFilter.attemptAuthentication(request, response);
		});

		assertEquals("java.io.IOException: Test IOException", exception.getMessage());
	}
}
