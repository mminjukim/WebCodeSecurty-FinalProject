package main.java.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.java.infrastructure.database.QueryExecutor;
import main.java.infrastructure.database.SqlQueryBuilder;

public class RoleDao {

	private static final String T_ROLES = "roles";

	private static final String C_ID = "id";
	private static final String C_ROLE_NAME = "role_name";
	private static final String C_PUBLIC_KEY = "public_key";
	private static final String C_PRIVATE_KEY = "private_key";

	public String getPublicKeyPathById(Connection conn, int id) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_PUBLIC_KEY)
				.from(T_ROLES)
				.where(C_ID + " = ?", id);
		return QueryExecutor.executeSelect(conn, builder, rs -> rs.getString(C_PUBLIC_KEY));
	}
}
