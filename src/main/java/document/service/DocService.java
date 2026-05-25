package main.java.document.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import main.java.DocSystem;
import main.java.document.dao.DocumentDao;
import main.java.document.dao.WhitelistDao;
import main.java.document.dto.DocumentDto;
import main.java.document.dto.DocumentSummaryDto;
import main.java.infrastructure.database.DBManager;
import main.java.infrastructure.key.KeyFileService;
import main.java.infrastructure.key.KeyFileService.KeyDomain;
import main.java.infrastructure.key.KeyFileService.KeyType;
import main.java.user.UserRole;
import main.java.user.dao.UserDao;

public class DocService {

	private static final String DOC_ROOT = ".document/";
	private static final String ALGORITHM = "AES";
	private static final int KEYSIZE = 256; // bits

	private final DocumentDao documentDao;
	private final UserDao userDao;
	private final WhitelistDao whitelistDao;
	private final DocEncryptService docEncryptService;
	private final DocDecryptService docDecryptService;
	private final DocSignatureService docSignatureService;
	private final EnvelopeService envelopeService;


	public DocService(DocumentDao documentDao, UserDao userDao, WhitelistDao whitelistDao,
			DocEncryptService docEncryptService, DocDecryptService docDecryptService, DocSignatureService docSignatureService,
			EnvelopeService envelopeService) {
		this.documentDao = documentDao;
		this.userDao = userDao;
		this.whitelistDao = whitelistDao;
		this.docEncryptService = docEncryptService;
		this.docDecryptService = docDecryptService;
		this.docSignatureService = docSignatureService;
		this.envelopeService = envelopeService;
	}


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


	/**
	 * 문서 관련 파일들을 저장할 경로를 생성
	 */
	public static void makeDocumentDir() {
		for (FileType filetype : FileType.values()) {
			Path path = Paths.get(DOC_ROOT, filetype.getDirName());
			try {
				Files.createDirectories(path);
			} catch (FileAlreadyExistsException e) {
				continue;
			} catch (IOException e) {
				System.out.println("[오류] 초기 문서 디렉토리 구성에 실패했습니다:" + path);
				System.exit(1);
			}
		}
	}


	/**
	 * 문서를 암호화해 저장 및 열람이 허용된 역할에 대해 전자봉투 생성
	 * 
	 * @param filePath  문서 경로
	 * @param whitelist 열람 허용된 역할 리스트
	 */
	public int upload(String filePath, List<UserRole> whitelist) {
		try (Connection conn = DBManager.getConnection()) {

			// 사용자 정보 조회 
			int userId = DocSystem.loggedInUser.getId();

			// 파일명 반환 
			String fileName = getFileName(filePath);

			// 비밀키 생성
			KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
			keyGen.init(KEYSIZE);
			SecretKey secretKey = keyGen.generateKey();

			// 문서를 비밀키로 암호화
			String outputFilePath = buildPath(FileType.ENCRYPTED, fileName);
			docEncryptService.encryptDocument(filePath, outputFilePath, secretKey);

			// 업로드한 사용자의 개인 키로 전자서명 생성 및 암호화
			String privateKeyPath = userDao.getPrivateKeyPathById(conn, userId);
			PrivateKey privateKey = (PrivateKey) KeyFileService.read(privateKeyPath);
			byte[] encryptedSignature = docEncryptService
					.encryptSignature(docSignatureService.createSignature(filePath, privateKey), secretKey);

			// 열람 허용된 역할들에 한해 전자봉투 생성 
			for (UserRole role : whitelist) {
				PublicKey publicKey = (PublicKey) KeyFileService.read(
						KeyFileService.buildKeyPath(KeyDomain.ROLE, KeyType.PUBLIC, role.name())
						);
				envelopeService.createEnvelope(
						publicKey, 
						secretKey, 
						buildPath(FileType.ENVELOPE, fileName + "_" + role.name())
						);
			}

			// 문서 정보 저장
			DocumentDto doc = new DocumentDto(fileName, userId, outputFilePath, encryptedSignature);
			int insertedId = documentDao.insertDocument(conn, doc);
			if (insertedId == 0) {
				throw new IllegalStateException("문서 데이터베이스 삽입에 실패했습니다.");
			}
			return insertedId;

		} catch (SQLException e) {
			throw new IllegalStateException("암호화된 파일 저장 중 오류가 발생했습니다.");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("파일 암호화에 실패했습니다.");
		}
	}


	/**
	 * 문서 열람 권한 정보 저장
	 * 
	 * @param docId     문서 id
	 * @param whitelist 열람 허용되는 UserRole들
	 */
	public void saveWhitelist(int docId, List<UserRole> whitelist) {
		try (Connection conn = DBManager.getConnection()) {
			for (UserRole role : whitelist) {
				whitelistDao.insertWhitelist(conn, docId, role.getId());
			}
		} catch (SQLException e) {
			throw new IllegalStateException("문서 열람 권한 정보 저장 중 오류가 발생했습니다.");
		}
	}


	/**
	 * 전체 파일 경로에서 앞의 경로 및 구분자를 제외한 파일명 반환
	 * 
	 * @param filePath 파일 경로
	 * @return 파일명
	 */
	private String getFileName(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return filePath;
		}
		String fileName = Paths.get(filePath).getFileName().toString();

		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0) {
			return fileName.substring(0, dotIndex);
		}
		return fileName;
	}


	/**
	 * 파일 타입과 파일 식별자로 경로 생성
	 * 
	 * @param type       전자봉투 또는 암호화된 문서
	 * @param identifier 파일 식별자
	 * @return 저장 경로
	 */
	private String buildPath(FileType type, String identifier) {
		return Paths.get(DOC_ROOT, type.getDirName(), identifier).toString();
	}


	/**
	 * 사용자의 역할 검증 후 전자봉투 복호화 및 문서 복호화
	 * 
	 * @param docId  문서 id
	 * @return 복호화된 문서 내용 (문자열)
	 */
	public String read(int docId) {

		// 사용자 역할 정보 조회 
		int roleId = DocSystem.loggedInUser.getRoleId();

		DocumentDto doc;
		String uploaderPublicKeyPath;

		try (Connection conn = DBManager.getConnection()) {
			//역할 권한 검증
			validatePermission(conn, docId, roleId);

			//문서 가져오기
			doc = documentDao.getDocumentById(conn, docId);
			if (doc == null) {
				throw new IllegalArgumentException("해당하는 문서가 존재하지 않습니다.");
			}

			// 사용자 공개 키 가져오기
			uploaderPublicKeyPath = userDao.getPublicKeyPathById(conn, doc.getUploaderId());
		} catch (SQLException e) {
			throw new IllegalStateException("문서 정보 조회 중 오류가 발생했습니다.");
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage() + "\n[알림] 문서 열람을 중단합니다.");
		}

		try {
			// 전자봉투 개봉하여 문서 비밀 키 복호화 
			UserRole role = UserRole.fromId(roleId);
			PrivateKey rolePrivateKey = (PrivateKey) KeyFileService.read(
					KeyFileService.buildKeyPath(KeyDomain.ROLE, KeyType.PRIVATE, role.name())
					);
			SecretKey secretKey = envelopeService.openEnvelope(
					rolePrivateKey, 
					ALGORITHM, 
					buildPath(FileType.ENVELOPE, doc.getTitle() + "_" + role.name())
					);

			//문서 비밀 키로 암호화된 문서 복호화
			String inputFilePath = buildPath(FileType.ENCRYPTED, doc.getTitle());
			String decryptedContent = docDecryptService.decryptDocument(inputFilePath, secretKey);

			// 전자서명 복호화 및 업로드한 사용자의 공개 키로 검증	
			byte[] encryptedSignature = doc.getSignature();
			byte[] signature = docDecryptService
					.decryptSignature(encryptedSignature, secretKey);

			PublicKey uploaderPublicKey = (PublicKey) KeyFileService.read(uploaderPublicKeyPath);
			boolean isValid = docSignatureService.verifySignature(decryptedContent, signature, uploaderPublicKey);
			if (!isValid) {
				throw new IllegalStateException("문서 검증을 실패했습니다.");
			}

			// 복호화된 문서 내용 반환
			return decryptedContent;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage() + "\n[알림] 문서 복호화 및 검증을 중단합니다.");
		}

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
	 * 역할 권한 검증
	 * 
	 * @param docId		문서 id
	 * @param roldId		역할 id
	 * @return 역할 권한 검증 결과
	 */
	private void validatePermission(Connection conn, int docId, int roleId) throws SQLException {
		boolean allowed = whitelistDao.existsByDocumentIdAndRoleId(conn, docId, roleId);

		if (!allowed) {
			throw new IllegalArgumentException("해당 문서에 대한 열람 권한이 없습니다.");
		}
	}
}
