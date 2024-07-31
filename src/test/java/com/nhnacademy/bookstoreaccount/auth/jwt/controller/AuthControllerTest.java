package com.nhnacademy.bookstoreaccount.auth.jwt.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.bookstoreaccount.auth.jwt.controller.AuthController;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.request.ReissueTokenRequest;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.PaycoLoginResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.ReissueTokensResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(AuthController.class)
@ContextConfiguration
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthService authService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	@Test
	@WithMockUser
	void getUserInfo() throws Exception {
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", "user123");
		userInfo.put("name", "John Doe");

		given(authService.getUserInfo(any(HttpServletRequest.class))).willReturn(userInfo);

		mockMvc.perform(get("/auth/info"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value("user123"))
			.andExpect(jsonPath("$.name").value("John Doe"));
	}

	@Test
	@WithMockUser
	void reissueTokensWithRefreshToken() throws Exception {
		ReissueTokenRequest request = new ReissueTokenRequest("dummy-refresh-token");
		ReissueTokensResponse response = new ReissueTokensResponse("new-access-token", "new-refresh-token");

		given(authService.reissueTokensWithRefreshToken(anyString())).willReturn(response);

		MockHttpServletRequestBuilder requestBuilder = post("/auth/reissue-with-refresh-token")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.accessToken").value("new-access-token"))
			.andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
	}

	@Test
	@WithMockUser
	void reissueTokensWithRefreshToken_badRequest() throws Exception {
		ReissueTokenRequest request = new ReissueTokenRequest("invalid-refresh-token");

		given(authService.reissueTokensWithRefreshToken(anyString())).willReturn(null);

		MockHttpServletRequestBuilder requestBuilder = post("/auth/reissue-with-refresh-token")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser
	void getTokensForPaycoUser() throws Exception {
		PaycoLoginResponse response = new PaycoLoginResponse("payco-access-token", "payco-refresh-token");

		given(authService.getTokensForPaycoUser(anyString())).willReturn(response);

		MockHttpServletRequestBuilder requestBuilder = post("/auth/tokens-for-payco-user")
			.param("paycoIdNo", "123456")
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.accessToken").value("payco-access-token"))
			.andExpect(jsonPath("$.refreshToken").value("payco-refresh-token"));
	}

	@Test
	@WithMockUser
	void getTokensForPaycoUser_badRequest() throws Exception {
		given(authService.getTokensForPaycoUser(anyString())).willReturn(null);

		MockHttpServletRequestBuilder requestBuilder = post("/auth/tokens-for-payco-user")
			.param("paycoIdNo", "invalid-id")
			.with(csrf());

		mockMvc.perform(requestBuilder)
			.andExpect(status().isBadRequest());
	}
}
