package com.gaoqs.auto.docin.upload;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.httpclient.HttpClientProcess;
import com.gaoqs.commons.path.RealPath;
import com.gaoqs.commons.soap.SoapRequest;
import com.gaoqs.commons.string.EncodingUtil;
import com.gaoqs.commons.string.StringProcess;


public class CopyOfDocinUploadMain implements Runnable{
	
	/**
	 * 请求文档序列soap信息
	 */
	private static String CHECKFILENAME_SOAP="<?xml version=\"1.0\" encoding=\"utf-8\"?><SOAP-ENV:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><checkFileName xmlns=\"http://web.service.projectx.wonibo.com\"><in0>${type}</in0><in1>${style}</in1></checkFileName></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	/**
	 * 添加文档的soap信息
	 */
	private static String ADDPRODUCT_SOAP="<?xml version=\"1.0\" encoding=\"utf-8\"?><SOAP-ENV:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><addProduct xmlns=\"http://web.service.projectx.wonibo.com\"><in0>${type}</in0></addProduct></SOAP-ENV:Body></SOAP-ENV:Envelope>";
	
	private Log log=LogFactory.getLog(CopyOfDocinUploadMain.class);
	
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
	
	public void run() {
		if(hasStrated){
			System.exit(0);
		}
		//验证码处理
		execute();
		hasStrated=true;
		stopThread=true;
	}
	
	private void execute() {
		//验证md5密码
		while (true) {	
			if(AutoBrowser.shell.isDisposed()){
				System.exit(0);
			 }
			
			AutoBrowser.currentUrl="http://www.docin.com/app/login";
			StringBuffer sb=new StringBuffer();						
			sb.append("document.getElementById('username1').value='"+AutoBrowser.property.getProperty("user_name")+"';");
			sb.append("document.getElementById('password1').value='"+AutoBrowser.property.getProperty("user_pwd")+"';");
			sb.append("document.getElementById('login').submit();");
			
			processDetails(AutoBrowser.currentUrl, sb.toString(), 10000, 10000);				
			
//			if(AutoBrowser.currentUrl.equals(AutoBrowser.getBrowserUrl())){
//				log.error("登陆不成功！");
//				//登陆不成功
//				return;
//			}
			 process = new HttpClientProcess();
			 process.getHeaderMap().put("Cookie", AutoBrowser.cookies);
			 process.getHeaderMap().put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)");
			 //开始上传文件
			 String uploadFolder=AutoBrowser.property.getProperty("upload_path");
			 if(uploadFolder==null){
				 AutoBrowser.infoMsg="上传目录未设置，请配置upload_path=属性";
				 return;
			 }
			 uploadFolder.replace("\\", "/");
			 if(!new File(uploadFolder).exists()){
				 AutoBrowser.infoMsg="上传目录不存在\n"+AutoBrowser.property.getProperty("upload_path");
				 return;
			 }
			 try{
				 uploadFile(new File(uploadFolder));
			 }catch(Exception e){
				 log.error("thread uploadFile error:"+BusinessExceptions.getDetailTrace(e));
			 }
			 if(AutoBrowser.title!=null && "上传文件出错，浏览器不可用！".equals(AutoBrowser.title)){
				 
			 }else
				 AutoBrowser.title="豆丁上传　　文件上传处理完成！";
			 return;
		}
		
	}
	
	private void uploadFile(File file) {
		if(AutoBrowser.shell.isDisposed()){
			System.exit(0);
		 }
		if (file == null){
			return;
		}
		if (file.isDirectory()) {
			File [] files=file.listFiles();
			if(files!=null)
			for ( int i=0;i<files.length;i++) {
				File subFile=files[i];
				if(AutoBrowser.shell.isDisposed()){
					System.exit(0);
				 }
				uploadFile(subFile);
			}
		}else{
			if(AutoBrowser.shell.isDisposed()){
				System.exit(0);
			 }
			if(!AutoBrowser.isBrowserActivate){
				AutoBrowser.title="上传文件出错，浏览器不可用！";
				return;
			}
			String parentType=null;
			String subType=null;
			String folderType=null;
			try{
				//取分类属性文件
				
				String configFilePath=BrowserUtil.getRealPathByName(file.getAbsolutePath().replace(file.getName(), ""), "hasuploaded_gaoqs_config.ini");
				
				File configFile=new File(configFilePath);
				
				if(configFile.exists()){
					if(currentConfigFile==null || !currentConfigFile.getAbsoluteFile().equals(configFile.getAbsolutePath())){
						pro=RealPath.loadConfigFile(configFilePath);
						currentConfigFile=configFile;
						parentType=pro.getProperty("type_parent", null);
						subType=pro.getProperty("type_sub", null);
						folderType=pro.getProperty("folder", null);
						parentType = new String(parentType.getBytes("ISO8859-1"), "UTF-8");
						subType = new String(subType.getBytes("ISO8859-1"), "UTF-8");
						folderType = new String(folderType.getBytes("ISO8859-1"), "UTF-8");

						log.warn("set type:"+parentType+"@"+subType+"@"+folderType);
					}
				}
			}catch(Exception e ){
				log.error(BusinessExceptions.getDetailTrace(e));
				parentType=folderType=subType=null;
			}
			//单个文件
			try{
				if(file.getName()!=null && !file.getName().startsWith("hasuploaded_")){
					AutoBrowser.isBrowserActivate=false;
					log.warn("file path:"+file.getAbsolutePath());
					//上传
					String fileType="doc";
					process.getHeaderMap().put("Cookie", AutoBrowser.cookies);
//					try{
//						process.get("http://www.docin.com/services/FileNameCheckWebService?wsdl",null);
//					}catch(Exception e){
//						log.error("get FileNameCheckWebService1 error:"+BusinessExceptions.getDetailTrace(e));
//						//Thread.sleep(5000);
//						return;	
//					}
					 try{
						 fileType=file.getName().substring(file.getName().lastIndexOf("."));
						}catch(Exception e){
							log.error("file name incrrect:"+file.getName());
							Thread.sleep(5000);
							return;
						}
						log.warn("process file="+file.getName()+"@type="+fileType);
						//是否处理不规则的文件，如不允许上传wps
						if(".wps,.exe,.dll,.ini,.properties".indexOf(fileType)!=-1){
							try{
								//修改文件名，使用加前辍的方式标识
								File newFile=new File(file.getPath().replace(file.getName(), "hasuploaded_nouploadable_"+file.getName()));
								file.renameTo(newFile);
							}catch(Exception e){
								log.error("rename nouploadable file error:"+BusinessExceptions.getDetailTrace(e));
								Thread.sleep(5000);
								return;
							}
							log.warn("no upladable file type");
							Thread.sleep(5000);
							return;
						}
						
						String fileSeq=null;
						try{
							fileSeq=getFileSequence(fileType);
						}catch(Exception e){
							log.error("get file seque error:"+BusinessExceptions.getDetailTrace(e));
							Thread.sleep(5000);
							return;				
						}
						//process.get("http://upload.docin.com/crossdomain.xml",null);
						
						// AutoBrowser.browser.setJavascriptEnabled(false);
		
						log.warn("file seq:"+fileSeq+"	file url:"+file.getPath());
						//文件上传
						try {
							HttpPost post=new HttpPost("http://upload.docin.com/uploadfile?productId="+fileSeq);
							FileBody upFile = new FileBody(file);  
						    //创建待处理的表单域内容文本  
						    StringBody descript=null;
								descript = new StringBody(file.getName());
						    MultipartEntity reqEntity = new MultipartEntity();  
						    reqEntity.addPart("Filedata", upFile);  
						    reqEntity.addPart("Filename", descript);  
						    post.setEntity(reqEntity);
						    //切换客户端类型，指定是从swf进行上传
						    process.getHeaderMap().put("User-Agent", "Shockwave Flash");
						    
						    String title="豆丁上传　　正在上传："+file.getName();
							long fileSize=file.length();
						    String realSize="0.0";
						    if(fileSize>1024*1024) {
						    	realSize=roundUp((fileSize/(1024*1024.0)),"0.00")+"MB";
						    }else if(fileSize>1024){
						    	realSize=roundUp((fileSize/(1024.0)),"0.00")+"KB";
						    }else{
						    	realSize=fileSize+"Byte";
						    }
						    title+= "　　大小："+realSize;
						    AutoBrowser.title=title;
						   // System.out.println("title="+title);
						    process.executeHttpRequest(post, "UTF-8");
						    process.getHeaderMap().put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)");
						} catch (UnsupportedEncodingException e) {
							log.error("file name to params error:"+BusinessExceptions.getDetailTrace(e));
							Thread.sleep(5000);
							return;
						}  
									
					    log.warn("上传文件完成，等待处理附加信息:"+file.getName());
					    //文件名参数，2次 编码
					    String encodingedFileName=EncodingUtil.getEncode(EncodingUtil.getEncode(file.getName().replace(fileType, ""),"UTF-8"), "UTF-8");
						 
					    AutoBrowser.currentUrl="http://www.docin.com/app/uploadFile/index?productId="+fileSeq+"&productName="+encodingedFileName;
					    Thread.sleep(5000);
						StringBuffer sb=new StringBuffer();
						int r=new Random(new Date().getTime()).nextInt();
						if(r<0) r*=-1;
						String lastPref=(r%100)+"";
						if(lastPref.length()==1){
							lastPref+="1";
						}
						
						//AutoBrowser.browser.setJavascriptEnabled(true);
						sb.append("document.getElementById('downPrice').value='"+AutoBrowser.property.getProperty("int_price")+"."+lastPref+"';");
						sb.append("document.getElementById('priceflag').checked=false;");
						//处理分类
						if(parentType!=null && !parentType.trim().equals("")){
							//System.out.println("parentType="+parentType);
							sb.append("var parentSel=document.getElementById('productParentCategoryId');");
							sb.append("for(var i=0;i<parentSel.options.length;i++){");
							sb.append("if(parentSel.options[i].text == '"+parentType+"'){");
							sb.append("parentSel.options[i].selected = true;");
							sb.append("changeCategory(parentSel.options[i].value);");
							sb.append("}");
							sb.append("}");							

							processDetails(null, sb.toString(), 0, 3000);

							if(subType!=null && !subType.trim().equals("")){
								sb=new StringBuffer();								
								//System.out.println("subType="+subType);
								//选择子类型
								sb.append("var subSel=document.getElementById('productCategoryId');");	
								sb.append("for(var i=0;i<subSel.options.length;i++){");
								sb.append("if(subSel.options[i].text == '"+subType+"'){");
								sb.append("subSel.options[i].selected = true;");							
								sb.append("}");
								sb.append("}");
								
								processDetails(null, sb.toString(), 0, 2000);
							}
							sb=new StringBuffer();
						}
						sb.append("document.getElementById('uploadDesc').submit();");
						AutoBrowser.executeScript=sb.toString();	
					    //文档提交完成后，等待22秒
					    try {
					    	int defalut_wait_time=17;
					    	try{
					    		defalut_wait_time=Integer.parseInt(AutoBrowser.property.getProperty("interval_time"));
					    	}catch(Exception e){
					    	}
					    	log.warn("next documents upload wait:"+defalut_wait_time);
							Thread.sleep(defalut_wait_time*1000);
						} catch (InterruptedException e) {
							log.warn("wait error:"+BusinessExceptions.getDetailTrace(e));
							Thread.sleep(5000);
							return;
						}
						//处理分类目录
						if(folderType!=null && !folderType.trim().equals("")){
							//System.out.println("folderType="+folderType);
							sb=new StringBuffer();
							sb.append("var subSel=document.getElementById('addToFolderSelect');");	
							sb.append("for(var i=0;i<subSel.options.length;i++){");
							sb.append("if(subSel.options[i].text == '"+folderType+"'){");
							sb.append("subSel.options[i].selected = true;");
							sb.append("on_addToFolder();");
							sb.append("}");
							sb.append("}");
														
							processDetails(null, sb.toString(), 0, 5000);
						}
						try{
							//上传完成后，修改文件名，使用加前辍的方式标识
							File newFile=new File(file.getPath().replace(file.getName(), "hasuploaded_"+file.getName()));
							file.renameTo(newFile);
						}catch(Exception e){
							log.error("rename file error:"+BusinessExceptions.getDetailTrace(e));
							Thread.sleep(5000);
							return;
						}
				}
			}catch(Exception e){
				log.error("upload single file error:"+BusinessExceptions.getDetailTrace(e));
				return;
			}
		}
	}
	
	
	/**
	 * 得到豆丁文档序列号
	 * 这个号，是跟用户登录绑定的，需要传cookies,搞了半天没提交上去
	 * 提示文档不是我的自己的，不能编辑
	 * @return
	 * @throws Exception 
	 */
	private String getFileSequence(String fileType) throws Exception{
		//请求webservice的2个方法
		//checkFileName 检查文档类型是否允许 
		//addProduct 生成文档的序号
		String seq=null;
//		String CHECKFILENAME_SOAP="";
//		String ADDPRODUCT_SOAP="";
//		try{
//			CHECKFILENAME_SOAP=FileUtils.readFileToString(new File(RealPath.getBasePath(DocinUploadMain.class)+"soap_docin_checkFileName.ini"));
//			ADDPRODUCT_SOAP=FileUtils.readFileToString(new File(RealPath.getBasePath(DocinUploadMain.class)+"soap_docin_addProduct.ini"));
//		}catch(Exception e){
//			log.error("process soap file error :"+BusinessExceptions.getDetailTrace(e));
//		}
		
		String currentCookies=AutoBrowser.cookies;
		//判断类型是否可用
		String temp=CHECKFILENAME_SOAP.replace("${type}", fileType);
		temp=temp.replace("${style}", "3");	
		SoapRequest soap=new SoapRequest("http://www.docin.com/services/FileNameCheckWebService",temp,currentCookies,"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)");
		String result=soap.executeRequest();

		String isEnable=StringProcess.processRegxSingle(result, "<ns1:out>.*</ns1:out>");
		isEnable=isEnable.replace("<ns1:out>", "");
		isEnable=isEnable.replace("</ns1:out>", "");
		if(isEnable==null || "false".equals(isEnable.trim())) return seq;

		temp=ADDPRODUCT_SOAP.replace("${type}", fileType);
		soap=new SoapRequest("http://www.docin.com/services/FileNameCheckWebService",temp,currentCookies,"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0;  Embedded Web Browser from: http://bsalsa.com/; .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022)");
		result=soap.executeRequest();
		
		seq=StringProcess.processRegxSingle(result, "<ns1:out>.*</ns1:out>");
		seq=seq.replace("<ns1:out>", "");
		seq=seq.replace("</ns1:out>", "");
		return seq;
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
	
	/**
	 * 保留小数
	 * @author Jeff
	 * @date 2010-6-6 下午03:00:03
	 * @param d
	 * @param RoundFormat
	 * @return
	 */
	public static String roundUp(double d, String RoundFormat) {
		DecimalFormat nf = new DecimalFormat(RoundFormat);
		return nf.format(d);
	}
}
