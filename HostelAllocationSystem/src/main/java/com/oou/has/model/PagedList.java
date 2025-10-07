/**
 *
 */
package com.oou.has.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author AAfolayan
 *
 */
public class PagedList<T> {

	private List<T> list = new ArrayList<>();
	private int count;

	public PagedList() {
	}

	/**
	 * @param list
	 * @param count
	 */
	public PagedList(List<T> list, int count) {
		super();
		this.list = list;
		this.count = count;
	}

	/**
	 * @return the list
	 */
	public List<T> getList() {
		return list;
	}

	/**
	 * @param list
	 *            the list to set
	 */
	public void setList(List<T> list) {
		this.list = list;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

}
