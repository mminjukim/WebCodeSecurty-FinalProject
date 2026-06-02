package main.document.service;

import java.sql.Connection;
import java.sql.SQLException;

import main.document.dao.WhitelistDao;
import main.user.dao.RoleDao;
import main.user.domain.UserRole;

/**
 * 문서 열람 권한 관리
 */
public class DocPermissionService {

	private final WhitelistDao whitelistDao;
	private final RoleDao roleDao;

	public DocPermissionService(WhitelistDao whitelistDao, RoleDao roleDao) {
		this.whitelistDao = whitelistDao;
		this.roleDao = roleDao;
	}

	/**
	 * 특정 문서에 해당 역할이 열람 권한 가지는지 검증
	 * 
	 * @param conn
	 * @param docId  문서 ID
	 * @param roleId 역할 ID
	 * @throws SQLException
	 */
	public void validateReadable(Connection conn, int docId, int roleId) throws SQLException {
		UserRole role = UserRole.valueOf(roleDao.getNameById(conn, roleId));

		if (role == UserRole.ADMIN) {
			return;
		}

		if (whitelistDao.existsByDocumentIdAndRoleId(conn, docId, roleId) == false) {
			throw new IllegalArgumentException("해당 문서에 대한 열람 권한이 없습니다.");
		}
	}
}