package main.common.exception;

public enum Error implements ErrorCode {

	DATABASE_ERROR("데이터베이스 처리에 실패했습니다."),
	FILE_NOT_FOUND("해당하는 파일을 찾을 수 없습니다."),
	FILE_PROCESS_ERROR("파일 처리에 실패했습니다."),
	HASH_ERROR("해시 처리 중 오류가 발생했습니다."),
	KEY_GENERATE_ERROR("키 생성 중 오류가 발생했습니다."),
	KEY_PROCESS_ERROR("키 처리 중 오류가 발생했습니다."),
	INTERNAL_ERROR("내부 처리 중 오류가 발생했습니다."),
	;

    private final String message;

    Error(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
