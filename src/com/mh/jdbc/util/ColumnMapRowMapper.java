package com.mh.jdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;


public class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String key = getColumnKey(DBUtil.lookupColumnName(rsmd, i));
			Object obj = getColumnValue(rs, i);
			mapOfColValues.put(key, obj);
		}
		return mapOfColValues;
	}

	private Object getColumnValue(ResultSet rs, int i)  throws SQLException {
		return DBUtil.getResultSetValue(rs, i);
	}

	private String getColumnKey(String lookupColumnName) {
		return lookupColumnName.toUpperCase();
	}

	private Map<String, Object> createColumnMap(int columnCount) {
		return new CaseInsensitiveLinkedHashMap(columnCount);
	}

}
