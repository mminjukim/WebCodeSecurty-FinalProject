package main.java.infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class QueryExecutor {

	private QueryExecutor() {
	}

	// 파라미터를 PreparedStatement에 매핑
	public static void mapParameters(PreparedStatement pstmt, List<Object> parameters) throws SQLException {
		for (int i = 0; i < parameters.size(); i++) {
			pstmt.setObject(i + 1, parameters.get(i));
		}
	}

	// SELECT 실행
	public static <T> T executeSelect(
			Connection conn, 
			SqlQueryBuilder builder, 
			ResultSetHandler<T> handler) 
	throws SQLException {
		try (PreparedStatement pstmt = conn.prepareStatement(builder.getQuery())) {
			mapParameters(pstmt, builder.getParameters());
			try (ResultSet rs = pstmt.executeQuery()) {
				return handler.handle(rs);
			}
		}
	}

	// INSERT 실행
	public static int executeInsert(Connection conn, SqlQueryBuilder builder) throws SQLException {
		try (PreparedStatement pstmt = conn.prepareStatement(builder.getQuery(), Statement.RETURN_GENERATED_KEYS)) {
			mapParameters(pstmt, builder.getParameters());
			int insertedRow = pstmt.executeUpdate();
			if (insertedRow > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						return rs.getInt(1);
					}
				}
			}
		}
		return 0;
	}

	// UPDATE, DELETE 등 실행
	public static int executeUpdate(Connection conn, SqlQueryBuilder builder) throws SQLException {
		try (PreparedStatement pstmt = conn.prepareStatement(builder.getQuery())) {
			mapParameters(pstmt, builder.getParameters());
			return pstmt.executeUpdate();
		}
	}

	// executeQuery 결과 ResultSet 처리용
	@FunctionalInterface
	public interface ResultSetHandler<T> {
		T handle(ResultSet rs) throws SQLException;
	}
}