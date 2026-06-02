package main.log.service;

import java.sql.Connection;
import java.sql.SQLException;

import main.application.session.Session;
import main.common.exception.Error;
import main.common.exception.SystemException;
import main.document.dao.DocumentDao;
import main.log.exception.LogError;
import main.user.domain.UserRole;
import main.user.service.UserService;

/**
 * 로그 열람 권한 관리
 */
public class ReadLogPermissionService {

	private final Session session;
	private final DocumentDao documentDao;
	private final UserService userService;

	public ReadLogPermissionService(Session session, DocumentDao documentDao, UserService userService) {
		this.session = session;
		this.documentDao = documentDao;
		this.userService = userService;
	}

	/**
	 * 현재 로그인 사용자가 문서 열람 권한 있는지 검증
	 * 
	 * @param conn
	 * @param docId 문서 ID
	 */
	public void validate(Connection conn, int docId) {
		int currentUserId = session.getCurrentUser().getId();
		int currentRoleId = session.getCurrentUser().getRoleId();

		// 관리자인 경우 허용
		if (userService.getRoleByRoleId(currentRoleId) == UserRole.ADMIN) {
			return;
		}

		try {
			int uploaderId = documentDao.getUploaderIdById(conn, docId);
			if (uploaderId != currentUserId) {
				throw new SystemException(LogError.NOT_AUTHORIZED);
			}
		} catch (SQLException e) {
			throw new SystemException(Error.DATABASE_ERROR, "문서 업로더 정보 조회 오류");
		}
	}
}