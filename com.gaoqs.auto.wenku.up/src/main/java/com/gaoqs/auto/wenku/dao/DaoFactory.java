package com.gaoqs.auto.wenku.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.path.RealPath;
import com.sun.media.Log;

/**
 * 用于产生beanfactory
 * 
 * @author jeff gao
 * 2010-3-24
 */
public class DaoFactory {

	private static ApplicationContext context;
	private static String sqlSelect = "spring/applicationContext.xml";

	static {
		context = new FileSystemXmlApplicationContext(RealPath.getBasePath(DaoFactory.class)+"classes/"+sqlSelect);
	}
	
	@SuppressWarnings("unchecked")
	public static <C> C  getBean(Class<C> entityClass,String beanName) {
		if(entityClass==null || beanName==null  || "".equals(beanName.trim())) return null;
		try{
			return (C)context.getBean(beanName);
		}catch(Exception e){
			Log.error("get bean error:"+beanName+"@class:"+entityClass+"\n"+BusinessExceptions.getDetailTrace(e));
		}
		return null;
	}
	
	public static void main(String a[]){
		new DaoFactory();
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
