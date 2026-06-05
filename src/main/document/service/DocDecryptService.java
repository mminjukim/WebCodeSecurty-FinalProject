package main.document.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import main.common.exception.Error;
import main.common.exception.SystemException;
import main.document.exception.DocError;

public class DocDecryptService {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final int BUFFER_SIZE = 1024; // 1KB
	private static final int IV_LENGTH = 16;

	/**
	 * 암호화된 문서를 읽어 비밀 키로 복호화해 바이트 배열로 반환
	 *
	 * @param inputFilePath  암호화된 문서 경로
	 * @param secretKey      AES-256 비밀키
	 * @return 복호화된 문서의 바이트 배열
	 */
	public byte[] decryptDocument(String inputFilePath, SecretKey secretKey) {
		try (FileInputStream fis = new FileInputStream(inputFilePath);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

			// 초기화 벡터 추출
			byte[] iv = new byte[IV_LENGTH];
			int ivBytesRead = bis.read(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// 파일 크기 검증
			if (ivBytesRead != IV_LENGTH) {
				throw new SystemException(DocError.WRONGLY_ENCRYPTED_DOC);
			}

			// cipher 객체 생성 및 초기화
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

			// 버퍼 크기만큼 암호화된 문서 읽기
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;

			// 버퍼 크기만큼 암호화된 문서 읽기
			while ((bytesRead = bis.read(buffer)) != -1) {
				byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
				if (outputBytes != null) {
					bos.write(outputBytes);
				}
			}

			// 문서 복호화
			byte[] finalBytes = cipher.doFinal();
			if (finalBytes != null) {
				bos.write(finalBytes);
			}

			// 바이트 배열로 반환
			return bos.toByteArray();

		} catch (FileNotFoundException e) {
			throw new SystemException(DocError.DOCUMENT_NOT_FOUND);
		} catch (IOException e) {
			throw new SystemException(Error.FILE_PROCESS_ERROR, "암호화 문서 파일 처리 실패");
		} catch (Exception e) {
			throw new SystemException(DocError.DOCUMENT_DECRYPTION_FAILED);
		}
	}

	/**
	 * 문서의 암호화된 전자서명을 비밀 키로 복호화
	 * 
	 * @param encryptedSignature 암호화된전자서명
	 * @param secretKey          비밀 키
	 * @return 복호화된 전자서명
	 */
	public byte[] decryptSignature(byte[] encryptedSignature, SecretKey secretKey) {
		try {
			// 입력값 검증
			if (encryptedSignature == null || encryptedSignature.length < IV_LENGTH) {
				throw new SystemException(DocError.WRONGLY_ENCRYPTED_SIG);
			}

			// 초기화 벡터 추출
			byte[] iv = new byte[IV_LENGTH];
			System.arraycopy(encryptedSignature, 0, iv, 0, IV_LENGTH);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// 암호문 추출
			int encryptedDataLength = encryptedSignature.length - IV_LENGTH;
			byte[] encryptedData = new byte[encryptedDataLength];
			System.arraycopy(encryptedSignature, IV_LENGTH, encryptedData, 0, encryptedDataLength);

			// cipher 객체 생성 및 초기화
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

			// 복호화 수행 및 반환
			return cipher.doFinal(encryptedData);

		} catch (Exception e) {
			throw new SystemException(DocError.SIGNATURE_DECRYPTION_FAILED);
		}
	}

}
