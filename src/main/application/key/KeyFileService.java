package main.application.key;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.security.Key;

public class KeyFileService {

	private static final String KEY_ROOT_DIR = ".keys/";
	private static final String KEY_EXTENSION = ".key";

	public enum KeyDomain {
		USER("user"), ROLE("role");

		private final String dirName;

		KeyDomain(String dirName) {
			this.dirName = dirName;
		}

		public String getDirName() {
			return dirName;
		}
	}

	public enum KeyType {
		PUBLIC("public"), PRIVATE("private");

		private final String dirName;

		KeyType(String dirName) {
			this.dirName = dirName;
		}

		public String getDirName() {
			return dirName;
		}
	}

	/**
	 * 키 저장 경로 반환
	 * 
	 * @param domain 키가 속한 도메인
	 * @param type   키 타입 (비밀키, 공개키, 개인키)
	 * @param identifier 키 이름이 될 문자열 
	 */
	public static String buildKeyPath(KeyDomain domain, KeyType type, String identifier) {
		String fileName = identifier + KEY_EXTENSION;
		return Paths.get(KEY_ROOT_DIR, domain.getDirName(), type.getDirName(), fileName).toString();
	}

	/**
	 * 파일에서 키 불러오기
	 *
	 * @param fname Key 가 저장된 파일명
	 * @return Key 객체
	 */
	public static Key read(String fname) {
		try (FileInputStream fis = new FileInputStream(fname);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			Object obj = ois.readObject();
			return (Key) obj;
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException("키를 불러오는 중 오류가 발생했습니다.");
		}
	}

	/**
	 * Key 객체를 파잍에 저장
	 *
	 * @param fname 파일명
	 * @param key   저장할 객체
	 */
	public static void write(String fname, Key key) {
		try (FileOutputStream fstream = new FileOutputStream(fname);
				ObjectOutputStream ostream = new ObjectOutputStream(fstream)) {
			ostream.writeObject(key);	
		} catch (IOException e) {
			throw new RuntimeException("키 저장 중 오류가 발생했습니다.");
		}
	}
}
