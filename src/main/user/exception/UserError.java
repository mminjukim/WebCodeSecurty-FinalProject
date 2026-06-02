package main.user.exception;

import main.common.exception.ErrorCode;

public enum UserError implements ErrorCode {

	INVALID_ROLE_NO("유효하지 않은 역할 번호입니다."),
	INVALID_INPUT("유효하지 않은 입력값입니다."),
	USER_ALREADY_EXISTS("이미 존재하는 아이디입니다."),
	NOT_AUTHENTICATED("아이디 또는 비밀번호가 일치하지 않습니다."),
	ROLE_NOT_FOUND("역할 정보를 찾을 수 없습니다."),
	USER_NOT_FOUND("사용자 정보를 찾을 수 없습니다."),
	NO_LOGIN_USER("로그인한 사용자가 없습니다."),
	CANNOT_CHANGE_ROLE("변경하려는 역할이 기존과 동일합니다."),
	;

    private final String message;

    UserError(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
