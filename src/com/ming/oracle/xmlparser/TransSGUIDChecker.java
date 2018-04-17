package com.ming.oracle.xmlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class TransSGUIDChecker{
	public static void main(String[] args) {
		HashSet<String> sguidset = new HashSet<String>();
		Boolean flag = false;
		if (args.length != 1) {
			System.err.println("TransSGUIDChecker RefFile List ThreadCount");
			System.exit(2);
		}
		
		String lRefFileName = args[0];

		File refFile = new File(lRefFileName);
        BufferedReader refreader = null;
        try {
//            System.out.println("");
        	refreader = new BufferedReader(new FileReader(refFile));
        	ConcurrentHashMap<String, String > gMap = new ConcurrentHashMap<String, String>();
        	String Refline = null;
        	while((Refline = refreader.readLine()) != null){
        		String []RefArray = Refline.split("\\|");
        		if(RefArray[4].equals("NONE")){
        			System.out.println("NullSGUID|"+Refline);
        			continue;
        		}
        		if(RefArray[3].equals("N")){
        			if(flag){
        				flag = false;
        				if(sguidset.size() > 1){
        					System.out.println("InValidDERec|"+RefArray[0]+sguidset.toArray());
        				}
        				sguidset.clear();
        			}
        			if(gMap.containsKey(RefArray[4])){
        				System.out.println("Duplicated|"+gMap.get(RefArray[4])+"|"+Refline);
        			}
        			gMap.put(RefArray[4], RefArray[0]+"|"+RefArray[2]+"|"+RefArray[4]);
        		}
        		if(RefArray[3].equals("Y")){
        			if(!flag )
        			{
        				flag = true;
        			}
        			sguidset.add(RefArray[4]);
        		}
        	}
        	refreader.close();
       } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (refreader != null) {
                try {
                    refreader.close();
                } catch (IOException e1) {
                }
            }
        }	
	}
}