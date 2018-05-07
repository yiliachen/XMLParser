package com.ming.oracle.xmlparser;

public class SdfModRecord {
	private String gBRANCH;
	private String gISSUE_TYPE;
	private String gFILEPATH;
	private String gCOMMENTS;
	private String gTXN_NAME;
	private String gROWKEY;
	
	public static final String CREATE_FILE = "CREATE_FILE";
	public static final String REMOVE_FILE = "REMOVE_FILE";
	public static final String ADD_ROW = "ADD_ROW";
	public static final String REMOVE_ROW = "REMOVE_ROW";
	
	public String getgBRANCH() {
		return gBRANCH;
	}
	public void setgBRANCH(String gBRANCH) {
		this.gBRANCH = gBRANCH;
	}
	public String getgISSUE_TYPE() {
		return gISSUE_TYPE;
	}
	public void setgISSUE_TYPE(String gISSUE_TYPE) {
		this.gISSUE_TYPE = gISSUE_TYPE;
	}
	public String getgFILEPATH() {
		return gFILEPATH;
	}
	public void setgFILEPATH(String gFILEPATH) {
		this.gFILEPATH = gFILEPATH;
	}
	public String getgCOMMENTS() {
		return gCOMMENTS;
	}
	public void setgCOMMENTS(String gCOMMENTS) {
		this.gCOMMENTS = gCOMMENTS;
	}
	public String getgTXN_NAME() {
		return gTXN_NAME;
	}
	public void setgTXN_NAME(String gTXN_NAME) {
		this.gTXN_NAME = gTXN_NAME;
	}
	public String getgROWKEY() {
		return gROWKEY;
	}
	public void setgROWKEY(String gROWKEY) {
		this.gROWKEY = gROWKEY;
	}
	
}
