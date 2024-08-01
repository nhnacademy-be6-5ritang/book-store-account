package com.nhnacademy.bookstoreaccount.keymanager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nhnacademy.bookstoreaccount.keymanager.property.RedisProperty;

class PropertyTest {
	private RedisProperty redisProperty;

	@BeforeEach
	void setUp() {
		redisProperty = new RedisProperty();
		redisProperty.setHost("localhost");
		redisProperty.setPort("6379");
		redisProperty.setPassword("password");
		redisProperty.setDatabase("0");
	}

	@Test
	void testRedisProperty() {
		assertEquals("localhost", redisProperty.getHost());
		assertEquals("6379", redisProperty.getPort());
		assertEquals("password", redisProperty.getPassword());
		assertEquals("0", redisProperty.getDatabase());
	}
}
