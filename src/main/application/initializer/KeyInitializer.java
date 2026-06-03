package main.application.initializer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.SQLException;

import main.application.database.DBManager;
import main.application.key.KeyFileService;
import main.application.key.KeyFileService.KeyDomain;
import main.application.key.KeyFileService.KeyType;
import main.common.exception.Error;
import main.common.exception.SystemException;
import main.common.util.QueryExecutor;
import main.common.util.SqlQueryBuilder;
import main.user.domain.UserRole;
import main.user.dto.UserDto;

/**
 * 키 초기화
 */
public class KeyInitializer {

	public static void initializeRoleKeys() {
		KeyPairGenerator keyPairGen = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("역할 대칭 키 초기화 실패");
			System.exit(1);
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
				KeyFileService.writePrivateKey(privateKeyPath, privateKey);

				SqlQueryBuilder query = new SqlQueryBuilder()
						.insertInto("roles")
						.value("role_name", role.name())
						.value("public_key", publicKeyPath)
						.value("private_key", privateKeyPath);
				
				QueryExecutor.executeInsert(conn, query);
			}

		} catch (SQLException e) {
			System.out.println("역할 대칭 키 초기화 실패");
			System.exit(1);
		}
	}

	public static UserDto initializeUserKeys(UserDto user) {
		KeyPairGenerator keyPairGen = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new SystemException(Error.KEY_GENERATE_ERROR);
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
		KeyFileService.writePrivateKey(privateKeyPath, privateKey);

		user.setPublicKey(publicKeyPath);
		user.setPrivateKey(privateKeyPath);

		return user;
	}
}
