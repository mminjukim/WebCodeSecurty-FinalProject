package main.java.document.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.java.infrastructure.database.QueryExecutor;
import main.java.infrastructure.database.SqlQueryBuilder;

public class WhitelistDao {

	private static final String T_WHITELIST = "whitelists";
	private static final String C_DOCUMENT_ID = "document_id";
	private static final String C_ROLE_ID = "role_id";
	
	public int insertWhitelist(Connection conn, int docId, int roleId) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.insertInto(T_WHITELIST)
				.value(C_DOCUMENT_ID, docId)
				.value(C_ROLE_ID, roleId);
		return QueryExecutor.executeInsert(conn, builder);
	}
}
