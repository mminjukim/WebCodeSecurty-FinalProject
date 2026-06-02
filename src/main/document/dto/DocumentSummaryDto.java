package main.document.dto;

public class DocumentSummaryDto {
	private int id;
	private String title;
	
	public DocumentSummaryDto (int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
}
