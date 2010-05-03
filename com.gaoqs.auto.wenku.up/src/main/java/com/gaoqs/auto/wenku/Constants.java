package com.gaoqs.auto.wenku;

/**
 * 数据库key
 * @author jeff gao
 * 2010-3-24
 */
public class Constants {
	/**
	 * 一次开启的上传线程数
	 */
	public static String DOCIN_UP_THREAD_NUM="docin.up.thread.num";
	
	/**
	 * 上传webservice地址
	 */
	public static String DOCIN_WEBSERVER_URL="docin.webserver.url";
	
	/**
	 * 上传方法
	 */
	public static String DOCIN_WEBSERVER_METHOD_URL="docin.webserver.method.url";

	/**
	 * 登录地址
	 */
	public static String DOCIN_LOGIN_URL="docin.login.url";
	
	/**
	 * 浏览器类型
	 */
	public static String DOCIN_UPLOAD_USER_AGENT="docin.upload.user.agent";
	
	/**
	 * 上传根目录
	 */
	public static String DOCIN_BASE_UPLOAD_FOLDER="docin.base.upload.folder";
	
	/**
	 * 是否包含子目录
	 */
	public static String DOCIN_INCLUDE_SUB="docin.include.sub";
	
	/**
	 * 上传编码
	 */
	public static String DOCIN_ENCODING="docin.encoding";
	
	public static String DOCIN_CHANGE_USER_TIME="docin.change.user.time";
	
	/**
	 * 默认的上传线程数
	 */
	public static int DEFAULT_UP_THREAD_NUM=5;
	
	public static String DEFAULT_DOCIN_WEBSERVER_URL="http://www.docin.com/services/FileNameCheckWebService?wsdl";
	
	public static String DEFAULT_DOCIN_WEBSERVER_METHOD_URL="http://www.docin.com/services/FileNameCheckWebService";

	public static String DEFAULT_DOCIN_LOGIN_URL="http://www.docin.com/app/login";
	
	public static String DEFAULT_DOCIN_UPLOAD_USER_AGENT="Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)";
	
	public static String DEFAULT_DOCIN_BASE_UPLOAD_FOLDER="D:/wenku/";
	
	public static String DEFAULT_DOCIN_INCLUDE_SUB="true";
	
	public static String DEFAULT_DOCIN_ENCODING="UTF-8";
	
	/**
	 * 默认文档收费
	 */
	public static String DEFAULT_DOCIN_MONEY="0.00";
	
	/**
	 * 默认上传切换用户时间(分钟)
	 */
	public static int DEFAULT_DOCIN_CHANGE_USER_TIME=20;
	
}
