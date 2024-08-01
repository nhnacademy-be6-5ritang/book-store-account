package com.nhnacademy.bookstoreaccount.keymanager.service;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.nhnacademy.bookstoreaccount.keymanager.dto.KeyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 이경헌
 * KeyManager는 인증서를 사용하여 클라우드에서 기밀 데이터를 가져오는 클래스입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeyManagerService {
	private final RestTemplate restTemplate;

	/**
	 * 인증서를 사용하여 클라우드에서 기밀 데이터를 가져오는 메서드
	 *
	 * @param keyId 조회를 원하는 데이터의 Key value
	 * @return key value 에 해당하는 데이터
	 */
	public String getSecret(String keyId) {
		try {
			// HTTP 요청을 위한 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(List.of(MediaType.APPLICATION_JSON));
			headers.set("X-TC-AUTHENTICATION-ID", "3bTA7VD3xkZzLXPnt31X");
			headers.set("X-TC-AUTHENTICATION-SECRET", "WuXXhpYwgdSoE3mY");

			// URI 생성
			String url =
				"https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/2SxwmBzUfnqJaA2A/secrets/" + keyId;

			// HttpEntity를 사용하여 헤더 포함
			HttpEntity<String> entity = new HttpEntity<>(headers);

			// 데이터 요청 및 반환
			ResponseEntity<KeyResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity,
				KeyResponse.class);
			KeyResponse responseBody = responseEntity.getBody();
			if (responseBody != null) {
				return responseBody.getBody().getSecret();
			} else {
				log.error("응답 본문이 비어있습니다.");
				return null;
			}
		} catch (Exception e) {
			log.error("키매니저 에러: {}", e.getMessage());
			return null;
		}
	}
}
