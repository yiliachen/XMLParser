package com.ming.oracle.xmlparser;

import java.util.Date;


public class FileEntry {
	
	private long id;
	private String filename;
	private String entryName;
	private String type;
	private Integer occourance;
	private String SguidIsNull;
	private String SguidPopulated;
	private String SguidDuplicated;
	private String HasRowkey;
	private Date timestamp;
	private String startPath;
	
	public Date getTimestamp() {
		return timestamp;
	}

	public String getStartPath() {
		return startPath;
	}

	public void setStartPath(String startPath) {
		this.startPath = startPath;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getSguidIsNull() {
		return SguidIsNull;
	}

	public void setSguidIsNull(String sguidIsNull) {
		SguidIsNull = sguidIsNull;
	}

	public String getSguidPopulated() {
		return SguidPopulated;
	}

	public void setSguidPopulated(String sguidPopulated) {
		SguidPopulated = sguidPopulated;
	}

	public String getSguidDuplicated() {
		return SguidDuplicated;
	}

	public void setSguidDuplicated(String sguidDuplicated) {
		SguidDuplicated = sguidDuplicated;
	}

	public String getHasRowkey() {
		return HasRowkey;
	}

	public void setHasRowkey(String hasRowkey) {
		HasRowkey = hasRowkey;
	}

	public FileEntry() {
		super();
	}
	
	public FileEntry(String filename, String entryName, String type, Integer occurance) {
		super();
		this.filename = filename;
		this.entryName = entryName;
		this.setType(type);
		this.occourance = occurance;
		
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		if(type!=null && type.indexOf("SGUIDPOP") >= 0){
			this.setSguidPopulated("Y");
		}else{
			this.setSguidPopulated("N");
		}
		
		if(type!=null && type.indexOf("DUPSGUID") >= 0){
			this.setSguidDuplicated("Y");
		}else{
			this.setSguidDuplicated("N");
		}
		
		if(type!=null && type.indexOf("NULLSGUID") >= 0){
			this.setSguidIsNull("Y");
		}else{
			this.setSguidIsNull("N");
		}
		
		if(type!=null && type.indexOf("rowkey") >= 0){
			this.setHasRowkey("Y");
		}else{
			this.setHasRowkey("N");
		}
	}

	public Integer getOccourance() {
		return occourance;
	}

	public void setOccourance(Integer occourance) {
		this.occourance = occourance;
	}
	
	public Boolean equals(FileEntry fe){
		if(fe.entryName.equals(this.entryName) &&
				fe.filename.equals(this.filename) && 
				fe.type.equals(this.type) && fe.occourance.equals(this.occourance)){
			return true;
		}else{
			return false;
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getFilename());
		sb.append("|");
		sb.append(this.getEntryName());
		sb.append("|");
		sb.append(this.getType());
		sb.append("|");
		return sb.toString();
	}
}