package com.nhnacademy.bookstoreaccount.keymanager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.nhnacademy.bookstoreaccount.keymanager.dto.KeyResponse;

class KeyResponseTest {

	@Test
	void testKeyResponseBody() {
		// Given
		KeyResponse.Body body = new KeyResponse.Body();
		// Using reflection or other means to set private fields
		setField(body, "secret", "testSecret");

		// When & Then
		assertNotNull(body);
		assertEquals("testSecret", body.getSecret());
	}

	@Test
	void testKeyResponseHeader() {
		// Given
		KeyResponse.Header header = new KeyResponse.Header();
		// Using reflection or other means to set private fields
		setField(header, "resultCode", 200);
		setField(header, "resultMessage", "Success");
		setField(header, "isSuccessful", true);

		// When & Then
		assertNotNull(header);
		assertEquals(200, header.getResultCode());
		assertEquals("Success", header.getResultMessage());
		assertEquals(true, header.isSuccessful());
	}

	@Test
	void testKeyResponse() {
		// Given
		KeyResponse keyResponse = new KeyResponse();
		KeyResponse.Body body = new KeyResponse.Body();
		setField(body, "secret", "testSecret");
		KeyResponse.Header header = new KeyResponse.Header();
		setField(header, "resultCode", 200);
		setField(header, "resultMessage", "Success");
		setField(header, "isSuccessful", true);

		// Using reflection or other means to set private fields
		setField(keyResponse, "body", body);
		setField(keyResponse, "header", header);

		// When & Then
		assertNotNull(keyResponse);
		assertNotNull(keyResponse.getBody());
		assertNotNull(keyResponse.getHeader());
		assertEquals("testSecret", keyResponse.getBody().getSecret());
		assertEquals(200, keyResponse.getHeader().getResultCode());
		assertEquals("Success", keyResponse.getHeader().getResultMessage());
		assertEquals(true, keyResponse.getHeader().isSuccessful());
	}

	private void setField(Object targetObject, String fieldName, Object value) {
		try {
			java.lang.reflect.Field field = targetObject.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(targetObject, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}