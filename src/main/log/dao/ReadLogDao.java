package main.log.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.common.util.QueryExecutor;
import main.common.util.SqlQueryBuilder;
import main.log.dto.ReadLogDto;


public class ReadLogDao {

	private static final String T_READ_LOG = "read_logs";
	private static final String C_ID = "id";
	private static final String C_DOC_ID = "doc_id";
	private static final String C_READER_ID = "reader_id";
	private static final String C_READER_ROLE = "reader_role";
	private static final String C_STATUS = "status";
	private static final String C_FAIL_REASON = "fail_reason";
	private static final String C_SIGNATURE = "signature";
	private static final String C_PREV_HASH = "prev_hash";
	private static final String C_CURRENT_HASH = "current_hash";
	private static final String C_READ_AT = "read_at";

	public int insertReadLog(Connection conn, ReadLogDto log) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.insertInto(T_READ_LOG)
				.value(C_DOC_ID, log.getDocId())
				.value(C_READER_ID, log.getReaderId())
				.value(C_READER_ROLE, log.getReaderRole())
				.value(C_STATUS, log.getStatus())
				.value(C_FAIL_REASON, log.getFailReason())
				.value(C_SIGNATURE, log.getSignature())
				.value(C_PREV_HASH, log.getPrevHash())
				.value(C_CURRENT_HASH, log.getCurrentHash())
				.value(C_READ_AT, log.getReadAt());
		return QueryExecutor.executeInsert(conn, builder);
	}

	public List<ReadLogDto> getLogsByDocId(Connection conn, int docId) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ID, C_DOC_ID, C_READER_ID, C_READER_ROLE, C_STATUS, C_FAIL_REASON, C_PREV_HASH, C_CURRENT_HASH, C_SIGNATURE, C_READ_AT)
				.from(T_READ_LOG)
				.where(C_DOC_ID + " = ?", docId)
				.orderBy(C_ID + " ASC");

		return QueryExecutor.executeSelect(conn, builder, rs -> {
			List<ReadLogDto> list = new ArrayList<>();
			while (rs.next()) {
				list.add(new ReadLogDto(
						rs.getInt(C_ID),
						rs.getInt(C_DOC_ID),
						rs.getInt(C_READER_ID),
						rs.getString(C_READER_ROLE),
						rs.getString(C_STATUS),
						rs.getString(C_FAIL_REASON),
						rs.getString(C_PREV_HASH),
						rs.getString(C_CURRENT_HASH),
						rs.getBytes(C_SIGNATURE),
						rs.getString(C_READ_AT)
				));
			}
			return list;
		});
	}


	public ReadLogDto getLatestLogByDocId(Connection conn, int docId) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_ID, C_DOC_ID, C_READER_ID, C_READER_ROLE, C_STATUS, C_FAIL_REASON, C_PREV_HASH, C_CURRENT_HASH, C_SIGNATURE, C_READ_AT)
				.from(T_READ_LOG)
				.where(C_DOC_ID + " = ?", docId)
				.orderBy(C_ID + " DESC")
				.limit(1);

		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
				return new ReadLogDto(
						rs.getInt(C_ID),
						rs.getInt(C_DOC_ID),
						rs.getInt(C_READER_ID),
						rs.getString(C_READER_ROLE),
						rs.getString(C_STATUS),
						rs.getString(C_FAIL_REASON),
						rs.getString(C_PREV_HASH),
						rs.getString(C_CURRENT_HASH),
						rs.getBytes(C_SIGNATURE),
						rs.getString(C_READ_AT)
				);
			}
			return null;
		});
	}
	
	public String getLatestLogHashByDocId(Connection conn, int docId) throws SQLException {
		SqlQueryBuilder builder = new SqlQueryBuilder()
				.select(C_CURRENT_HASH)
				.from(T_READ_LOG)
				.where(C_DOC_ID + " = ?", docId)
				.orderBy(C_ID + " DESC")
				.limit(1);

		return QueryExecutor.executeSelect(conn, builder, rs -> {
			if (rs.next()) {
				return rs.getString(C_CURRENT_HASH);
			}
			return null;
		});
	}

}
