package com.mh.jdbc.util;

import java.util.ArrayList;
import java.util.List;

public class Pager<T> {

	private int totalCount = 0;
	private List<T> resultSet = new ArrayList<T>();

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getResultSet() {
		return resultSet;
	}

	public void setResultSet(List<T> resultSet) {
		this.resultSet = resultSet;
	}
	
	public void addResultSet(T result) {
		resultSet.add(result);
	}

}
