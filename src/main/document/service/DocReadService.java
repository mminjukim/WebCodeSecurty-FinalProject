package main.document.service;

import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;

import javax.crypto.SecretKey;

import main.application.database.DBManager;
import main.application.key.KeyFileService;
import main.application.key.KeyFileService.KeyDomain;
import main.application.key.KeyFileService.KeyType;
import main.application.session.Session;
import main.document.dao.DocumentDao;
import main.document.dto.DocumentDto;
import main.log.service.ReadLogService;
import main.log.service.ReadLogService.FailReason;
import main.user.dao.RoleDao;
import main.user.dao.UserDao;
import main.user.domain.UserRole;

/**
 * 문서 열람 관리
 */
public class DocReadService {

	private static final String DOC_ROOT = ".document/";
	private static final String ALGORITHM = "AES";

	private final Session session;
	private final DocumentDao documentDao;
	private final UserDao userDao;
	private final RoleDao roleDao;
	private final DocPermissionService permissionService;
	private final DocDecryptService decryptService;
	private final DocSignatureService signatureService;
	private final EnvelopeService envelopeService;
	private final ReadLogService readLogService;

	public DocReadService(Session session, DocumentDao documentDao, UserDao userDao, RoleDao roleDao,
			DocPermissionService permissionService, DocDecryptService decryptService,
			DocSignatureService signatureService, EnvelopeService envelopeService, ReadLogService readLogService) {
		this.session = session;
		this.documentDao = documentDao;
		this.userDao = userDao;
		this.roleDao = roleDao;
		this.permissionService = permissionService;
		this.decryptService = decryptService;
		this.signatureService = signatureService;
		this.envelopeService = envelopeService;
		this.readLogService = readLogService;
	}

	/**
	 * 문서 열람 및 복호화된 내용 반환
	 * 
	 * @param docId 문서 ID
	 * @return 복호화된 내용
	 */
	public String read(int docId) {
		try (Connection conn = DBManager.getConnection()) {

			// 현재 사용자의 역할 확인
			int roleId = session.getCurrentUser().getRoleId();

			// 문서 열람 권한 확인
			permissionService.validateReadable(conn, docId, roleId);

			// DB에서 문서 정보 불러오기
			DocumentDto doc = documentDao.getDocumentById(conn, docId);
			if (doc == null) {
				throw new IllegalStateException("해당하는 문서가 존재하지 않습니다.");
			}

			// 역할 개인 키 불러오기
			UserRole role = UserRole.valueOf(roleDao.getNameById(conn, roleId));
			PrivateKey rolePrivateKey = (PrivateKey) KeyFileService
					.read(KeyFileService.buildKeyPath(KeyDomain.ROLE, KeyType.PRIVATE, role.name()));

			// 역할 개인 키로 전자봉투 복호화해 비밀 키 반환
			SecretKey secretKey = envelopeService.openEnvelope(
					rolePrivateKey, 
					ALGORITHM,
					buildPath(DocService.FileType.ENVELOPE, doc.getTitle() + "_" + role.name())
			);

			// 비밀 키로 문서 및 전자서명 복호화
			String content = decryptService.decryptDocument(
					buildPath(DocService.FileType.ENCRYPTED, doc.getTitle()),
					secretKey
			);
			byte[] signature = decryptService.decryptSignature(doc.getSignature(), secretKey);

			// 문서 업로더의 공개 키 불러오기
			PublicKey uploaderPublicKey = (PublicKey) KeyFileService
					.read(userDao.getPublicKeyPathById(conn, doc.getUploaderId()));

			// 업로더 공개 키로 전자서명 검증
			if (signatureService.verifySignature(content, signature, uploaderPublicKey) == false) {
				throw new IllegalStateException("문서 검증을 실패했습니다.");
			}

			// 열람 로그 기록
			readLogService.recordSuccessLog(docId);

			return content;

		} catch (IllegalArgumentException e) {
			readLogService.recordFailLog(docId, FailReason.NO_PERMISSION);
			throw new IllegalStateException(e.getMessage(), e);

		} catch (IllegalStateException e) {
			readLogService.recordFailLog(docId, FailReason.ERROR);
			throw e;

		} catch (Exception e) {
			readLogService.recordFailLog(docId, FailReason.DECRYPT_FAIL);
			throw new IllegalStateException("문서 복호화 및 검증을 중단합니다.", e);
		}
	}

	private String buildPath(DocService.FileType type, String identifier) {
		return Paths.get(DOC_ROOT, type.name(), identifier).toString();
	}
}