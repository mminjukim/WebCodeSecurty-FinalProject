package main.log.service;

import java.sql.Connection;
import java.sql.SQLException;

import main.application.session.Session;
import main.document.dao.DocumentDao;
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
				throw new IllegalStateException("해당 문서에 대한 로그 열람 권한이 없습니다.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException("문서 업로더 정보를 가져오는 중 오류가 발생했습니다.");
		}
	}
}