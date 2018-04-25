package com.mh.jdbc.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class commonMailtest {

	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
		 HtmlEmail email = new HtmlEmail();
		 email.setAuthentication("ming.c.chen@oracle.com", "1MarGaimima");
		  email.setHostName("stbeehive.oracle.com");
		  email.setSmtpPort(465);
		  email.setSSLOnConnect(true);
		  try {
			email.addTo("ming.c.chen@oracle.com", "ming");
		
		  email.setFrom("minhua.yao@oracle.com", "Minhua.Yao");
		  email.setSubject("Test email with inline image");
		  
		  // embed the image and get the content id
//		  URL url = new URL("http://www.apache.org/images/asf_logo_wide.gif");
//		  String cid = email.embed(url, "Apache logo");
		  
		  // set the html message
		  HTMLTableBuilder htmlBuilder = new HTMLTableBuilder(null, true, 2, 3);
		  htmlBuilder.addTableHeader("1H", "2H", "3H");
		  htmlBuilder.addRowValues("1", "2", "3");
		  htmlBuilder.addRowValues("4", "5", "6");
		  htmlBuilder.addRowValues("9", "8", "7");
		  String table = htmlBuilder.build();
//		  System.out.println(table.toString());
		  email.setHtmlMsg("<html>The apache logo - <img src=\"http://www.apache.org/images/asf_logo_wide.gif\"></html>"+table);

		  // set the alternative message
		  email.setTextMsg("Your email client does not support HTML messages");

		  // send the email
		  email.send();
		  } catch (EmailException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
