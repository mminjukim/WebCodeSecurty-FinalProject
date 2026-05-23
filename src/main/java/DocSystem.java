package main.java;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import main.java.document.controller.DocController;
import main.java.infrastructure.lifecycle.AppConfig;
import main.java.infrastructure.lifecycle.ServerLifeCycle;
import main.java.user.controller.UserController;
import main.java.user.dto.UserDto;

public class DocSystem {

	public static UserDto loggedInUser = null;

	public static void main(String[] args) throws NoSuchAlgorithmException {

		// 프로그램 시작
		ServerLifeCycle.start();

		AppConfig appConfig = new AppConfig();
		UserController userController = appConfig.getUserController();
		DocController docController = appConfig.getDocController();
		Scanner scanner = appConfig.getScanner();

		// 메뉴 입력
		char choice = 'A';
		while (choice != 'Q') {

			if (loggedInUser == null) {
				System.out.println("\n\n---------------Menu---------------");
				System.out.println("  - 회원가입: [S]ignup");
				System.out.println("  - 로그인: [L]ogin");
				System.out.println("  - 프로그램 종료: [Q]uit");
				System.out.println("----------------------------------");
				System.out.print(">> 메뉴를 선택하세요: ");

				String input = scanner.nextLine().trim();
				if (input.isEmpty()) {
					System.out.println("\n[알림] 메뉴를 입력해 주세요.\n");
					continue;
				}
				choice = input.toUpperCase().charAt(0);

				switch (choice) {
				case 'S':
					userController.processSignup();
					System.out.println();
					break;
				case 'L':
					loggedInUser = userController.processLogin();
					System.out.println();
					break;
				case 'Q':
					System.out.println("프로그램을 종료합니다.");
					break;
				default:
					System.out.println("\n[알림] 올바른 메뉴를 입력해 주세요.\n");
				}

			} else {
				System.out.println("\n\n---------------Menu---------------");
				System.out.println("  - 문서 업로드: [U]pload");
				System.out.println("  - 문서 열람: [R]ead");
				System.out.println("  - 문서 로그 확인: [H]istory");
				System.out.println("  - 로그아웃 [L]ogout");
				System.out.println("  - 프로그램 종료: [Q]uit");
				System.out.println("----------------------------------");
				System.out.print(">> 메뉴를 선택하세요: ");

				String input = scanner.nextLine().trim();
				if (input.isEmpty()) {
					System.out.println("\n[알림] 메뉴를 입력해 주세요.\n");
					continue;
				}
				choice = input.toUpperCase().charAt(0);

				switch (choice) {
				case 'U':
					docController.uploadDocument();
					break;
				case 'R':
					// TODO: 로직 추가
					break;
				case 'H':
					// TODO: 로직 추가
					break;
				case 'L':
					System.out.println("\n[알림] 로그아웃 되었습니다.\n");
					loggedInUser = null;
					break;
				case 'Q':
					System.out.println("프로그램을 종료합니다.");
					break;
				default:
					System.out.println("\n[알림] 올바른 메뉴를 입력해 주세요.\n");
				}
			}
		}

		scanner.close();
		ServerLifeCycle.stop();
	}
}
