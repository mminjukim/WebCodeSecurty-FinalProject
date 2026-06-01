package main.java.user;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

import main.java.infrastructure.database.DBManager;
import main.java.user.dao.RoleDao;
import main.java.user.dto.SignupRequestDto;
import main.java.user.service.UserService;

public class AdminInitializer {

	private static final String ADMIN_SECRET_FILE = ".admin.txt";

	private static class AdminCredentials {
		String username;
		char[] password;
	}

	/**
	 * 관리자 계정 초기화
	 */
	public static void init(RoleDao roleDao, UserService userService) {
		AdminCredentials credentials = readCredentials(ADMIN_SECRET_FILE);

		if (credentials == null || credentials.username.isEmpty() || credentials.password.length == 0) {
			if (credentials != null && credentials.password != null) {
				Arrays.fill(credentials.password, (char) 0);
			}
			throw new IllegalStateException("관리자 계정 정보 파일을 읽을 수 없습니다.");
		}

		try (Connection conn = DBManager.getConnection()) {
			SignupRequestDto requestDto = new SignupRequestDto(
					credentials.username, 
					credentials.password, credentials.password,
					UserRole.ADMIN.getRoleNo()
			);
			userService.signup(requestDto);

		} catch (IllegalArgumentException e) { // 관리자 계정이 이미 존재
			if ("이미 사용 중인 아이디입니다.".equals(e.getMessage())) {
				return;
			}

		} catch (Exception e) {
			throw new IllegalStateException("관리자 계정 초기화에 실패했습니다.");

		} finally {
			// 메모리의 평문 비밀번호 파기
			if (credentials.password != null) {
				Arrays.fill(credentials.password, (char) 0);
			}
		}
	}

	/**
	 * 파일에서 관리자 계정 정보 읽기
	 * 
	 * @param filePath 계정 정보 파일
	 * @return AdminCredentials
	 */
	private static AdminCredentials readCredentials(String filePath) {
		AdminCredentials creds = new AdminCredentials();
		char[] pwBuffer = new char[100];
		StringBuilder idBuilder = new StringBuilder();

		boolean isFirstLine = true;
		int pwLength = 0;

		try (FileReader reader = new FileReader(filePath)) {
			int data;
			while ((data = reader.read()) != -1) {
				if (isFirstLine) { // 파일의 첫 줄 아이디 읽기
					if (data == '\n') {
						isFirstLine = false;
					} else if (data != '\r' && data != ' ') { // 비밀번호 읽기
						idBuilder.append((char) data);
					}
				} else {
					if (data != '\r' && data != '\n' && data != ' ') {
						pwBuffer[pwLength++] = (char) data;
					}
				}
			}
			creds.username = idBuilder.toString();
			creds.password = Arrays.copyOf(pwBuffer, pwLength);
			return creds;

		} catch (IOException e) {
			return null;

		} finally {
			Arrays.fill(pwBuffer, (char) 0);
		}
	}
}