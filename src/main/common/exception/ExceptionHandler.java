package main.common.exception;

/**
 * 전역 예외 처리
 */
public class ExceptionHandler {

	private ExceptionHandler() {
	}

	/**
	 * 발생한 예외를 처리해 콘솔에 적절한 메시지 출력
	 */
	public static void handle(Exception e) {
		if (e instanceof SystemException dse) {
			System.out.println("\n[오류] " + dse.getMessage() + "\n");
		} else {
			System.out.println("\n[오류] 시스템 오류가 발생했습니다: " + e.getMessage() + "\n");
        }
	}
}