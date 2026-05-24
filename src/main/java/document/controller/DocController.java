package main.java.document.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import main.java.document.dto.DocumentSummaryDto;
import main.java.document.service.DocService;
import main.java.user.UserRole;

public class DocController {

	private final Scanner scanner;
	private final DocService docService;

	public DocController(Scanner scanner, DocService docService) {
		this.scanner = scanner;
		this.docService = docService;
	}

	/**
	 * 문서 업로드
	 */
	public void uploadDocument() {
		System.out.println("\n---------Upload Document----------");

		try {
			System.out.print("1. 업로드할 문서의 경로를 입력하세요: ");
			String filePath = scanner.nextLine();

			// 빈 문자열 체크
			if (filePath == null || filePath.isEmpty()) {
				throw new IllegalArgumentException("파일명을 입력해주세요.");
			}

			// 파일 존재 유무 우선적 확인
			try (FileInputStream fis = new FileInputStream(filePath)) {
			} catch (IOException e) {
				throw new IllegalArgumentException("업로드할 문서를 찾을 수 없습니다.");
			}

			System.out.println("2. 열람 권한을 허용할 역할의 번호들을 입력하세요. [ " + UserRole.getNumberAndKorName() + " ]");
			System.out.print("   (예시: 1, 2, 3 또는 1 2 3) : ");
			String inputWhitelist = scanner.nextLine();
			List<UserRole> whitelist = parseRoles(inputWhitelist);

			System.out.println("[알림] 문서 암호화 및 업로드를 시작합니다...");
			int insertedId = docService.upload(filePath, whitelist);
			System.out.println("[알림] 권한 정보를 저장합니다...");
			docService.saveWhitelist(insertedId, whitelist);
			System.out.println("[알림] 문서가 성공적으로 업로드되었습니다.");

			System.out.println("----------------------------------\n");

		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("\n[오류] " + e.getMessage());
		}
	}


	/**
	 * 문서 열람
	 */
	public void readDocument() {
		System.out.println("\n---------Access Document----------");

		try {
			System.out.println("[알림] 전체 문서 목록을 조회합니다...");

			List<DocumentSummaryDto> documents = docService.getDocumentList();
			System.out.println("\n[문서 목록]");
			for (DocumentSummaryDto doc : documents) {
				System.out.println("[" + doc.getId() + "] " + doc.getTitle());
			}

			System.out.print("\n열람할 문서의 번호를 입력하세요: ");
			String docInput = scanner.nextLine();

			int docId = Integer.parseInt(docInput);

			//해당하는 번호의 문서가 없을때
			List<Integer> ids = documents.stream()
					.map(DocumentSummaryDto::getId)
					.toList();
			if (!ids.contains(docId)) {
				throw new IllegalArgumentException("[오류] 해당 번호의 문서가 존재하지 않습니다.");
			}

			System.out.println("[알림] 문서 복호화를 시작합니다...");
			String documentContent = docService.read(docId);

			System.out.println("[알림] 문서가 성공적으로 복호화 되었습니다.");
			System.out.println("----------[문서 본문 내용]-----------");
			System.out.println("==================================");
			System.out.println(documentContent);
			System.out.println("==================================");

			System.out.println("----------------------------------\n");

		} catch (NumberFormatException e) {
			System.out.println("[오류] 열람할 문서의 번호를 입력해야 합니다.");
		}  catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println("\n[오류] " + e.getMessage());
		}
}

/**
 * 입력된 역할 번호들을 역할 리스트로 변환
 */
private List<UserRole> parseRoles(String input) throws IllegalArgumentException {
	if (input == null || input.trim().isEmpty()) {
		throw new IllegalArgumentException("열람 허용할 역할을 입력해야 합니다.");
	}
	if (input.matches("^[0-9, ]+$") == false) {
		throw new IllegalArgumentException("숫자, 콤마, 공백만 입력 가능합니다. 잘못된 입력값: " + input);
	}
	return Arrays.stream(input.split("[, ]+"))
			.filter(token -> token.isEmpty() == false)
			.map(Integer::parseInt)
			.map(UserRole::fromId)
			.distinct()
			.collect(Collectors.toList());
}
}
