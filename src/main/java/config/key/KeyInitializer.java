package main.java.config.key;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import main.java.config.database.DBManager;
import main.java.user.UserRole;

/**
 * 키 초기화
 */
public class KeyInitializer {

	public static void initializeRoleKeys() {
		KeyPairGenerator keyPairGen;

		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		}

		String sql = "INSERT INTO roles (role_name, public_key, encrypted_private_key) VALUES (?, ?, ?)";
		try (Connection conn = DBManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			for (UserRole role : UserRole.values()) {
				KeyPair keyPair = keyPairGen.generateKeyPair();
				
				byte[] publicKey = keyPair.getPublic().getEncoded();
				byte[] privateKey = keyPair.getPrivate().getEncoded();
				//TODO: 개인키를 AES 마스터키로 암호화 추가
				
				pstmt.setString(1, role.name());
				pstmt.setBytes(2, publicKey);
				pstmt.setBytes(3, privateKey); // encoded_privateKey로 변경 필요
				
				pstmt.addBatch();				
			}
			
			pstmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
