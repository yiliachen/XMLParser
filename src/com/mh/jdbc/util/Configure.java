package com.mh.jdbc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Configure {

	private static String dbType;
	private static String driverClassName;
	private static String url;
	private static String username;
	private static String password;

	public static void initConfig(String configPath) throws Exception {
		File file = new File(configPath);
//		File file = new File("./src/database.properties");
//		System.out.println("Read from "+configPath);
		InputStream fis = new FileInputStream(file);
		Properties props = new Properties();
		props.load(fis);
		dbType = props.getProperty("db.type");
		driverClassName = props.getProperty("driver.class.name");
		url = props.getProperty("connection.url");
		username = props.getProperty("connection.username");
		password = props.getProperty("connection.password");
//		System.out.print(dbType+driverClassName+url+username+password);
	}

	public static String getDbType() {
		return dbType;
	}

	public static String getDriverClassName() {
		return driverClassName;
	}

	public static String getUrl() {
		return url;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

}
