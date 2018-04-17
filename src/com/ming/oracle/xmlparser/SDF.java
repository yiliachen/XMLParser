package com.ming.oracle.xmlparser;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SDF implements Runnable {
	private String mEntryName = null;
	private String mVO = null;
	private String mAM = null;

//	private ConcurrentHashMap<String, String> _g_map;

	public SDF(String pEntryName ) {
		this.mEntryName = pEntryName;
	}

	public void process() {
		try {
			File inputFile = new File(this.mEntryName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			Node root = doc.getDocumentElement();
			this.mAM = root.getAttributes().getNamedItem("am").getNodeValue();
			this.mVO = root.getAttributes().getNamedItem("vo").getNodeValue();
			String fullAM = this.mAM;
			String[] amarray = this.mAM.split("\\.");
			this.mAM = amarray[amarray.length-1];
			System.out.println(mAM+"_"+mVO+"|"+this.mEntryName+"|"+fullAM);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.process();
	}

}
