package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class SGUIDValidator {
	private String branchName;
	private String label;
	private ConcurrentHashMap<String, String> RowkeySguidMap;
	private ConcurrentHashMap<String, String> SguidInfo;
	private ConcurrentHashMap<String, String> SguidVOInfo;
	private ConcurrentHashMap<String, String> SguidVOInfo2;
	
	public ConcurrentHashMap<String, String> getSguidVOInfo2() {
		return SguidVOInfo2;
	}
	public void setSguidVOInfo2(ConcurrentHashMap<String, String> sguidVOInfo2) {
		SguidVOInfo2 = sguidVOInfo2;
	}
	public ConcurrentHashMap<String, String> getRowkeySguidMap() {
		return RowkeySguidMap;
	}
	public void setRowkeySguidMap(ConcurrentHashMap<String, String> rowkeySguidMap) {
		RowkeySguidMap = rowkeySguidMap;
	}
	public ConcurrentHashMap<String, String> getSguidInfo() {
		return SguidInfo;
	}
	public void setSguidInfo(ConcurrentHashMap<String, String> sguidInfo) {
		SguidInfo = sguidInfo;
	}
	public ConcurrentHashMap<String, String> getSguidVOInfo() {
		return SguidVOInfo;
	}
	public void setSguidVOInfo(ConcurrentHashMap<String, String> sguidVOInfo) {
		SguidVOInfo = sguidVOInfo;
	}
	
	public SGUIDValidator(){}
	public SGUIDValidator(String RefFileName){
        BufferedReader refreader = null;
        
        File lrefFile = new File(RefFileName);
        ConcurrentHashMap<String, String> gMap = new ConcurrentHashMap<String, String>();
        ConcurrentHashMap<String, String> rowkeyMap = new ConcurrentHashMap<String, String>();
        ConcurrentHashMap<String, String> sguidmap = new ConcurrentHashMap<String, String>();
        try{
		refreader = new BufferedReader(new FileReader(lrefFile));
        String lRefLine = null;
        while((lRefLine = refreader.readLine()) != null){
        	/*
        	 * SGUID should be the key of the map 
        	 * <SGUID> => <Path
        	 * SGUID should not be changed between Branches.
        	 */
        	String []lRefArray = lRefLine.split("\\|");
        	//format of the key voinstancesguid
        	gMap.put(lRefArray[1]+"|"+lRefArray[4], lRefLine);
        	//format rowkey sguid
        	rowkeyMap.put(lRefArray[2]+"|"+lRefArray[1], lRefArray[4]);
        	//Sguid 
        	sguidmap.put(lRefArray[4], lRefLine);
        }
        refreader.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
        finally{
        	if(refreader != null){
        		try{
        			refreader.close();
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}
        }
        this.setRowkeySguidMap(rowkeyMap);
        this.setSguidVOInfo(gMap);
        this.setSguidVOInfo2(sguidmap);
	}
	public void compare(SGUIDValidator svd){
		//Sef as source
		//Parameter as target
		KeySetView<String, String> pKeyset = svd.getSguidVOInfo().keySet();
		Iterator<String> pIter = pKeyset.iterator();
		while(pIter.hasNext()){
			String lVoSguid = pIter.next();
			//contains voinstanceSGUID combination
			//If current branch contains 
			//Source
			String lVal = this.getSguidVOInfo().get(lVoSguid);
			
			//Target
			String pMapStr = svd.getSguidVOInfo().get(lVoSguid);
			String [] pArray = pMapStr.split("\\|");
			if(this.getSguidVOInfo().containsKey(lVoSguid)){
			String [] lArray = lVal.split("\\|");
				//Rowkey matched
				if(pArray[2].equals(lArray[2])){
					//Different files
					if(!pArray[0].equals(lArray[0])){
						//WARNROWMOVE|Filepath|rowkey|srcbranch|filepath|trgbranch|filepath
						System.out.println("WARNROWMOVE|"+lArray[0]+"|"+pArray[2]+ "|" + this.getBranchName()+"|"+lArray[0]+"|"+svd.getBranchName()+"|"+pArray[0]);
					}
				}else{
					//ERROR_DUPSGUID|Filepath|SGUID|SrcBranch|SrcRowkey|TrgBranch|TrgRowkey
					System.out.println("ERROR_DUPSGUID|"+lArray[0]+"|"+lArray[4]+"|"+this.getBranchName()+"|" +lArray[2]+"|"+ svd.getBranchName() +"|"+pArray[2]);
				}
			}else{
				//vo sguid can not be found but rowkey exists means it assigned different rowkey
				if(this.getRowkeySguidMap().containsKey(pArray[2])){
					String lSrcSguid = this.getRowkeySguidMap().get(pArray[2]);
					//if sguid can be found in target branch and rowkey and filepath are the same it means the VO instance got changed
					
					if(this.getSguidVOInfo2().containsKey(lSrcSguid)){
						String recordInfo = this.getSguidVOInfo2().get(lSrcSguid);
						String []srcArray = recordInfo.split("\\|");
						if(srcArray[0].equals(pArray[0]) && srcArray[2].equals(pArray[2]) && srcArray[4].equals(pArray[4])){
							continue;
						}
					}
					//ERROR_SGUIDDIFF|Filepath|srcSGUID|trgSGUID|rowkey|srcBranch|trgBranch
					System.out.println("ERROR_SGUIDDIFF|"+pArray[0]+"|"+lSrcSguid+"|"+ pArray[4]+ "|"+pArray[2] + "|"+this.getBranchName()+"|"+svd.getBranchName());
				}else{
//					System.out.println("INFOHasMoreRowkey|"+pArray[0]+"| rowkey " +pArray[2]+" in target branch: "+svd.getBranchName()+" not in source:"+this.getBranchName()  );
//					INFOHasMoreRowkey|filepath|voname|rowkey|branch(has rowkey)|branch(not has rowkey)
					System.out.println("INFOHasMoreRowkey|"+pArray[0]+"|"+pArray[1]+"|" +pArray[2]+"|"+svd.getBranchName()+"_"+svd.getLabel()+"|"+this.getBranchName()+"_"+this.getLabel()  );
				}
			}
		}
	}
	public Boolean compSguid(String srcSguid, String trgSguid){
		if(srcSguid == null || trgSguid == null){
			return false;
		}else{
			
		return true;
		}
	}
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
