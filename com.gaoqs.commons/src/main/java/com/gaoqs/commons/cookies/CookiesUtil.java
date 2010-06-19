package com.gaoqs.commons.cookies;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.gaoqs.commons.exception.BusinessExceptions;

public class CookiesUtil {

	private static Log log=LogFactory.getLog(CookiesUtil.class);
	/**
	 * 存储本地 cookies地址
	 */
	public static String BASE_COOKIES_PATH="C:/Documents and Settings/Administrator/Cookies/";
	
	public static String getCookiesFile(String fileName){
		return getCookiesFile(fileName,true);
	}
	
	/**
	 * 读取cookies文件件内容
	 * @param fileName 文件名
	 * @param autoSearch 是否自动搜索 如：
	 * administrator@docin[1].txt administrator@docin[2].txt
	 * @return
	 */
	public static String getCookiesFile(String fileName,boolean autoSearch){
		String cookiesFileContent="";
		try{
			if(!autoSearch){
				//直接找指定的cookies文件
				cookiesFileContent=FileUtils.readFileToString(new File(fileName));
				return cookiesFileContent;
			}		
			for(int i=1;i<10;i++){
				String fileFullName=BASE_COOKIES_PATH+fileName+"["+i+"].txt";
				File file=new File(fileFullName);
				if(file.exists()){
					log.warn("read file:"+fileFullName);
					cookiesFileContent=FileUtils.readFileToString(file);
					return cookiesFileContent;	
				}else{
					log.warn("cookies file not exists:"+fileFullName);
				}
			}	
		}catch(Exception e){
			log.error("process cookies file error:"+BusinessExceptions.getDetailTrace(e));
		}
		return cookiesFileContent; 
	}
	
	/**
	 * 将cookies文件内容字符串，转成cookies
	 * @param content
	 * @return
	 */
	public static List<Cookie> parseStringToCookies(String content) {
		List<Cookie> cookies = new ArrayList<Cookie>();
		BufferedReader in = new BufferedReader(new StringReader(content));
		String line="",name=null,value=null;
		int currentLineNum=0;
		try{
			while ((line = in.readLine()) != null) {
				currentLineNum++;
				if (line != null && !"".equals(line.trim())) {
					if(currentLineNum%9==1){
						name=line;
					}else if(currentLineNum%9==2){
						value=line;
					}
					if(name!=null && value!=null){
						BasicClientCookie tempCookie = new BasicClientCookie(name, value);
						cookies.add(tempCookie);
						name=null;
						value=null;
					}
				}
			}
		}catch(Exception e){
			log.error("parse cookies error:"+BusinessExceptions.getDetailTrace(e));
		}
		return cookies;
	}
	
	
//	public static void main(String args[]){
//		parseStringToCookies(getCookiesFile("administrator@docin"));
//	}
}
