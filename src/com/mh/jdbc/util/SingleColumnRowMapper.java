package com.mh.jdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class SingleColumnRowMapper<T> implements RowMapper<T> {
	private Class<T> requiredType;
	
	public SingleColumnRowMapper(Class<T> requiredType) {
		this.requiredType = requiredType;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int nrOfColumns = rsmd.getColumnCount(); 
		if (nrOfColumns != 1) {
			throw new SQLException("JdbcTemplate::Incorrect column count: expected " + 1 + ", actual " + nrOfColumns);
		}
		return (T) this.getColumnValue(rs, 1, requiredType);
	}
	
	private Object getColumnValue(ResultSet rs, int index, Class<T> requiredType) throws SQLException {
		return DBUtil.getResultSetValue(rs, index, requiredType);
	}

}
