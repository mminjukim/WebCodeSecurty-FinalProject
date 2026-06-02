package main.user.dto;

import java.util.Arrays;

public class SignupRequestDto {
	private final String username;
	private final char[] password;
	private final int roleNo;

	public SignupRequestDto(String username, char[] password, char[] confirmPassword, int roleNo) {
		if (username == null || username.trim().isEmpty()) {
			throw new IllegalArgumentException("아이디는 필수 입력값입니다.");
		}
		if (password == null || password.length == 0) {
			throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
		}
		if (Arrays.equals(password, confirmPassword) == false) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		this.username = username;
		this.password = password;
		this.roleNo = roleNo;
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
		return password;
	}

	public int getRoleNo() {
		return roleNo;
	}
}