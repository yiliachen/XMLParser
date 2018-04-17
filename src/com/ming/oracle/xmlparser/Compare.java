package com.ming.oracle.xmlparser;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Compare implements Runnable {
	private String mEntryName = null;
	private boolean mDebug = false;
	private boolean mDE = false;
	private String mAM = null;
	private SGUIDValidator svad = null;
	private ConcurrentHashMap<String, String> _g_map;
	private ConcurrentHashMap<String, String> _g_keyset;

	public Compare(String pEntryName, SGUIDValidator svad) {
		this.mEntryName = pEntryName;
		this._g_map = svad.getSguidVOInfo();
		this._g_keyset = svad.getRowkeySguidMap();
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
			this.iterDocs(doc.getDocumentElement());
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkDebegin(Element pEle, StringBuilder psb) {
		if (pEle.hasAttribute("debegin")) {
			this.mDE = true;
			if (pEle.hasAttribute("deend")) {
				this.mDE = false;
			}
		}
	}

	public void checkDeend(Element pEle, StringBuilder psb) {
		if (pEle.hasAttribute("deend") && this.mDE ) {
			this.mDE = false;
		}
	}

	public void checkRowkey(Element pEle, StringBuilder psb) {
		if (pEle.hasAttribute("rowkey")) {
			psb.append(pEle.getAttribute("rowkey"));
			psb.append("|");
		}
	}

	public synchronized void iterDocs(Node pNode) {
		NodeList nl = pNode.getChildNodes();
		for (int iter = 0; iter < nl.getLength(); iter++) {
			Node l_node = nl.item(iter);
			StringBuilder lsb = new StringBuilder();
			if (l_node.getNodeType() == Node.ELEMENT_NODE && l_node.getNodeName().equals("SGUID")) {
					Node l_parentNode = l_node.getParentNode();
					if (l_parentNode.getNodeType() == Node.ELEMENT_NODE) {
						Element l_ele = (Element) l_parentNode;
						lsb.append(this.mEntryName + "|");
						lsb.append(l_ele.getNodeName()+"|");
						this.checkRowkey(l_ele, lsb);
						this.checkDebegin(l_ele, lsb);
						this.checkDeend(l_ele, lsb);
						if(this.mDE){
							lsb.append("Y|");
						}else{
							lsb.append("N|");
						}
					}
					if (l_node.getTextContent().length() != 0) {
						lsb.append(l_node.getTextContent());
					}else{
						lsb.append("NONE");
					}
					//lRef file entries from Transactional file
					//lValArray branch entries
					lsb.append("|"+this.mAM);
					String [] lRef = lsb.toString().split("\\|");
					//VO and SGUID found in Ref
					String lvosguid = lRef[1]+lRef[4];
					if(this._g_map.containsKey(lvosguid)){
						String val = this._g_map.get(lvosguid);
						String [] ValArray = val.split("\\|");
						//Rowkey matched
						if(lRef[2].equals(ValArray[1])){
							//Different files
							if(!lRef[0].equals(ValArray[0])){
								System.out.println("WARNING:Rowkey Moved Across files: "+ValArray[1]+" Transaction File:"+lRef[0]+" Comparing file: "+ValArray[0]);
							}
						}else{
							System.out.println("ERROR_DUPSGUID:SGUID mapped to different rowkeys in transaction file: "+ lRef[0]+" "+lRef[1]+lRef[4]+" Mapped to key in transaction:"+lRef[2]+" key in comparing branch "+ValArray[0]);
						}
					}else{
						if(this._g_keyset.containsKey(lRef[2])){
							System.out.println("ERROR_SGUIDDIFF:SGUID Transaction SGUID: "+lRef[4]+" Compare SGUID: "+ this._g_keyset.get(lRef[2])+" changed for rowkey: "+lRef[2] + " for file "+ lRef[0]);
						}else{
//							System.out.println("INFO:New rowkey " +lRef[2]+" in transaction file! "+lRef[0]+" with SGUID "+lRef[4]);
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
