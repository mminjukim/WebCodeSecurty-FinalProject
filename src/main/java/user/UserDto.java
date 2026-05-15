package main.java.user;

public class UserDto {
	private String username;
	private byte[] password;
	private int roleId;

	public UserDto(String username, byte[] password, int roleId) {
		this.username = username;
		this.password = password;
		this.roleId = roleId;
	}

	public String getUsername() {
		return username;
	}

	public byte[] getPassword() {
		return password;
	}

	public int getRoleId() {
		return roleId;
	}
}
