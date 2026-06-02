package main.document.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import main.application.database.DBManager;
import main.document.dao.DocumentDao;
import main.document.dto.DocumentSummaryDto;
import main.user.domain.UserRole;

/**
 * 문서 관리 서비스
 */
public class DocService {

	private final DocUploadService uploadService;
	private final DocReadService readService;
	private final DocumentDao documentDao;

	public DocService(DocUploadService uploadService, DocReadService readService, DocumentDao documentDao) {
		this.uploadService = uploadService;
		this.readService = readService;
		this.documentDao = documentDao;
	}

	/**
	 * 문서 업로드 및 역할 권한 저장
	 * 
	 * @param filePath  업로드할 문서 경로
	 * @param whitelist 열람 허용된 역할 리스트
	 * @return 업로드된 문서 ID
	 */
	public int upload(String filePath, List<UserRole> whitelist) {
		return uploadService.upload(filePath, whitelist);
	}

	/**
	 * 문서 열람 및 복호화된 내용 반환
	 * 
	 * @param docId 문서 ID
	 * @return 복호화된 내용
	 */
	public String read(int docId) {
		return readService.read(docId);
	}

	/**
	 * 전체 문서 목록 반환
	 * 
	 * @return 전체 문서 목록
	 */
	public List<DocumentSummaryDto> getDocumentList() {
		try (Connection conn = DBManager.getConnection()) {
			return documentDao.getAllDocumentSummaries(conn);
		} catch (SQLException e) {
			throw new IllegalStateException("전체 문서 목록 조회 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 문서 타입
	 */
	public enum FileType {
		ENVELOPE("envelope"), ENCRYPTED("encrypted");

		private final String dirName;

		FileType(String dirName) {
			this.dirName = dirName;
		}

		public String getDirName() {
			return dirName;
		}
	}
}
