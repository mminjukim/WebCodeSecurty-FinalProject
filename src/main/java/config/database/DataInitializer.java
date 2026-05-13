package main.java.config.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import main.java.config.key.KeyInitializer;

/**
 * 데이터 초기화
 */
public class DataInitializer {

	/**
	 * 역할, 사용자, 문서 테이블이 존재하지 않으면 생성
	 */
	public static final void createTables() {
		String createRoles = """
				CREATE TABLE IF NOT EXISTS roles (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				   	role_name VARCHAR(50) NOT NULL UNIQUE,
				    public_key BLOB NOT NULL,
				    encrypted_private_key BLOB NOT NULL
				);
				""";
		String createUsers = """
				CREATE TABLE IF NOT EXISTS users (
					id INT AUTO_INCREMENT PRIMARY KEY,
				    username VARCHAR(50) NOT NULL UNIQUE,
				    password_hash VARCHAR(255) NOT NULL,
				    public_key BLOB NOT NULL,
				    encrypted_private_key BLOB NOT NULL,
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
				    encrypted_content LONGBLOB NOT NULL,
				    encrypted_secret_key BLOB NOT NULL,
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
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * 역할 데이터를 초기화
	 */
	public static final void initializeRoles() {
		KeyInitializer.initializeRoleKeys();
	}

}
