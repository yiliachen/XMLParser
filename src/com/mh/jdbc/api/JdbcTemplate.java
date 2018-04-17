package com.mh.jdbc.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.mh.jdbc.util.ColumnMapRowMapper;
import com.mh.jdbc.util.DBUtil;
import com.mh.jdbc.util.Pager;
import com.mh.jdbc.util.RowMapper;
import com.mh.jdbc.util.SingleColumnRowMapper;

public class JdbcTemplate implements JdbcOperations {
	private DataSource dataSrouce = null;

	public JdbcTemplate(DataSource dataSource) {
		this.dataSrouce = dataSource;
	}
	
	public JdbcTemplate() {}

	public Connection getConnection() {
		Connection conn = null;
		try {
			if (this.dataSrouce == null) {
				conn = DBUtil.getConnection();
			} else {
				conn = this.dataSrouce.getConnection();
			}
		} catch (SQLException e) {
			// do nothing
		}
		return conn;
	}

	@Override
	public Map<String, Object> queryForMap(String sql) throws SQLException {
		return queryForObject(sql, getColumnMapRowMapper());
	}

	@Override
	public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws SQLException {
		return queryForObject(sql, args, argTypes, this.getColumnMapRowMapper());
	}

	@Override
	public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws SQLException {
		List<T> results = queryForList(sql, rowMapper);
		return DBUtil.requiredSingleResult(results);
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws SQLException {
		List<T> results = queryForList(sql, args, argTypes, rowMapper);
		return DBUtil.requiredSingleResult(results);
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> requiredType) throws SQLException {
		List<T> lsResult = this.queryForList(sql, new SingleColumnRowMapper<T>(requiredType));
		return DBUtil.requiredSingleResult(lsResult);
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws SQLException {
		List<T> lsResult = this.queryForList(sql, args, argTypes, new SingleColumnRowMapper<T>(requiredType));
		return DBUtil.requiredSingleResult(lsResult);
	}

	@Override
	public List<Map<String, Object>> queryForListMap(String sql, Object[] args, int[] argTypes) throws SQLException {
		return this.queryForList(sql, args, argTypes, this.getColumnMapRowMapper());
	}

	@Override
	public List<Map<String, Object>> queryForListMap(String sql) throws SQLException {
		return this.queryForList(sql, this.getColumnMapRowMapper());
	}

	@Override
	public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> requiredType)
			throws SQLException {
		return this.queryForList(sql, args, argTypes, new SingleColumnRowMapper<T>(requiredType));
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> requiredType) throws SQLException {
		return this.queryForList(sql, null, null, new SingleColumnRowMapper<T>(requiredType));
	}

	@Override
	public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) throws SQLException {
		return this.queryForList(sql, null, null, rowMapper);
	}

	@Override
	public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper)
			throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<T> lsResult = new ArrayList<T>();
		try {
			conn = this.getConnection();
			pstmt = prepareStatement(sql, args, argTypes, conn);
			rs = pstmt.executeQuery();
			int rowNum = 1;
			while (rs.next()) {
				lsResult.add(rowMapper.mapRow(rs, rowNum));
				rowNum++;
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DBUtil.closeAll(conn, pstmt, rs);
		}
		return lsResult;
	}

	@Override
	public boolean executeInsert(String sql, Object[] args, int[] argTypes) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = true;
		try {
			conn = this.getConnection();
			pstmt = this.prepareStatement(sql, args, argTypes, conn);
			success = pstmt.executeUpdate() == 1;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtil.closeConnAndStmt(conn, pstmt);
		}
		return success;
	}
	
	public void prepareBatch(PreparedStatement pstmt, Object[] args, int[] argTypes) throws SQLException {
		if (args != null) {
			int temp = 1;
			for (Object arg : args) {
				pstmt.setObject(temp, arg, argTypes[temp - 1]);
				temp++;
			}
			pstmt.addBatch();
		}
	}

	@Override
	public int executeUpdate(String sql, Object[] args, int[] argTypes) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		int affectRows = 0;
		try {
			conn = this.getConnection();
			pstmt = this.prepareStatement(sql, args, argTypes, conn);
			affectRows = pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtil.closeConnAndStmt(conn, pstmt);
		}
		return affectRows;
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		return this.executeUpdate(sql, null, null);
	}

	@Override
	public int executeDelete(String sql, Object[] args, int[] argTypes) throws SQLException {
		return this.executeUpdate(sql, args, argTypes);
	}

	@Override
	public int executeDelete(String sql) throws SQLException {
		return this.executeUpdate(sql, null, null);
	}

	private PreparedStatement prepareStatement(String sql, Object[] args, int[] argTypes, Connection conn)
			throws SQLException {
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(sql);
		if (args != null) {
			int temp = 1;
			for (Object arg : args) {
				pstmt.setObject(temp, arg, argTypes[temp - 1]);
				temp++;
			}
		}
		return pstmt;
	}

	private RowMapper<Map<String, Object>> getColumnMapRowMapper() {
		return new ColumnMapRowMapper();
	}

	@Override
	public int queryForInt(String sql, Object[] args, int[] argTypes) throws SQLException {
		Number number = queryForObject(sql, args, argTypes, Integer.class);
		return (number != null ? number.intValue() : 0);
	}

	@Override
	public int queryForInt(String sql) throws Exception {
		Number number = queryForObject(sql, Integer.class);
		return (number != null ? number.intValue() : 0);
	}

	@Override
	public long queryForLong(String sql, Object[] args, int[] argTypes) throws SQLException {
		Number number = queryForObject(sql, args, argTypes, Long.class);
		return (number != null ? number.longValue() : 0);
	}

	@Override
	public long queryForLong(String sql) throws Exception {
		Number number = queryForObject(sql, Long.class);
		return (number != null ? number.longValue() : 0);
	}

	@Override
	public double queryForDouble(String sql, Object[] args, int[] argTypes) throws SQLException {
		Number number = queryForObject(sql, args, argTypes, Double.class);
		return (number != null ? number.doubleValue() : 0);
	}

	@Override
	public double queryForDouble(String sql) throws Exception {
		Number number = queryForObject(sql, Double.class);
		return (number != null ? number.doubleValue() : 0);
	}

	@Override
	public <T> Pager<T> queryForPager(String selectSQL, String countSQL, Object[] args, int[] argTypes,
			RowMapper<T> rowMapper) throws SQLException {
		Pager<T> pager = new Pager<T>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			pstmt = this.prepareStatement(countSQL, args, argTypes, conn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pager.setTotalCount(new SingleColumnRowMapper<>(Integer.class).mapRow(rs, 1));
			}
			pstmt = this.prepareStatement(selectSQL, args, argTypes, conn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pager.addResultSet(rowMapper.mapRow(rs, 1));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtil.closeAll(conn, pstmt, rs);
		}
		return pager;
	}

	@Override
	public <T> Pager<T> queryForPager(String selectSQL, String countSQL, RowMapper<T> rowMapper) throws SQLException {
		return this.queryForPager(selectSQL, countSQL, null, null, rowMapper);
	}


}
