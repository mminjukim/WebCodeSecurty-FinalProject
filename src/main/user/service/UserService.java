package main.user.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import main.application.database.DBManager;
import main.application.initializer.KeyInitializer;
import main.common.exception.Error;
import main.common.exception.SystemException;
import main.common.util.PasswordHasher;
import main.user.dao.RoleDao;
import main.user.dao.UserDao;
import main.user.domain.UserRole;
import main.user.dto.SignupRequestDto;
import main.user.dto.UserDto;
import main.user.exception.UserError;

public class UserService {

	private final UserDao userDao;
	private final RoleDao roleDao;

	public UserService(UserDao userDao, RoleDao roleDao) {
		this.userDao = userDao;
		this.roleDao = roleDao;
	}

	/**
	 * 회원가입
	 */
	public void signup(SignupRequestDto request) {
		try (Connection conn = DBManager.getConnection()) {
			if (userDao.isUsernameExist(conn, request.getUsername())) {
				throw new SystemException(UserError.USER_ALREADY_EXISTS);
			}

			UserRole role = UserRole.fromRoleNo(request.getRoleNo());
			int roleId = roleDao.getRoleId(conn, role);

			// 비밀번호 해싱
			String hashedPassword = PasswordHasher.hash(request.getPassword());
			UserDto newUser = new UserDto(request.getUsername(), hashedPassword, roleId);

			// 키 생성
			newUser = KeyInitializer.initializeUserKeys(newUser);

			int result = userDao.insertUser(conn, newUser);
			if (result == 0) {
				throw new SystemException(Error.DATABASE_ERROR, "사용자를 저장할 수 없음");
			}
		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "회원가입 처리 중 오류");
		} finally {
			request.clearPassword();
		}
	}

	/**
	 * 로그인
	 */
	public UserDto login(String username, char[] password) {
		try (Connection conn = DBManager.getConnection()) {
			UserDto user = userDao.getUserByUsername(conn, username);

			if (user == null) {
				throw new SystemException(UserError.NOT_AUTHENTICATED);
			}
			if (PasswordHasher.verify(password, user.getPassword()) == false) {
				throw new SystemException(UserError.NOT_AUTHENTICATED);
			}

			return user;

		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "로그인 정보 조회 중 오류");
		}
	}

	/**
	 * 역할 정보 불러오기
	 */
	public UserRole getRoleByRoleId(int roleId) {
		try (Connection conn = DBManager.getConnection()) {
			String roleName = roleDao.getNameById(conn, roleId);
			return UserRole.valueOf(roleName);

		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "역할 정보 조회 중 오류");
		}
	}

	/**
	 * 전체 사용자 리스트 불러오기
	 */
	public List<UserDto> getAllUsers() {
		try (Connection conn = DBManager.getConnection()) {
			int adminRoleId = roleDao.getAdminRoleId(conn);
			List<UserDto> users = userDao.getAllUsers(conn, adminRoleId);
			return users;
		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "사용자 리스트 조회 중 오류");
		}
	}

	/**
	 * 사용자의 역할을 새로 변경
	 */
	public void changeUserRole(UserDto user, int roleNo) {
		try (Connection conn = DBManager.getConnection()) {
			int roleId = roleDao.getRoleId(conn, UserRole.fromRoleNo(roleNo));
			if (user.getRoleId() == roleId) {
				throw new SystemException(UserError.CANNOT_CHANGE_ROLE);
			}
			userDao.updateRoleId(conn, user.getId(), roleId);

		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "역할 변경 중 오류");
		}
	}

}
