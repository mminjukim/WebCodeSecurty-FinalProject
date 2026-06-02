package main.application.session;

import main.user.dto.UserDto;

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
			throw new IllegalStateException("로그인된 사용자가 없습니다.");
		}
		return currentUser;
	}
}
