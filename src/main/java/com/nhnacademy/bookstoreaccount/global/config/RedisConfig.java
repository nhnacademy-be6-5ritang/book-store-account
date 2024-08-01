package com.nhnacademy.bookstoreaccount.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.nhnacademy.bookstoreaccount.keymanager.property.RedisProperty;
import com.nhnacademy.bookstoreaccount.keymanager.service.KeyManagerService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
	private final RedisProperty redisProperty;
	private final KeyManagerService keyManagerService;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(keyManagerService.getSecret(redisProperty.getHost()));
		redisStandaloneConfiguration.setPort(Integer.parseInt(keyManagerService.getSecret(redisProperty.getPort())));
		redisStandaloneConfiguration.setPassword(keyManagerService.getSecret(redisProperty.getPassword()));
		redisStandaloneConfiguration.setDatabase(
			Integer.parseInt(keyManagerService.getSecret(redisProperty.getDatabase())));
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> sessionRedisTemplate = new RedisTemplate<>();
		sessionRedisTemplate.setConnectionFactory(redisConnectionFactory);
		sessionRedisTemplate.setKeySerializer(new StringRedisSerializer());
		sessionRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		sessionRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
		sessionRedisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		return sessionRedisTemplate;
	}
}
