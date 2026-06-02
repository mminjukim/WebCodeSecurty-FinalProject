package main.log.exception;

import main.common.exception.ErrorCode;

public enum LogError implements ErrorCode {

	SIGNATURE_CREATION_FAILED("로그 전자서명 생성에 실패했습니다."),
	INVALID_HASH_CHAIN("로그 해시 체이닝이 불일치합니다."),
	LOG_INTEGRATION_FAILED("로그 무결성 검증에 실패했습니다."),
	INVALID_LOG_SIGNATURE("로그 전자서명 검증에 실패했습니다."),
	HASH_GENERATION_FAILED("로그 해시 생성 중 오류가 발생했습니다."),
	NOT_AUTHORIZED("로그 열람 권한이 없습니다."),
	;

	private final String message;

	LogError(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
