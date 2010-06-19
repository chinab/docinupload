package com.gaoqs.auto.docin.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

import com.gaoqs.commons.exception.BusinessExceptions;

public class BrowserUtil {
	
	private static final Log log=LogFactory.getLog(BrowserUtil.class);
	
	/**
	 * 用户配置
	 */
	private static UserDialog userDialog;
	
	private static DocinUploadMain up;
	
	/** 
	 * 开始填充事件
	 * @param browser
	 * @param item
	 * @param ruleName
	 */
	public static void addStartProcessSelectionListener(MenuItem item){
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				Properties property=AutoBrowser.property;
				if(property.getProperty("user_name")==null || property.getProperty("user_name").trim().equals("")){
					AutoBrowser.showMessageBox("未设置用户登陆名");
					openUserDialog();
					return;
				}
				if(property.getProperty("user_pwd")==null || property.getProperty("user_pwd").trim().equals("")){
					AutoBrowser.showMessageBox("未设置用户登陆密码");
					openUserDialog();
					return;
				}
				//TODO 已经不检查用户注册码
//				String oldMac=property.getProperty("mac_code","");
//				String oldReg=property.getProperty("reg_code","");
//				String newMac=getSecurityMac();
//				if(!oldMac.equals(newMac)){
//					AutoBrowser.showMessageBox("机器已经改变，请重新注册\n"+oldMac+"\n"+newMac);
//					openUserDialog();
//					return;
//				}
//				if(!oldReg.trim().equals(encodingSecurityMac(newMac))){
//					AutoBrowser.showMessageBox("注册码错误，请重新输入");
//					openUserDialog();
//					return;
//				}
				 String uploadFolder=AutoBrowser.property.getProperty("upload_path");
				 if(uploadFolder==null){
					 AutoBrowser.showMessageBox("上传目录未设置");
					 openUserDialog();
					 return;
				 }
				 uploadFolder.replace("\\", "/");
				 if(!new File(uploadFolder).exists()){
					 AutoBrowser.showMessageBox("上传目录不存在\n"+AutoBrowser.property.getProperty("upload_path"));
					 openUserDialog();
					 return;
				 }
				try{
					if(up==null || up.stopThread) {
						up.stopThread=false;
						up=  new DocinUploadMain();
					}
					new Thread(up).start();
				}catch(Exception e){
					log.error(BusinessExceptions.getDetailTrace(e));
				}
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}
	
	/** 
	 * 用户设置事件
	 * @param browser
	 * @param item
	 * @param ruleName
	 */
	public static void addUserProcessSelectionListener(MenuItem item){
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				openUserDialog();
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}	
	
	/**
	 * 打开用户设置
	 * @author Jeff
	 * @date 2010-5-30 上午10:09:04
	 */
	public static void openUserDialog(){
		try{
			if(userDialog==null){
				userDialog=new UserDialog(AutoBrowser.shell);
			}
			userDialog.open();
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
	}
	
	/**
	 * 替换的字符
	 */
	private static String keys[]=new String[]{"lOvEzDlP","gaOqs.Com"};
	
	/**
	 * 得到mac地址
	 * @author Jeff
	 * @date 2010-5-24 下午04:54:26
	 * @return
	 */
	public static String getSecurityMac(){
		String line;
		String physicalAddress = "read MAC error!";
		try {
			Process p = Runtime.getRuntime().exec("cmd.exe /c ipconfig /all");
			// p.waitFor();
			BufferedReader bd = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			while ((line = bd.readLine()) != null) {
//				if (line.indexOf("Physical Address. . . . . . . . . :") != -1) {
//					if (line.indexOf(":") != -1) {
//						physicalAddress = line.substring(line.indexOf(":") + 2);
//						break; // 找到MAC,退出循环
//					}
//				}
				//用正则取mac地址
				//中间为:-或空格的mac地址
				Matcher mc = Pattern.compile("([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})").matcher(line);
				if(mc.find()){
					physicalAddress= mc.group();
				}else{
					//中间无任何符号的mac
					mc = Pattern.compile("([0-9a-fA-F]{2})(([0-9a-fA-F]{2}){5})").matcher(line);
					if(mc.find()){
						physicalAddress= mc.group();
					}
				}
				
			}
			p.waitFor();
		} catch (Exception e) {
			AutoBrowser.showMessageBox("无法计算注册码！请确网络连接是否正常！");
			return null;
		}
		if(physicalAddress.equals("read MAC error!")){
			AutoBrowser.showMessageBox("无法计算注册码！请确认网络连接是否正常！");
			return null;
		}
		return Md5Util.md5(physicalAddress, keys[0]);
		//return physicalAddress;
	}
	
	
	/**
	 * 验证注册码
	 * @author Jeff
	 * @date 2010-5-29 下午11:05:43
	 * @param input
	 * @return
	 */
	public static String encodingSecurityMac(String input){
		String temp=Md5Util.md5(input, keys[1]);
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<temp.length();i+=2){
			sb.append(temp.charAt(i));
		}
		return sb.toString(); 
	}
	
	/**
	 * 检查浏览器是否可用
	 * @author Jeff
	 * @date 2010-5-30 下午07:31:02
	 * @return
	 */
	public static boolean checkBrowserActivate(){
		AutoBrowser.isBrowserActivate=false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}		
		System.out.println("back.."+AutoBrowser.isBrowserActivate);
		return AutoBrowser.isBrowserActivate;		
	}

	 /**
	 * 向上层目录找文件
	 * @author Jeff
	 * @date 2010-6-11 下午06:32:18
	 * @param folderName
	 * @param fileName
	 * @return
	 */
	public static String getRealPathByName(String folderName, String fileName){
	   if(folderName==null || folderName.trim().equals("") || fileName==null || fileName.trim().equals("")){
		   return "";
	   }
	   String basePathFile=folderName;
	   try{
		   basePathFile=basePathFile.replace("\\", "/");
		   if(!basePathFile.endsWith("/")) basePathFile+="/";
		   basePathFile+=fileName;
	
		    while (!(new File(basePathFile).exists())) {
		    	basePathFile=basePathFile.replace(fileName, "").substring(0,basePathFile.lastIndexOf("/"));
		    	basePathFile=basePathFile.substring(0,basePathFile.lastIndexOf("/"));
		    	folderName=basePathFile+"/";
		    	basePathFile+="/"+fileName;
		    }
	    }catch(Exception e){
	    	return "";
	    }	    
	    return basePathFile;
	  }
	  
//	  public static void main(String a[]){
//		  String aaa=null;
//		  File configFile=new File("");
//		  System.out.println(configFile.exists());
//		  System.out.println(BrowserUtil.getRealPathByName("D:\\wenku3\\11\\tttt", "hasuploaded_gaoqs_config.ini"));
//	  }
}
