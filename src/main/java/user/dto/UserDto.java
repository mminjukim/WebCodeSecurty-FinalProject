package main.java.user.dto;

public class UserDto {
	private long id;
	private String username;
	private String password;
	private String publicKey;
	private String privateKey;
	private int roleId;

	public UserDto(String username, String password, int roleId) {
		this.username = username;
		this.password = password;
		this.roleId = roleId;
	}

	public UserDto(long id, String username, String password, 
			String publicKey, String privateKey, int roleId) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
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

	public String getPublicKey() {
		return publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

}
