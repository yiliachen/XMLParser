package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.mh.jdbc.api.JdbcTemplate;
import com.mh.jdbc.util.DBUtil;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.a7.A7_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

public class postToApex {

	private String branch;
	
	public static void main(String[] args) {
		try{
		if(args.length != 3){
			System.out.println("Usage:postToApex logfile url branch");
			System.exit(-1);
		}
		postToApex self = new postToApex();
		self.setBranch(args[2]);
		System.out.println(self.getBranch());
		
		File lLogfile = new File(args[0]);
		
		try{
		BufferedReader lbr = new BufferedReader(new FileReader(lLogfile));
		String lLine;
		int counter = 0;
		ArrayList<SguidIssueRecord> issues = new ArrayList<SguidIssueRecord>();
		while((lLine = lbr.readLine()) != null ){
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
			lsir.setgSTATUS("NEW");
			lsir.setgBRANCH(args[2]);
			issues.add(lsir);
			//				dopost(issues, args[1]);
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
		String delsql = "delete from sguid_issue_list where branch = ?";
		PreparedStatement psdelstmt = conn.prepareStatement(delsql);
		psdelstmt.setString(1, getBranch());
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
	 
	public void postmail() {
	    String getMaillist = "select mail,path from pocfiles";
		String sql = "select branch,  \n" + "product, \n" + "sum(NullSGUID) as NullSGUID, \n"
				+ "sum(Duplicated) as Duplicated, \n" + "sum( BULKSEEDMissing) as BULKSEEDMissing, \n"
				+ "sum( ERROR_SGUIDDIFF) as ERROR_SGUIDDIFF\n"
				+ "from  \n" + "(select branch, \n" + "filepath, \n"
				+ "case when product = 'hrx' then substring(substring_index(substring_index(filepath,'/',6),'/',-1) ,4,5) else product end as product, \n"
				+ "NullSGUID, \n" + "Duplicated, \n" + "BULKSEEDMissing, \n" + "ERROR_DUPSGUID, \n"
				+ "ERROR_SGUIDDIFF\n"
				+ " from (select branch, filepath, substring_index(substring_index(filepath,'/',3),'/',-1) as product,\n"
				+ "sum(case when issue_type = 'NullSGUID' then 1 else 0 end) as 'NullSGUID', \n"
				+ "sum(case when issue_type = 'Duplicated' then 1 else 0 end ) as 'Duplicated',\n"
				+ "sum(case when issue_type = 'BULKSEEDMissing' then 1 else 0 end) as 'BULKSEEDMissing',\n"
				+ "sum(case when issue_type = 'ERROR_DUPSGUID' then 1 else 0 end ) as 'ERROR_DUPSGUID',\n"
				+ "sum(case when issue_type = 'ERROR_SGUIDDIFF' then 1 else 0 end) as 'ERROR_SGUIDDIFF'\n"
				+ "from sguid_issue_list issuelist group by branch, filepath, substring_index(substring_index(filepath,'/',3),'/',-1)) tmp where NullSGUID > 0 OR Duplicated > 0 OR BULKSEEDMissing > 0 OR ERROR_SGUIDDIFF > 0) tmp2 \n"
				+ "left join pocfiles poc on tmp2.product = poc.path\n" + "group by branch, product\n";
		JdbcTemplate lJT = new JdbcTemplate(DBUtil.getDataSource());
		System.out.println(sql);
		ArrayList<ProductIssue> issuelist = null;
		ArrayList<pocsRMapper> pocs = null;
		try {
			issuelist = (ArrayList<ProductIssue>)lJT.queryForList(sql,new ProductIssue());
			pocs = (ArrayList<pocsRMapper>)lJT.queryForList(getMaillist, new pocsRMapper());
			
			StringBuilder maillist = new StringBuilder();
			Iterator<pocsRMapper> mailiter = pocs.iterator();
			if(mailiter.hasNext()){
				maillist.append(mailiter.next().getMail());
				while(mailiter.hasNext()){
					maillist.append(",");
					maillist.append(mailiter.next().getMail());
				}
			}
			
			HashMap<String, ArrayList<ProductIssue>> issuemap = new HashMap<String, ArrayList<ProductIssue>>();
			Iterator<ProductIssue> iter = issuelist.iterator();
			while(iter.hasNext()){
				ProductIssue pi = iter.next();
				if(!issuemap.containsKey(pi.getProduct())){
					ArrayList<ProductIssue> listpi = new ArrayList<ProductIssue>();
					listpi.add(pi);
					issuemap.put(pi.getProduct(), listpi);
				}else{
					issuemap.get(pi.getProduct()).add(pi);
				}
			}
			Iterator<String> keyiter = issuemap.keySet().iterator();
			StringBuilder fsb = new StringBuilder();
			AsciiTable title = new AsciiTable();
			AsciiTable tab = new AsciiTable();
			String mail = new String();
			title.getContext().setGrid(A7_Grids.minusBarPlusEquals());
			title.addRule();
			title.setTextAlignment(TextAlignment.CENTER);
			title.addRow("SGUID Checker Reminder");
			title.addRule();
			mail = maillist.toString();
			while(keyiter.hasNext()){
				String product = keyiter.next();
				tab.getContext().setGrid(A7_Grids.minusBarPlusEquals());
//				tab.getContext().setWidth(13);
//				issuemap.get(product).get(0).getHeader(tab);
//				tab.addRule();
//				mail = issuemap.get(product).get(0).getMail();
				Iterator<ProductIssue> iterissue = issuemap.get(product).iterator();
				while(iterissue.hasNext()){
					tab.addRule();
					ProductIssue pi = iterissue.next();
					pi.getHeader(tab);
					tab.addRule();
					pi.toRow(tab);
					tab.addRule();
				}
			}
			fsb.append(title.render());
			fsb.append("\n");
			fsb.append(tab.render());
			
			System.out.println(fsb.toString());
			try {
				PrintWriter writer = new PrintWriter("/tmp/tmpmail.mail", "UTF-8");
				writer.println(title.render());
				writer.println("Sguid issue found, Here are the details:");
				writer.println(tab.render());
				writer.println("For more information, please visit https://apex.oraclecorp.com/pls/apex/f?p=22627:2:12229513583982::NO:RP::");
				writer.println("Or send mail to ming.c.chen@oracle.com Thanks!");
				writer.close();
				
				Runtime r = Runtime.getRuntime();
				System.out.println("/net/slc09pot.us.oracle.com/scratch/SeedProcess/utils/sendmail.sh "+mail);
				Process p = r.exec("/net/slc09pot.us.oracle.com/scratch/SeedProcess/utils/sendmail.sh " +mail);
				p.waitFor();
				BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";

				while ((line = b.readLine()) != null) {
				  System.out.println(line);
				}

				b.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(JSON.toJSON(issuelist));
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
