package main.application.initializer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import main.common.exception.Error;
import main.common.exception.SystemException;
import main.user.domain.UserRole;
import main.user.dto.SignupRequestDto;
import main.user.exception.UserError;
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
			throw new SystemException(Error.FILE_NOT_FOUND, "관리자 계정 정보");
		}

		try {
			SignupRequestDto request = new SignupRequestDto(
					credentials.username, 
					credentials.password,
					credentials.password, 
					UserRole.ADMIN.getRoleNo()
			);
			userService.signup(request);

		} catch (SystemException e) {
			if (e.getErrorCode() == UserError.USER_ALREADY_EXISTS) {
				return;
			}
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