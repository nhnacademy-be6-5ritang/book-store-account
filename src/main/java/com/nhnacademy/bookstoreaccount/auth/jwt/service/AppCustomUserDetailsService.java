package com.nhnacademy.bookstoreaccount.auth.jwt.service;

import java.util.Objects;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nhnacademy.bookstoreaccount.auth.jwt.client.UserInfoClient;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.AppCustomUserDetails;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.GetUserInfoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppCustomUserDetailsService implements UserDetailsService {
	private final UserInfoClient userInfoClient;

	@Override
	public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
		GetUserInfoResponse user = userInfoClient.getUserInfoByEmail(userEmail);

		if (Objects.isNull(user)) {
			return null;
		}

		return new AppCustomUserDetails(user);
	}
}
