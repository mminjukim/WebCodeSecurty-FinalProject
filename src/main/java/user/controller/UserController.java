package main.java.user.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import main.java.user.UserService;
import main.java.user.dto.SignupRequestDto;
import main.java.user.dto.UserDto;

public class UserController {

	private final UserService userService;
	private final Scanner scanner;

	public UserController(Scanner scanner, UserService userService) {
		this.scanner = scanner;
		this.userService = userService;
	}

	/**
	 * 회원가입 화면 출력 및 사용자 입력 처리
	 */
	public void processSignup() {
		System.out.println("\n--------------SignUp--------------");

		System.out.print("1. 아이디를 입력하세요: ");
		String username = scanner.nextLine();

		System.out.print("2. 역할을 입력하세요 (숫자만 입력. 1-인사, 2-재무, 3-영업, 4-법무): ");
		String roleInput = scanner.nextLine();

		System.out.print("3. 비밀번호를 입력하세요: ");
		String password = scanner.nextLine();

		System.out.print("4. 비밀번호를 다시 입력하세요: ");
		String confirmPassword = scanner.nextLine();
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
		System.out.println("\n--------------Login---------------");

		System.out.print("아이디를 입력하세요: ");
		String username = scanner.nextLine();

		System.out.print("비밀번호를 입력하세요: ");
		String password = scanner.nextLine();
		System.out.println("----------------------------------\n");

		if (username.trim().isEmpty() || password.trim().isEmpty()) {
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
}