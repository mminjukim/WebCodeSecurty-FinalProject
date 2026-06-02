package main.application.session;

import main.common.exception.SystemException;
import main.user.dto.UserDto;
import main.user.exception.UserError;

/**
 * 로그인한 사용자를 관리
 */
public class Session {

	private UserDto currentUser;

	public void login(UserDto user) {
		this.currentUser = user;
	}

	public void logout() {
		this.currentUser = null;
	}

	public boolean isLoggedIn() {
		return currentUser != null;
	}

	public UserDto getCurrentUser() {
		if (currentUser == null) {
			throw new SystemException(UserError.NO_LOGIN_USER);
		}
		return currentUser;
	}
}
