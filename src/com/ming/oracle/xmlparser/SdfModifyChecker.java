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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mh.jdbc.api.JdbcTemplate;
import com.mh.jdbc.util.DBUtil;

public class SdfModifyChecker{
	public static void main(String[] args) {

		if (args.length != 4) {
			System.err.println("Usages: SdfModifyChecker transdesc adeViewRoot labelserver dbconfig");
			System.exit(2);
		}

		String transdesc = args[0];
		String adeViewRoot = args[1];
		String labelserver = args[2];
		DBUtil.initConfig(args[3]);
		File descfile = new File(transdesc);
        BufferedReader descreader = null;
    	String descLine = null;
    	String txnName = null;
    	String [] lfields = null;
    	List<SdfModRecord> records = new ArrayList<SdfModRecord>();
    	List<String> modSdfList = new ArrayList<String>();
    	List<String> newSdfList = new ArrayList<String>();
    	List<String> rmSdfList = new ArrayList<String>();
    	boolean allAffectedFiles = false;
    	boolean newFiles = false;
    	boolean removedFiles = false;
    	String filePath = null;
    	
		try {
        	descreader = new BufferedReader(new FileReader(descfile));
        	while ((descLine = descreader.readLine()) != null) {
        		if(descLine.contains("TRANSACTION:")) {
        			lfields = descLine.split(":");
        			txnName = lfields[1].trim();
        		}
        		if("ALL AFFECTED FILES:".equals(descLine)) {
        			allAffectedFiles = true;
        		}
        		if(allAffectedFiles) {
        			if(!descLine.isEmpty()) {
        				if(descLine.contains("SD.xml")) {
        					filePath = descLine.substring(descLine.indexOf("fusionapps"),descLine.indexOf("SD.xml")+6);
        					if(!modSdfList.contains(filePath)) {
        						modSdfList.add(filePath);
        					}
        				}
        			}else {
        				allAffectedFiles = false;	
        			}
        		}
        		if("NEW FILES:".equals(descLine)) {
        			newFiles = true;
        		}
        		if(newFiles) {
        			if(!descLine.isEmpty()) {
        				if(descLine.contains("SD.xml")) {
        					filePath = descLine.substring(descLine.indexOf("fusionapps"),descLine.indexOf("SD.xml")+6);
        					if(!newSdfList.contains(filePath)) {
        						newSdfList.add(filePath);
        						SdfModRecord rec = new SdfModRecord();
        						rec.setgISSUE_TYPE(SdfModRecord.CREATE_FILE);
        						rec.setgBRANCH(labelserver);
        						rec.setgFILEPATH(filePath);
        						rec.setgTXN_NAME(txnName);
        						records.add(rec);
        					}
        				}
        			}else {
        				newFiles = false;	
        			}
        		}
        		if("RMNAMED (REMOVED) FILES:".equals(descLine)) {
        			removedFiles = true;
        		}
        		if(removedFiles) {
        			if(!descLine.isEmpty()) {
        				if(descLine.contains("SD.xml")) {
        					filePath = descLine.substring(descLine.indexOf("fusionapps"),descLine.indexOf("SD.xml")+6);
        					if(!rmSdfList.contains(filePath)) {
        						rmSdfList.add(filePath);
        						SdfModRecord rec = new SdfModRecord();
        						rec.setgISSUE_TYPE(SdfModRecord.REMOVE_FILE);
        						rec.setgBRANCH(labelserver);
        						rec.setgFILEPATH(filePath);
        						rec.setgTXN_NAME(txnName);
        						records.add(rec);
        					}
        				}
        			}else {
        				removedFiles = false;	
        			}
        		}
        	}
        	modSdfList.removeAll(newSdfList);
        	modSdfList.removeAll(rmSdfList);
            descreader.close();
        	
            String labelFile = null;
            String txnFile = null;
            List<String> labelRowkeyList = null;
            List<String> txnRowkeyList = null;
            Collection<String> removeRowkeyList = null;
            Collection<String> addRowkeyList = null;
            for (String sdf: modSdfList) {
        		labelFile = labelserver+"/"+sdf;
        		txnFile = adeViewRoot+"/"+sdf;
        		labelRowkeyList = getRowkeyList(labelFile);
        		txnRowkeyList = getRowkeyList(txnFile);
        		removeRowkeyList = CollectionUtils.subtract(labelRowkeyList, txnRowkeyList);
        		addRowkeyList = CollectionUtils.subtract(txnRowkeyList, labelRowkeyList);
        		if(!removeRowkeyList.isEmpty()) {
        			System.out.println("Following rowkey removed form "+sdf);
        			for(String s:removeRowkeyList) {
						SdfModRecord rec = new SdfModRecord();
						rec.setgISSUE_TYPE(SdfModRecord.REMOVE_ROW);
						rec.setgBRANCH(labelserver);
						rec.setgFILEPATH(sdf);
						rec.setgTXN_NAME(txnName);
						rec.setgROWKEY(s);
						records.add(rec);
						System.out.println(s);
        			}
        		}
        		if(!addRowkeyList.isEmpty()) {
//        			System.out.println("Following rowkey added to "+sdf);
        			for(String s:addRowkeyList) {
        				SdfModRecord rec = new SdfModRecord();
						rec.setgISSUE_TYPE(SdfModRecord.ADD_ROW);
						rec.setgBRANCH(labelserver);
						rec.setgFILEPATH(sdf);
						rec.setgTXN_NAME(txnName);
						rec.setgROWKEY(s);
						records.add(rec);
//						System.out.println(s);
        			}
        		}
            }
            
//			System.out.print("Ready to post records to db");
			postDBRecords(records, txnName);
			
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (SQLException e1) {
		        e1.printStackTrace();
		    } finally {
		        if (descreader != null) {
		            try {
		            	descreader.close();
		            } catch (IOException e1) {
		            	e1.printStackTrace();
		            }
		        }
		    }

	}
	
	
	
	private static List<String> getRowkeyList(String filePath){
		List<String> result = new ArrayList<String>();
		try {
        	File inputFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			Node root = doc.getDocumentElement();
			iterDocs(root, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return result;
	}
	
	private static void iterDocs(Node pNode, List<String> rowkeyList) {
		NodeList nl = pNode.getChildNodes();
		String rowkey = null;
		for (int iter = 0; iter < nl.getLength(); iter++) {
			Node l_node = nl.item(iter);
			if (l_node.getNodeType() == Node.ELEMENT_NODE) {
				Element l_ele = (Element) l_node;
				if (l_ele.hasAttribute("rowkey")){
					rowkey = l_ele.getAttribute("rowkey");
					if(!rowkeyList.contains(rowkey)) {
						rowkeyList.add(rowkey);	
					}
				}
				if (l_node.hasChildNodes()) {
					iterDocs(l_node, rowkeyList);
				}
			}
		}
	}
	
	
	public static void postDBRecords(List<SdfModRecord> records, String txnName) throws SQLException {
		Connection conn = DBUtil.getConnection();
		conn.setAutoCommit(false);
		String delsql = "delete from sdf_mod_list where txn_name = ?";
		PreparedStatement psdelstmt = conn.prepareStatement(delsql);
		psdelstmt.setString(1, txnName);
		psdelstmt.executeUpdate();

		Iterator<SdfModRecord> lIter = records.iterator();
		JdbcTemplate lJT = new JdbcTemplate(DBUtil.getDataSource());
		String lSql = "insert into sdf_mod_list(branch, issue_type, filepath, txn_name, rowkey, add_date) "+
				"values(?,?,?,?,?,now()) on duplicate key update add_date = now()";
		PreparedStatement pstmt = conn.prepareStatement(lSql);

		try{
			while(lIter.hasNext()){
				SdfModRecord rec = lIter.next();
				Object [] args = {rec.getgBRANCH(), rec.getgISSUE_TYPE(), rec.getgFILEPATH(), rec.getgTXN_NAME(), rec.getgROWKEY() };
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
	
	
}