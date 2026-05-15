package main.java.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {

	private PasswordHasher() {
	}

	public static String hash(String plainPassword) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedBytes = md.digest(plainPassword.getBytes());
			return Base64.getEncoder().encodeToString(hashedBytes);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("해싱 알고리즘을 초기화할 수 없습니다.", e);
		}
	}
}
