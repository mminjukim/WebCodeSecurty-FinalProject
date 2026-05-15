package main.java.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.config.database.QueryExecutor;
import main.java.config.database.SqlQueryBuilder;

public class UserDao {

	private static final UserDao instance = new UserDao();

	private UserDao() {
	}

	public static UserDao getInstance() {
		return instance;
	}

	public int insertUser(Connection conn, UserDto user) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.insertInto("users")
				.value("username", user.getUsername())
				.value("password_hash", user.getPassword())
				.value("role_id", user.getRoleId());
		return QueryExecutor.executeUpdate(conn, builder);
	}
	
	public UserDto getUserByUsername(Connection conn, String username) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
            .select("id", "username", "password_hash", "role_id")
            .from("users")
            .where("username = ?", username);

        try (ResultSet rs = QueryExecutor.executeQuery(conn, builder)) {
            if (rs.next()) {
                UserDto user = new UserDto(
                    rs.getString("username"),
                    rs.getBytes("password_hash"),
                    rs.getInt("role_id")
                );
                return user;
            }
        }
        return null;
    }
}
