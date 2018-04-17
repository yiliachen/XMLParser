package com.mh.jdbc.util;

import java.util.LinkedHashMap;

public class CaseInsensitiveLinkedHashMap extends LinkedHashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2004723003445062897L;

	public CaseInsensitiveLinkedHashMap(int columnCount) {
		super(columnCount);
	}

	@Override
	public Object get(Object key) {
		return super.get(key.toString().toUpperCase());
	}

}
