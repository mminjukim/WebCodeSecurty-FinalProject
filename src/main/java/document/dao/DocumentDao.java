package main.java.document.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.java.document.dto.DocumentDto;
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
}
