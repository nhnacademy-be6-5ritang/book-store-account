package com.nhnacademy.bookstoreaccount.auth.jwt.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserInfoResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppCustomUserDetails implements UserDetails {
	private final GetUserInfoResponse user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();

		collection.add(
			(GrantedAuthority)user::role
		);

		return collection;
	}

	@Override
	public String getPassword() {
		return user.password();
	}

	@Override
	public String getUsername() {
		return String.valueOf(user.id());
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
