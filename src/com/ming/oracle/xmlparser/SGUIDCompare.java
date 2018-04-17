package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SGUIDCompare{
	/**
	 * First it will parse all files within the transaction.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usages:SGUIDCompare fileList RefName");
			System.exit(2);
		}
		
		Integer lthreadCount = 10;
		String lfileName = args[0];

		File lrefFile = new File(args[1]);
		File file = new File(lfileName);
		
        BufferedReader reader = null;
        BufferedReader refreader = null;
        
        try {
//            System.out.println("");
        	ConcurrentHashMap<String, String > gMap = new ConcurrentHashMap<String, String>();
        	refreader = new BufferedReader(new FileReader(lrefFile));
            reader = new BufferedReader(new FileReader(file));
            ConcurrentHashMap<String, String> rowkeyMap = new ConcurrentHashMap<String, String>();
            HashSet<String> lkeyset = new HashSet<String>();
            String lRefLine = null;
            while((lRefLine = refreader.readLine()) != null){
            	/*
            	 * SGUID should be the key of the map 
            	 * <SGUID> => <Path
            	 * SGUID should not be changed between Branches.
            	 */
            	String []lRefArray = lRefLine.split("\\|");
            	//format of the key voinstancesguid
            	gMap.put(lRefArray[1]+lRefArray[4], lRefArray[0]+"|"+lRefArray[2]+"|"+lRefArray[5]);
            	//format rowkey sguid
            	rowkeyMap.put(lRefArray[2], lRefArray[4]);
            }
            SGUIDValidator svad = new SGUIDValidator();
            String lLine = null;
            // Read one line at one time util null encountered
            ExecutorService lThreadPool = Executors.newFixedThreadPool(lthreadCount);
            svad.setSguidVOInfo(gMap);
            svad.setRowkeySguidMap(rowkeyMap);
            while ((lLine = reader.readLine()) != null) {
            	lThreadPool.execute(new Compare(lLine, svad));
            }
            lThreadPool.shutdown();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	}
}