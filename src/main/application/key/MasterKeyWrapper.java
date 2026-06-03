package main.application.key;


import java.security.PrivateKey;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import main.common.exception.Error;
import main.common.exception.SystemException;

public class MasterKeyWrapper {
	
	private static final String WRAP_ALGORITHM = "AESWrapPad";
	private static final String KEY_ALGORITHM = "RSA";
	
	private MasterKeyWrapper() {
	}
	
	private static SecretKey getMasterKey() {
		return MasterKeyManager.getInstance().initializeMasterKey();
	}
	
	/**
	 * RSA 개인키 래핑
	 *
	 * @param privateKey 보호할 키
	 * @return 래핑된 바이트 배열
	 */
	public static byte[] wrapPrivateKey(PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(WRAP_ALGORITHM);
			cipher.init(Cipher.WRAP_MODE, getMasterKey());
			return cipher.wrap(privateKey);

		} catch (Exception e) {
			throw new SystemException(Error.KEY_PROCESS_ERROR, "개인키 래핑 실패");
		}
	}

	/**
	 * RSA 개인키 언래핑
	 *
	 * @param wrappedKey 래핑된 키 데이터
	 * @return 복원된 개인키
	 */
	public static PrivateKey unwrapPrivateKey(byte[] wrappedKey) {
		try {
			Cipher cipher = Cipher.getInstance(WRAP_ALGORITHM);
			cipher.init(Cipher.UNWRAP_MODE, getMasterKey());
			return (PrivateKey) cipher.unwrap(wrappedKey, KEY_ALGORITHM, Cipher.PRIVATE_KEY);

		} catch (Exception e) {
			throw new SystemException(Error.KEY_PROCESS_ERROR, "개인키 언래핑 실패");
		}
	}
	
}
