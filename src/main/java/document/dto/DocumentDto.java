package main.java.document.dto;

import java.time.LocalDateTime;

public class DocumentDto {
	private int id;
	private String title;
	private int uploaderId;
	private String contentPath;
	private byte[] encryptedSignature;
	private LocalDateTime createdAt;

	public DocumentDto(String title, int uploaderId, String contentPath, byte[] signature) {
		this.title = title;
		this.uploaderId = uploaderId;
		this.contentPath = contentPath; 
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

	public byte[] getSignature() {
		return encryptedSignature;
	}
}
