package main.user.dto;

import java.util.Arrays;

import main.common.exception.SystemException;
import main.user.exception.UserError;

public class SignupRequestDto {
	private final String username;
	private final char[] password;
	private final int roleNo;

	public SignupRequestDto(String username, char[] password, char[] confirmPassword, int roleNo) {
		if (username == null || username.trim().isEmpty()) {
			throw new SystemException(UserError.INVALID_INPUT, "아이디 입력 필요");
		}
		if (password == null || password.length == 0) {
			throw new SystemException(UserError.INVALID_INPUT, "비밀번호 입력 필요");
		}
		if (Arrays.equals(password, confirmPassword) == false) {
			throw new SystemException(UserError.INVALID_INPUT, "비밀번호가 일치하지 않음");
		}

		this.username = username;
		this.password = Arrays.copyOf(password, password.length);
		this.roleNo = roleNo;
	}

	public String getUsername() {
		return username;
	}

	public char[] getPassword() {
	    return Arrays.copyOf(password, password.length);
	}

	public int getRoleNo() {
		return roleNo;
	}
}