package main.java.document.service;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

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
			throw new IllegalStateException("문서의 전자봉투 생성 중 오류가 발생했습니다.");
		}
	}

}
