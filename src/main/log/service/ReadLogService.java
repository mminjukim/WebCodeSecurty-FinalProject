package main.log.service;
import java.security.PrivateKey;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import main.application.database.DBManager;
import main.application.key.KeyFileService;
import main.application.session.Session;
import main.log.dao.ReadLogDao;
import main.log.dto.ReadLogDto;
import main.user.dao.UserDao;
import main.user.domain.UserRole;
import main.user.service.UserService;

/**
 * 문서 로그 관리 서비스
 */
public class ReadLogService {

	private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final Session session;
	private final ReadLogDao readLogDao;
	private final UserDao userDao;
	private final UserService userService;
	private final ReadLogPermissionService permissionService;
	private final ReadLogIntegrityService integrityService;
	private final ReadLogFormatter formatter;

	/**
	 * 열람 성공 여부
	 */
	public enum Status {
		SUCCESS, FAIL
	}

	/**
	 * 열람 실패 이유
	 */
	public enum FailReason {
		NONE, NO_PERMISSION, DOC_NOT_FOUND, SIGNATURE_INVALID, DECRYPT_FAIL, ERROR
	}

	public ReadLogService(Session session, ReadLogDao readLogDao, UserDao userDao, UserService userService,
			ReadLogPermissionService permissionService, ReadLogIntegrityService integrityService,
			ReadLogFormatter formatter) {
		this.session = session;
		this.readLogDao = readLogDao;
		this.userDao = userDao;
		this.userService = userService;
		this.permissionService = permissionService;
		this.integrityService = integrityService;
		this.formatter = formatter;
	}

	/**
	 * 성공 로그 저장
	 * 
	 * @param docId 열람한 문서 ID
	 */
	public void recordSuccessLog(int docId) {
		saveLog(docId, Status.SUCCESS, FailReason.NONE);
	}

	/**
	 * 실패 로그 저장
	 * 
	 * @param docId      열람한 문서 ID
	 * @param failReason 실패 이유
	 */
	public void recordFailLog(int docId, FailReason failReason) {
		saveLog(docId, Status.FAIL, failReason);
	}

	/**
	 * 로그 저장
	 * 
	 * @param docId      문서 ID
	 * @param status     열람 성공 여부
	 * @param failReason 열람 실패 이유
	 */
	private void saveLog(int docId, Status status, FailReason failReason) {
		try (Connection conn = DBManager.getConnection()) {

			// 현재 사용자 정보 불러오기
			int userId = session.getCurrentUser().getId();
			int roleId = session.getCurrentUser().getRoleId();
			UserRole role = userService.getRoleByRoleId(roleId);

			// 최근 해시 불러오기
			String prevHash = readLogDao.getLatestLogHashByDocId(conn, docId);
			if (prevHash == null) {
				prevHash = integrityService.initialPrevHash();
			}

			// 로그 정보 생성
			String readAt = LocalDateTime.now().format(LOG_TIME_FORMAT);
			String dataToSign = docId + "|" + userId + "|" + role.name() + "|" + prevHash + "|" + status.name() + "|"
					+ failReason.name() + "|" + readAt;

			// 현재 사용자의 개인 키 불러오기
			PrivateKey privateKey = (PrivateKey) KeyFileService.read(userDao.getPrivateKeyPathById(conn, userId));

			// 열람자(현재 사용자) 개인 키로 로그 전자서명 생성
			byte[] signature = integrityService.sign(dataToSign, privateKey);

			// 로그 해시 생성
			String currentHash = integrityService.hash(dataToSign + Base64.getEncoder().encodeToString(signature));

			// DB에 로그 정보 저장
			readLogDao.insertReadLog(conn, new ReadLogDto(docId, userId, role.name(), status.name(), failReason.name(),
					prevHash, currentHash, signature, readAt));

		} catch (Exception e) {
			throw new IllegalStateException("로그 기록을 실패했습니다.", e);
		}
	}

	/**
	 * 문서 로그 열람
	 * 
	 * @param docId 로그를 열람할 문서 ID
	 * @return 로그 내용
	 */
	public String viewLogs(int docId) {
		try (Connection conn = DBManager.getConnection()) {

			// 로그 열람 권한 검증
			permissionService.validate(conn, docId);

			// DB에서 로그 불러오기
			List<ReadLogDto> logs = readLogDao.getLogsByDocId(conn, docId);
			if (logs.isEmpty()) {
				throw new IllegalStateException("조회된 로그가 없습니다.");
			}

			// 로그 무결성 검증
			integrityService.validate(conn, logs);

			// 로그 내용 이어붙이기
			StringBuilder sb = new StringBuilder();
			for (ReadLogDto log : logs) {
				sb.append(formatter.format(conn, log)).append("\n");
			}
			return sb.toString();

		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
}