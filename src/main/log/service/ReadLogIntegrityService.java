package main.log.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;

import main.application.key.KeyFileService;
import main.log.dto.ReadLogDto;
import main.user.dao.UserDao;

/**
 * 로그 무결성 관리
 */
public class ReadLogIntegrityService {

	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final String INITIAL_PREV_HASH = "GENESIS";

	private final UserDao userDao;

	public ReadLogIntegrityService(UserDao userDao) {
		this.userDao = userDao;
	}

	public String initialPrevHash() {
		return INITIAL_PREV_HASH;
	}

	/**
	 * 전자서명 생성
	 * 
	 * @param data       서명 대상 데이터
	 * @param privateKey 서명에 사용할 개인 키
	 * @return 생성된 전자서명
	 */
	public byte[] sign(String data, PrivateKey privateKey) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initSign(privateKey);
			signature.update(data.getBytes(StandardCharsets.UTF_8));
			return signature.sign();
		} catch (Exception e) {
			throw new IllegalStateException("전자서명 생성 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 해시 체이닝을 통한 로그 무결성 검증
	 * 
	 * @param conn
	 * @param logs 검증 대상 로그 리스트
	 */
	public void validate(java.sql.Connection conn, List<ReadLogDto> logs) {
		String expectedPrevHash = INITIAL_PREV_HASH;

		for (ReadLogDto log : logs) {
			if (!log.getPrevHash().equals(expectedPrevHash)) {
				throw new IllegalStateException("로그 해시 체이닝이 불일치합니다.");
			}

			String signData = buildSignData(log);
			try {
				PublicKey publicKey = (PublicKey) KeyFileService
						.read(userDao.getPublicKeyPathById(conn, log.getReaderId()));

				Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
				verifier.initVerify(publicKey);
				verifier.update(signData.getBytes(StandardCharsets.UTF_8));

				if (!verifier.verify(log.getSignature())) {
					throw new IllegalStateException("로그 전자서명 검증에 실패했습니다.");
				}
			} catch (Exception e) {
				throw new IllegalStateException("로그 전자서명 검증에 실패했습니다.", e);
			}

			String expectedCurrentHash = hash(signData + Base64.getEncoder().encodeToString(log.getSignature()));
			if (!expectedCurrentHash.equals(log.getCurrentHash())) {
				throw new IllegalStateException("로그 무결성 검증을 실패했습니다.");
			}

			expectedPrevHash = log.getCurrentHash();
		}
	}

	/**
	 * 해시 생성
	 * 
	 * @param data 대상 문자열
	 * @return 생성된 해시
	 */
	public String hash(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			return Base64.getEncoder().encodeToString(md.digest(data.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException("해시 생성 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 로그 정보로 한 줄짜리 문자열 생성
	 * 
	 * @param log 로그 정보
	 * @return 생성된 문자열
	 */
	public String buildSignData(ReadLogDto log) {
		return log.getDocId() + "|" + log.getReaderId() + "|" + log.getReaderRole() + "|" + log.getPrevHash() + "|"
				+ log.getStatus() + "|" + log.getFailReason() + "|" + log.getReadAt();
	}
}