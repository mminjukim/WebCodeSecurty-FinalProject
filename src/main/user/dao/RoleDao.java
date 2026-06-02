package main.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.infrastructure.database.QueryExecutor;
import main.infrastructure.database.SqlQueryBuilder;
import main.user.UserRole;

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
	
	public int getRoleId(Connection conn, UserRole role) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ID)
				.from(T_ROLE)
				.where(C_ROLE_NAME + " = ?", role.name());
		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
	            return rs.getInt(C_ID);
	        }
			throw new IllegalArgumentException("DB에서 해당 이름의 역할 정보를 찾을 수 없습니다.");
		});
	}
	
	public int getAdminRoleId(Connection conn) throws SQLException {
		return getRoleId(conn, UserRole.ADMIN);
	}
}
