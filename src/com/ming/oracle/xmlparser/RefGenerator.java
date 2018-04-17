package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RefGenerator{
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usages:CheckSguid fileList threadcount");
			System.exit(2);
		}
		
		Integer lthreadCount = 10;
		String lfileName = args[0];

		try{
			lthreadCount = Integer.parseInt(args[1]);
		}catch(Exception e){
			e.printStackTrace();
		}
		File file = new File(lfileName);
        BufferedReader reader = null;
        
        try {
//            System.out.println("");
        	ConcurrentHashMap<String, String > gMap = new ConcurrentHashMap<String, String>();
            reader = new BufferedReader(new FileReader(file));
            String lLine = null;
            // Read one line at one time util null encountered
            ExecutorService lThreadPool = Executors.newFixedThreadPool(lthreadCount);
            while ((lLine = reader.readLine()) != null) {
                // 显示行号
//            	lThreadPool.execute(new AttributeProcessor(lLine));
//                System.out.println("line " + line + ": " + lLine);
//            	  AttributeProcessor ap = new AttributeProcessor(lLine);
//            	  ap.setgMap(gMap);
//                  lThreadPool.execute(ap);
            	lThreadPool.execute(new FileKeySGUID(lLine, gMap));
//                  lThreadPool.execute(new DeRecValidate(lLine));
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