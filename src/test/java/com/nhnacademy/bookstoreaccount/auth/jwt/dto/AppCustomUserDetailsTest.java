package com.nhnacademy.bookstoreaccount.auth.jwt.dto;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.AppCustomUserDetails;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserTokenInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppCustomUserDetailsTest {
	private AppCustomUserDetails appCustomUserDetails;

	@Mock
	private GetUserTokenInfoResponse user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		user = new GetUserTokenInfoResponse(1L, "password123", List.of("ROLE_USER"), "ACTIVE");
		appCustomUserDetails = new AppCustomUserDetails(user);
	}

	@Test
	void getAuthorities() {
		Collection<? extends GrantedAuthority> authorities = appCustomUserDetails.getAuthorities();
		assertNotNull(authorities);
		assertEquals(1, authorities.size());
		assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
	}

	@Test
	void getPassword() {
		assertEquals("password123", appCustomUserDetails.getPassword());
	}

	@Test
	void getUsername() {
		assertEquals("1", appCustomUserDetails.getUsername());
	}

	@Test
	void isAccountNonExpired() {
		assertTrue(appCustomUserDetails.isAccountNonExpired());
	}

	@Test
	void isAccountNonLocked() {
		assertTrue(appCustomUserDetails.isAccountNonLocked());
	}

	@Test
	void isCredentialsNonExpired() {
		assertTrue(appCustomUserDetails.isCredentialsNonExpired());
	}

	@Test
	void isEnabled() {
		assertTrue(appCustomUserDetails.isEnabled());
	}
}
