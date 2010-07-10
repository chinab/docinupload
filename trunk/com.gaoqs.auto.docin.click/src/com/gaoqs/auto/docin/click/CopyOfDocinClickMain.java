package com.gaoqs.auto.docin.click;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.httpclient.HttpClientProcess;
import com.gaoqs.commons.path.RealPath;
import com.gaoqs.commons.string.StringProcess;


public class CopyOfDocinClickMain implements Runnable{
	
	/**
	 * 请求文档序列soap信息
	 */
	private static String CHECKFILENAME_SOAP="<?xml version=\"1.0\" encoding=\"utf-8\"?><SOAP-ENV:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><checkFileName xmlns=\"http://web.service.projectx.wonibo.com\"><in0>${type}</in0><in1>${style}</in1></checkFileName></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	/**
	 * 添加文档的soap信息
	 */
	private static String ADDPRODUCT_SOAP="<?xml version=\"1.0\" encoding=\"utf-8\"?><SOAP-ENV:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><addProduct xmlns=\"http://web.service.projectx.wonibo.com\"><in0>${type}</in0></addProduct></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	
	private Log log=LogFactory.getLog(CopyOfDocinClickMain.class);
	
	private HttpClientProcess process;	
	
	private boolean hasStrated=false;
	
	/**
	 * 文档分类属性
	 */
	private Properties pro;
	
	/**
	 * 当前配置文件
	 */
	private File currentConfigFile;
	
	/**
	 * 是否停止线程
	 */
	public static boolean stopThread=false;
	
	private int defalutWaitTime=5;
	
	private String userNames="";
	
	private String urlType="每次增量添加";
	
	private static int loadTheSameNum=0;
	
	private Map<String,String> urlMap=new HashMap();
	
	private static Map<String,String> fileUrlMap=new HashMap();
	
	public static String DEFAULT_DOCIN_UPLOAD_USER_AGENT="Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)";
	
	public void run() {
		if(hasStrated){
			System.exit(0);
		}
		process=new HttpClientProcess();
		process.getHeaderMap().put("User-Agent", DEFAULT_DOCIN_UPLOAD_USER_AGENT);
		//验证码处理
		execute();
		hasStrated=true;
		stopThread=true;
	}
	
	private void execute() {
		//TODO 判断是否重复点击
		while (true) {	
			if(AutoBrowser.shell.isDisposed()){
				System.exit(0);
			}			
			String tempUserNames = AutoBrowser.property.getProperty("user_name");
			if (tempUserNames != null) {
				userNames = tempUserNames;
			}
			try {
				defalutWaitTime = Integer.parseInt(AutoBrowser.property.getProperty("interval_time"));
			} catch (Exception e) {
			}
			log.warn("next documents upload wait:" + defalutWaitTime);
			
			urlType= AutoBrowser.property.getProperty("url_type","每次增量添加");
			if(urlType.equals("只读取配置文件")){
				if(urlMap.keySet()!=null && urlMap.keySet().size()==0){
					//加载初始的链接
					loadUrls();
				}
			}else{
				if(urlType.equals("每次增量添加") && urlMap.keySet()!=null && urlMap.keySet().size()==0){
					//加载初始的链接
					loadUrls();
				}
				
				String [] userName=userNames.split(",");
				if(userName==null){
					AutoBrowser.infoMsg="用户名格式不正确";				
					return;
				}
				//广告链接
				String adUrls[]=null;
				try{
					String temp=process.get("http://www.gaoqs.com/docs/click_list.html", null);
					if(temp!=null){
						adUrls=temp.split(",");
					}
				}catch(Exception e){
					adUrls="http://www.docin.com/p-57057752.html,http://www.docin.com/p-54951341.html,http://www.docin.com/p-54509683.html,http://www.docin.com/p-53132050.html,http://www.docin.com/p-55622535.html".split(",");
				}
				//广告链接
				for (String name : userName) {
					if(AutoBrowser.shell.isDisposed()){
						System.exit(0);
					}
					if(adUrls!=null){
						for (String adUrl : adUrls) {
							urlMap.put(adUrl, name);
						}
					}
					AutoBrowser.title="豆丁点击　　初始化用户链接："+name;
					String url="";
					//设置为访问100
					String html=null;
					try{
						html=process.get("http://www.docin.com/"+name, null);
					}catch(Exception e){
						log.error("get url error:"+BusinessExceptions.getDetailTrace(e));
					}
					//解析得到url 如： mydoc-8982308-1
					if(html==null ) continue;
					String userIds=StringProcess.processRegxSingle(html, "mydoc-[0-9]*-1");
					if(userIds==null || userIds.trim().equals("")){
						log.error("process user error2:"+name);
						continue;
					}
					String userId=userIds.replace("mydoc-", "");
					userId=userId.substring(0,userId.indexOf("-"));
					loadTheSameNum=0;
					//访问不同的页面
					for(int j=1;;j++){
						if(AutoBrowser.shell.isDisposed()){
							System.exit(0);
						}
						//处理不同的页面
						String docPage="http://www.docin.com/mydoc-"+userId+"-"+j+".html&&folderId=0";
						System.out.println("处理页面："+docPage);
						html=null;
						try{
							html=process.get(docPage, null);
						}catch(Exception e){
							log.error("get url error:"+BusinessExceptions.getDetailTrace(e));
						}
						if(html==null ) continue;
						//processDetails(docPage, null, 10000+defalutWaitTime*1000, 10000+defalutWaitTime*1000);
						List<String> urlLists=StringProcess.processRegx(html, "/p-[0-9]*[.]html");
						if(urlLists==null || urlLists.size()==0){
							System.out.println("user process over:"+name);
							break;
						}
						for (String docUrl : urlLists) {
							if(AutoBrowser.shell.isDisposed()){
								System.exit(0);
							}
							if(fileUrlMap.get("http://www.docin.com"+docUrl)==null){
								System.out.println("添加链接："+docUrl);
								urlMap.put("http://www.docin.com"+docUrl, name);
							}else{
								System.out.println("增量完成");
								loadTheSameNum++;
							}
						}	
						//连续25篇一样，则不取了
						if(urlType.equals("每次增量添加") && loadTheSameNum>25) {
							break;
						}
					}				
				}
			
			}
			
			//保存配置链接
			if(!urlType.equals("只读取配置文件")){
				saveUrls();
			}
			
			fileUrlMap=urlMap;
			
			AutoBrowser.title="豆丁点击　　正在点击...";
			//开始点击链接
			for (String url : urlMap.keySet()) {
				if(AutoBrowser.shell.isDisposed()){
					System.exit(0);
				}
				AutoBrowser.currentUrl=url;
				System.out.println("process url:"+url);
				
				 try {
					 Thread.sleep(defalutWaitTime*1000);
				 } catch (InterruptedException e) {
					 e.printStackTrace();
				 }
				//processDetails(url, null, defalutWaitTime*1000, 0);
			}
		}
		
	}
	
	/**
	 * 打开网页，并执行脚本
	 * @author Jeff
	 * @date 2010-5-26 下午02:35:16
	 * @param url 网页
	 * @param scripts 脚本
	 * @param openWaitTime 打开网页后的等待时间
	 * @param afterWaitTime 执行脚本后的等待时间
	 */
	private void processDetails(String url,String scripts,int openWaitTime,int afterWaitTime){
		try{
			if(url!=null){
				log.warn("execute url\n" + url);
				AutoBrowser.currentUrl = url;
				try {
					if (openWaitTime + Constants.BASE_WAIT_TIME < 0) {
						Thread.sleep(30000);
					} else {
						Thread.sleep(openWaitTime + Constants.BASE_WAIT_TIME);
					}
				} catch (InterruptedException e) {
					log.error(BusinessExceptions.getDetailTrace(e));
				}
			}
			if(scripts!=null){
				AutoBrowser.executeScript=scripts;
				log.warn("execute url over");
				try {
					if (afterWaitTime + Constants.BASE_WAIT_TIME < 0) {
						Thread.sleep(30000);
					} else {
						Thread.sleep(afterWaitTime + Constants.BASE_WAIT_TIME);
					}
				} catch (InterruptedException e) {
					log.error(BusinessExceptions.getDetailTrace(e));
				}
			}
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
	}

	
	private void saveUrls(){
		Map<String,StringBuffer> tempMap=new HashMap<String,StringBuffer>();
		String baseFolder="";
		try{
			baseFolder=new File(".").getAbsolutePath();
			baseFolder=baseFolder.substring(0, baseFolder.length()-1);
			RealPath.createMultiFolders(baseFolder+"/click/");
			for (String key : urlMap.keySet()) {
				if(tempMap.get(urlMap.get(key))==null){
					StringBuffer sb=new StringBuffer();
					sb.append(key+"\r\n");
					tempMap.put(urlMap.get(key), sb);
				}else{
					tempMap.get(urlMap.get(key)).append(key+"\r\n");
				}
			}	
			for (String key : tempMap.keySet()) {
				FileUtils.writeStringToFile(new File(baseFolder+"/click/"+key+".txt"), tempMap.get(key).toString(), "UTF-8");
			}
			
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		
	}
	
	private void loadUrls(){		
		String baseFolder="";
		try{
			baseFolder=new File(".").getAbsolutePath();
			baseFolder=baseFolder.substring(0, baseFolder.length()-1);
			RealPath.createMultiFolders(baseFolder+"/click/");
			File folders=new File(baseFolder+"/click/");
			if(folders.exists()){
				File files[]=folders.listFiles();
				for (File file : files) {
					List<String> lines=FileUtils.readLines(file, "utf-8");
					String type=file.getName();
					if(file.getName().lastIndexOf(".")!=-1){
						type=file.getName().substring(0,file.getName().lastIndexOf("."));
					}
					for (String str : lines) {
						if(str!=null && !"".equals(str.trim())){
							urlMap.put(str, type);
							fileUrlMap.put(str, type);
						}
					}				
				}
			}	
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		
	}
	

}




