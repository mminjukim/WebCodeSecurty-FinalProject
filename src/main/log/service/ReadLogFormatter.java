package main.log.service;

import java.sql.Connection;
import java.sql.SQLException;

import main.log.dto.ReadLogDto;
import main.user.dao.UserDao;

/**
 * 로그 출력 관리
 */
public class ReadLogFormatter {

	private final UserDao userDao;

	public ReadLogFormatter(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * 로그 정보를 한 줄의 문자열로 변환
	 * 
	 * @param conn
	 * @param log  로그 정보
	 * @return 로그 문자열
	 */
	public String format(Connection conn, ReadLogDto log) {
		try {
			String username = userDao.getUsernameById(conn, log.getReaderId());
			return log.getReadAt() + " READ: user=" + username + ",\trole=" + log.getReaderRole() + ",\tstatus="
					+ log.getStatus() + ",\tfail=" + log.getFailReason();
		} catch (SQLException e) {
			throw new IllegalStateException("로그 열람한 사용자 정보를 조회 중 오류가 발생했습니다.");
		}
	}
}