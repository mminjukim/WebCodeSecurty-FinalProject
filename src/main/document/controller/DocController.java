package main.document.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import main.common.exception.SystemException;
import main.document.dto.DocumentSummaryDto;
import main.document.exception.DocError;
import main.document.service.DocService;
import main.user.domain.UserRole;
import main.user.exception.UserError;

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

		System.out.print("1. 업로드할 문서의 경로를 입력하세요: ");
		String filePath = scanner.nextLine();

		// 빈 문자열 체크
		if (filePath == null || filePath.trim().isEmpty()) {
			throw new IllegalArgumentException("파일명을 입력해주세요.");
		}

		// 파일 존재 유무 우선적 확인
		try (FileInputStream fis = new FileInputStream(filePath)) {
		} catch (IOException e) {
			throw new SystemException(DocError.DOCUMENT_NOT_FOUND);
		}

		System.out.println("2. 열람 권한을 허용할 역할의 번호들을 입력하세요. [ " + UserRole.getNumberAndKorName() + " ]");
		System.out.print("   (예시: 1, 2, 3 또는 1 2 3) : ");
		String inputWhitelist = scanner.nextLine();
		List<UserRole> whitelist = parseRoles(inputWhitelist);
		whitelist.add(UserRole.ADMIN);

		System.out.println("[알림] 문서 암호화 및 업로드를 시작합니다...");
		docService.upload(filePath, whitelist);
		System.out.println("[알림] 문서가 성공적으로 업로드되었습니다.");

		System.out.println("----------------------------------\n");
	}

	/**
	 * 문서 열람
	 */
	public void readDocument() {
		System.out.println("\n---------Access Document----------");

		try {
			System.out.println("[알림] 전체 문서 목록을 조회합니다...");

			List<DocumentSummaryDto> documents = docService.getDocumentList();
			if (documents.isEmpty()) {
				System.out.println("[알림] 등록된 문서가 없습니다.");
				return;
			}
			System.out.println("\n::: 문서 목록 :::");
			for (DocumentSummaryDto doc : documents) {
				System.out.println("[" + doc.getId() + "] " + doc.getTitle());
			}
			

			System.out.print("\n열람할 문서의 번호를 입력하세요: ");
			String docInput = scanner.nextLine();
			if (docInput == null || docInput.trim().isEmpty()) {
				throw new SystemException(DocError.INVALID_DOC_NO, "빈 값 입력됨");
			}

			int docId = Integer.parseInt(docInput);
			if (docId <= 0 || docId > documents.size()) {
				throw new SystemException(DocError.INVALID_DOC_NO);
			}

			//해당하는 번호의 문서가 없을때
			List<Integer> ids = documents.stream()
					.map(DocumentSummaryDto::getId)
					.toList();
			if (ids.contains(docId) == false) {
				throw new SystemException(DocError.DOCUMENT_NOT_FOUND);
			}

			System.out.println("[알림] 문서 복호화를 시작합니다...");
			String documentContent = docService.read(docId);

			System.out.println("[알림] 문서가 성공적으로 복호화 되었습니다.");
			System.out.println("\n=============CONTENT==============\n");
			System.out.println(documentContent);
			System.out.println("\n==================================\n");

		} catch (NumberFormatException e) {
			throw new SystemException(DocError.INVALID_DOC_NO, "숫자만 입력");
		}
	}

	/**
	 * 입력된 역할 번호들을 역할 리스트로 변환
	 */
	private List<UserRole> parseRoles(String input) throws IllegalArgumentException {
		if (input == null || input.trim().isEmpty()) {
			throw new SystemException(UserError.INVALID_ROLE_NO, "빈 값 입력됨");
		}
		if (input.matches("^[0-9, ]+$") == false) {
			throw new SystemException(UserError.INVALID_ROLE_NO, "숫자, 콤마, 공백만 입력 가능");
		}
		return Arrays.stream(input.split("[, ]+"))
				.filter(token -> token.isEmpty() == false)
				.map(Integer::parseInt)
				.map(UserRole::fromRoleNo)
				.distinct()
				.collect(Collectors.toList());
	}
}
