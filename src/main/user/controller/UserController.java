package main.user.controller;

import java.io.Console;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import main.application.session.Session;
import main.common.exception.Error;
import main.common.exception.SystemException;
import main.user.domain.UserRole;
import main.user.dto.SignupRequestDto;
import main.user.dto.UserDto;
import main.user.exception.UserError;
import main.user.service.UserService;

public class UserController {

	private final UserService userService;
	private final Scanner scanner;
	private final Console console;

	public UserController(Scanner scanner, Console console, UserService userService, Session session) {
		this.scanner = scanner;
		this.console = console;
		this.userService = userService;
	}

	/**
	 * 회원가입 화면 출력 및 사용자 입력 처리
	 */
	public void processSignup() {
		if (console == null) {
			throw new SystemException(Error.INTERNAL_ERROR, "현재 환경에서 콘솔이 지원되지 않음");
		}

		System.out.println("\n--------------SignUp--------------");

		System.out.print("1. 아이디를 입력하세요: ");
		String username = scanner.nextLine();

		if (username == null || username.trim().isEmpty()) {
			throw new SystemException(UserError.INVALID_INPUT, "빈 아이디 입력됨");
		}

		System.out.print("2. 역할을 입력하세요 (숫자만 입력. " + UserRole.getNumberAndKorName() + "): ");
		String roleInput = scanner.nextLine();

		if (roleInput == null || roleInput.trim().isEmpty()) {
			throw new SystemException(UserError.INVALID_ROLE_NO, "빈 역할 입력됨");
		}

		char[] password = null;
		char[] confirmPassword = null;
		try {
			password = console.readPassword("3. 비밀번호를 입력하세요: ");
			if (password == null || password.length == 0) {
				throw new SystemException(UserError.INVALID_INPUT, "빈 비밀번호 입력됨");
			}

			confirmPassword = console.readPassword("4. 비밀번호를 다시 입력하세요: ");
			if (confirmPassword == null || confirmPassword.length == 0) {
				throw new SystemException(UserError.INVALID_INPUT, "빈 비밀번호 입력됨");
			}

			int roleNo = Integer.parseInt(roleInput.trim());
			if (roleNo == 0) {
				throw new SystemException(UserError.INVALID_ROLE_NO);
			}
			System.out.println("----------------------------------\n");

			SignupRequestDto requestDto = new SignupRequestDto(username, password, confirmPassword, roleNo);
			userService.signup(requestDto);
			System.out.println("::: 회원가입 완료 ::: \n" + username + "님 환영합니다.");

		} catch (NumberFormatException e) {
			throw new SystemException(UserError.INVALID_ROLE_NO, "숫자만 입력");

		} finally {
			if (password != null) {
				Arrays.fill(password, '\0');
			}
			if (confirmPassword != null) {
				Arrays.fill(confirmPassword, '\0');
			}
		}
	}

	/**
	 * 로그인 화면 출력 및 사용자 입력 처리
	 */
	public UserDto processLogin() {
		if (console == null) {
			throw new SystemException(Error.INTERNAL_ERROR, "현재 환경에서 콘솔이 지원되지 않음");
		}

		System.out.println("\n--------------Login---------------");

		System.out.print("아이디를 입력하세요: ");
		String username = scanner.nextLine();
		char[] password = console.readPassword("비밀번호를 입력하세요: ");
		System.out.println("----------------------------------\n");

		if ((username == null || username.trim().isEmpty()) || (password == null || password.length == 0)) {
			throw new SystemException(UserError.INVALID_INPUT);
		}

		UserDto loginUser = userService.login(username, password);

		System.out.println("::: 로그인 완료 ::: \n" + loginUser.getUsername() + "님 환영합니다.");
		return loginUser;
	}

	/**
	 * 관리자 여부 판단
	 */
	public boolean isAdmin(UserDto user) {
		return userService.getRoleByRoleId(user.getRoleId()) == UserRole.ADMIN;
	}

	/**
	 * (관리자) 사용자들의 역할을 변경
	 */
	public void manageUsers() {
		System.out.println("\n-----------Manage Users-----------\n");

		List<UserDto> allUsers = userService.getAllUsers();
		if (allUsers == null || allUsers.isEmpty()) {
			System.out.println("[알림] 등록된 사용자가 없습니다.\n");
			return;
		}

		for (int i = 0; i < allUsers.size(); i++) {
			UserDto user = allUsers.get(i);
			String roleName = userService.getRoleByRoleId(user.getRoleId()).getKorName();
			System.out.println("[" + (i + 1) + "] " + user.getUsername() + "\t\t(" + roleName + ")");
		}

		System.out.print("\n1. 역할을 변경할 사용자를 선택해주세요 ([번호] 입력): ");
		String strInput = scanner.nextLine();
		if (strInput == null || strInput.trim().isEmpty()) {
			throw new SystemException(UserError.INVALID_INPUT, "빈 값 입력됨");
		}

		try {
			int userIdx = Integer.parseInt(strInput) - 1;
			if (userIdx < 0 || userIdx >= allUsers.size()) {
				throw new SystemException(UserError.INVALID_INPUT);
			}

			UserDto user = allUsers.get(userIdx);

			System.out.print("2. 새로 부여할 역할을 입력하세요 (숫자만 입력. " + UserRole.getNumberAndKorName() + "): ");
			String roleInput = scanner.nextLine();
			if (roleInput == null || roleInput.trim().isEmpty()) {
				throw new SystemException(UserError.INVALID_ROLE_NO, "빈 값 입력됨");
			}
			int roleNo = Integer.parseInt(roleInput);
			if (roleNo == 0) {
				throw new SystemException(UserError.INVALID_ROLE_NO);
			}

			userService.changeUserRole(user, roleNo);
			
			System.out.println("\n[알림] 사용자 " + user.getUsername() + " 의 역할이 변경되었습니다: "
					+ UserRole.fromRoleNo(roleNo).getKorName() + "\n");

		} catch (NumberFormatException e) {
			throw new SystemException(UserError.INVALID_ROLE_NO, "숫자만 입력");
		}

		System.out.println("----------------------------------\n");
	}
}