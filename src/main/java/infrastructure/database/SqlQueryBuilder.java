package main.java.infrastructure.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlQueryBuilder {

	private enum QueryType {
		SELECT, INSERT, UPDATE
	}

	private QueryType queryType;
	private String table = "";

	// 구문들
	private List<String> selectColumns = new ArrayList<>();
	private List<String> insertColumns = new ArrayList<>();
	private List<String> setClauses = new ArrayList<>();
	private List<String> whereConditions = new ArrayList<>();

	// 파라미터들
	private List<Object> insertParameters = new ArrayList<>();
	private List<Object> setParameters = new ArrayList<>();
	private List<Object> whereParameters = new ArrayList<>();

	/**
	 * select 할 필드 지정
	 * 
	 * @param columns
	 */
	public SqlQueryBuilder select(String... columns) {
		this.queryType = QueryType.SELECT;
		if (columns == null || columns.length == 0) {
			this.selectColumns.add("*");
		} else {
			Collections.addAll(this.selectColumns, columns);
		}
		return this;
	}

	/**
	 * from 테이블 지정
	 * 
	 * @param table
	 */
	public SqlQueryBuilder from(String table) {
		this.table = table;
		return this;
	}

	/**
	 * insert into 할 테이블 지정
	 * 
	 * @param table
	 */
	public SqlQueryBuilder insertInto(String table) {
		this.queryType = QueryType.INSERT;
		this.table = table;
		return this;
	}

	/**
	 * insert 시 각 컬럼별 값 지정
	 * 
	 * @param column
	 * @param value
	 */
	public SqlQueryBuilder value(String column, Object value) {
		this.insertColumns.add(column);
		this.insertParameters.add(value);
		return this;
	}

	/**
	 * update 할 테이블 지정
	 * 
	 * @param table
	 */
	public SqlQueryBuilder update(String table) {
		this.queryType = QueryType.UPDATE;
		this.table = table;
		return this;
	}

	/**
	 * update 할 컬럼과 값 지정
	 * 
	 * @param column
	 * @param value
	 */
	public SqlQueryBuilder set(String column, Object value) {
		this.setClauses.add(column + " = ?");
		this.setParameters.add(value);
		return this;
	}

	/**
	 * where 조건 지정
	 * 
	 * <pre>{@code
	 * builder.where("username = ?", "dwu").where("role_id = ?", 1);
	 * }</pre>
	 * 
	 * @param condition ?를 포함한 where 절 (e.g. "id = ?")
	 * @param params    =의 오른쪽 ?에 들어갈 값
	 */
	public SqlQueryBuilder where(String condition, Object... params) {
		this.whereConditions.add(condition);
		Collections.addAll(this.whereParameters, params);
		return this;
	}

	// 쿼리 합치기
	public String getQuery() {
		if (this.queryType == QueryType.SELECT) {
			return buildSelectQuery();
		} else if (this.queryType == QueryType.INSERT) {
			return buildInsertQuery();
		} else if (this.queryType == QueryType.UPDATE) {
			return buildUpdateQuery();
		}
		throw new IllegalStateException("쿼리 타입이 지정되지 않음");
	}

	private String buildSelectQuery() {
		String query = "SELECT " + String.join(", ", selectColumns) + " FROM " + table;
		if (whereConditions.isEmpty() == false) {
			query += " WHERE " + String.join(" AND ", whereConditions);
		}
		return query;
	}

	private String buildInsertQuery() {
		List<String> placeholders = new ArrayList<>();
		for (int i = 0; i < insertColumns.size(); i++) {
			placeholders.add("?");
		}
		return "INSERT INTO " + table + " (" + String.join(", ", insertColumns) + ") " + "VALUES ("
				+ String.join(", ", placeholders) + ")";
	}

	private String buildUpdateQuery() {
		String query = "UPDATE " + table + " SET " + String.join(", ", setClauses);
		if (!whereConditions.isEmpty()) {
			query += " WHERE " + String.join(" AND ", whereConditions);
		}
		return query;
	}

	// 파라미터 합치기
	public List<Object> getParameters() {
		List<Object> allParameters = new ArrayList<>();

		if (this.queryType == QueryType.SELECT) {
			allParameters.addAll(whereParameters);
		} else if (this.queryType == QueryType.INSERT) {
			allParameters.addAll(insertParameters);
		} else if (this.queryType == QueryType.UPDATE) {
			allParameters.addAll(setParameters);
			allParameters.addAll(whereParameters);
		}

		return allParameters;
	}
}
