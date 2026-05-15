package main.java.config.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryExecutor {

	// 파라미터를 PreparedStatement에 매핑
	public static void mapParameters(PreparedStatement pstmt, List<Object> parameters) throws SQLException {
		for (int i = 0; i < parameters.size(); i++) {
			pstmt.setObject(i + 1, parameters.get(i));
		}
	}

	// SELECT 실행
	public static ResultSet executeQuery(Connection conn, SqlQueryBuilder builder) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(builder.getQuery());
		mapParameters(pstmt, builder.getParameters());
		return pstmt.executeQuery();
	}

	// INSERT, UPDATE 실행
	public static int executeUpdate(Connection conn, SqlQueryBuilder builder) throws SQLException {
		try (PreparedStatement pstmt = conn.prepareStatement(builder.getQuery())) {
			mapParameters(pstmt, builder.getParameters());
			return pstmt.executeUpdate();
		}
	}
}