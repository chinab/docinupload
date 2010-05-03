package com.gaoqs.commons.path;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * 得到jar包的绝对路径
 * 
 */
public class RealPath {
	
	/**
	 * 根据RealPath的class编译路径得到工程所有的目录
	 * 
	 * @param theObject class对象所在的目录
	 * @return 工程所在的目录
	 */
	@SuppressWarnings("unchecked")
	public static String getBasePath(Object theObject) {
			if(theObject==null) theObject=new RealPath();
			Class claz=null;
			if(theObject instanceof Class){
				claz=(Class)theObject;
			}else{
				claz=theObject.getClass();
			}
			URL path = claz.getProtectionDomain().getCodeSource().getLocation();
			String realPathDistination = path.getPath();
			// win32下形如:/D:/workspace-aplan2/cn.tsoft.common/bin/
			if (realPathDistination == null)
				return "";
			realPathDistination = realPathDistination.replace("\\", "/");
			//去掉最后的/
			if(realPathDistination.endsWith("/")) realPathDistination=realPathDistination.substring(0,realPathDistination.length()-1);
			int lastPathIndex=realPathDistination.lastIndexOf("/");
			if(lastPathIndex!=-1){
				realPathDistination=realPathDistination.substring(0, lastPathIndex+1);
			}			
			return realPathDistination;
	}

	/**
	 * 加载配置文件
	 * 
	 * @param path
	 *            配置文件地址
	 * @return
	 */
	public static Properties loadConfigFile(String path) {
		Properties pro = new Properties();
		InputStream input=null;
		try {
			input = new FileInputStream(new File(path));
			pro.load(input);
			
		} catch (IOException e) {
			e.printStackTrace();
			return pro;
		}finally{
			if(input!=null)
				try {
					input.close();
				} catch (IOException e) {
				}
		}
		return pro;
	}

	/**
	 * 加载配置文件
	 * 
	 * @param path
	 *            配置文件地址
	 * @return
	 */
	public static Properties loadConfigFileByString(String content) {
		Properties pro = new Properties();
		try {
			InputStream input = new ByteArrayInputStream(content.getBytes());
			pro.load(input);
		} catch (IOException e) {
			e.printStackTrace();
			return pro;
		}
		return pro;
	}
	
	/**
	 * 创建多级目录
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createMultiFolders(String path) {
		if (path == null)
			return true;
		path = path.replace("\\", "/");
		try {
			StringTokenizer st = new StringTokenizer(path, "/");
			String path1 = st.nextToken() + "/";
			String path2 = path1;
			while (st.hasMoreTokens()) {
				path1 = st.nextToken() + "/";
				path2 += path1;
				File inbox = new File(path2);
				if (!inbox.exists())
					inbox.mkdir();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
}
