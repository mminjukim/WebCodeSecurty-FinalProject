package main.document.exception;

import main.common.exception.ErrorCode;

public enum DocError implements ErrorCode {

	INVALID_DOC_NO("올바른 문서 번호를 입력하세요."),
	DOCUMENT_NOT_FOUND("해당하는 문서를 찾을 수 없습니다."),
	INVALID_DOC_SIGNATURE("문서 전자서명 검증에 실패했습니다."),
	ENVELOPE_ERROR("문서 전자봉투 처리에 실패했습니다."),
	NOT_AUTHORIZED("문서 열람 권한이 없습니다."),
	WRONGLY_ENCRYPTED_DOC("잘못된 방법으로 암호화된 문서입니다."),
	WRONGLY_ENCRYPTED_SIG("잘못된 방법으로 암호화된 전자서명입니다."),
	DOCUMENT_ENCRYPTION_FAILED("문서 암호화에 실패했습니다."),
	DOCUMENT_DECRYPTION_FAILED("문서 복호화에 실패했습니다."),
	SIGNATURE_ENCRYPTION_FAILED("전자서명 암호화에 실패했습니다."),
	SIGNATURE_DECRYPTION_FAILED("전자서명 복호화에 실패했습니다."),
	SIGNATURE_CREATION_FAILED("전자서명 생성에 실패했습니다."),
	;

	private final String message;

	DocError(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
