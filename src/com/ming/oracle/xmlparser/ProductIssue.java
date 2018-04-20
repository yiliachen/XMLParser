package com.ming.oracle.xmlparser;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mh.jdbc.util.HTMLTableBuilder;
import com.mh.jdbc.util.RowMapper;

import de.vandermeer.asciitable.AsciiTable;

public class ProductIssue implements RowMapper<ProductIssue> {
	private String branch;
//	private String mail;
//	private String product;
	private String NullSGUID;
	private String Duplicated;
	private String BULKSEEDMissing;
//	private String ERROR_DUPSGUID;
	private String ERROR_SGUIDDIFF;
	private String IndexMissing;
	
	public static AsciiTable getHeader(AsciiTable tab ){
		tab.addRow("Branch","product","Duplicated","Null SGUID","Bulk Sql Missing", "sguid diff for same rowkey accross branches", "tableFile does not have sguid indexed");
		return tab;
	}
	
	public static HTMLTableBuilder setHtmlHeader(HTMLTableBuilder htb){
		htb.addTableHeader("Branch","Duplicated","Null SGUID","Bulk Sql Missing", "sguid diff for same rowkey accross branches", "tableFile does not have sguid indexed");
		return htb;
	}
	
	public HTMLTableBuilder setHtmlRow(HTMLTableBuilder htb){
		htb.addRowValues(this.branch, 
				this.Duplicated, 
				this.NullSGUID, 
				this.BULKSEEDMissing, 
				this.ERROR_SGUIDDIFF, 
				this.IndexMissing);
		return htb;
	}
	
	public AsciiTable toRow(AsciiTable tab){
		tab.addRow(this.branch, 
				this.Duplicated, 
				this.NullSGUID, 
				this.BULKSEEDMissing, 
				this.ERROR_SGUIDDIFF, 
				this.IndexMissing);
		return tab;
	}
	
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

//	public String getMail() {
//		return mail;
//	}

//	public void setMail(String mail) {
//		if(mail == null || mail.length() == 0 )
//			mail = "ming.c.chen@oracle.com";
//		this.mail = mail;
//	}

//	public String getProduct() {
//		return product;
//	}
//
//	public void setProduct(String product) {
//		this.product = product;
//	}

	public String getNullSGUID() {
		return NullSGUID;
	}

	public void setNullSGUID(String nullSGUID) {
		NullSGUID = nullSGUID;
	}

	public String getDuplicated() {
		return Duplicated;
	}

	public void setDuplicated(String duplicated) {
		Duplicated = duplicated;
	}

	public String getBULKSEEDMissing() {
		return BULKSEEDMissing;
	}

	public void setBULKSEEDMissing(String bULKSEEDMissing) {
		BULKSEEDMissing = bULKSEEDMissing;
	}

//	public String getERROR_DUPSGUID() {
//		return ERROR_DUPSGUID;
//	}
//
//	public void setERROR_DUPSGUID(String eRROR_DUPSGUID) {
//		ERROR_DUPSGUID = eRROR_DUPSGUID;
//	}

	public String getERROR_SGUIDDIFF() {
		return ERROR_SGUIDDIFF;
	}

	public void setERROR_SGUIDDIFF(String eRROR_SGUIDDIFF) {
		ERROR_SGUIDDIFF = eRROR_SGUIDDIFF;
	}

	public String getIndexMissing() {
		return IndexMissing;
	}

	public void setIndexMissing(String indexMissing) {
		IndexMissing = indexMissing;
	}

	@Override
	public ProductIssue mapRow(ResultSet rs, int rowNum) throws SQLException {
		ProductIssue pi = new ProductIssue();
		pi.setBranch(rs.getString(1));
//		pi.setMail(rs.getString(2));
//		pi.setProduct(rs.getString(2));
		pi.setDuplicated(rs.getString(2));
		pi.setNullSGUID(rs.getString(3));
		pi.setBULKSEEDMissing(rs.getString(4));
//		pi.setERROR_DUPSGUID(rs.getString(7));
		pi.setERROR_SGUIDDIFF(rs.getString(5));
		pi.setIndexMissing(rs.getString(6));
		return pi;
	}
}
