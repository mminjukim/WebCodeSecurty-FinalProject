package main.application.initializer;

import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import main.user.domain.UserRole;
import main.user.dto.SignupRequestDto;
import main.user.service.UserService;

/**
 * 관리자 계정 초기화 
 */
public class AdminInitializer {

	private static final String ADMIN_SECRET_FILE = ".admin.txt";

	private static class AdminCredentials {
		String username;
		char[] password;
	}

	/**
	 * 관리자 계정 존재 여부 확인 및 없는 경우 초기화 
	 */
	public void init(UserService userService) {
		AdminCredentials credentials = readCredentials();

		if (credentials == null || credentials.username.isEmpty() || credentials.password.length == 0) {
			throw new IllegalStateException("관리자 계정 정보 파일을 읽을 수 없습니다.");
		}

		try {
			SignupRequestDto request = new SignupRequestDto(
					credentials.username, 
					credentials.password,
					credentials.password, 
					UserRole.ADMIN.getRoleNo()
			);
			userService.signup(request);
		} catch (IllegalArgumentException e) {
			if (!"이미 사용 중인 아이디입니다.".equals(e.getMessage())) {
				throw e;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("관리자 계정 비밀번호 해싱 중 오류가 발생했습니다.");
		} finally {
			Arrays.fill(credentials.password, (char) 0);
		}
	}

	/**
	 * 관리자 계정 파일에서 정보 읽기
	 */
	private AdminCredentials readCredentials() {
		AdminCredentials creds = new AdminCredentials();
		char[] pwBuffer = new char[100];
		StringBuilder username = new StringBuilder();

		try (FileReader reader = new FileReader(ADMIN_SECRET_FILE)) {
			boolean readingUsername = true;
			int pwLength = 0;
			int data;

			while ((data = reader.read()) != -1) {
				if (readingUsername && data == '\n') {
					readingUsername = false;
				} else if (readingUsername && data != '\r' && data != ' ') {
					username.append((char) data);
				} else if (!readingUsername && data != '\r' && data != '\n' && data != ' ') {
					pwBuffer[pwLength++] = (char) data;
				}
			}

			creds.username = username.toString();
			creds.password = Arrays.copyOf(pwBuffer, pwLength);
			return creds;
		} catch (IOException e) {
			return null;
		} finally {
			Arrays.fill(pwBuffer, (char) 0);
		}
	}
}