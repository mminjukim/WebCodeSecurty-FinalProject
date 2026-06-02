package main.document.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import main.common.exception.SystemException;
import main.document.exception.DocError;

public class EnvelopeService {

	private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

	public void createEnvelope(PublicKey publicKey, SecretKey secretKey, String outputPath) {
		try {
			// Cipher 객체 초기화
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.WRAP_MODE, publicKey);

			// SecretKey 암호화
			byte[] encryptedKeyBytes = cipher.wrap(secretKey);

			// 파일로 출력
			try (FileOutputStream fos = new FileOutputStream(outputPath);
					BufferedOutputStream bos = new BufferedOutputStream(fos)) {
				bos.write(encryptedKeyBytes);
				bos.flush();
			}
		} catch (Exception e) {
			throw new SystemException(DocError.ENVELOPE_ERROR, "생성 중 오류");
		}
	}

	public SecretKey openEnvelope(PrivateKey privateKey, String keyAlgorithm, String inputPath) {
		try {
			// 전자봉투 가져오기
			byte[] encryptedKeyBytes = new byte[0];
			try (FileInputStream fis = new FileInputStream(inputPath);
					BufferedInputStream bis = new BufferedInputStream(fis)) {
				encryptedKeyBytes = bis.readAllBytes();
			}

			// Cipher 객체 초기화
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.UNWRAP_MODE, privateKey);

			// SecretKey 복호화 후 반환
			return (SecretKey)cipher.unwrap(encryptedKeyBytes, keyAlgorithm, Cipher.SECRET_KEY);

		} catch (Exception e) {
			throw new SystemException(DocError.ENVELOPE_ERROR, "개봉 중 오류");
		}
	}
}
