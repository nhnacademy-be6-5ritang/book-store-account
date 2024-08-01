package com.nhnacademy.bookstoreaccount.global;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.nhnacademy.bookstoreaccount.global.util.LogNCrashAppender;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;

class LogNCrashAppenderTest {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private LogNCrashAppender logNCrashAppender;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Create a LoggerContext
		LoggerContext loggerContext = new LoggerContext();
		logNCrashAppender.setContext(loggerContext);
		logNCrashAppender.start();
	}

	@Test
	void testAppend() {
		ILoggingEvent loggingEvent = mock(ILoggingEvent.class);

		when(loggingEvent.getFormattedMessage()).thenReturn("Test log message");
		when(loggingEvent.getLevel()).thenReturn(ch.qos.logback.classic.Level.INFO);

		Map<String, Object> logData = new HashMap<>();
		logData.put("projectName", "Xyx7DoyszcG66ULx");
		logData.put("projectVersion", "1.0.0");
		logData.put("logVersion", "v2");
		logData.put("body", loggingEvent.getFormattedMessage());
		logData.put("logSource", "http");
		logData.put("logType", "log");
		logData.put("platform", "5ritang-Gateway");
		logData.put("host", "192.168.0.75");
		logData.put("logLevel", loggingEvent.getLevel().toString());

		when(restTemplate.postForEntity("https://api-logncrash.nhncloudservice.com/v2/log", logData, String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

		logNCrashAppender.doAppend(loggingEvent);

	}

}
