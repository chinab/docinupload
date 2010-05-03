package com.gaoqs.auto.wenku.click;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.auto.wenku.dao.BaseDao;
import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.commons.exception.BusinessExceptions;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * 自动点击器
 * @author jeff gao
 *
 */
public class AutoClickMain {

	private static Log log=LogFactory.getLog(AutoClickMain.class);
	
	private static Selenium browser=null;
	
	public static void main(String args[]){
		//记录当前的网址
		new DaoFactory();
		BaseDao dao=DaoFactory.getBean(BaseDao.class, "baseDao");
		dao.executeHql("update DocinProcessLog set status='0'");
		
		String hql="select id as id,url as url from DocinProcessLog where status='0'";
		while(true){
			List<Map<String,Object>> list=dao.getMapListByHqlPage(hql, 0, 10);
			int r=new Random(new Date().getTime()).nextInt();
			if(r<0) r*=-1;
			if(list==null || list.size()==0){
				//一天只处理一次访问
				log.warn("process over....");
				return;
			}
			Map<String,Object> mapConfig=list.get(r%list.size());
			String loginUrl=mapConfig.get("url").toString();
			if(browser==null){
				browser = new DefaultSelenium("localhost", 4444, "*iehta",loginUrl);
				// 启动服务
				browser.start();
				browser.setTimeout("120000");
			}
			log.warn("process url:"+loginUrl);
			browser.open(loginUrl);
			dao.executeHql("update DocinProcessLog set status='1' where id='"+mapConfig.get("id")+"'");
			try {
				Thread.sleep(20*1000);
			} catch (InterruptedException e) {
				log.error("wait error:"+BusinessExceptions.getDetailTrace(e));
			}
		}
		
	}
}
