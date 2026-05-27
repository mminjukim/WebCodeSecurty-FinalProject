package main.java.document.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.java.document.dto.DocumentDto;
import main.java.document.dto.DocumentSummaryDto;
import main.java.infrastructure.database.QueryExecutor;
import main.java.infrastructure.database.SqlQueryBuilder;

public class DocumentDao {

	private static final String T_DOCUMENT = "documents";
	private static final String C_ID = "id";
	private static final String C_TITLE = "title";
	private static final String C_UPLOADER_ID = "uploader_id";
	private static final String C_ENCRYPTED_CONTENT = "encrypted_content";
	private static final String C_SIGNATURE = "encrypted_signature";
	private static final String C_CREATED_AT = "created_at";
	
	public int insertDocument(Connection conn, DocumentDto doc) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.insertInto(T_DOCUMENT)
				.value(C_TITLE, doc.getTitle())
				.value(C_UPLOADER_ID, doc.getUploaderId())
				.value(C_ENCRYPTED_CONTENT, doc.getContentPath())
				.value(C_SIGNATURE, doc.getSignature());
		return QueryExecutor.executeInsert(conn, builder);
	}
	
	public List<DocumentSummaryDto> getAllDocumentSummaries(Connection conn) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ID, C_TITLE)
				.from(T_DOCUMENT);
		return QueryExecutor.executeSelect(conn, builder, rs -> {
			List<DocumentSummaryDto> list = new ArrayList<>();
			while (rs.next()) {
				list.add(new DocumentSummaryDto(
						rs.getInt(C_ID),
						rs.getString(C_TITLE)
				));
			}		
			return list;
		});
	}
	
	public DocumentDto getDocumentById(Connection conn, int id) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ID, C_TITLE, C_UPLOADER_ID, C_ENCRYPTED_CONTENT, C_SIGNATURE, C_CREATED_AT)
				.from(T_DOCUMENT)
				.where(C_ID + " = ?", id);
		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
				return new DocumentDto(
						rs.getString(C_TITLE),
						rs.getInt(C_UPLOADER_ID),
						rs.getString(C_ENCRYPTED_CONTENT),
						rs.getBytes(C_SIGNATURE)			
				);
			}
			return null;
		});
	}
	
	public int getUploaderIdById(Connection conn, int id) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_UPLOADER_ID)
				.from(T_DOCUMENT)
				.where(C_ID + " = ?", id);
		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
				return rs.getInt(C_UPLOADER_ID);
			}
			return null;
		});
	}
	
}
