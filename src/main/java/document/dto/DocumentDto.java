package main.java.document.dto;

import java.time.LocalDateTime;

public class DocumentDto {
	private int id;
	private String title;
	private int uploaderId;
	private String contentPath;
	private String secretKeyPath;
	private byte[] encryptedSignature;
	private LocalDateTime createdAt;

	public DocumentDto(String title, int uploaderId, 
			String contentPath, String secretKeyPath, byte[] signature) {
		this.title = title;
		this.uploaderId = uploaderId;
		this.contentPath = contentPath; 
		this.secretKeyPath = secretKeyPath;
		this.encryptedSignature = signature;
	}

	public String getTitle() {
		return title;
	}

	public int getUploaderId() {
		return uploaderId;
	}

	public String getContentPath() {
		return contentPath;
	}

	public String getSecretKeyPath() {
		return secretKeyPath;
	}

	public byte[] getSignature() {
		return encryptedSignature;
	}
	
	
}
