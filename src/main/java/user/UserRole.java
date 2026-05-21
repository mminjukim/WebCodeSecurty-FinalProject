package main.java.user;

public enum UserRole {
	HR(1), FINANCE(2), SALES(3), LEGAL(4);

	private final int id;

	UserRole(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static UserRole fromId(int id) {
		for (UserRole role : values()) {
			if (role.getId() == id) {
				return role;
			}
		}
		throw new IllegalArgumentException("유효하지 않은 역할 번호입니다: " + id);
	}
}
