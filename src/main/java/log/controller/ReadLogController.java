package main.java.log.controller;

import java.util.List;
import java.util.Scanner;

import main.java.document.dto.DocumentSummaryDto;
import main.java.document.service.DocService;
import main.java.log.service.ReadLogService;

public class ReadLogController {

	private final Scanner scanner;
	private final ReadLogService readLogService;
	private final DocService docService;

	public ReadLogController(Scanner scanner, ReadLogService readLogService, DocService docService) {
		this.scanner = scanner;
		this.readLogService = readLogService;
		this.docService = docService;
	}

	/**
	 * 문서 로그 확인
	 */
	public void viewLogs() {
		System.out.println("\n----------View History------------");

		try {
			//전체 문서 목록 출력
			System.out.println("[알림] 전체 문서 목록을 출력합니다...");
			List<DocumentSummaryDto> documents = docService.getDocumentList();
			if (documents.isEmpty()) {
				System.out.println("[알림] 등록된 문서가 없습니다.");
				return;			
			}
			System.out.println("\n::: 문서 목록 :::");
			for (DocumentSummaryDto doc : documents) {
				System.out.println("[" + doc.getId() + "] " + doc.getTitle());
			}

			System.out.print("\n로그를 확인할 문서의 번호를 입력하세요 : ");	
			int docId = Integer.parseInt(scanner.nextLine());

			//문서 존재 유무 확인
			List<Integer> ids = documents.stream()
					.map(DocumentSummaryDto::getId)
					.toList();
			if (!ids.contains(docId)) {
				throw new IllegalArgumentException("해당 번호의 문서가 존재하지 않습니다.");
			}

			System.out.println("[알림] 문서 로그 검증 및 출력을 시작합니다...");
			String result = readLogService.viewLogs(docId);

			System.out.println("[알림] 로그 검증을 성공했습니다.");
			System.out.println("\n==============LOG===============\n");
			System.out.println(result);
			System.out.println("\n==================================\n");

		} catch (NumberFormatException e) {
			System.out.println("[오류] 숫자를 입력하세요.");
		} catch (Exception e) {
			System.out.println("[오류] " + e.getMessage());
		}
	}
}
