package com.nhnacademy.bookstoreaccount.global.exception;

import com.nhnacademy.bookstoreaccount.global.exception.payload.ErrorStatus;

import lombok.Getter;

@Getter
public class UserNotFoundException extends GlobalException {
	public UserNotFoundException(ErrorStatus errorStatus) {
		super(errorStatus);
	}
}
