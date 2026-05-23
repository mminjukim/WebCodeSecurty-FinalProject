package main.java.document.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;

public class DocSignatureService {

	private static final String HASH_ALGORITHM = "SHA-256";
	private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
	private static final int BUFFER_SIZE = 1024; // 1KB


	/**
	 * 문서의 해시 값을 작성자의 개인 키로 암호화해 전자서명 생성
	 * 
	 * @param filePath   원본 문서 경로
	 * @param privateKey 작성자 개인 키
	 * @return 문서 전자서명 값
	 */
	public byte[] createSignature(String filePath, PrivateKey privateKey) {
		byte[] hash = generateFileHash(filePath);
		try {
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return cipher.doFinal(hash);
		} catch (Exception e) {
			throw new IllegalStateException("문서의 전자서명 생성 중 오류가 발생했습니다.");
		}  
	}

	/**
	 * 파일 읽고 SHA-256 해시 생성
	 * 
	 * @param filePath 파일 경로
	 * @return 생성된 해시 값 byte[]
	 */
	private byte[] generateFileHash(String filePath) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);

			try (FileInputStream fis = new FileInputStream(filePath);
					BufferedInputStream bis = new BufferedInputStream(fis)) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;
				while ((bytesRead = bis.read(buffer)) != -1) {
					md.update(buffer, 0, bytesRead);
				}
			}
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("문서의 해시 생성 중 오류가 발생했습니다.");
		} catch (IOException e) {
			throw new IllegalStateException("파일을 읽는 중 오류가 발생했습니다.");
		}
	}
}
