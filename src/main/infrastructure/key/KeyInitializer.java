package main.infrastructure.key;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import main.infrastructure.database.DBManager;
import main.infrastructure.database.QueryExecutor;
import main.infrastructure.database.SqlQueryBuilder;
import main.infrastructure.key.KeyFileService.KeyDomain;
import main.infrastructure.key.KeyFileService.KeyType;
import main.user.UserRole;
import main.user.dto.UserDto;
import main.util.MasterKeyCryptor;

/**
 * 키 초기화
 */
public class KeyInitializer {

	public static void initializeRoleKeys() {
		KeyPairGenerator keyPairGen = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[오류] 역할 키 생성 중 오류 발생");
			return;
		}
		keyPairGen.initialize(2048);
		
		try (Connection conn = DBManager.getConnection()) {

			for (UserRole role : UserRole.values()) {
				KeyPair keyPair = keyPairGen.generateKeyPair();

				PublicKey publicKey = keyPair.getPublic();
				PrivateKey privateKey = keyPair.getPrivate();
				
				String publicKeyPath = KeyFileService.buildKeyPath(
						KeyDomain.ROLE, KeyType.PUBLIC, role.name()
				);
				String privateKeyPath = KeyFileService.buildKeyPath(
						KeyDomain.ROLE, KeyType.PRIVATE, role.name()
				);
				
				KeyFileService.write(publicKeyPath, publicKey);
				KeyFileService.write(privateKeyPath, privateKey);

				SqlQueryBuilder query = new SqlQueryBuilder()
						.insertInto("roles")
						.value("role_name", role.name())
						.value("public_key", publicKeyPath)
						.value("private_key", privateKeyPath);
				
				QueryExecutor.executeInsert(conn, query);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static UserDto initializeUserKeys(UserDto user) {
		KeyPairGenerator keyPairGen = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[오류] 사용자 키 생성 중 오류 발생");
			return user;
		}
		keyPairGen.initialize(2048);

		KeyPair keyPair = keyPairGen.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		String publicKeyPath = KeyFileService.buildKeyPath(
				KeyDomain.USER, KeyType.PUBLIC, user.getUsername()
		);
		String privateKeyPath = KeyFileService.buildKeyPath(
				KeyDomain.USER, KeyType.PRIVATE, user.getUsername()
		);

		KeyFileService.write(publicKeyPath, publicKey);
		KeyFileService.write(privateKeyPath, privateKey);

		user.setPublicKey(publicKeyPath);
		user.setPrivateKey(privateKeyPath);

		return user;
	}
}
