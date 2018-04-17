package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.xmlpaser.AttributeProcessor;

public class xmlParser{
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usages:CheckSguid fileList threadcount RefFile");
			System.exit(2);
		}
		
		Integer lthreadCount = 10;
		String lfileName = args[0];
		String lRefFileName = args[2];

		try{
			lthreadCount = Integer.parseInt(args[1]);
		}catch(Exception e){
			e.printStackTrace();
		}
		File file = new File(lfileName);
		File refFile = new File(lRefFileName);
        BufferedReader reader = null;
        
        try {
        	//Load the preChecked List
        	SGUIDValidator svad = new SGUIDValidator();
        	
        	ConcurrentHashMap<String, String > sguidInfo = new ConcurrentHashMap<String, String>();
        	ConcurrentHashMap<String, String> rowkeySguidMap = new ConcurrentHashMap<String, String>();
        	ConcurrentHashMap<String, String> sguidvoMap = new ConcurrentHashMap<String, String>();
        	
        	BufferedReader refreader = new BufferedReader(new FileReader(refFile));
        	String Refline = null;
        	while((Refline = refreader.readLine()) != null){
        		String []RefArray = Refline.split("\\|");
        		if(RefArray[3].equals("N")){
        			sguidInfo.put(RefArray[4], RefArray[0]+"|"+RefArray[2]+"|"+RefArray[4]);
        			rowkeySguidMap.put(RefArray[2], RefArray[4]);
        			sguidvoMap.put(RefArray[1]+RefArray[4], RefArray[0]+"|"+RefArray[2]+"|"+RefArray[5]);
        		}
        	}
        	refreader.close();
            reader = new BufferedReader(new FileReader(file));
            String lLine = null;
            ExecutorService lThreadPool = Executors.newFixedThreadPool(lthreadCount);
            svad.setRowkeySguidMap(rowkeySguidMap);
        	svad.setSguidInfo(sguidInfo);
        	svad.setSguidVOInfo(sguidvoMap);
        	
            while ((lLine = reader.readLine()) != null) {
//            	Compare comp = new Compare(lLine, svad);
            	  AttributeProcessor ap = new AttributeProcessor(lLine);
            	  ap.setSvd(svad);
                  lThreadPool.execute(ap);
//                  lThreadPool.execute(comp);
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