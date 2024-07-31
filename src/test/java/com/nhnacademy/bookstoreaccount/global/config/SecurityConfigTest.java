package com.nhnacademy.bookstoreaccount.global.config;

import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	private String validRefreshToken;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		validRefreshToken = jwtUtils.generateToken("refresh", 1L, List.of("ROLE_USER"), 3600000L);
		when(jwtUtils.generateToken("refresh", 1L, List.of("ROLE_USER"), 3600000L)).thenReturn(validRefreshToken);
	}

	@Test
	void contextLoads() {
		// 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인합니다.
	}

	@Test
	void testLoginEndpointAccessible() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/login"))
			.andExpect(status().isOk());
	}

	@Test
	void testReissueEndpointAccessible() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/reissue"))
			.andExpect(status().isOk());
	}

	@Test
	void testLogoutEndpointAccessible() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/auth/logout")
				.header("Refresh-Token", validRefreshToken))
			.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	void testOtherEndpointsAccessible() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/some/other/endpoint"))
			.andExpect(status().isOk());
	}
}
