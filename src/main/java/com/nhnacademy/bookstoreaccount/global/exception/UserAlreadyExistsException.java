package com.nhnacademy.bookstoreaccount.global.exception;

import com.nhnacademy.bookstoreaccount.global.exception.payload.ErrorStatus;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends GlobalException {
	public UserAlreadyExistsException(ErrorStatus errorStatus) {
		super(errorStatus);
	}
}
