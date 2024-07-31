package com.nhnacademy.bookstoreaccount.auth.jwt.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.nhnacademy.bookstoreaccount.auth.jwt.client.UserInfoClient;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.AppCustomUserDetails;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserTokenInfoResponse;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AppCustomUserDetailsService;

class AppCustomUserDetailsServiceTest {

	@Mock
	private UserInfoClient userInfoClient;

	@InjectMocks
	private AppCustomUserDetailsService appCustomUserDetailsService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testLoadUserByUsername_UserNotFound() {
		String userEmail = "nonexistent@example.com";

		when(userInfoClient.getUserInfoByEmail(userEmail)).thenReturn(null);

		UserDetails userDetails = appCustomUserDetailsService.loadUserByUsername(userEmail);

		assertThat(userDetails).isNull();
	}

	@Test
	void testLoadUserByUsername_UserWithdrawn() {
		String userEmail = "withdrawn@example.com";
		GetUserTokenInfoResponse withdrawnUser = GetUserTokenInfoResponse.builder()
			.id(1L)
			.password("password")
			.roles(List.of("ROLE_USER"))
			.status("WITHDRAW")
			.build();

		when(userInfoClient.getUserInfoByEmail(userEmail)).thenReturn(withdrawnUser);

		UserDetails userDetails = appCustomUserDetailsService.loadUserByUsername(userEmail);

		assertThat(userDetails).isNull();
	}

	@Test
	void testLoadUserByUsername_UserActive() {
		String userEmail = "active@example.com";
		GetUserTokenInfoResponse activeUser = GetUserTokenInfoResponse.builder()
			.id(1L)
			.password("password")
			.roles(List.of("ROLE_USER"))
			.status("ACTIVE")
			.build();

		when(userInfoClient.getUserInfoByEmail(userEmail)).thenReturn(activeUser);

		UserDetails userDetails = appCustomUserDetailsService.loadUserByUsername(userEmail);

		assertThat(userDetails).isNotNull();
		assertThat(userDetails).isInstanceOf(AppCustomUserDetails.class);
		assertThat(userDetails.getUsername()).isEqualTo(String.valueOf(activeUser.id()));
	}
}
