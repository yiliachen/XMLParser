package com.example.xmlpaser;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ming.oracle.xmlparser.FileEntry;
import com.ming.oracle.xmlparser.Processor;
import com.ming.oracle.xmlparser.SGUIDValidator;


public class AttributeProcessor implements Processor 
{
	private String mEntryName = null;
	private boolean mDebug = false;
	private Set<FileEntry> fes = null;
	private String startPath = null;
	private boolean mFirstDe;
	private SGUIDValidator svd = null;
	public String getmEntryName() {
		return mEntryName;
	}
	public SGUIDValidator getSvd() {
		return svd;
	}


	public void setSvd(SGUIDValidator svd) {
		this.svd = svd;
	}
	private Hashtable<String,String> mkeyset = new Hashtable<String, String>();
	/**
	 * gMap the key is SGUID, the value of the map is filePath, rowkey, SGUID
	 */
	private ConcurrentHashMap<String, String> gMap;
	private boolean mDe = false;


	public void setmEntryName(String mEntryName) {
		this.mEntryName = mEntryName;
	}


	public ConcurrentHashMap<String, String> getgMap() {
		return svd.getSguidInfo();
	}


	public void setgMap(ConcurrentHashMap<String, String> gMap) {
		this.svd.setSguidInfo(gMap);
	}


	public Set<FileEntry> getFes() {
		return fes;
	}


	public void setFes(Set<FileEntry> fes) {
		this.fes = fes;
	}


	public AttributeProcessor(String pEntryName){
		this.mEntryName = pEntryName;
	}

	public void process(){
		try {
			Boolean issueFound = false;
			File inputFile = new File(this.mEntryName);
			StringBuffer sb = new StringBuffer();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			this.iterDocs(doc.getDocumentElement());
			Iterator<String> iter = this.getVOKeyCount().keySet().iterator();
//			sb.append(this.mEntryName);
//			sb.append("|");
			
			while (iter.hasNext()) {
				String l_key = iter.next();
				FileEntry fe = new FileEntry();
				fe.setFilename(this.mEntryName);
				
				if (l_key.indexOf("|-|NULL") > 0 ) {
					fe.setEntryName(l_key);
					fe.setType("SGUID isNull='true' case");
					fe.setOccourance(this.getVOKeyCount().get(l_key));
					issueFound = true;
				}
				else if(l_key.indexOf("DUPSGUID_") >= 0){
					fe.setEntryName(l_key);
					fe.setType("Duplicated SGUID");
					fe.setOccourance(this.getVOKeyCount().get(l_key));
					issueFound = true;
				}
				else if(l_key.indexOf("DERecord_") >= 0 ){
					fe.setEntryName(l_key);
					fe.setType("DE Records with different SGUIDs within same range");
					fe.setOccourance(this.getVOKeyCount().get(l_key));
					issueFound = true;
				}
				else if(l_key.indexOf("rowkey") >= 0){
					fe.setEntryName(l_key);
					fe.setType("RowKey Counts");
					fe.setOccourance(this.getVOKeyCount().get(l_key));
				}
				else if (l_key.indexOf("SGUID") < 0 && l_key.indexOf("NoSguidTag_") <0) {
					fe.setEntryName(l_key);
					fe.setType("This VO's row count");
					fe.setOccourance(this.getVOKeyCount().get(l_key));
				}
//				else if (l_key.indexOf("NoSguidTag_") >= 0) {
//					fe.setEntryName(l_key);
//					fe.setType("No Sguid Tag");
//					fe.setOccourance(this.getVOKeyCount().get(l_key));
//					issueFound = true;
//				}
				else if (this.mDebug) {
//					System.out.println(l_key + ":" + this.getVOKeyCount().get(l_key));
				} else {
					continue;
				}
				fe.setStartPath(this.getStartPath());
//				fes.add(fe);
				sb.append(fe.toString());
				if(issueFound){
					System.out.println(sb.toString());
					issueFound = false;
					sb.delete(0, sb.length());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	Map<String, Integer> VOKeyCount = new HashMap<String, Integer>();

	public void selfIncrease(String p_Key) {
		if (this.VOKeyCount.containsKey(p_Key)) {
			this.VOKeyCount.put(p_Key, this.VOKeyCount.get(p_Key) + 1);
		} else {
			this.VOKeyCount.put(p_Key, 1);
		}
	}
	
	public void checkDebegin(Element pEle){
		if (pEle.hasAttribute("debegin")) {
			this.mDe = true;
			if(pEle.hasAttribute("deend")){
				this.mFirstDe = false;
				this.mDe = false;
			}
		}
	}
	
	public void checkDeend(Element pEle) {
		if (pEle.hasAttribute("deend") && this.mDe) {
			this.mDe = false;
			if(this.mkeyset.keySet().size() > 1){
				Iterator<Entry<String, String>> liter = this.mkeyset.entrySet().iterator();
				StringBuffer sb = new StringBuffer();
				while (liter.hasNext()) {
					Entry<String, String> lentry = liter.next();
					sb.append(lentry.getKey() + "_" + lentry.getValue()+"\n");
				}

				this.selfIncrease("DERecord_"+sb.toString());
			}
			this.mkeyset.clear();
		}
	}
	
	public void checkRowkey(Element pEle, StringBuilder psb) {
		if (pEle.hasAttribute("rowkey")) {
			psb.append(pEle.getAttribute("rowkey"));
			psb.append("|");
		}
	}

	public void iterDocs(Node pNode) {
//		if (pNode.getNodeType() == Node.ELEMENT_NODE) {
//			Element l_ele = (Element) pNode;
//		}
		NodeList nl = pNode.getChildNodes();
		String lvoName = "";
		for (int iter = 0; iter < nl.getLength(); iter++) {
			Node l_node = nl.item(iter);
			//construct items for gMap 
			//value of value:FilePath|Rowkey|SGUID
			StringBuilder lsb = new StringBuilder();
			if (l_node.getNodeName().equals("SGUID")) {
				Node lParentNode = l_node.getParentNode();
				
				lsb.append(this.mEntryName + "|");
				lvoName = lParentNode.getNodeName();
				
				if (lParentNode.getNodeType() == Node.ELEMENT_NODE) {
					Element lParentElem = (Element) lParentNode;
					this.checkDebegin(lParentElem);
//					this.selfIncrease(l_node.getParentNode().getNodeName() + "_" + l_node.getNodeName());
					
					if(l_node.getTextContent().length() != 0 && this.mDe) {
					    this.mkeyset.put(l_node.getTextContent(),lParentElem.getAttribute("rowkey"));
					}
					
					if (l_node.getTextContent().length() != 0 ) {
						this.mFirstDe = true;
						String lrowkey = lParentElem.getAttribute("rowkey");
						lsb.append(lrowkey);
						lsb.append("|"+l_node.getTextContent());
						if(this.getgMap().containsKey(l_node.getTextContent()))
						{
							//rowkey and sguid are not identical
							String [] sbs = this.getgMap().get(l_node.getTextContent()).split("\\|");
							if(sbs.length == 3 && !sbs[1].equals(lrowkey) && !this.mkeyset.containsKey(l_node.getTextContent())){
								selfIncrease("DUPSGUID_"+lsb.toString()+"|"+this.getgMap().get(l_node.getTextContent()));
							}
						}else{
							this.getgMap().put(l_node.getTextContent(), lsb.toString());
						}
//						selfIncrease(l_node.getParentNode().getNodeName() + "_" + l_node.getNodeName() + "_"
//								+ l_node.getTextContent());
					} else if(l_node.getTextContent().length() == 0){
						selfIncrease(l_node.getParentNode().getNodeName() + "_" + l_node.getNodeName() + lParentElem.getAttribute("rowkey")+"|-|NULL");
					}
					this.checkDeend(lParentElem);
				}
			}

			if (l_node.hasChildNodes()) {
				iterDocs(l_node);
			}
		}
	}

	public Map<String, Integer> getVOKeyCount() {
		return VOKeyCount;
	}
	public boolean getmDebug() {
		return mDebug;
	}

	public void setmDebug(boolean mDebug) {
		this.mDebug = mDebug;
	}


	@Override
	public void run() {
		this.process();
	}
	
	public String getStartPath() {
		return startPath;
	}
	
	public void setStartPath(String startPath) {
		this.startPath = startPath;
	}

}
