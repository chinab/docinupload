package com.gaoqs.auto.wenku.click;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 自动点击程序
 * @author jeff gao
 *
 */
public class AutoClickMain {

	private static Log log = LogFactory.getLog(AutoClickMain.class);
	
	public static Browser browser;
	public static Shell shell;
	public static Display display;
	public static String preUrl;
	public static String currentUrl;

	public static void main(String args[]) {
		display = new Display();
		shell = new Shell(display);
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		shell.setText("Docin点击器");
		// 直接跟swt的组件是一样的，可以提供模板，方便开发
		browser = new Browser(shell, SWT.NONE);
		browser.setUrl("http://gaoqs.com");
		// t.setUrl("http://www.docin.com/p-48482511.html");
		shell.open();
//		System.out.println("ggggggggggggg1");
//		Menu menuBar = new Menu(shell, SWT.BAR); 
//		
//		MenuItem selectMenuHeader=new MenuItem(menuBar,SWT.CASCADE);
//		selectMenuHeader.setText("选择加载项(&C)");
//		Menu selectMenu=new Menu(shell,SWT.DROP_DOWN);		
//		selectMenuHeader.setMenu(selectMenu);
//		MenuItem file0Item = new MenuItem(selectMenu, SWT.PUSH);
//		file0Item.setText("execute");
		
//		file0Item.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent selectionevent) {
//				Runnable a=new Test();
//				display.syncExec(a);
//				a.run();				
//				//execute();
//			}
//		});
		
		new Thread(new DocUrlGetProcess()).start();

//		shell.setMenuBar(menuBar);
		while (!shell.isDisposed()) {
			//监听内部变量的变化，重新请求地址
			if(currentUrl!=null && preUrl==null){
				//browser.dispose();
				//browser = new Browser(shell, SWT.NONE);
				preUrl=currentUrl;
				browser.setUrl(currentUrl);
			}
			if(currentUrl!=null && preUrl!=null && !currentUrl.equals(preUrl)){
				//browser.dispose();
				//browser = new Browser(shell, SWT.NONE);
				preUrl=currentUrl;
				browser.setUrl(currentUrl);
			}
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	
}
