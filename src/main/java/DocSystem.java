package main.java;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import main.java.document.controller.DocController;
import main.java.infrastructure.lifecycle.AppConfig;
import main.java.infrastructure.lifecycle.ServerLifeCycle;
import main.java.log.controller.ReadLogController;
import main.java.user.AdminInitializer;
import main.java.user.controller.UserController;
import main.java.user.dto.UserDto;

public class DocSystem {

	public static UserDto loggedInUser = null;

	public static void main(String[] args) throws NoSuchAlgorithmException {

		// 프로그램 시작
		ServerLifeCycle.start();

		// 의존성
		AppConfig appConfig = new AppConfig();
		UserController userController = appConfig.getUserController();
		DocController docController = appConfig.getDocController();
		ReadLogController readLogController = appConfig.getReadLogController();
		Scanner scanner = appConfig.getScanner();

		// 관리자 계정 초기화
		AdminInitializer.init(appConfig.getRoleDao(), appConfig.getUserService());

		boolean isRunning = true;

		while (isRunning) {
			if (loggedInUser == null) {
				isRunning = handleGuestMenu(scanner, userController);
			} else if (userController.isAdmin(loggedInUser)) {
				isRunning = handleAdminMenu(scanner, docController, readLogController);
			} else {
				isRunning = handleUserMenu(scanner, docController, readLogController);
			}
		}

		scanner.close();
		ServerLifeCycle.stop();
	}

	// 비로그인 사용자 메뉴
	private static boolean handleGuestMenu(Scanner scanner, UserController userController) {
		System.out.println("\n\n---------------Menu---------------");
		System.out.println("  - 회원가입: [S]ignup");
		System.out.println("  - 로그인: [L]ogin");
		System.out.println("  - 프로그램 종료: [Q]uit");
		System.out.println("----------------------------------");

		char choice = getMenuChoice(scanner);
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
			System.out.println("\n[알림] 프로그램을 종료합니다.\n");
			return false;
		default:
			System.out.println("\n[알림] 올바른 메뉴를 입력해 주세요.\n");
		}
		return true;
	}

	// 관리자 메뉴
	private static boolean handleAdminMenu(
			Scanner scanner, 
			DocController docController,
			ReadLogController readLogController
	) {
		System.out.println("\n\n------------ADMIN Menu------------");
		System.out.println("  - 문서 열람: [R]ead");
		System.out.println("  - 문서 로그 확인: [H]istory");
		System.out.println("  - 사용자 관리: [M]anage");
		System.out.println("  - 로그아웃: [L]ogout");
		System.out.println("  - 프로그램 종료: [Q]uit");
		System.out.println("----------------------------------");

		char choice = getMenuChoice(scanner);
		switch (choice) {
		case 'R':
			docController.readDocument();
			break;
		case 'H':
			readLogController.viewLogs();
			break;
		case 'M':
			// userController.manageUsers();
			break;
		case 'L':
			System.out.println("\n[알림] 로그아웃 되었습니다.\n");
			loggedInUser = null;
			break;
		case 'Q':
			System.out.println("\n[알림] 프로그램을 종료합니다.\n");
			return false;
		default:
			System.out.println("\n[알림] 올바른 메뉴를 입력해 주세요.\n");
		}
		return true;
	}

	// 일반 사용자 메뉴
	private static boolean handleUserMenu(
			Scanner scanner, 
			DocController docController,
			ReadLogController readLogController
	) {
		System.out.println("\n\n---------------Menu---------------");
		System.out.println("  - 문서 업로드: [U]pload");
		System.out.println("  - 문서 열람: [R]ead");
		System.out.println("  - 문서 로그 확인: [H]istory");
		System.out.println("  - 로그아웃: [L]ogout");
		System.out.println("  - 프로그램 종료: [Q]uit");
		System.out.println("----------------------------------");

		char choice = getMenuChoice(scanner);
		switch (choice) {
		case 'U':
			docController.uploadDocument();
			break;
		case 'R':
			docController.readDocument();
			break;
		case 'H':
			readLogController.viewLogs();
			break;
		case 'L':
			System.out.println("\n[알림] 로그아웃 되었습니다.\n");
			loggedInUser = null;
			break;
		case 'Q':
			System.out.println("\n[알림] 프로그램을 종료합니다.\n");
			return false;
		default:
			System.out.println("\n[알림] 올바른 메뉴를 입력해 주세요.\n");
		}
		return true;
	}

	// 사용자 입력 처리
	private static char getMenuChoice(Scanner scanner) {
		System.out.print(">> 메뉴를 선택하세요: ");
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) {
			return ' ';
		}
		return input.toUpperCase().charAt(0);
	}
}