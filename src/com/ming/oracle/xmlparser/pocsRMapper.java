package com.ming.oracle.xmlparser;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mh.jdbc.util.RowMapper;

public class pocsRMapper implements RowMapper<pocsRMapper>{
	private String mail;
	private String product;

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	@Override
	public pocsRMapper mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		pocsRMapper prm = new pocsRMapper();
		prm.setMail(rs.getString(1));
		prm.setProduct(rs.getString(2));
		return prm;
	}

}
