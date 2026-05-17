package main.java;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import main.java.infrastructure.lifecycle.ServerLifeCycle;
import main.java.user.UserDto;
import main.java.user.UserService;

public class DocSystem {

	private static UserDto loggedInUser = null;

	public static void main(String[] args) throws NoSuchAlgorithmException {
		ServerLifeCycle.start();
		printWelcome();

		Scanner scanner = new Scanner(System.in);
		UserService userService = UserService.getInstance();

		char choice = 'A';
		while (choice != 'Q') {

			if (loggedInUser == null) {
				System.out.println("\n\n---------------Menu---------------");
				System.out.println("  - 회원가입: [S]ignup");
				System.out.println("  - 로그인: [L]ogin");
				System.out.println("  - 프로그램 종료: [Q]uit");
				System.out.println("----------------------------------");
				System.out.print(">> 메뉴를 선택하세요: ");

				choice = scanner.nextLine().toUpperCase().charAt(0);

				switch (choice) {
				case 'S':
					userService.signup(scanner);
					System.out.println();
					break;
				case 'L':
					loggedInUser = userService.login(scanner);
					System.out.println();
					break;
				case 'Q':
					System.out.println("프로그램을 종료합니다.");
					break;
				default:
					System.out.println("[알림] 올바른 메뉴를 입력해 주세요.\n");
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

				choice = scanner.nextLine().toUpperCase().charAt(0);

				switch (choice) {
				case 'U':
					// TODO: 로직 추가
					break;
				case 'R':
					// TODO: 로직 추가
					break;
				case 'H':
					// TODO: 로직 추가
					break;
				case 'L':
					System.out.println("[알림] 로그아웃 되었습니다.\n");
					loggedInUser = null;
					break;
				case 'Q':
					System.out.println("프로그램을 종료합니다.");
					break;
				default:
					System.out.println("[알림] 올바른 메뉴를 입력해 주세요.\n");
				}
			}
		}

		scanner.close();
	}

	private static void printWelcome() {
		String str = """

				       ************************************************************************
				       *                                                                      *
				       *    ██████╗  ██████╗  ██████╗                                         *
				       *    ██╔══██╗██╔═══██╗██╔════╝                                         *
				       *    ██║  ██║██║   ██║██║                                              *
				       *    ██║  ██║██║   ██║██║                                              *
				       *    ██████╔╝╚██████╔╝╚██████╗██╗                                      *
				       *    ╚═════╝  ╚═════╝  ╚═════╝╚═╝                                      *
				       *                                                                      *
				       *    ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗            *
				       *    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝            *
				       *    ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗              *
				       *    ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝              *
				       *    ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗            *
				       *    ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝            *
				       *                                                                      *
				       *    ███████╗██╗   ██╗███████╗████████╗███████╗███╗   ███╗             *
				       *    ██╔════╝╚██╗ ██╔╝██╔════╝╚══██╔══╝██╔════╝████╗ ████║             *
				       *    ███████╗ ╚████╔╝ ███████╗   ██║   █████╗  ██╔████╔██║             *
				       *    ╚════██║  ╚██╔╝  ╚════██║   ██║   ██╔══╝  ██║╚██╔╝██║             *
				       *    ███████║   ██║   ███████║   ██║   ███████╗██║ ╚═╝ ██║             *
				       *    ╚══════╝   ╚═╝   ╚══════╝   ╚═╝   ╚══════╝╚═╝     ╚═╝             *
				       *                                                                      *
				       ************************************************************************
				       *                                                                      *
				       *   Document Management System                                         *
				       *   전자봉투를 이용한 기업 문서 관리 시스템 프로토타입                             *
				       *                                                                      *
				       *                                                                      *
				       ************************************************************************

				""";
		System.out.println(str);
	}
}
