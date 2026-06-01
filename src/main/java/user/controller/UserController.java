package main.java.user.controller;

import java.io.Console;
import java.security.NoSuchAlgorithmException;
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
			int roleId = Integer.parseInt(roleInput);
			SignupRequestDto requestDto = new SignupRequestDto(username, password, confirmPassword, roleId);
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
}