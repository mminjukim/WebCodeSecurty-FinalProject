package main.java.user;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import main.java.infrastructure.database.DBManager;
import main.java.infrastructure.key.KeyInitializer;
import main.java.util.PasswordHasher;

public class UserService {

	private static final UserService instance = new UserService();

	private final UserDao userDao = UserDao.getInstance();
	private final KeyInitializer keyInitializer = KeyInitializer.getInstance();

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

		// 아이디 입력
		System.out.print("1. 아이디를 입력하세요: ");
		String username = scanner.nextLine();

		// 아이디 유효성 체크
		if (username.trim().isEmpty()) {
			System.out.println("[오류] 아이디는 필수로 입력해야 합니다.\n");
			return false;
		}
		try {
			if (userDao.isUsernameExist(conn, username)) {
				System.out.println("[오류] 이미 사용 중인 아이디입니다.\n");
				return false;
			}
		} catch (SQLException e) {
			System.out.println("[오류] 아이디 중복 확인 중 오류가 발생했습니다.");
			return false;
		}

		// 역할 입력
		System.out.print("2. 역할을 입력하세요 (숫자만 입력. 1-인사, 2-재무, 3-영업, 4-법무): ");
		int roleId = 0;
		try {
			roleId = Integer.parseInt(scanner.nextLine());
		} catch (NumberFormatException e) {
			System.out.println("숫자를 입력하세요.");
			return false;
		}

		// 역할 유효성 체크
		if (roleId < 1 || roleId > 4) {
			System.out.println("[오류] 역할 번호는 1에서 4 사이여야 합니다.");
			return false;
		}

		// 비밀번호 입력
		System.out.print("3. 비밀번호를 입력하세요: ");
		String password = scanner.nextLine();

		System.out.print("4. 비밀번호를 다시 입력하세요: ");
		String confirmPassword = scanner.nextLine();

		// 비밀번호 유효성 체크
		if (password.trim().isEmpty()) {
			System.out.println("[오류] 비밀번호는 필수 입력값입니다.\n");
			return false;
		}
		if (!password.equals(confirmPassword)) {
			System.out.println("[오류] 비밀번호가 일치하지 않습니다.");
			return false;
		}

		System.out.println("----------------------------------\n");

		// 비밀번호 해시
		String hashedPassword = PasswordHasher.hash(password);
		UserDto newUser = new UserDto(username, hashedPassword, roleId);

		// 공개키, 비밀키 생성
		newUser = keyInitializer.initializeUserKeys(newUser);

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
			String hashedPassword = PasswordHasher.hash(password);

			if (user.getPassword().equals(hashedPassword)) {
				System.out.println("::: 로그인 완료 ::: \n" + user.getUsername() + "님 환영합니다.");
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
