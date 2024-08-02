package com.nhnacademy.bookstoreaccount.auth.jwt.service;

import java.util.Objects;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nhnacademy.bookstoreaccount.auth.jwt.client.UserInfoClient;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.AppCustomUserDetails;
import com.nhnacademy.bookstoreaccount.auth.jwt.dto.response.GetUserTokenInfoResponse;

import lombok.RequiredArgsConstructor;

/**
 * @author 김태환
 * 사용자 정보를 로드하여 Spring Security 의 {@link UserDetails} 객체를 반환하는 서비스입니다.
 * 사용자 정보를 외부 클라이언트에서 가져와서 {@link UserDetails} 구현체를 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class AppCustomUserDetailsService implements UserDetailsService {
	private final UserInfoClient userInfoClient;

	/**
	 * 사용자 이메일을 기반으로 {@link UserDetails}를 로드합니다.
	 * 외부 서비스에서 사용자 정보를 가져오고, 사용자의 상태에 따라 적절한 {@link UserDetails} 객체를 반환합니다.
	 *
	 * @param userEmail 사용자의 이메일 주소.
	 * @return 사용자 이메일에 해당하는 {@link UserDetails} 객체. 사용자가 존재하지 않거나 상태가 "WITHDRAW"인 경우 null 을 반환합니다.
	 * @throws UsernameNotFoundException 사용자가 존재하지 않는 경우 발생합니다.
	 */
	@Override
	public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
		GetUserTokenInfoResponse user = userInfoClient.getUserInfoByEmail(userEmail);

		if (Objects.isNull(user)) {
			return null;
		}

		if ("WITHDRAW".equals(user.status())) {
			return null;
		}

		return new AppCustomUserDetails(user);
	}
}
