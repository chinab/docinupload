package com.gaoqs.auto.wenku;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.dao.SysConfigDao;
import com.gaoqs.auto.wenku.model.DocinUserModel;
import com.gaoqs.auto.wenku.model.DocinUserUpModel;
import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.httpclient.HttpClientProcess;
import com.gaoqs.commons.path.RealPath;
import com.gaoqs.commons.soap.SoapRequest;
import com.gaoqs.commons.string.EncodingUtil;
import com.gaoqs.commons.string.StringProcess;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
/**
 * 
 * 豆丁上传程序
 * 
 * @author jeff gao
 * 
 */
public class DocinUploadMain  implements Runnable{

	private Log log = LogFactory.getLog(DocinUploadMain.class);

	/**
	 * 生成文件序列号的soap的webservice地址
	 */
	private String WEBSERVER_URL=Constants.DEFAULT_DOCIN_WEBSERVER_URL;
	
	private String WEBSERVER_METHOD_URL=Constants.DEFAULT_DOCIN_WEBSERVER_METHOD_URL;

	/**
	 * 登录地址
	 */
	private String LOGIN_URL=Constants.DEFAULT_DOCIN_LOGIN_URL;
	
	/**
	 * 浏览器类型，用于骗过服务器
	 */
	private String USER_AGENT=Constants.DEFAULT_DOCIN_UPLOAD_USER_AGENT;
	
	private String USER_NAME;
	
	private String PASSWORD;
	
	/**
	 * soap_addProduct请求模板
	 */
	private String ADDPRODUCT_SOAP="";
	
	/**
	 * soap_checkFileName请求模板
	 */
	private String CHECKFILENAME_SOAP="";
	
	/**
	 * 编码
	 */
	private String ENCODING=Constants.DEFAULT_DOCIN_ENCODING;	
	
	/**
	 * 上传路径
	 */
	private String UPLOAD_FOLDER=Constants.DEFAULT_DOCIN_BASE_UPLOAD_FOLDER;
	
	/**
	 * 是否包括子目录
	 */
	private String INCLUDE_SUB=Constants.DEFAULT_DOCIN_INCLUDE_SUB;
	
	/**
	 * 文档收费
	 */
	private String DOC_MONEY=Constants.DEFAULT_DOCIN_MONEY;
	
	/**
	 * 切换用户时间
	 */
	private int CHANGE_USER_TIME=Constants.DEFAULT_DOCIN_CHANGE_USER_TIME;
	
	/**
	 * 文件上传完后的标识
	 */
	private static String prefxStr="hasuploaded_";
	
	private HttpClientProcess process;	
	
	/**
	 * 是否已经启动上传
	 */
	private boolean started=false;
	
	/**
	 * 当前处理的文件类型 如.doc
	 */
	private String fileType="";
	
	/**
	 * 测试的浏览器
	 */
	private Selenium browser =null;
	
	/**
	 * 当前处理的配置id
	 */
	private String currentConfigId;
	
	/**
	 * 当前线程启动时间
	 */
	private long startTime;
	
	DocinUploadMain(Map<String, String> configMap,DocinUserModel upUser){
		if(configMap==null || upUser==null) return;
		init(configMap,upUser);		
	}
	
	private void init(Map<String, String> configMap,DocinUserModel upUser){
		SysConfigDao sysDao = DaoFactory.getBean(SysConfigDao.class,"sysConfigDao");
		WEBSERVER_URL = sysDao.getSysconfigValue(Constants.DOCIN_WEBSERVER_URL,WEBSERVER_URL);
		WEBSERVER_METHOD_URL=sysDao.getSysconfigValue(Constants.DOCIN_WEBSERVER_METHOD_URL,WEBSERVER_METHOD_URL);
		LOGIN_URL=sysDao.getSysconfigValue(Constants.DOCIN_LOGIN_URL,LOGIN_URL);
		ENCODING=sysDao.getSysconfigValue(Constants.DOCIN_ENCODING,ENCODING);
		//用户名，密码
		USER_NAME=upUser.getName();
		PASSWORD=upUser.getPassword();
		UPLOAD_FOLDER=sysDao.getSysconfigValue(Constants.DOCIN_BASE_UPLOAD_FOLDER,Constants.DEFAULT_DOCIN_BASE_UPLOAD_FOLDER);
		UPLOAD_FOLDER+=configMap.get("folderPath");		
		//是否包含子目录
		if("true".equals(configMap.get("includeSub")) || "false".equals(configMap.get("includeSub")))
			INCLUDE_SUB=configMap.get("includeSub");
		try{
			Double.parseDouble(configMap.get("docMoney"));
			DOC_MONEY=configMap.get("docMoney");
		}catch(Exception e){	
			e.printStackTrace();
		}
		currentConfigId=configMap.get("id");
		DocinUserDao docDao = DaoFactory.getBean(DocinUserDao.class,"docinUserDao");
		//保存状态不可用
		DocinUserUpModel currentUpUser=docDao.get(DocinUserUpModel.class, currentConfigId);
		currentUpUser.setLastProcessDate(new Date());
		currentUpUser.setStatus(DocinUserUpModel.IN_USE);
		docDao.saveSimple(currentUpUser);	
		startTime=new Date().getTime();
	}
	
	public void run() {
		while(true){
			try{
				if(started){
					//运行一段时间后，自动切换用户
					if(CHANGE_USER_TIME*60*1000<(new Date().getTime()-startTime)){
						try{
							processNextUser();
							startWebBrowser();
						}catch(Exception e){
							log.error("change user error:"+BusinessExceptions.getDetailTrace(e));
						}
						startTime=new Date().getTime();
					}
				}else{
					started=true;
					try{
						startWebBrowser();
					}catch(Exception e){
						log.error("single start browser error:"+BusinessExceptions.getDetailTrace(e));
					}
				}
			}catch(Exception e){
				log.error("Thread error:"+BusinessExceptions.getDetailTrace(e));
			}
			try {
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				log.error("wait error:"+BusinessExceptions.getDetailTrace(e));
			}
		}
	}
	
	private void startWebBrowser(){
		//Selenium模拟登录
		//browser = new DefaultSelenium("localhost", 4444, "*iexplore",LOGIN_URL);
		if(browser!=null){
//			//退出上次登录
//			try{
//				browser.open("http://www.docin.com/app/loginOut");
//			}catch(Exception e){
//				//可能是超时异常，首页内容太多
//				log.error("exit error:"+BusinessExceptions.getDetailTrace(e));
//			}
//			//关闭上一次的浏览器
//			browser.close();
			browser.close();
			browser.stop();
			browser=null;
			try {
				//关闭后待10秒
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error("wait error:"+BusinessExceptions.getDetailTrace(e));
			}
		}
		browser = new DefaultSelenium("localhost", 4444, "*iehta",LOGIN_URL);
		// 启动服务
		browser.start();
		browser.setTimeout("120000");
		browser.open(LOGIN_URL);
		browser.type("username", USER_NAME);
		browser.type("password", PASSWORD);
		log.warn("up user login:"+USER_NAME);
		browser.click("xpath=//input[@value='登录']");
		// 30秒等待页面装载完成
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			log.error("wait error:"+BusinessExceptions.getDetailTrace(e));
		}
		//得到cookies
		String cookies = browser.getCookie();
		process = new HttpClientProcess();
		process.getHeaderMap().put("Cookie", cookies);
		process.getHeaderMap().put("User-Agent", USER_AGENT);
		try{
			//循环目录下的文件，进行上传
			processFileUpload(new File(UPLOAD_FOLDER),true); 
		}catch(Exception e){
			log.error("process folder error:"+BusinessExceptions.getDetailTrace(e));
		}
		//所有文件处理完成后，自动切换到下一用户进行上传
		processNextUser();
		startWebBrowser();
	}
	
	/**
	 * 处理 下一个用户
	 */
	private void processNextUser(){
		DocinUserDao docDao = DaoFactory.getBean(DocinUserDao.class,"docinUserDao");
		//保存原有用户操作信息
		DocinUserUpModel currentUpUser=docDao.get(DocinUserUpModel.class, currentConfigId);
		currentUpUser.setLastProcessDate(new Date());
		currentUpUser.setStatus(DocinUserUpModel.UN_USE);
		docDao.saveSimple(currentUpUser);
		
		//得到一个活动用户
		List<Map<String, String>> upUserConfigList = docDao.getActiveUpUser(1);
		if (upUserConfigList == null || upUserConfigList.size() == 0) {
			log.error("has no active user!!");
			return;
		}
		String upUserId = upUserConfigList.get(0).get("upId");
		DocinUserModel upUserModel = docDao.get(DocinUserModel.class,upUserId);
		if (upUserModel == null) {
			log.error("can't find the upload user of id =" + upUserId);
			return;
		}
		init(upUserConfigList.get(0),upUserModel);			
	}
	
	private void processFileUpload(File file,boolean processFolder){
		if(file.isDirectory() && processFolder){
			File[] filesOrDirs = file.listFiles();
			if(filesOrDirs==null || filesOrDirs.length==0) return;
			for (File file2 : filesOrDirs) {
				//如果文件以prefxStr指定的串开头，则不处理上传
				if(file2.getName().startsWith(prefxStr)) continue;
				processFileUpload(file2,"true".equals(INCLUDE_SUB));
			}
			return;
		}
		//得到web service的wsdl
		process.get(WEBSERVER_URL, null);
		try{
			fileType=file.getName().substring(file.getName().lastIndexOf("."));
		}catch(Exception e){
			log.error("file name incrrect:"+file.getName());
			return;
		}
		log.warn("process file="+file.getName()+"@type="+fileType);
		String fileSeq=null;
		try{
			fileSeq=getFileSequence();
		}catch(Exception e){
			log.error("get file seque error:"+BusinessExceptions.getDetailTrace(e));
			return;
		}
		process.get("http://upload.docin.com/crossdomain.xml",null);

		log.warn("file seq:"+fileSeq+"	file url:"+file.getPath());
		//文件上传
		HttpPost post=new HttpPost("http://upload.docin.com/uploadfile?productId="+fileSeq);
		FileBody upFile = new FileBody(file);  
	    //创建待处理的表单域内容文本  
	    StringBody descript=null;
		try {
			descript = new StringBody(file.getName());
		} catch (UnsupportedEncodingException e) {
			log.error("file name to params error:"+BusinessExceptions.getDetailTrace(e));
			return;
		}  
	    MultipartEntity reqEntity = new MultipartEntity();  
	    reqEntity.addPart("Filedata", upFile);  
	    reqEntity.addPart("Filename", descript);  
	    post.setEntity(reqEntity);
	    //切换客户端类型，指定是从swf进行上传
	    process.getHeaderMap().put("User-Agent", "Shockwave Flash");
	    process.executeHttpRequest(post, ENCODING);
	    process.getHeaderMap().put("User-Agent", USER_AGENT);
	  
	    log.warn("上传文件完成，等待处理附加信息:"+file.getName());
	    //文件名参数，2次 编码
	    String encodingedFileName=EncodingUtil.getEncode(EncodingUtil.getEncode(file.getName().replace(fileType, ""),"UTF-8"), "UTF-8");
	    //用httpclient上传文件完成后，定向到文档附加的页面进行设置后，提交
	    //服务器需要运行在java -jar selenium-server-standalone-2.0a2.jar -singleWindow模式下，以启动多线程
	    try{
	    	browser.open("http://www.docin.com/app/uploadFile/index?productId="+fileSeq+"&productName="+encodingedFileName);
	    }catch(Exception e){
	    	log.error("open new page error:"+BusinessExceptions.getDetailTrace(e));
	    	return;
	    }
	    //根据文件大小，指定等待时间
	    double size=file.length();
	    if(size<1024*1024) {
	    	//1M以内等30秒上传
	    	try {
	    		log.warn("file size less then 1M wait 30s");
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				log.warn("wait error:"+BusinessExceptions.getDetailTrace(e));
			}
	    }
	    else{
	    	//每增1M，加30秒
	    	double per=size/(1024*1024);
	    	int waitTime=30000+1000*30*((int)(per+0.5));
	    	try {
	    		log.warn("file size more then 1M wait "+(waitTime/1000)+"s");
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				log.warn("wait error:"+BusinessExceptions.getDetailTrace(e));
			}	    	
	    }
	    //点击确定提交文档
	    try{
	    	//设置价格，并让豆丁自动调整
			browser.type("downPrice", DOC_MONEY);
			browser.uncheck("priceflag");
	    	browser.click("xpath=//input[@type='submit']");
	    }catch(Exception e){
	    	log.error("click submit error:"+BusinessExceptions.getDetailTrace(e));
	    }
	    //文档提交完成后，等待5秒
	    try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.warn("wait error:"+BusinessExceptions.getDetailTrace(e));
		}
		try{
			//上传完成后，修改文件名，使用加前辍的方式标识
			File newFile=new File(file.getPath().replace(file.getName(), prefxStr+file.getName()));
			file.renameTo(newFile);
		}catch(Exception e){
			log.error("rename file error:"+BusinessExceptions.getDetailTrace(e));
		}
	}	
	
	/**
	 * 得到豆丁文档序列号
	 * 这个号，是跟用户登录绑定的，需要传cookies,搞了半天没提交上去
	 * 提示文档不是我的自己的，不能编辑
	 * @return
	 * @throws Exception 
	 */
	private String getFileSequence() throws Exception{
		//请求webservice的2个方法
		//checkFileName 检查文档类型是否允许 
		//addProduct 生成文档的序号
		String seq=null;
		try{
			CHECKFILENAME_SOAP=FileUtils.readFileToString(new File(RealPath.getBasePath(DocinUploadMain.class)+"classes/soap_docin_checkFileName.ini"));
			ADDPRODUCT_SOAP=FileUtils.readFileToString(new File(RealPath.getBasePath(DocinUploadMain.class)+"classes/soap_docin_addProduct.ini"));
		}catch(Exception e){
			log.error("process soap file error :"+BusinessExceptions.getDetailTrace(e));
		}
		
		String currentCookies=browser.getCookie();
		//判断类型是否可用
		String temp=CHECKFILENAME_SOAP.replace("${type}", fileType);
		temp=temp.replace("${style}", "3");	
		SoapRequest soap=new SoapRequest(WEBSERVER_METHOD_URL,temp,currentCookies,USER_AGENT);
		String result=soap.executeRequest();

		String isEnable=StringProcess.processRegxSingle(result, "<ns1:out>.*</ns1:out>");
		isEnable=isEnable.replace("<ns1:out>", "");
		isEnable=isEnable.replace("</ns1:out>", "");
		if(isEnable==null || "false".equals(isEnable.trim())) return seq;

		temp=ADDPRODUCT_SOAP.replace("${type}", fileType);
		soap=new SoapRequest(WEBSERVER_METHOD_URL,temp,currentCookies,USER_AGENT);
		result=soap.executeRequest();
		
		seq=StringProcess.processRegxSingle(result, "<ns1:out>.*</ns1:out>");
		seq=seq.replace("<ns1:out>", "");
		seq=seq.replace("</ns1:out>", "");
		return seq;
	}
	
}
