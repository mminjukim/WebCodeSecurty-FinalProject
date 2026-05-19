package main.java.infrastructure.key;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MasterKeyManager {

	private static final MasterKeyManager instance = new MasterKeyManager();

	private static final String MASTER_KEY_PATH = ".keys/master.key";
	private static final String ALGORITHM = "AES";
	private static int KEY_SIZE = 256;

	private MasterKeyManager() {
	}

	public static MasterKeyManager getInstance() {
		return instance;
	}

	public SecretKey initializeMasterKey() {
		if (Files.exists(Paths.get(MASTER_KEY_PATH))) {
			return loadMasterKey();
		}
		return createAndSaveMasterKey();
	}

	private SecretKey createAndSaveMasterKey() {
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("키 생성 알고리즘을 초기화할 수 없습니다.", e);
		}
		keyGen.init(KEY_SIZE);
		SecretKey secretKey = keyGen.generateKey();
		
		File masterKeyFile = new File(MASTER_KEY_PATH);
	    File parentDir = masterKeyFile.getParentFile();
	    if (parentDir != null && !parentDir.exists()) {
	        if (!parentDir.mkdirs()) {
	            throw new RuntimeException("마스터키 디렉토리 생성중 오류가 발생했습니다.");
	        }
	    }

		try (FileOutputStream fos = new FileOutputStream(masterKeyFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(secretKey);
		} catch (IOException e) {
			throw new RuntimeException("마스터키를 저장할 수 없습니다.", e);
		}

		return secretKey;
	}

	private SecretKey loadMasterKey() {
		try (FileInputStream fis = new FileInputStream(MASTER_KEY_PATH);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			Object obj = ois.readObject();
			SecretKey secretKey = (SecretKey)obj;
			return secretKey;

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("마스터키 파일을 읽을 수 없습니다.", e);
		} catch (IOException e) {
			throw new RuntimeException("마스터키를 로드할 수 없습니다.", e);
		}
	}

}
