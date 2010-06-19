package com.gaoqs.commons.string;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.commons.exception.BusinessExceptions;

public class StringProcess {

	private static final Log log=LogFactory.getLog(StringProcess.class);
	/**
	 * 截取字符串  
	 * etc.   processDetail("<a href='g.cn'</td>","href='","</td","href='".length(),"</td".length()-1)，返回g.cn
	 * @param tempStr 原字串 
	 * @param startFlag 开始标识
	 * @param endFlag 结束标识
	 * @param starts 开始+
	 * @param ends 结束-
	 * @return
	 */
	public static String processDetail(String tempStr,String startFlag,String endFlag,int starts,int ends){
		if(tempStr==null || "".equals(tempStr)) {
			log.warn("input string is blank");
			return "";
		}
		int start=0;
		if(startFlag!=null && !"".equals(startFlag.trim())){
			start=tempStr.indexOf(startFlag);
		}
		int end=tempStr.length();
		if(endFlag!=null && !"".equals(endFlag.trim())){
			//默认结束符在开始符之后
			end=tempStr.indexOf(endFlag,start);
		}
		if(start==-1 || end==-1 || (end-ends)<(start+starts)) {
			log.warn("string index is error:"+start+","+end+","+starts+","+ends);
			return "";
		}
		try{
			tempStr=tempStr.substring(start+starts,end-ends);
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		return tempStr;
	}
	
	/**
	 * 匹配所有的字符串
	 * @param tempStr
	 * @param startFlag
	 * @param endFlag
	 * @param starts
	 * @param ends
	 * @return
	 */
	public static List processAllDetail(String tempStr,String startFlag,String endFlag,int starts,int ends){
		List result=new ArrayList();
		if(tempStr==null || "".equals(tempStr)) {
			log.warn("input string is blank");
			return result;
		}
		int start=0;
		while(true){
			if(startFlag!=null && !"".equals(startFlag.trim())){
				start=tempStr.indexOf(startFlag);
			}
			int end=tempStr.length();
			if(endFlag!=null && !"".equals(endFlag.trim())){
				//默认结束符在开始符之后
				end=tempStr.indexOf(endFlag,start);
			}
			if(start==-1 || end==-1 || (end-ends)<(start+starts)) {
				log.warn("string index is error:"+start+","+end+","+starts+","+ends);
				break;
			}
			try{
				result.add(tempStr.substring(start+starts,end-ends));
				tempStr=tempStr.substring(end-ends);
			}catch(Exception e){
				log.error(BusinessExceptions.getDetailTrace(e));
			}
		}
		return result;
	}
	
	/**
	 * 正则式匹配结果 复杂模式
	 * @param context
	 * @param rex
	 * @param startFlag
	 * @param endFlag
	 * @param starts
	 * @param ends
	 * @return
	 */
	public static List<String> processRegx(String context,String rex,String startFlag,String endFlag,int starts,int ends){
		Matcher mc = Pattern.compile(rex).matcher(context);
		List<String> resultList=new ArrayList<String>();		
		while(mc.find()){
			String tempStr=mc.group();
			try{
				tempStr=processDetail(tempStr,startFlag,endFlag,starts,ends);
				resultList.add(tempStr);
			}catch(Exception e){
				log.error(BusinessExceptions.getDetailTrace(e));
			}
		}
		return resultList;
	}
	
	/**
	 * 正则式匹配结果 返回单个
	 * @param context
	 * @param rex
	 * @return
	 */
	public static String processRegxSingle(String context,String rex,String startFlag,String endFlag,int starts,int ends){
		List<String> list=processRegx(context,rex,startFlag,endFlag,starts,ends);
		if(list!=null && list.size()>0) return list.get(0);
		log.warn("unfind the match characters:"+rex);
		return "";
	}
	
	/**
	 * 正则式匹配结果 简单模式
	 * @param context
	 * @param rex
	 * @return
	 */
	public static List<String> processRegx(String context,String rex){
		return processRegx(context,rex,null,null,0,0);
	}
	
	/**
	 * 正则式匹配结果 返回单个
	 * @param context
	 * @param rex
	 * @return
	 */
	public static String processRegxSingle(String context,String rex){
		List<String> list=processRegx(context,rex,null,null,0,0);
		if(list!=null && list.size()>0) return list.get(0);
		log.warn("unfind the match characters:"+rex);
		return "";
	}
	
	/**
	 * 替换多个成空
	 * @param in
	 * @param replaces
	 * @return
	 */
	public static String replaceAll(String in ,String[] replaces){
		if(in==null || "".equals(in.toString())) return "";
		if(replaces==null) return in;
		for (String str : replaces) {
			in=in.replace(str, "");
		}
		return in;
		
	}
}
