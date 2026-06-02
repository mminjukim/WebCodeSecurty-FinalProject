package main.application.initializer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import main.application.database.DBManager;
import main.util.QueryExecutor;
import main.util.SqlQueryBuilder;

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
				    encrypted_content VARCHAR(255),
				    encrypted_signature BLOB,
				    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
				    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
				);
				""";
		String createWhitelists = """
				CREATE TABLE IF NOT EXISTS whitelists (
				    document_id INT NOT NULL,
				    role_id INT NOT NULL,
				    PRIMARY KEY (document_id, role_id),
				    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
				    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
				);
				""";
		String createReadLogs = """
		        CREATE TABLE IF NOT EXISTS read_logs (
		            id INT AUTO_INCREMENT PRIMARY KEY,
		            doc_id INT NOT NULL,  
		            reader_id INT NOT NULL, 
		            reader_role VARCHAR(50) NOT NULL,
		            status VARCHAR(20) NOT NULL,
		            fail_reason VARCHAR(50),
		            signature BLOB NOT NULL,
		            prev_hash VARCHAR(64) NOT NULL,
		            current_hash VARCHAR(64) NOT NULL,
		            read_at VARCHAR(30) NOT NULL		            
		        );
		        """;
		try (Connection conn = DBManager.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(createRoles);
			stmt.executeUpdate(createUsers);
			stmt.executeUpdate(createDocuments);
			stmt.executeUpdate(createWhitelists);
			stmt.executeUpdate(createReadLogs);
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
			boolean needsInitialization = QueryExecutor.executeSelect(conn, query, rs -> {
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

	/**
	 * 테이블 전부 삭제
	 */
	public static final void dropAllTables() {
		try (Connection conn = DBManager.getConnection(); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("drop table if exists read_logs;");
			stmt.executeUpdate("drop table if exists whitelists;");
			stmt.executeUpdate("drop table if exists documents;");
			stmt.executeUpdate("drop table if exists users;");
			stmt.executeUpdate("drop table if exists roles;");
		} catch (SQLException e) {
			throw new IllegalStateException("데이터베이스 테이블 삭제에 실패했습니다.", e);
		}
	}
}
