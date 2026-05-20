package main.java.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.infrastructure.database.QueryExecutor;
import main.java.infrastructure.database.SqlQueryBuilder;
import main.java.user.dto.UserDto;

public class UserDao {

	private static final String TABLE_USERS = "users";

	private static final String COL_ID = "id";
	private static final String COL_USERNAME = "username";
	private static final String COL_PASSWORD_HASH = "password_hash";
	private static final String COL_PUBLIC_KEY = "public_key";
	private static final String COL_PRIVATE_KEY = "private_key";
	private static final String COL_ROLE_ID = "role_id";

	public int insertUser(Connection conn, UserDto user) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.insertInto(TABLE_USERS)
				.value(COL_USERNAME, user.getUsername())
				.value(COL_PASSWORD_HASH, user.getPassword())
				.value(COL_PUBLIC_KEY, user.getPublicKey())
				.value(COL_PRIVATE_KEY, user.getPrivateKey())
				.value(COL_ROLE_ID, user.getRoleId());
		return QueryExecutor.executeUpdate(conn, builder);
	}

	public UserDto getUserByUsername(Connection conn, String username) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(COL_ID, COL_USERNAME, COL_PASSWORD_HASH, COL_ROLE_ID, COL_PUBLIC_KEY, COL_PRIVATE_KEY)
				.from(TABLE_USERS)
				.where(COL_USERNAME + " = ?", username);

		try (ResultSet rs = QueryExecutor.executeQuery(conn, builder)) {
			if (rs.next()) {
				return new UserDto(
						rs.getLong(COL_ID), 
						rs.getString(COL_USERNAME), 
						rs.getString(COL_PASSWORD_HASH),
						rs.getString(COL_PUBLIC_KEY), 
						rs.getString(COL_PRIVATE_KEY), 
						rs.getInt(COL_ROLE_ID)
				);
			}
		}
		return null;
	}

	public boolean isUsernameExist(Connection conn, String username) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select("1")
				.from(TABLE_USERS)
				.where(COL_USERNAME + " = ?", username);

		try (ResultSet rs = QueryExecutor.executeQuery(conn, builder)) {
			return rs.next();
		}
	}
}
