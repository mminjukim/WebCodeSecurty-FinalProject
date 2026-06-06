package main.log.domain;

/**
 * 열람 실패 이유
 */
public enum FailReason {
	NONE, NO_PERMISSION, DOC_NOT_FOUND, SIGNATURE_INVALID, DECRYPT_FAIL, ERROR
}