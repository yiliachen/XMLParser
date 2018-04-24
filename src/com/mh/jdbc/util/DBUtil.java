package com.mh.jdbc.util;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;

public class DBUtil {
    public static void initConfig(String path){
    	try {
			Configure.initConfig(path);
			Class.forName(Configure.getDriverClassName());
		} catch (Exception e) {
			// do nothing
		}
    }
	
	public static DataSource getDataSource(){
		DruidDataSource ds = new DruidDataSource();
		ds.setUsername(Configure.getUsername());
		ds.setUrl(Configure.getUrl());
		ds.setPassword(Configure.getPassword());
		ds.setDriverClassName(Configure.getDriverClassName());
		ds.setInitialSize(5);
		ds.setMinIdle(1);
		ds.setMaxActive(5);
		ds.setMinEvictableIdleTimeMillis(50000);
		ds.setTestWhileIdle(false);
		ds.setTestOnReturn(false);
		ds.setTestOnBorrow(false);
		return ds;
	}

	public static Connection getConnection() throws SQLException {
		return getConnection(true);
	}

	public static Connection getConnectionWithAutoCommit() throws SQLException {
		return getConnection(true);
	}

	public static Connection getConnectionWithMannualCommit() throws SQLException {
		return getConnection(false);
	}

	public static Connection getConnection(boolean isAutoCommit) throws SQLException {
		Connection conn = null;
		conn = DriverManager.getConnection(Configure.getUrl(), Configure.getUsername(), Configure.getPassword());
		conn.setAutoCommit(isAutoCommit);
		return conn;
	}

	public static void closeConn(Connection conn) throws SQLException {
		if (conn != null) {
			conn.close();
			conn = null;
		}
	}

	public static void closeStmt(Statement stmt) throws SQLException {
		if (stmt != null) {
			stmt.close();
			stmt = null;
		}
	}

	public static void closeRs(ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
			rs = null;
		}
	}

	public static void closeConnAndStmt(Connection conn, Statement stmt) throws SQLException {
		closeStmt(stmt);
		closeConn(conn);
	}

	public static void closeAll(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
		closeRs(rs);
		closeConnAndStmt(conn, stmt);
	}

	public static <T> Object getResultSetValue(ResultSet rs, int index, Class<T> requiredType) throws SQLException {
		if (requiredType == null) {
			return getResultSetValue(rs, index);
		}

		Object value = null;
		boolean wasNullCheck = false;

		if (String.class.equals(requiredType)) {
			value = rs.getString(index);
		} else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
			value = rs.getBoolean(index);
			wasNullCheck = true;
		} else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
			value = rs.getByte(index);
			wasNullCheck = true;
		} else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
			value = rs.getShort(index);
			wasNullCheck = true;
		} else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
			value = rs.getInt(index);
			wasNullCheck = true;
		} else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
			value = rs.getLong(index);
			wasNullCheck = true;
		} else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
			value = rs.getFloat(index);
			wasNullCheck = true;
		} else if (double.class.equals(requiredType) || Double.class.equals(requiredType)
				|| Number.class.equals(requiredType)) {
			value = rs.getDouble(index);
			wasNullCheck = true;
		} else if (byte[].class.equals(requiredType)) {
			value = rs.getBytes(index);
		} else if (java.sql.Date.class.equals(requiredType)) {
			value = rs.getDate(index);
		} else if (java.sql.Time.class.equals(requiredType)) {
			value = rs.getTime(index);
		} else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
			value = rs.getTimestamp(index);
		} else if (BigDecimal.class.equals(requiredType)) {
			value = rs.getBigDecimal(index);
		} else if (Blob.class.equals(requiredType)) {
			value = rs.getBlob(index);
		} else if (Clob.class.equals(requiredType)) {
			value = rs.getClob(index);
		} else {
			value = getResultSetValue(rs, index);
		}

		if (wasNullCheck && value != null && rs.wasNull()) {
			value = null;
		}
		return value;
	}

	public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		} else if (obj instanceof Clob) {
			obj = rs.getString(index);
		} else if (className != null
				&& ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(index);
		} else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			} else {
				obj = rs.getDate(index);
			}
		} else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

	public static <T> T requiredSingleResult(Collection<T> results) throws SQLException {
		int size = (results != null ? results.size() : 0);
		if (size != 1) {
			throw new SQLException("JdbcTemplate::Incorrect result size: expected 1, actual " + size);
		}
		return results.iterator().next();
	}

	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

}
