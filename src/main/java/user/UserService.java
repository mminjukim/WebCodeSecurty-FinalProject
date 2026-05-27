package main.java.user;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import main.java.infrastructure.database.DBManager;
import main.java.infrastructure.key.KeyInitializer;
import main.java.user.dao.UserDao;
import main.java.user.dto.SignupRequestDto;
import main.java.user.dto.UserDto;
import main.java.util.PasswordHasher;

public class UserService {

	private final UserDao userDao;

	public UserService(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * 회원가입
	 */
	public void signup(SignupRequestDto request) throws NoSuchAlgorithmException {
		try (Connection conn = DBManager.getConnection()) {
			if (userDao.isUsernameExist(conn, request.getUsername())) {
				throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
			}

			UserRole role = UserRole.fromId(request.getRoleId());

			// 비밀번호 해싱
			String hashedPassword = PasswordHasher.hash(request.getPassword());
			UserDto newUser = new UserDto(request.getUsername(), hashedPassword, role.getId());

			// 키 생성
			newUser = KeyInitializer.initializeUserKeys(newUser);

			int result = userDao.insertUser(conn, newUser);
			if (result == 0) {
				throw new IllegalStateException("회원가입 데이터베이스 삽입에 실패했습니다.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException("데이터베이스 연결 오류가 발생했습니다.");
		}
	}

	/**
	 * 로그인
	 */
	public UserDto login(String username, char[] password) throws NoSuchAlgorithmException {
		try (Connection conn = DBManager.getConnection()) {
			UserDto user = userDao.getUserByUsername(conn, username);

			if (user == null) {
				throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
			}

			if (PasswordHasher.verify(password, user.getPassword()) == false) {
				throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
			}

			return user;

		} catch (SQLException e) {
			throw new IllegalStateException("데이터베이스 연결 오류가 발생했습니다.");
		}
	}

}
