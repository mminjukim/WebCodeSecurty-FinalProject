package main.document.service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import main.common.exception.SystemException;
import main.document.exception.DocError;

/**
 * 문서 전자서명 생성
 */
public class DocSignatureService {

	private static final int BUFFER_SIZE = 1024; // 1KB
	private static final String ALGORITHM = "SHA256withRSA";

	/**
	 * 문서의 해시 값을 작성자의 개인 키로 암호화해 전자서명 생성
	 * 
	 * @param filePath   원본 문서 경로
	 * @param privateKey 작성자 개인 키
	 * @return 문서 전자서명 값
	 */
	public byte[] createSignature(String filePath, PrivateKey privateKey) {
		try {
			Signature signature = Signature.getInstance(ALGORITHM);
			signature.initSign(privateKey);

			try (FileInputStream fis = new FileInputStream(filePath);
					BufferedInputStream bis = new BufferedInputStream(fis)) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead;
				while ((bytesRead = bis.read(buffer)) != -1) {
					signature.update(buffer, 0, bytesRead);
				}
			}

			return signature.sign();

		} catch (Exception e) {
			throw new SystemException(DocError.SIGNATURE_CREATION_FAILED);
		}
	}

	/**
	 * 전자서명을 작성자의 공개키로 검증
	 * 
	 * @param documentContent 	검증할 문서 바이트 배열
	 * @param signatureData		복호화된 전자서명
	 * @param publicKey		 	작성자 공개 키
	 * @return 전자서명 검증 결과
	 */
	public boolean verifySignature(byte[] documentContent, byte[] signatureData, PublicKey publicKey) {
		try {
			Signature signature = Signature.getInstance(ALGORITHM);
			signature.initVerify(publicKey);

			signature.update(documentContent);

			return signature.verify(signatureData);

		} catch (Exception e) {
			throw new SystemException(DocError.INVALID_DOC_SIGNATURE);
		}

	}
}
