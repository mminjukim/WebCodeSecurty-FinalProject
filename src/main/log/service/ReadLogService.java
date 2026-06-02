package main.log.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import main.DocSystem;
import main.document.dao.DocumentDao;
import main.infrastructure.database.DBManager;
import main.infrastructure.key.KeyFileService;
import main.log.dao.ReadLogDao;
import main.log.dto.ReadLogDto;
import main.user.UserRole;
import main.user.dao.UserDao;
import main.user.dto.UserDto;
import main.user.service.UserService;



public class ReadLogService {
	
	private static final DateTimeFormatter LOG_TIME_FORMAT =
	        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	private static final String HASH_ALGORITHM = "SHA-256";
	private static final String INITIAL_PREV_HASH = "GENESIS";

	private final ReadLogDao readLogDao;
	private final DocumentDao documentDao;
	private final UserDao userDao;

	private final UserService userService;

	public ReadLogService(ReadLogDao readLogDao, DocumentDao documentDao, UserDao userDao, UserService userService) {
		this.readLogDao = readLogDao;
		this.documentDao = documentDao;
		this.userDao = userDao;
		this.userService = userService;
	}

	public enum Status {
		SUCCESS, FAIL
	}

	public enum FailReason {
		NONE,
		NO_PERMISSION,
		DOC_NOT_FOUND,
		SIGNATURE_INVALID,
		DECRYPT_FAIL,
		ERROR
	}
	
	/**
	 * 문서 열람 성공 시 로그 저장
	 * 
	 * @param docId	문서 id
	 */
	public void recordSuccessLog (int docId) {
		saveLog(docId, Status.SUCCESS, FailReason.NONE);
	}
	
	/**
	 * 문서 열람 실패 시 로그 저장
	 * 
	 * @param docId			문서 id
	 * @param failReason		열람 실패 이유 
	 */
	public void recordFailLog(int docId, FailReason failReason) {
		//TODO: FailReason 직접 처리는 후에 수정 analyzeFailReason(Exception e)
		saveLog(docId, Status.FAIL, failReason);
	}

	/**
	 * 문서별 로그 해시체이닝 및 열람자 전자서명을 포함한 로그 저장
	 * 
	 * @param docId			열람 문서 id
	 * @param status			문서 열람 상태 (성공, 실패)
	 * @param failReason		문서 열람 실패 이유
	 */
	public void saveLog(int docId, Status status, FailReason failReason) {
		try (Connection conn = DBManager.getConnection()) {
			// 열람자  정보 조회
			int userId = DocSystem.loggedInUser.getId();
			int roleId = DocSystem.loggedInUser.getRoleId();
			UserRole role = userService.getRoleByRoleId(roleId);

			// prevHash 조회
			String prevHash = readLogDao.getLatestLogHashByDocId(conn, docId);
			if(prevHash == null) {
				prevHash = INITIAL_PREV_HASH;
			}

			// 열람 시간 조회
			String readAt = LocalDateTime.now().format(LOG_TIME_FORMAT).toString();

			// 서명 대상 데이터 구성
			String dataToSign =
					docId + "|" + 
					userId + "|" + 
					role.name() + "|" + 
					prevHash + "|" + 
					status.name() + "|" + 
					failReason.name() + "|" + 
					readAt;

			// 사용자 개인키로 전자서명 생성
			String privateKeyPath = userDao.getPrivateKeyPathById(conn, userId);
			PrivateKey privateKey = (PrivateKey) KeyFileService.read(privateKeyPath);
			byte[] signature = sign(dataToSign, privateKey);

			// currentHash 생성 (서명 포함)
			String currentHash = hash(dataToSign + Base64.getEncoder().encodeToString(signature));

			//로그 저장
			ReadLogDto log = new ReadLogDto(docId, userId, role.name(), status.name(), failReason.name(), 
					prevHash, currentHash, signature, readAt);
			readLogDao.insertReadLog(conn, log);

		} catch (Exception e) {
			throw new IllegalStateException("로그 기록을 실패했습니다. " + e.getMessage());
		}
	}
	
	/**
	 * 문서 로그 조회 및 무결성 검증 후 출력
	 * 
	 * @param docId
	 * @return 검증 완료된 로그 출력 문자열
	 */
	public String viewLogs(int docId) {
		try (Connection conn = DBManager.getConnection()) {
			//사용자 권한 검증
			UserDto user = DocSystem.loggedInUser;
			if (userService.getRoleByRoleId(user.getRoleId()) != UserRole.ADMIN) {
				int userId = user.getId();
				validatePermission(conn, docId, userId);
			}

			// 해당 문서 로그 조회
			List<ReadLogDto> logs = readLogDao.getLogsByDocId(conn, docId);
			if (logs.isEmpty()) {
				throw new IllegalStateException("조회된 로그가 없습니다.");
			}
				
			// 로그 검증
			validateLogs(conn, logs);
			
			// 로그 반환
			String logLines = "";
			for (ReadLogDto log: logs) {
				logLines += getLogLine(conn, log) + "\n";
			}
			return logLines;
			
		} catch (SQLException e) {
			throw new IllegalStateException("로그 검증 중 오류가 발생했습니다.");
		} catch (IllegalStateException e) {
			throw new IllegalStateException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	/**
	 * 로그 무결성(해시체이닝, 전자서명, currentHash) 검증
	 * 
	 * @param logs 검증 대상 로그 리스트
	 */
	public void validateLogs(Connection conn, List<ReadLogDto> logs) {
		try {
			String expectedPrevHash = INITIAL_PREV_HASH;

			for (ReadLogDto log : logs) {
				String errorMessage = "[원인] " + "LOG: " + getLogLine(conn, log) + "\n";
				
				// prevHash 검증
				if (!log.getPrevHash().equals(expectedPrevHash)) {
					throw new IllegalStateException(errorMessage + "로그 해시 체이닝이 불일치합니다.");
				}			

				// 전자서명 검증
				String signData =
						log.getDocId() + "|" +
						log.getReaderId() + "|" +
						log.getReaderRole() + "|" +
						log.getPrevHash() + "|" +
						log.getStatus() + "|" +
						log.getFailReason() + "|" +
						log.getReadAt();
				
				String publicKeyPath =
						userDao.getPublicKeyPathById(conn, log.getReaderId());
				PublicKey publicKey =
						(PublicKey) KeyFileService.read(publicKeyPath);

				boolean signatureValid = verify(signData, log.getSignature(), publicKey);
				if (signatureValid == false) {
					throw new IllegalStateException(errorMessage + "로그 전자서명 검증에 실패했습니다.");
				}

				// current hash 검증
				String expectedCurrentHash = hash(signData + Base64.getEncoder().encodeToString(log.getSignature()));
				if (!expectedCurrentHash.equals(log.getCurrentHash())) {
					throw new IllegalStateException(errorMessage + "로그 무결성 검증을 실패했습니다.");
				}

				// 다음 체이닝 검증용
				expectedPrevHash = log.getCurrentHash();
			}
		} catch (IllegalStateException e) {
			throw new IllegalStateException("로그 검증을 실패했습니다.\n" + e.getMessage());
		} catch (SQLException e) {
			throw new IllegalStateException("로그 검증 중 오류가 발생했습니다.");
		} 		
	}
	
	 /**
	  * 전자서명 생성
	  * 
	  * @param data			서명 대상 데이터
	  * @param privateKey	서명에 사용할 개인 키
	  * @return 생성된 전자서명
	  */
    private byte[] sign(String data, PrivateKey privateKey) {
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
     * 전자서명 검증
     * 
     * @param signData		검증할 데이터
     * @param signatureData	전자서명 값
     * @param publicKey		검증에 사용할 공개 키
     * @return 서명 검증 결과
     */
    private boolean verify(String signData, byte[] signatureData, PublicKey publicKey) {
    		try {
    			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
    			signature.initVerify(publicKey);
    			signature.update(signData.getBytes(StandardCharsets.UTF_8));
    			return signature.verify(signatureData);

    		} catch (Exception e) {
    			throw new IllegalStateException("전자서명 검증 중 오류가 발생했습니다.");
    		}
    }

	/**
	 * 해시 생성
	 * @param data	해시 생성할 문자열
	 * @return 생성된 해시
	 */
	private static String hash(String data) {
		try {
			MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hashedBytes = md.digest(data.getBytes());
			return Base64.getEncoder().encodeToString(hashedBytes);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("해싱 알고리즘을 초기화할 수 없습니다.");
		} catch (Exception e) {
            throw new RuntimeException("해시 생성 중 오류가 발생했습니다.");
        }
	}

	
	/**
	 * 출력용 logLine 반환
	 * 
	 * @param conn
	 * @param log	로그 DTO 	
	 * @return 출력용 로그 문자열
	 */
	private String getLogLine(Connection conn, ReadLogDto log){
		String username;
		try {
			username = userDao.getUsernameById(conn, log.getReaderId());
		} catch (SQLException e) {
			throw new IllegalStateException("사용자 이름 조회 중 오류가 발생했습니다.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(log.getReadAt())
		  .append(" READ: ")
		  .append("user=").append(username)
		  .append(",\trole=").append(log.getReaderRole())
		  .append(",\tstatus=").append(log.getStatus())
		  .append(",\tfail=").append(log.getFailReason());
		return sb.toString();
	}
	
	/**
	 * 역할 권한 검증
	 * 
	 * @param conn
	 * @param docId  문서 id
	 * @param userId 사용자 id
	 * @return 사용자 권한 검증 결과
	 */
	private void validatePermission(Connection conn, int docId, int userId) throws SQLException {
		int uploaderId = documentDao.getUploaderIdById(conn, docId);
		if (uploaderId != userId) {
			throw new IllegalStateException("해당 문서에 대한 로그 열람 권한이 없습니다.");
		}
	}
}
