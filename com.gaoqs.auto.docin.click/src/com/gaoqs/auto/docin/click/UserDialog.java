package com.gaoqs.auto.docin.click;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gaoqs.commons.exception.BusinessExceptions;
import com.gaoqs.commons.path.RealPath;

public class UserDialog extends Dialog {

	private Label machineLabel;
	private Label regLabel;
	private Label userNameLabel;
	private Label timeIntervalLabel;
	private Label urlLabel;
	
	private Text machineText;
	private Text regText;
	private Text userNameText;
	private Text timeIntervalText;
	private Combo urlCombo;
	
	private Log log=LogFactory.getLog(UserDialog.class); 
	
	public UserDialog(Shell parent) {
		super(parent);
		this.setText("用户配置-修改后需重启软件生效");
	}
	
	public Object open() {
        Shell parent = getParent();
        //实例化子框体，显示
        Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = gridLayout.marginRight = 15; 
        gridLayout.numColumns = 2;
        shell.setLayout(gridLayout);
        shell.setLayout(gridLayout);
        shell.setText(getText());
        shell.setLocation(parent.getLocation().x+100, parent.getLocation().y+50);
        shell.setSize(350,250);
        initData(shell);
        addComponentEvent(shell);
        shell.open();
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return this;
    }

	/**
	 * 只有当先去焦点后，再获得，才弹出文件选择
	 */
	private static int hasLosted=0;	
	
	/**
	 * 添加组件
	 * @author Jeff
	 * @date 2010-5-28 上午10:53:55
	 * @param shell
	 * @param formLayout
	 */
	private void addComponentEvent(final Shell shell){
		//关闭事件，保存配置
		ShellListener listener = new ShellAdapter() {
			public void shellClosed(ShellEvent arg0) {
				// 保存配置到config.ini
				AutoBrowser.property.setProperty("mac_code", machineText.getText());
				AutoBrowser.property.setProperty("reg_code", regText.getText());
				AutoBrowser.property.setProperty("user_name", userNameText.getText());
				AutoBrowser.property.setProperty("url_type", urlCombo.getText());
				String wait = timeIntervalText.getText();
				if (wait.indexOf(".") != -1) {
					wait = wait.substring(0, wait.indexOf("."));
					try {
						int waits = Integer.parseInt(wait);
						if (waits < 1) wait = "1";
					} catch (Exception e) {
						wait = "1";
					}
				}
				AutoBrowser.property.setProperty("interval_time", wait);
				try {
					String baseFolder = new File(".").getAbsolutePath();
					baseFolder = baseFolder.substring(0,baseFolder.length() - 1);
					AutoBrowser.property.store(new FileOutputStream(new File(baseFolder + "/config.ini")), null);
				} catch (Exception e) {
					log.error("back up config.ini fail:"+ BusinessExceptions.getDetailTrace(e));
				}
			}
		};
		shell.addShellListener(listener);
	}
	
	/**
	 * 初始化数据，添加事件监听
	 * @author Jeff
	 * @date 2010-5-28 上午11:46:19
	 * @param shell
	 */
	private void initData(Shell shell){
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		 
		machineLabel=new Label(shell,SWT.NONE);
		machineLabel.setText("机器码：");
		machineText=new Text(shell, SWT.BORDER);
		machineText.setText(BrowserUtil.getSecurityMac());
		machineText.setEditable(false);
		machineText.setLayoutData(gridData);
		
		regLabel=new Label(shell,SWT.NONE);
		regLabel.setText("注册码：");
		regText=new Text(shell, SWT.BORDER);
		regText.setLayoutData(gridData);
		regText.setEditable(false);
		regText.setText(BrowserUtil.encodingSecurityMac(machineText.getText()));
		
		userNameLabel=new Label(shell,SWT.NONE);
		userNameLabel.setText("豆丁用户名：");
		userNameText=new Text(shell, SWT.BORDER);
		userNameText.setToolTipText("英文,分隔多个用户名");
		userNameText.setLayoutData(gridData);
		
		timeIntervalLabel=new Label(shell,SWT.NONE);
		timeIntervalLabel.setText("点击间隔时间：");		
		timeIntervalText=new Text(shell,SWT.BORDER);
		timeIntervalText.setEditable(true);
		timeIntervalText.setText("15");
		timeIntervalText.setLayoutData(gridData);
		urlLabel=new Label(shell,SWT.NONE);
		urlLabel.setText("链接来源");
		urlCombo=new Combo(shell,SWT.READ_ONLY);
		urlCombo.setItems(urlTypeItems);
		urlCombo.select(0);
		
		if(AutoBrowser.property!=null){
			String oldMachine=AutoBrowser.property.getProperty("mac_code","");
			if(oldMachine.equals(machineText.getText())){
				regText.setText(AutoBrowser.property.getProperty("reg_code","请输入注册码"));
			}else{
				regText.setText("请输入注册码");
				//拷另一份文件
				try {
					String baseFolder=new File(".").getAbsolutePath();
					baseFolder=baseFolder.substring(0, baseFolder.length()-1);
					AutoBrowser.property.store(new FileOutputStream(new File(baseFolder+"/config_backup.ini")), "machine_code change backup");
				} catch (Exception e) {
					log.error("back up config.ini fail:"+BusinessExceptions.getDetailTrace(e));
				}
			}
			userNameText.setText(AutoBrowser.property.getProperty("user_name",""));
			//TODO 已经不检查注册码
			regText.setText("http://www.gaoqs.com");
			timeIntervalText.setText(AutoBrowser.property.getProperty("interval_time","5"));
			urlCombo.setText(AutoBrowser.property.getProperty("url_type","每次增量添加"));
		}
	}
	
	public static String urlTypeItems[]= new String[]{"每次增量添加","只读取配置文件","每次都全部获取"};
}
