package com.gaoqs.auto.wenku.click;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.auto.wenku.Constants;
import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.model.DocinProcessLog;
import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.httpclient.HttpClientProcess;
import com.gaoqs.commons.string.StringProcess;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * 模拟点击豆丁文档<p>
 * 下载所有的用户的文档链接
 * @author jeff gao
 *
 */
public class CheckUserDocsMain implements Runnable {

	private Log log=LogFactory.getLog(CheckUserDocsMain.class);
	
	private String USER_NAME;
	
	private String PASSWORD;
	/**
	 * 登录地址
	 */
	private static String LOGIN_URL=Constants.DEFAULT_DOCIN_LOGIN_URL;
	
	private static String BASE_DOC_URL="http://www.docin.com/app/my/docin/myBook?styleList=1&orderName=0&orderDate=1&orderVisit=0&orderStatus=0&orderFolder=0&folderId=0&myKeyword=&currentPage=";
	
	public static void main(String args[]){
		new DaoFactory();
		int start=1;
		int end=200;
		if(args!=null && args.length>1){
			try{
				start=Integer.parseInt(args[1]);
			}catch(Exception e ){
			}			
		}
		if(args!=null && args.length>2){
			try{
				end=Integer.parseInt(args[2]);
			}catch(Exception e ){
			}			
		}
		
		for(int i=start;i<=end;i++){
			DocinUserDao dao=DaoFactory.getBean(DocinUserDao.class, "docinUserDao");
			List<Map<String,String>> list=dao.getMapListByHql("select name as name,password as password from DocinUserModel where id='"+i+"'");
			if(list!=null && list.size()>0 && list.get(0)!=null){
				Map<String,String> map=list.get(0);
				CheckUserDocsMain cud=new CheckUserDocsMain(map.get("name"),map.get("password"));
				cud.run();
			}
		}
	}
	
	public CheckUserDocsMain(){		
	}
	
	public CheckUserDocsMain(String userName,String password){
		USER_NAME=userName;
		PASSWORD=password;
		log.warn("process user:"+userName);
	}
	
	public void run() {
		Selenium browser = new DefaultSelenium("localhost", 4444, "*iehta",LOGIN_URL);
		// 启动服务
		browser.start();
		browser.setTimeout("120000");
		browser.open(LOGIN_URL);
		browser.type("username", USER_NAME);
		browser.type("password", PASSWORD);
		log.warn("process user:"+USER_NAME);
		browser.click("xpath=//input[@value='登录']");		
		try {
			//关闭后待10秒
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			log.error("wait error:"+BusinessExceptions.getDetailTrace(e));
		}
		String cookies=browser.getCookie();
		browser.close();
		browser.stop();
		browser=null;
		//得到cookies后，用httpclient模拟
		HttpClientProcess process=new HttpClientProcess();
		process.getHeaderMap().put("Cookie", cookies);
		process.getHeaderMap().put("User-Agent", Constants.DEFAULT_DOCIN_UPLOAD_USER_AGENT);
		DocinUserDao dao=DaoFactory.getBean(DocinUserDao.class, "docinUserDao");
		//是否处理过最后一页
		boolean lastProcess=false;
		for (int i = 1;; i++) {
			String resultStr=process.get(BASE_DOC_URL+i, null);
			if(i==1){
				//第1页，记录名称和金钱
				String userNetName=StringProcess.processDetail(resultStr, "username", "</a>", 0, 0);
				userNetName=userNetName.replaceAll("username.*>", "");
				String moneyStr=StringProcess.processDetail(resultStr, "￥", "</a>", "￥".length(), 0);
				moneyStr=moneyStr.replaceAll("<a.*>", "");
//				System.out.println("钱："+moneyStr);
//				System.out.println("名："+userNetName);
				dao.executeHql("update DocinUserModel set money='"+moneyStr+"',netName='"+userNetName+"' where name='"+USER_NAME+"'");
			}
			//判断是否有文档，若有，处理存数据库，没有则break
			//<a href="/p-48945079.html
			List urlLists=StringProcess.processRegx(resultStr, "<a href=\\\"/p-[^title]*html");
			//html" title="
			List titleLists=StringProcess.processRegx(resultStr, "html\" title=\"[^\\\"]*\\\"");			
			//<td width="53%" height="27" align="center"></td>
			String rex="<td width=\\\"53\\\\%\\\" height=\\\"27\\\" align=\\\"center\\\">[^<td]*</td>";
			List statusList=StringProcess.processAllDetail(resultStr, "<td width=\"53%\" height=\"27\" align=\"center\">", "</td>", "<td width=\"53%\" height=\"27\" align=\"center\">".length(), "</td>".length());
			
			if(urlLists==null || urlLists.size()==0 || titleLists==null || titleLists.size()==0)
				break;
			//判断<!-- 下一页 -->的标记中是否有值，若没有，则已经到达最后一页
			String nextHasedStr=StringProcess.processDetail(resultStr, "<!-- 下一页 -->", "</div>", 0,0);
			if(nextHasedStr==null) break;
			//保证最后一页处理到
			if(lastProcess) break;
			if(nextHasedStr.indexOf("<a href")==-1) {
				lastProcess=true;
			}
			
			//循环内部的各个文档，并记录数据库
			for (int j = 0; j < urlLists.size(); j++) {
				if(statusList!=null && statusList.size()>j && statusList.get(j)!=null){
					//处理未发布状态的情况(审核中，转换中，转换失败)
					String tempStr=statusList.get(j).toString();
					if(tempStr.indexOf("已发布")==-1 ) continue;					
				}
				DocinProcessLog log=new DocinProcessLog();
				if(urlLists.get(j)!=null)
				log.setUrl("http://www.docin.com/"+urlLists.get(j).toString().replace("<a href=\"/", ""));
				log.setAuther(USER_NAME);
				List list =dao.find(log);
				if(list==null || list.size()==0){
					//没有查到，保存
					if(titleLists!=null && titleLists.size()>j && titleLists.get(j)!=null){
						String temp=titleLists.get(j).toString().replace("html\" title=\"", "");
						temp=temp.replace("\"", "");
						log.setName(temp);
						log.setProcessTime(new Date());
						log.setStatus("0");
						dao.saveSimple(log);
					}
				}
			}
		}
		//退出
		process.get("http://www.docin.com/app/loginOut", null);		
	}
}
