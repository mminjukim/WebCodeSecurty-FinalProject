package main.java.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.java.infrastructure.database.QueryExecutor;
import main.java.infrastructure.database.SqlQueryBuilder;

public class RoleDao {

	private static final String T_ROLE = "roles";
	private static final String C_ID = "id";
	private static final String C_ROLE_NAME = "role_name";
	private static final String C_PUBLIC_KEY = "public_key";
	private static final String C_PRIVATE_KEY = "private_key";
	
	public String getNameById(Connection conn, int id) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ROLE_NAME)
				.from(T_ROLE)
				.where(C_ID + " = ?", id);
		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
	            return rs.getString(C_ROLE_NAME);
	        }
			throw new IllegalArgumentException("해당 ID의 역할 정보를 찾을 수 없습니다: " + id);
		});
	}
}
