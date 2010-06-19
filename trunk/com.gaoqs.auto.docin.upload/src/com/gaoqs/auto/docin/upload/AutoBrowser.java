package com.gaoqs.auto.docin.upload;

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.path.RealPath;

public class AutoBrowser {

	private static Log log = LogFactory.getLog(AutoBrowser.class);
	
	public static Browser browser;
	public static Shell shell;
	public static Display display;
	public static String preUrl;
	public static String currentUrl;
	public static String cookies;
	public static ProgressListener listener;
	public static String executeScript;
	public static boolean isRunnScript=true;
	public static Properties property;
	public static String errorMsg;
	public static String infoMsg;
	public static boolean isBrowserActivate=true;
	private static String realCompleteUrl;
	public static String title;
	private static String preTitle;
	
	public static void main(String args[]) {
		display = new Display();
		shell = new Shell(display);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		String baseFolder="";
		try{
			baseFolder=new File(".").getAbsolutePath();
			baseFolder=baseFolder.substring(0, baseFolder.length()-1);
			property=RealPath.loadConfigFile(baseFolder+"/config.ini");
			
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
			infoMsg="无法加载配置文件！config.ini";
		}
		shell.setText("豆丁上传");
		
		shell.setImage(display.getSystemImage(SWT.ICON_WORKING));

		browser = new Browser(shell, SWT.NONE);
		ProgressListener listener=new ProgressListener() {
			public void completed(ProgressEvent event) {
				try{
					log.warn("load url complete..."+browser.getUrl());
					Object htmlCookieObject=browser.evaluate("return document.cookie;");
					if(htmlCookieObject!=null){
						cookies=htmlCookieObject.toString();
					}
					realCompleteUrl=browser.getUrl();
				}catch(Exception e){
					log.error("execute scripts failed:"+e.toString());
				}
			}

			public void changed(ProgressEvent arg0) {
			}
		};
		browser.setUrl("http://gaoqs.com/docs/docin_help.html");
		browser.addProgressListener(listener);
		//添加菜单
		Menu menuBar = new Menu(shell, SWT.BAR); 
		
		MenuItem selectMenuHeader=new MenuItem(menuBar,SWT.CASCADE);
		selectMenuHeader.setText("用户操作(&F)");
		Menu selectMenu=new Menu(shell,SWT.DROP_DOWN);		
		selectMenuHeader.setMenu(selectMenu);
		
		MenuItem file0Item = new MenuItem(selectMenu, SWT.PUSH);
		file0Item.setText("开始上传(&S)");
		BrowserUtil.addStartProcessSelectionListener(file0Item);
		
		MenuItem file1Item = new MenuItem(selectMenu, SWT.PUSH);
		file1Item.setText("系统设置(&C)");
		BrowserUtil.addUserProcessSelectionListener(file1Item);

		shell.setMenuBar(menuBar);
		shell.open();
		try{
			File pFile=new File(baseFolder+"/config.ini");
			if(!pFile.exists()){
				pFile.createNewFile();
				BrowserUtil.openUserDialog();
			}
		}catch(Exception e){
			log.error("open config error"+BusinessExceptions.getDetailTrace(e));
		}
		while (!shell.isDisposed()) {
//			boolean enable=AutoBrowser.browser.isEnabled();
//			System.out.println(enable);
//			boolean focus=AutoBrowser.browser.forceFocus();
//			System.out.println(focus);
			//System.out.println("ggggg");
			
			if(errorMsg!=null){
				showMessageBox(errorMsg);
				errorMsg=null;
				shell.close();
			}
			if(infoMsg!=null){
				showMessageBox(infoMsg);
				infoMsg=null;
			}
			if(executeScript!=null && isRunnScript){
				isRunnScript=false;
				boolean s=browser.execute(executeScript);
				System.out.println("execute script:"+s);
				isRunnScript=true;
				executeScript=null;
			}
			//监听内部变量的变化，重新请求地址
			if(currentUrl!=null && preUrl==null){
				preUrl=currentUrl;
				browser.setUrl(currentUrl);
			}
			if(currentUrl!=null && preUrl!=null && !currentUrl.equals(preUrl)){
				preUrl=currentUrl;
				browser.setUrl(currentUrl);
			}
			if(title!=null && preTitle==null){
				preTitle=title;
				shell.setText(preTitle);
				System.out.println("change...."+preTitle);
				shell.pack();
			}
			if(title!=null && preTitle!=null && !title.equals(preTitle)){
				preTitle=title;
				System.out.println("change2...."+preTitle);
				shell.setText(preTitle);
				shell.pack();
			}
			//System.out.println("nnnnnnn");
			if (!display.readAndDispatch()) {
				//System.out.println("eeeeee");
				display.sleep();
				
				//System.out.println("fffffffffff");
			}
			isBrowserActivate=true;
		}
		//System.out.println("wowowow");
		display.dispose();
	}
	
	/**
	 * 提示框
	 * @author Jeff
	 * @date 2010-5-15 下午10:22:02
	 * @param shell
	 * @param title
	 * @param notes
	 * @return
	 */
	public static int showMessageBox(String notes){
		if(notes==null || "".equals(notes.trim())){
			return -1;
		}
		String title=null;
		MessageBox messageBox = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
		if(title==null || title.trim().equals("")){
			title="提示";
		}
		messageBox.setText(title);
		messageBox.setMessage(notes);
		int r = messageBox.open();
		return r;
	}
	
	/**
	 * 得到browser url
	 * @author Jeff
	 * @date 2010-5-30 下午08:50:08
	 * @return
	 */
	public static String getBrowserUrl(){
		return realCompleteUrl;
	}
}
