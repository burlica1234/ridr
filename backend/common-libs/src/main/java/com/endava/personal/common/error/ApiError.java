package com.endava.personal.common.error;

import java.util.List;

public record ApiError(String errorCode, String message, List<ApiErrorDetail> details) {
	public static ApiError of(String errorCode, String message) {
		return new ApiError(errorCode, message, List.of());
	}

	public static ApiError of(String errorCode, String message, List<ApiErrorDetail> details) {
		return new ApiError(errorCode, message, details == null ? List.of() : details);
	}
}
