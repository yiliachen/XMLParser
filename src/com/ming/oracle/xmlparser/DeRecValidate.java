package com.ming.oracle.xmlparser;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DeRecValidate implements Runnable {
	private String mEntryName = null;
	private boolean mDebug = false;
	private boolean mEndFound = false;
	private boolean mDe = false;
	private Hashtable<String,String> mkeyset = null;

	public DeRecValidate(String pEntryName) {
		this.mEntryName = pEntryName;
		this.mkeyset = new Hashtable<String,String>();
	}

	public void process() {
		try {
			File inputFile = new File(this.mEntryName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			this.iterDocs(doc.getDocumentElement());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkDebegin(Element pEle){
		if (pEle.hasAttribute("debegin")) {
			this.mDe = true;
			if(pEle.hasAttribute("deend")){
				this.mEndFound = true;
				this.mDe = false;
			}
//			this.mkeyset.put("debegin",pEle.getNodeName()+"|"+pEle.getAttribute("rowkey"));
		}
	}
	
	public void checkDeend(Element pEle) {
		if (pEle.hasAttribute("deend") && this.mEndFound == false) {
			// this.mkeyset.put("deend",pEle.getNodeName()+"|"+pEle.getAttribute("rowkey"));
			this.mDe = false;
			if (this.mkeyset.keySet().size() > 1) {
				System.out
						.println("!!!File: " + this.mEntryName + "contains Records have different SGUID within DE:!!!");

				Iterator<Entry<String, String>> liter = this.mkeyset.entrySet().iterator();
				while (liter.hasNext()) {
					Entry<String, String> lentry = liter.next();
					System.out.println(lentry.getKey() + "|" + lentry.getValue());
				}
			}
			this.mkeyset.clear();
		}
	}

    
	public void iterDocs(Node pNode) {
		NodeList nl = pNode.getChildNodes();
		for (int iter = 0; iter < nl.getLength(); iter++) {
			Node l_node = nl.item(iter);
			if (l_node.getNodeType() == Node.ELEMENT_NODE && l_node.getNodeName().equals("SGUID")) {
				if (l_node.getTextContent().length() != 0) {

					Node l_parentNode = l_node.getParentNode();
					if (l_parentNode.getNodeType() == Node.ELEMENT_NODE) {
						Element l_ele = (Element) l_parentNode;
						this.checkDebegin(l_ele);
						
						if(this.mDe){
							this.mkeyset.put(l_node.getTextContent(),l_ele.getAttribute("rowkey"));
						}
						this.checkDeend(l_ele);
					}
				}
			}
			if (l_node.hasChildNodes()) {
				iterDocs(l_node);
			}
		}
	}

	@Override
	public void run() {
		this.process();
	}

	public boolean getmDebug() {
		return mDebug;
	}

	public void setmDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}

}
