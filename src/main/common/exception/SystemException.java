package main.common.exception;

/**
 * 커스텀 정의 예외
 */
public class SystemException extends RuntimeException {

	private final ErrorCode errorCode;

	// 정의된 기본 메시지만 사용
	public SystemException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	// 기본 메시지 외에 상세정보 추가
	public SystemException(ErrorCode errorCode, String detailMessage) {
		super(errorCode.getMessage() + " : " + detailMessage);
		this.errorCode = errorCode;
	}

	// SQLException 등 기존 시스템 에러
	public SystemException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}