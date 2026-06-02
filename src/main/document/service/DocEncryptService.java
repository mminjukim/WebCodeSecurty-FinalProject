package main.document.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import main.common.exception.Error;
import main.common.exception.SystemException;
import main.document.exception.DocError;

public class DocEncryptService {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final int BUFFER_SIZE = 1024; // 1KB
	private static final int IV_LENGTH = 16;

	/**
	 * 특정 버퍼 크기만큼 문서를 읽고 비밀 키로 암호화해 저장
	 *
	 * @param inputFilePath  원본 문서 경로
	 * @param outputFilePath 암호화된 문서 저장 경로
	 * @param secretKey      AES-256 비밀키
	 */
	public void encryptDocument(String inputFilePath, String outputFilePath, SecretKey secretKey) {
		try {
			// 초기화 벡터 생성
			byte[] iv = new byte[IV_LENGTH];
			SecureRandom secureRandom = SecureRandom.getInstanceStrong();
			secureRandom.nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// cipher 객체 생성
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

			try (FileInputStream fis = new FileInputStream(inputFilePath);
					BufferedInputStream bis = new BufferedInputStream(fis);
					FileOutputStream fos = new FileOutputStream(outputFilePath);
					BufferedOutputStream bos = new BufferedOutputStream(fos)) {

				// 랜덤값 iv 먼저 파일에 저장
				bos.write(iv);

				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;

				// 버퍼 크기만큼 문서 읽기
				while ((bytesRead = bis.read(buffer)) != -1) {
					byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
					if (outputBytes != null) {
						bos.write(outputBytes);
					}
				}

				// 암호화된 문서 저장
				byte[] finalBytes = cipher.doFinal();
				if (finalBytes != null) {
					bos.write(finalBytes);
				}

				bos.flush();

			} catch (FileNotFoundException e) {
				throw new SystemException(DocError.DOCUMENT_NOT_FOUND);

			} catch (IOException e) {
				throw new SystemException(Error.FILE_PROCESS_ERROR);
			}
		} catch (Exception e) {
			throw new SystemException(DocError.DOCUMENT_ENCRYPTION_FAILED);
		}
	}

	/**
	 * 문서의 전자서명을 비밀 키로 암호화
	 * 
	 * @param signature 전자서명
	 * @param secretKey 비밀 키
	 * @return 암호화된 전자서명
	 */
	public byte[] encryptSignature(byte[] signature, SecretKey secretKey) {
		try {
			// 초기화 벡터 생성
			byte[] iv = new byte[IV_LENGTH];
			SecureRandom secureRandom = SecureRandom.getInstanceStrong();
			secureRandom.nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// cipher 객체 생성
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

			// 전자서명을 비밀키로 암호화
			byte[] encryptedSignature = cipher.doFinal(signature);

			// IV+암호화된 전자서명을 반환
			byte[] result = new byte[IV_LENGTH + encryptedSignature.length];
			System.arraycopy(iv, 0, result, 0, IV_LENGTH);
			System.arraycopy(encryptedSignature, 0, result, IV_LENGTH, encryptedSignature.length);
			return result;

		} catch (Exception e) {
			throw new SystemException(DocError.SIGNATURE_ENCRYPTION_FAILED);
		}
	}
}
