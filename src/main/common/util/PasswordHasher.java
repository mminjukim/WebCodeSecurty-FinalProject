package main.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import main.common.exception.Error;
import main.common.exception.SystemException;

public class PasswordHasher {

	private static final String ALGORITHM = "SHA-512";
	private static final int SALT_LENGTH = 16;
	private static final int ITERATIONS = 3000;

	private PasswordHasher() {
	}

	/**
	 * 입력받은 평문 비밀번호를 솔트 값과 함께 해시
	 * 
	 * @param plainPassword 사용자가 입력한 평문 비밀번호
	 * @return 반복횟수:솔트값:솔트+평문해시 문자열
	 */
	public static String hash(char[] plainPassword) {
		byte[] passwordBytes = null;
		try {
			// 비밀번호를 바이트 배열로 변환
			passwordBytes = charToBytes(plainPassword);

			// 솔트 생성
			byte[] salt = new byte[SALT_LENGTH];
			SecureRandom.getInstanceStrong().nextBytes(salt);

			// (솔트 + 비밀번호) 해시 수행
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			md.update(salt);
			byte[] hash = md.digest(passwordBytes);

			// 이전 해시값을 다시 해시 반복
			for (int i = 1; i < ITERATIONS; i++) {
				md.reset();
				hash = md.digest(hash);
			}

			String encodedSalt = Base64.getEncoder().encodeToString(salt);
			String encodedHash = Base64.getEncoder().encodeToString(hash);
			return ITERATIONS + ":" + encodedSalt + ":" + encodedHash;

		} catch (NoSuchAlgorithmException e) {
			throw new SystemException(Error.HASH_ERROR);

		} finally {
			if (passwordBytes != null) {
				Arrays.fill(passwordBytes, (byte) 0);
			}
		}
	}

	/**
	 * 입력받은 평문 비밀번호가 저장된 해시 문자열과 일치하는지 검증
	 * 
	 * @param plainPassword    사용자가 로그인 시 입력한 평문 비밀번호
	 * @param storedHashString DB에 저장되어 있는 해시 문자열
	 * @return 일치하면 true, 아니면 false
	 */
	public static boolean verify(char[] plainPassword, String storedHashString) {
		byte[] passwordBytes = null;
		try {
			// 저장된 해시 데이터 분리 및 복원
			String[] parts = storedHashString.split(":");
			if (parts.length != 3) {
				return false;
			}

			int iterations = Integer.parseInt(parts[0]);
			byte[] salt = Base64.getDecoder().decode(parts[1]);
			byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

			// 입력된 평문 비밀번호를 byte 배열로 변환
			passwordBytes = charToBytes(plainPassword);

			// 동일한 솔트 및 반복 횟수를 적용하여 해시 수행
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			md.update(salt);
			byte[] testHash = md.digest(passwordBytes);

			for (int i = 1; i < iterations; i++) {
				md.reset();
				testHash = md.digest(testHash);
			}

			return Arrays.equals(expectedHash, testHash);

		} catch (Exception e) {
			return false;

		} finally {
			if (passwordBytes != null) {
				Arrays.fill(passwordBytes, (byte) 0);
			}
		}
	}

	/**
	 * char[] 배열을 byte[] 배열로 변환
	 */
	private static byte[] charToBytes(char[] chars) {
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}
		Arrays.fill(chars, (char) 0);
		return bytes;
	}
}
