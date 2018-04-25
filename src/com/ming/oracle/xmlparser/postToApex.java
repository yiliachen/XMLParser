package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.mh.jdbc.api.JdbcTemplate;
import com.mh.jdbc.util.DBUtil;
import com.mh.jdbc.util.HTMLTableBuilder;

public class postToApex {

	private String branch;
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getBranchLabel(){
		return this.branch+"_"+this.label;
	}

	public static void main(String[] args) {
		
		try{
			if(args.length != 4){
				System.out.println("Usage:postToApex logfile url branch config");
				System.exit(-1);
			}
			DBUtil.initConfig(args[3]);
			postToApex self = new postToApex();
			String [] branchAndLabel = args[2].split("\\|");;
			self.setBranch(branchAndLabel[0]);
			self.setLabel(branchAndLabel[1]);
			System.out.println(self.getBranch());

			File lLogfile = new File(args[0]);

			try{
				BufferedReader lbr = new BufferedReader(new FileReader(lLogfile));
				String lLine;
				int counter = 0;
				ArrayList<SguidIssueRecord> issues = new ArrayList<SguidIssueRecord>();
				while((lLine = lbr.readLine()) != null ){
					if(lLine.indexOf("|") == -1){
						continue;
					}
					String [] lfields = lLine.split("\\|");
					SguidIssueRecord lsir = new SguidIssueRecord();
					//System.out.println(lLine);
					if(lfields[0].equals("NullSGUID")){
						lsir.setgISSUE_TYPE("NullSGUID");
						lsir.setgFILEPATH(lfields[1]);
						lsir.setgCOMMENTS(lfields[2]+"|"+lfields[3]+"|"+lfields[4]+"|"+lfields[5]+"|"+lfields[6]);
					}
					if(lfields[0].equals("Duplicated")){
						lsir.setgISSUE_TYPE("Duplicated");
						lsir.setgFILEPATH(lfields[1]);
						lsir.setgCOMMENTS(lfields[2]+"|"+lfields[3]+"|"+lfields[4]+"|"+lfields[5]+"|"+lfields[6]+"|"+lfields[7]+"|"+lfields[8]+"|"+lfields[9]+"|"+lfields[10]);
					}
					if(lfields[0].equals("BULKSEEDMissing")){
						lsir.setgISSUE_TYPE("BULKSEEDMissing");
						lsir.setgFILEPATH(lfields[2]);
						lsir.setgCOMMENTS(lfields[3]+"|"+lfields[4]+"|"+lfields[5]);
					}
					if(lfields[0].equals("WARNROWMOVE") || lfields[0].equals("INFOHasMoreRowkey")){
						continue;
					}
					if(lfields[0].equals("ERROR_DUPSGUID") 
							|| lfields[0].equals("ERROR_SGUIDDIFF")){
						lsir.setgISSUE_TYPE(lfields[0]);
						lsir.setgFILEPATH(lfields[1]);
						lsir.setgCOMMENTS(lLine);
					}
					if(lfields[0].equals("ERROR_SGUIDINDEX")){
						lsir.setgISSUE_TYPE(lfields[0]);
						lsir.setgFILEPATH(lfields[1]);
						lsir.setgCOMMENTS(lLine);
					}
					lsir.setgSTATUS("NEW");
					lsir.setgBRANCH(self.getBranchLabel());
					issues.add(lsir);
					//dopost(issues, args[1]);
				}
				System.out.print("Ready to post records to db");
				self.splitAndPostDBRecords(issues);
				self.postmail();
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public void postDBRecords(List<SguidIssueRecord> pIssues) throws SQLException {
		Iterator<SguidIssueRecord> lIter = pIssues.iterator();
		JdbcTemplate lJT = new JdbcTemplate(DBUtil.getDataSource());
		String lSql = "insert into sguid_issue_list(filepath, issue_type, comments, branch, add_date) "+
				"values(substring(?,instr(?, 'fusionapps')),?,?,?,now()) on duplicate key update add_date = now()";
		Connection conn = DBUtil.getConnection();
		conn.setAutoCommit(false);
		//		String delsql = "delete from sguid_issue_list where branch = ?";
		//		PreparedStatement psdelstmt = conn.prepareStatement(delsql);
		PreparedStatement pstmt = conn.prepareStatement(lSql);

		String lBranchName = "";
		try{
			while(lIter.hasNext()){
				SguidIssueRecord lSir = lIter.next();
				if("".equals(lBranchName)){
					lBranchName = lSir.getgBRANCH();
				}
				Object [] args = {lSir.getgFILEPATH(), lSir.getgFILEPATH(), lSir.getgISSUE_TYPE(), lSir.getgCOMMENTS(), lSir.getgBRANCH() };
				//System.out.println("executing sql:"+lSql+args);
				int[] argtypes = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
				try {
					lJT.prepareBatch(pstmt, args, argtypes);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			pstmt.executeBatch();
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			conn.rollback();
		}finally{
			DBUtil.closeStmt(pstmt);
			//			DBUtil.closeStmt(psdelstmt);
			DBUtil.closeConn(conn);
		}
	}

	public void splitAndPostDBRecords(ArrayList<SguidIssueRecord> pIssues) throws SQLException {
		Connection conn = DBUtil.getConnection();
		conn.setAutoCommit(false);
		String delsql = "delete from sguid_issue_list where branch like ?";
		PreparedStatement psdelstmt = conn.prepareStatement(delsql);
		psdelstmt.setString(1, getBranch()+"%");
		psdelstmt.executeUpdate();
		conn.commit();
		DBUtil.closeStmt(psdelstmt);
		System.out.println("Records Size " + pIssues.size() + " Split by 1000");
		int limitsize = 1000;
		int part = pIssues.size() / limitsize;
		if (pIssues.size() <= limitsize) {
			postDBRecords(pIssues);
		} else {
			for (int i = 0; i < part; i++) {
				System.out.println("1000 processed...");
				List sublist = pIssues.subList(0, limitsize);
				postDBRecords(sublist);
				pIssues.subList(0, limitsize).clear();
			}
			if (pIssues.size() > 0) {
				postDBRecords(pIssues);
				System.out.println(String.valueOf(pIssues.size()) + " processed...");
			}
		}
	}

	public void postmail() throws EmailException, SQLException {
		String getMaillist = "select mail,path from pocfiles";
		String sql = "select branch,  \n" 
	   	        + "sum(NullSGUID) as NullSGUID, \n"
				+ "sum(Duplicated) as Duplicated, \n" 
				+ "sum( BULKSEEDMissing) as BULKSEEDMissing, \n"
				+ "sum( ERROR_SGUIDDIFF) as ERROR_SGUIDDIFF,\n"
				+ "sum( ERROR_SGUIDINDEX) as ERROR_SGUIDINDEX\n"
				+ "from  \n" + "(select branch, \n" + "filepath, \n"
				+ "case when product = 'hrx' then substring(substring_index(substring_index(filepath,'/',6),'/',-1) ,4,5) else product end as product, \n"
				+ "NullSGUID, \n" + "Duplicated, \n" + "BULKSEEDMissing, \n" + "ERROR_DUPSGUID, \n"
				+ "ERROR_SGUIDDIFF,\n"+ "ERROR_SGUIDINDEX\n"
				+ " from (select branch, filepath, substring_index(substring_index(filepath,'/',3),'/',-1) as product,\n"
				+ "sum(case when issue_type = 'NullSGUID' then 1 else 0 end) as 'NullSGUID', \n"
				+ "sum(case when issue_type = 'Duplicated' then 1 else 0 end ) as 'Duplicated',\n"
				+ "sum(case when issue_type = 'BULKSEEDMissing' then 1 else 0 end) as 'BULKSEEDMissing',\n"
				+ "sum(case when issue_type = 'ERROR_DUPSGUID' then 1 else 0 end ) as 'ERROR_DUPSGUID',\n"
				+ "sum(case when issue_type = 'ERROR_SGUIDDIFF' then 1 else 0 end) as 'ERROR_SGUIDDIFF',\n"
				+ "sum(case when issue_type = 'ERROR_SGUIDINDEX' then 1 else 0 end) as 'ERROR_SGUIDINDEX'\n"
				+ "from sguid_issue_list issuelist group by branch, filepath, substring_index(substring_index(filepath,'/',3),'/',-1)) tmp where NullSGUID > 0 OR Duplicated > 0 OR BULKSEEDMissing > 0 OR ERROR_SGUIDDIFF > 0 OR ERROR_SGUIDINDEX > 0 order by product) tmp2 \n"
				+ "left join pocfiles poc on tmp2.product = poc.path\n" + "group by branch\n";
		JdbcTemplate lJT = new JdbcTemplate(DBUtil.getDataSource());
		System.out.println(sql);
		ArrayList<ProductIssue> issuelist = null;
		ArrayList<pocsRMapper> pocs = null;
		issuelist = (ArrayList<ProductIssue>)lJT.queryForList(sql,new ProductIssue());
		pocs = (ArrayList<pocsRMapper>)lJT.queryForList(getMaillist, new pocsRMapper());

		Iterator<pocsRMapper> mailiter = pocs.iterator();
		//get mail for to list
		HtmlEmail email = new HtmlEmail();
		email.setAuthentication("ming.c.chen@oracle.com", "1MarGaimima");
		email.setHostName("stbeehive.oracle.com");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);

		email.setFrom("sguidchecker@oracle.com", "Sguid Checker");
		email.setSubject("SGUID issue summary");
		if(mailiter.hasNext()){
			while(mailiter.hasNext()){
				String mail = mailiter.next().getMail();
				email.addTo(mail, mail);
			}
		}

		HTMLTableBuilder htb = new HTMLTableBuilder(null, true, 0, 6);
		Iterator<ProductIssue> iter = issuelist.iterator();
		ProductIssue.setHtmlHeader(htb);
		while(iter.hasNext())
		{
				ProductIssue pi = iter.next();
				pi.setHtmlRow(htb);
		}
		
//		String header = "Sguid issue found, Here are the details:\n";
		// set the html message
		//iterate by product
		String footer = "For more information, please visit https://apex.oraclecorp.com/pls/apex/f?p=22627:2:12229513583982::NO:RP::\n Or send mail to ming.c.chen@oracle.com Thanks!";
		email.setHtmlMsg(htb.build()+footer);

		email.send();
	}

	/**
	 * This get each issue count for each person, issue type, issue count, to a person
	 * send apex website address to poc for them to take care.
	 * @param pIssues
	 * @param pHost
	 * @throws IOException
	 */
	public static void dopost(ArrayList<SguidIssueRecord> pIssues, String pHost) throws IOException{
		System.out.println("posting every 500 rows...");
		String recordsJson = JSON.toJSONString(pIssues);
		Records innerRec = new Records();
		Records outerRec = new Records();

		innerRec.setgRecords(recordsJson);
		outerRec.setgRecords(JSON.toJSONString(innerRec).replaceAll("\\\\", ""));
		String lPostingJson = JSON.toJSONString(outerRec).replaceFirst("\\\\\"\\[", "[").replaceFirst("\\]\\\\\"", "]");

		CloseableHttpClient lClient = HttpClients.createDefault();
		System.out.println("posting "+lPostingJson+" to "+pHost);
		HttpPost httpPost = new HttpPost(pHost);
		StringEntity entity = new StringEntity(lPostingJson);
		httpPost.setEntity(entity);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		CloseableHttpResponse response = lClient.execute(httpPost);
		System.out.println(response.getStatusLine());
		lClient.close();
		pIssues.clear();
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

}
