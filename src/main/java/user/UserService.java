package main.java.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import main.java.config.database.DBManager;

public class UserService {

	private static final UserService instance = new UserService();

	private final UserDao userDao = UserDao.getInstance();

	private UserService() {
	}

	public static UserService getInstance() {
		return instance;
	}

	/**
	 * 회원가입
	 * 
	 * @param scanner
	 * @return 회원가입 성공 여부
	 * @throws NoSuchAlgorithmException
	 */
	public boolean signup(Scanner scanner) throws NoSuchAlgorithmException {

		Connection conn = DBManager.getConnection();

		System.out.println("\n--------------SignUp--------------");

		System.out.print("1. 아이디를 입력하세요: ");
		String username = scanner.nextLine();

		System.out.print("2. 역할을 입력하세요 (숫자만 입력. 1-인사, 2-영업, 3-재무, 4-법무): ");
		int roleId = 0;
		try {
			roleId = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("숫자를 입력하세요.");
			return false;
		}

		System.out.print("3. 비밀번호를 입력하세요: ");
		String password = scanner.nextLine();

		System.out.print("4. 비밀번호를 다시 입력하세요: ");
		String confirmPassword = scanner.nextLine();

		System.out.println("----------------------------------\n");

		if (!password.equals(confirmPassword)) {
			System.out.println("[오류] 비밀번호가 일치하지 않습니다.");
			return false;
		}
		if (roleId < 1 || roleId > 4) {
			System.out.println("[오류] 역할 번호는 1에서 4 사이여야 합니다.");
			return false;
		}
		if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			System.out.println("[오류] 아이디와 비밀번호는 필수로 입력해야 합니다.");
			return false;
		}

		// 비밀번호 해시
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashedPasswd = md.digest(password.getBytes());

		UserDto newUser = new UserDto(username, hashedPasswd, roleId);

		try {
			int result = userDao.insertUser(conn, newUser);
			if (result > 0) {
				System.out.println("::: 회원가입 완료 ::: \n" + username + "님 환영합니다.");
				return true;
			}
		} catch (SQLException e) {
			System.out.println("[오류] 데이터베이스 오류 발생 (" + e.getMessage() + ")");
		}

		return false;
	}

	/**
	 * 로그인
	 * 
	 * @param scanner
	 * @return 로그인된 사용자 DTO
	 * @throws NoSuchAlgorithmException
	 */
	public UserDto login(Scanner scanner) throws NoSuchAlgorithmException {

		Connection conn = DBManager.getConnection();

		System.out.println("\n--------------Login---------------");
		System.out.print("아이디를 입력하세요: ");
		String username = scanner.nextLine();

		System.out.print("비밀번호를 입력하세요: ");
		String password = scanner.nextLine();
		System.out.println("----------------------------------\n");

		try {
			UserDto user = userDao.getUserByUsername(conn, username);

			if (user == null) {
				System.out.println("[오류] 존재하지 않는 아이디입니다.");
				return null;
			}

			// 비밀번호 해시
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedInputPassword = md.digest(password.getBytes());

			if (user.getPassword().equals(hashedInputPassword)) {
				System.out.println("::: 로그인 완료 ::: \n" + user.getUsername() + "님, 환영합니다!");
				return user;
			} else {
				System.out.println("[오류] 비밀번호가 일치하지 않습니다.");
				return null;
			}
		} catch (SQLException e) {
			System.out.println("[오류] 데이터베이스 오류 발생 (" + e.getMessage() + ")");
			return null;
		}
	}

}
