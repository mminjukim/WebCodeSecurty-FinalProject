package main.document.dao;

import java.sql.Connection;
import java.sql.SQLException;

import main.util.QueryExecutor;
import main.util.SqlQueryBuilder;

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
	
	public boolean existsByDocumentIdAndRoleId(Connection conn, int docId, int roleId) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
  				.select("1")
  				.from(T_WHITELIST)
  				.where(C_DOCUMENT_ID + " = ?", docId)
  				.where(C_ROLE_ID + " = ?", roleId);
			
	    return QueryExecutor.executeSelect(conn, builder, rs -> rs.next());
	}
}
