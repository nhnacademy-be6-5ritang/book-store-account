package com.nhnacademy.bookstoreaccount.global.handler;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 20;

	public CustomPageableHandlerMethodArgumentResolver() {
		super();
	}

	@Override
	public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {

		Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

		if (webRequest.getParameter("size") == null) {
			pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
		} else if (pageable.getPageSize() > MAX_PAGE_SIZE) {
			pageable = PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
		}

		return pageable;
	}
}
