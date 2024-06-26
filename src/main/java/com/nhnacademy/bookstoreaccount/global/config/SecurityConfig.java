package com.nhnacademy.bookstoreaccount.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import com.nhnacademy.bookstoreaccount.auth.jwt.filter.AppCustomLogoutFilter;
import com.nhnacademy.bookstoreaccount.auth.jwt.filter.LoginFilter;
import com.nhnacademy.bookstoreaccount.auth.jwt.service.AppCustomUserDetailsService;
import com.nhnacademy.bookstoreaccount.auth.jwt.utils.JwtUtils;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final AuthenticationConfiguration authenticationConfiguration;
	private final JwtUtils jwtUtils;
	private final RedisTemplate<String, Object> redisTemplate;
	private final AppCustomUserDetailsService userDetailsService;

	@Value("${spring.jwt.access-token.expires-in}")
	private Long accessTokenExpiresIn;
	@Value("${spring.jwt.refresh-token.expires-in}")
	private Long refreshTokenExpiresIn;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/login", "/auth/reissue").permitAll()
				.anyRequest().permitAll()
			)
			// .addFilterBefore(new JwtFilter(jwtUtils), LoginFilter.class)
			.addFilterAt(
				new LoginFilter(
					authenticationManager(authenticationConfiguration),
					jwtUtils,
					redisTemplate,
					accessTokenExpiresIn,
					refreshTokenExpiresIn
				),
				UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new AppCustomLogoutFilter(redisTemplate, jwtUtils), LogoutFilter.class)
			.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.userDetailsService(userDetailsService)
			.build();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
