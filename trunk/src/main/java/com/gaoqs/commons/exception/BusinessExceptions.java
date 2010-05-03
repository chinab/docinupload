package com.gaoqs.commons.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义异常
 */
public class BusinessExceptions extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 异常集合
	 */
	private List<Exception> exceptions = new ArrayList<Exception>(); // 异常集合

	public void add(Exception exception) {
		exceptions.add(exception);
	}

	public BusinessExceptions() {
		super();
	}

	public BusinessExceptions(String message) {
		super();
		exceptions.add(new Exception(message));
	}

	public BusinessExceptions(String message, String[] params) {
		super();
		String temp = "";

		for (String p : params) {
			temp = temp + "##" + p;
		}
		exceptions.add(new Exception(message + temp));
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}

	public int size() {
		return exceptions.size();
	}

	/**
	 *  重载getMessage()
	 */
	public String getMessage() {
		String ret = "";
		for (Exception e : exceptions) {
			ret = ret + e.getMessage() + "@;@";
		}
		return ret;
	}

	/**
	 * 得到异常的trace的字符串
	 * 
	 */
	public static String getDetailTrace(Exception e) {
		StackTraceElement[] tracks = e.getStackTrace();
		StringBuffer sb = new StringBuffer();
		sb.append(e.toString() + "\r\n");
		for (StackTraceElement trace : tracks) {
			sb.append("\r\n\tat " + trace);
		}
		return sb.toString();
	}

}