package main.user.domain;

import java.util.ArrayList;

import main.common.exception.SystemException;
import main.user.exception.UserError;

public enum UserRole {
	ADMIN(0, "관리자"), HR(1, "인사"), FINANCE(2, "재무"), SALES(3, "영업"), LEGAL(4, "법무");

	private final int roleNo;
	private final String korName;

	UserRole(int id, String korName) {
		this.roleNo = id;
		this.korName = korName;
	}

	public int getRoleNo() {
		return roleNo;
	}

	public String getKorName() {
		return korName;
	}

	public static UserRole fromRoleNo(int roleNo) {
		for (UserRole role : values()) {
			if (role.getRoleNo() == roleNo) {
				return role;
			}
		}
		throw new SystemException(UserError.INVALID_ROLE_NO);
	}

	public static String getNumberAndKorName() {
		ArrayList<String> list = new ArrayList<>();
		for (UserRole role : values()) {
			if (role != UserRole.ADMIN) {
				list.add(role.getRoleNo() + "-" + role.getKorName());
			}
		}
		return String.join(", ", list);
	}
}
