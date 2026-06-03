package main.document.service;

import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import main.application.database.DBManager;
import main.application.key.KeyFileService;
import main.application.key.KeyFileService.KeyDomain;
import main.application.key.KeyFileService.KeyType;
import main.application.session.Session;
import main.common.exception.Error;
import main.common.exception.SystemException;
import main.document.dao.DocumentDao;
import main.document.dao.WhitelistDao;
import main.document.dto.DocumentDto;
import main.user.dao.RoleDao;
import main.user.dao.UserDao;
import main.user.domain.UserRole;

/**
 * 문서 업로드 관리
 */
public class DocUploadService {

	private static final String DOC_ROOT = ".document/";
	private static final String ALGORITHM = "AES";
	private static final int KEYSIZE = 256;

	private final Session session;
	private final DocumentDao documentDao;
	private final UserDao userDao;
	private final RoleDao roleDao;
	private final WhitelistDao whitelistDao;
	private final DocEncryptService encryptService;
	private final DocSignatureService signatureService;
	private final EnvelopeService envelopeService;

	public DocUploadService(Session session, DocumentDao documentDao, UserDao userDao, RoleDao roleDao,
			WhitelistDao whitelistDao, DocEncryptService encryptService, DocSignatureService signatureService,
			EnvelopeService envelopeService) {
		this.session = session;
		this.documentDao = documentDao;
		this.userDao = userDao;
		this.roleDao = roleDao;
		this.whitelistDao = whitelistDao;
		this.encryptService = encryptService;
		this.signatureService = signatureService;
		this.envelopeService = envelopeService;
	}

	/**
	 * 문서 업로드 및 열람 허용된 역할 권한 저장
	 * 
	 * @param filePath  업로드할 문서 경로
	 * @param whitelist 열람 허용된 역할 리스트
	 * @return 업로드된 문서 ID
	 */
	public int upload(String filePath, List<UserRole> whitelist) {
		try (Connection conn = DBManager.getConnection()) {

			// 현재 사용자 확인
			int userId = session.getCurrentUser().getId();

			// 문서이름 추출
			String fileName = getFileName(filePath);

			// 문서를 암호화할 비밀키 생성
			KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
			keyGen.init(KEYSIZE);
			SecretKey secretKey = keyGen.generateKey();

			// 문서를 비밀 키로 암호화
			String encryptedPath = buildPath(DocService.FileType.ENCRYPTED, fileName);
			encryptService.encryptDocument(filePath, encryptedPath, secretKey);

			// 현재 사용자의 개인 키 불러오기
			String privateKeyPath = userDao.getPrivateKeyPathById(conn, userId);
			PrivateKey privateKey = KeyFileService.readPrivateKey(privateKeyPath);

			// 사용자 개인 키로 문서 전자서명 생성 후 비밀 키로 전자서명 암호화
			byte[] encryptedSignature = encryptService
					.encryptSignature(signatureService.createSignature(filePath, privateKey), secretKey);

			// 역할 공개 키로 열람 허용된 역할별 전자봉투 생성 
			for (UserRole role : whitelist) {
				PublicKey publicKey = (PublicKey) KeyFileService
						.read(KeyFileService.buildKeyPath(KeyDomain.ROLE, KeyType.PUBLIC, role.name()));
				envelopeService.createEnvelope(
						publicKey, 
						secretKey,
						buildPath(DocService.FileType.ENVELOPE, fileName + "_" + role.name())
				);
			}

			// DB에 문서 정보 저장
			DocumentDto doc = new DocumentDto(fileName, userId, encryptedPath, encryptedSignature);
			int docId = documentDao.insertDocument(conn, doc);

			// DB에 열람 권한 정보 저장
			for (UserRole role : whitelist) {
				int roleId = roleDao.getRoleId(conn, role);
				whitelistDao.insertWhitelist(conn, docId, roleId);
			}

			return docId;

		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR);
		} catch (NoSuchAlgorithmException e) {
			throw new SystemException(Error.KEY_GENERATE_ERROR, "문서 암호화 키");
		}
	}

	/**
	 * 문서 경로에서 순수 문서명만 추출
	 * 
	 * @param filePath 문서 경로
	 * @return 문서 이름
	 */
	private String getFileName(String filePath) {
		String fileName = Paths.get(filePath).getFileName().toString();
		int dotIndex = fileName.lastIndexOf('.');
		return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
	}

	/**
	 * 문서 타입, 식별자로 문서 경로 생성
	 * 
	 * @param type       문서 타입
	 * @param identifier 문서 식별자
	 * @return 문서 경로
	 */
	private String buildPath(DocService.FileType type, String identifier) {
		return Paths.get(DOC_ROOT, type.name(), identifier).toString();
	}
}