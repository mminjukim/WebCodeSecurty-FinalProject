package main.log.dto;

import java.util.Arrays;

public class ReadLogDto {
	private int id;                 
	private int docId;             
	private int readerId;            
	private String readerRole;        
	private String status;           
	private String failReason;      
	private String prevHash;        
	private String currentHash;     
	private byte[] signature;       
	private String readAt;    

	public ReadLogDto(int id, int docId, int readerId, String readerRole, String status, String failReason,
			String prevHash, String currentHash, byte[] signature, String readAt) {
		this.id = id;
		this.docId = docId;
		this.readerId = readerId;
		this.readerRole = readerRole;
		this.status = status;
		this.failReason = failReason;
		this.prevHash = prevHash;
		this.currentHash = currentHash;
		this.signature = signature;
		this.readAt = readAt;
	}
	
	public ReadLogDto(int docId, int readerId, String readerRole, String status, String failReason,
			String prevHash, String currentHash, byte[] signature, String readAt) {
		this.docId = docId;
		this.readerId = readerId;
		this.readerRole = readerRole;
		this.status = status;
		this.failReason = failReason;
		this.prevHash = prevHash;
		this.currentHash = currentHash;
		this.signature = signature;
		this.readAt = readAt;
	}

	public int getId() {
		return id;
	}

	public int getDocId() {
		return docId;
	}

	public int getReaderId() {
		return readerId;
	}

	public String getReaderRole() {
		return readerRole;
	}

	public String getStatus() {
		return status;
	}

	public String getFailReason() {
		return failReason;
	}

	public String getPrevHash() {
		return prevHash;
	}

	public String getCurrentHash() {
		return currentHash;
	}
	
	public byte[] getSignature() {
	    return Arrays.copyOf(signature, signature.length);
	}

	public String getReadAt() {
		return readAt;
	}


}
