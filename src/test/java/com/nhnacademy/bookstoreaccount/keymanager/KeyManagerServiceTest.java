package com.nhnacademy.bookstoreaccount.keymanager;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.nhnacademy.bookstoreaccount.keymanager.dto.KeyResponse;
import com.nhnacademy.bookstoreaccount.keymanager.service.KeyManagerService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class KeyManagerServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private KeyManagerService keyManagerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetSecret_Success() {
		String keyId = "testKeyId";
		String secret = "testSecret";

		// Mocking KeyResponse and its body
		KeyResponse responseDto = mock(KeyResponse.class);
		KeyResponse.Body body = mock(KeyResponse.Body.class);
		when(body.getSecret()).thenReturn(secret);
		when(responseDto.getBody()).thenReturn(body);

		// Mocking ResponseEntity
		ResponseEntity<KeyResponse> responseEntity = mock(ResponseEntity.class);
		when(responseEntity.getBody()).thenReturn(responseDto);

		// Mocking restTemplate.exchange method
		when(restTemplate.exchange(
			anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class)))
			.thenReturn(responseEntity);

		String url =
			"https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/2SxwmBzUfnqJaA2A/secrets/" + keyId;

		// Execute the method to be tested
		String result = keyManagerService.getSecret(keyId);

		// Verify that restTemplate.exchange was called with the expected parameters
		verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class));

		// Verify that the result is as expected
		assert result.equals(secret);
	}

	@Test
	void testGetSecret_NullResponseBody() {
		String keyId = "testKeyId";

		// Mocking ResponseEntity with null body
		ResponseEntity<KeyResponse> responseEntity = mock(ResponseEntity.class);
		when(responseEntity.getBody()).thenReturn(null);

		// Mocking restTemplate.exchange method
		when(restTemplate.exchange(
			anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class)))
			.thenReturn(responseEntity);

		String url =
			"https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/2SxwmBzUfnqJaA2A/secrets/" + keyId;

		// Execute the method to be tested
		String result = keyManagerService.getSecret(keyId);

		// Verify that restTemplate.exchange was called with the expected parameters
		verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class));

		// Verify that the result is null
		assert result == null;
	}

	@Test
	void testGetSecret_Exception() {
		String keyId = "testKeyId";

		// Mocking restTemplate.exchange method to throw an exception
		when(restTemplate.exchange(
			anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class)))
			.thenThrow(new RuntimeException("Test Exception"));

		String url =
			"https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/2SxwmBzUfnqJaA2A/secrets/" + keyId;

		// Execute the method to be tested
		String result = keyManagerService.getSecret(keyId);

		// Verify that restTemplate.exchange was called with the expected parameters
		verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(KeyResponse.class));

		// Verify that the result is null
		assert result == null;
	}
}