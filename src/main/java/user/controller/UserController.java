package main.java.user.controller;

import java.io.Console;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

import main.java.user.UserRole;
import main.java.user.dto.SignupRequestDto;
import main.java.user.dto.UserDto;
import main.java.user.service.UserService;

public class UserController {

	private final UserService userService;
	private final Scanner scanner;
	private final Console console;

	public UserController(Scanner scanner, Console console, UserService userService) {
		this.scanner = scanner;
		this.console = console;
		this.userService = userService;
	}

	/**
	 * 회원가입 화면 출력 및 사용자 입력 처리
	 */
	public void processSignup() {
		if (console == null) {
			System.out.println("\n[오류] 콘솔이 지원되지 않는 환경입니다.");
			return;
		}

		System.out.println("\n--------------SignUp--------------");

		System.out.print("1. 아이디를 입력하세요: ");
		String username = scanner.nextLine();

		System.out.print("2. 역할을 입력하세요 (숫자만 입력. " + UserRole.getNumberAndKorName() + "): ");
		String roleInput = scanner.nextLine();

		char[] password = console.readPassword("3. 비밀번호를 입력하세요: ");
		char[] confirmPassword = console.readPassword("4. 비밀번호를 다시 입력하세요: ");

		System.out.println("----------------------------------\n");

		try {
			int roleNo = Integer.parseInt(roleInput);
			SignupRequestDto requestDto = new SignupRequestDto(username, password, confirmPassword, roleNo);
			userService.signup(requestDto);
			System.out.println("::: 회원가입 완료 ::: \n" + username + "님 환영합니다.");

		} catch (NumberFormatException e) {
			System.out.println("[오류] 역할은 숫자로 입력해야 합니다.");
		} catch (Exception e) {
			System.out.println("[오류] " + e.getMessage());
		}
	}

	/**
	 * 로그인 화면 출력 및 사용자 입력 처리
	 */
	public UserDto processLogin() {
		if (console == null) {
			System.out.println("\n[오류] 콘솔이 지원되지 않는 환경입니다.");
			return null;
		}

		System.out.println("\n--------------Login---------------");

		System.out.print("아이디를 입력하세요: ");
		String username = scanner.nextLine();
		char[] password = console.readPassword("비밀번호를 입력하세요: ");
		System.out.println("----------------------------------\n");

		if ((username == null || username.trim().isEmpty()) || (password == null || password.length == 0)) {
			System.out.println("[오류] 아이디와 비밀번호는 필수로 입력해야 합니다.");
			return null;
		}

		try {
			UserDto loginUser = userService.login(username, password);
			System.out.println("::: 로그인 완료 ::: \n" + loginUser.getUsername() + "님 환영합니다.");
			return loginUser;

		} catch (IllegalArgumentException e) {
			System.out.println("[오류] " + e.getMessage());
		} catch (IllegalStateException e) {
			System.out.println("[오류] " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[오류] " + e.getMessage());
		}
		return null;
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
		for (int i = 0; i < allUsers.size(); i++) {
			UserDto user = allUsers.get(i);
			String roleName = userService.getRoleByRoleId(user.getRoleId()).getKorName();
			System.out.println("[" + (i + 1) + "] " + user.getUsername() + "\t\t(" + roleName + ")");
		}

		System.out.print("\n1. 역할을 변경할 사용자를 선택해주세요 ([번호] 입력): ");
		String strInput = scanner.nextLine();

		try {
			UserDto user = allUsers.get(Integer.parseInt(strInput) - 1);

			System.out.print("2. 새로 부여할 역할을 입력하세요 (숫자만 입력. " + UserRole.getNumberAndKorName() + "): ");
			int roleNo = Integer.parseInt(scanner.nextLine());

			userService.changeUserRole(user, roleNo);
			
			System.out.println("\n[알림] 사용자 " + user.getUsername() + " 의 역할이 변경되었습니다: "
					+ UserRole.fromRoleNo(roleNo).getKorName() + "\n");

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("역할은 숫자로 입력해야 합니다.");
		}

		System.out.println("----------------------------------\n");
	}
}