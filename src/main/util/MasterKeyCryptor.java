package main.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import main.application.key.MasterKeyManager;

public class MasterKeyCryptor {
	
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int IV_SIZE = 16;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	private MasterKeyCryptor() {
	}
	
	private static SecretKey getMasterKey() {
		return MasterKeyManager.getInstance().initializeMasterKey();
	}
	
	public static byte[] masterKeyEncrypt(byte[] plainBytes) {
		byte[] iv = new byte[IV_SIZE];
		SECURE_RANDOM.nextBytes(iv);
        
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, getMasterKey(), ivSpec);
			byte[] encrypted = cipher.doFinal(plainBytes);
			
			//복호화를 위해 IV + 암호문 합친 배열 반환
			byte[] ivAndEncrypted = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, ivAndEncrypted, 0, IV_SIZE);
			System.arraycopy(encrypted, 0, ivAndEncrypted, iv.length, encrypted.length);
			return ivAndEncrypted;
			
		} catch(NoSuchAlgorithmException | NoSuchPaddingException | 
				InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("암호화를 초기화할 수 없습니다.", e);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException("암호화를 처리할 수 없습니다.", e);
		}
	}
	
	public static byte[] masterKeyDecrypt(byte[] encryptedBytes) {
		if (encryptedBytes == null || encryptedBytes.length < IV_SIZE) {
			throw new IllegalArgumentException("복호화할 데이터가 너무 짧거나 유효하지 않습니다.");
		}
		
		byte[] iv = new byte[IV_SIZE];
		System.arraycopy(encryptedBytes, 0, iv, 0, iv.length);
		
		byte[] encrypted = new byte[encryptedBytes.length - IV_SIZE];
		System.arraycopy(encryptedBytes, IV_SIZE, encrypted, 0, encrypted.length);
	
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), new IvParameterSpec(iv));
			return cipher.doFinal(encrypted);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException |
				InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("복호화를 초기화할 수 없습니다.", e);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException("복호화를 처리할 수 없습니다.", e);
		}
	}
	
}
