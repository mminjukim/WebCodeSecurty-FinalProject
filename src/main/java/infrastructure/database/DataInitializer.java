package main.java.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import main.java.infrastructure.key.KeyInitializer;

/**
 * 데이터 초기화
 */
public class DataInitializer {

	private DataInitializer() {
	}

	/**
	 * 역할, 사용자, 문서 테이블이 존재하지 않으면 생성
	 */
	public static final void createTables() {
		String createRoles = """
				CREATE TABLE IF NOT EXISTS roles (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				   	role_name VARCHAR(50) NOT NULL UNIQUE,
				    public_key VARCHAR(255) NOT NULL,
				    private_key VARCHAR(255) NOT NULL
				);
				""";
		String createUsers = """
				CREATE TABLE IF NOT EXISTS users (
					id INT AUTO_INCREMENT PRIMARY KEY,
				    username VARCHAR(50) NOT NULL UNIQUE,
				    password_hash VARCHAR(255) NOT NULL,
				    public_key VARCHAR(255) NOT NULL,
				    private_key VARCHAR(255) NOT NULL,
				    role_id INT NOT NULL,
				    FOREIGN KEY (role_id) REFERENCES roles(id)
				);
				""";
		String createDocuments = """
				CREATE TABLE IF NOT EXISTS documents (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				    title VARCHAR(255) NOT NULL,
				    uploader_id INT NOT NULL,
				    role_id INT NOT NULL,
				    encrypted_content VARCHAR(255) NOT NULL,
				    secret_key VARCHAR(255) NOT NULL,
				    encrypted_signature BLOB NOT NULL,
				    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
				    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
				    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
				);
				""";
		try (Connection conn = DBManager.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(createRoles);
			stmt.executeUpdate(createUsers);
			stmt.executeUpdate(createDocuments);
		} catch (SQLException e) {
			throw new IllegalStateException("데이터베이스 테이블 생성 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 역할 데이터를 초기화
	 */
	public static final void initializeRoles() {
		SqlQueryBuilder query = new SqlQueryBuilder().select("count(*)").from("roles");
		try (Connection conn = DBManager.getConnection()) {
			boolean needsInitialization = QueryExecutor.executeQuery(conn, query, rs -> {
				if (rs.next()) {
					return rs.getInt(1) == 0;
				}
				return false;
			});
			if (needsInitialization) {
				KeyInitializer.initializeRoleKeys();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("역할 데이터 초기화 중 오류가 발생했습니다.", e);
		}
	}

}
