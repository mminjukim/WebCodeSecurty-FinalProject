package main.java.user.dto;

public class SignupRequestDto {
	private final String username;
	private final String password;
	private final int roleId;

	public SignupRequestDto(String username, String password, String confirmPassword, int roleId) {
		if (username == null || username.trim().isEmpty()) {
			throw new IllegalArgumentException("아이디는 필수 입력값입니다.");
		}
		if (password == null || password.trim().isEmpty()) {
			throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
		}
		if (!password.equals(confirmPassword)) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}

		this.username = username;
		this.password = password;
		this.roleId = roleId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getRoleId() {
		return roleId;
	}
}