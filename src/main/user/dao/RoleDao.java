package main.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.common.exception.SystemException;
import main.common.util.QueryExecutor;
import main.common.util.SqlQueryBuilder;
import main.user.domain.UserRole;
import main.user.exception.UserError;

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
			throw new SystemException(UserError.ROLE_NOT_FOUND);
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
			throw new SystemException(UserError.ROLE_NOT_FOUND);
		});
	}
	
	public int getAdminRoleId(Connection conn) throws SQLException {
		return getRoleId(conn, UserRole.ADMIN);
	}
}
