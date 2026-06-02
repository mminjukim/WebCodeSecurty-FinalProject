package main.log.controller;

import java.util.List;
import java.util.Scanner;

import main.common.exception.SystemException;
import main.document.dto.DocumentSummaryDto;
import main.document.exception.DocError;
import main.document.service.DocService;
import main.log.service.ReadLogService;

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
			String docInput = scanner.nextLine();
			if (docInput == null || docInput.trim().isEmpty()) {
				throw new SystemException(DocError.INVALID_DOC_NO, "빈 값 입력됨");
			}

			int docId = Integer.parseInt(docInput);
			if (docId <= 0 || docId > documents.size()) {
				throw new SystemException(DocError.INVALID_DOC_NO);
			}

			//문서 존재 유무 확인
			List<Integer> ids = documents.stream()
					.map(DocumentSummaryDto::getId)
					.toList();
			if (!ids.contains(docId)) {
				throw new SystemException(DocError.DOCUMENT_NOT_FOUND);
			}

			System.out.println("[알림] 문서 로그 검증 및 출력을 시작합니다...");
			String result = readLogService.viewLogs(docId);

			if (result == null) {
				return;
			}

			System.out.println("[알림] 로그 검증을 성공했습니다.");
			System.out.println("\n================LOG===============\n");
			System.out.println(result);
			System.out.println("==================================\n");

		} catch (NumberFormatException e) {
			throw new SystemException(DocError.INVALID_DOC_NO, "숫자만 입력");
		}
	}
}
