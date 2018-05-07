package com.ming.oracle.xmlparser;

import com.mh.jdbc.util.DBUtil;

public class PostEmail {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			if(args.length != 1){
				System.out.println("Usage:PostEmail config");
				System.exit(-1);
			}
			DBUtil.initConfig(args[0]);
			postToApex self = new postToApex();
			self.postmail();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
