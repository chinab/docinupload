package com.gaoqs.auto.docin.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
	private Label pwdLabel;
	private Label upLoadPathLabel;
	private Label priceLabel;
	private Label timeIntervalLabel;
	private Label typeSelecteLabel;
	private Label folderLabel;
	
	private Text machineText;
	private Text regText;
	private Text userNameText;
	private Text pwdText;
	private Text chooserText;
	private Text priceText;
	private Text timeIntervalText;
	private Combo typeSelecteCombo;
	private Text folderText;
	
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
        shell.setSize(350,350);
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
		//文件夹选择事件
		FocusListener listener = new FocusListener(){
            public void focusGained(FocusEvent e){
            	if(hasLosted%2==0){
            		folderDig(shell);
            	}
            	hasLosted++;
            }

            public void focusLost(FocusEvent e) {
            	hasLosted++;
            }
        };
		chooserText.addFocusListener(listener);
		
		//关闭事件，保存
		ShellListener closeListener = new ShellAdapter() {
			public void shellClosed(ShellEvent arg0) {
				//保存配置到config.ini
				AutoBrowser.property.setProperty("mac_code", machineText.getText());
				AutoBrowser.property.setProperty("reg_code", regText.getText());
				AutoBrowser.property.setProperty("user_name", userNameText.getText());
				AutoBrowser.property.setProperty("user_pwd", pwdText.getText());
				AutoBrowser.property.setProperty("upload_path", chooserText.getText());
				String price=priceText.getText();
				if(price.indexOf(".")!=-1){
					price=price.substring(0,price.indexOf("."));
					try{
						Integer.parseInt(price);
					}catch(Exception e){
						price="5";
					}
				}
				AutoBrowser.property.setProperty("int_price", price);
				String wait= timeIntervalText.getText();
				if(wait.indexOf(".")!=-1){
					wait=wait.substring(0,wait.indexOf("."));
					try{
						int waits=Integer.parseInt(wait);
						if(waits<15) wait="15";
					}catch(Exception e){
						wait="16";
					}
				}
				AutoBrowser.property.setProperty("interval_time",wait);
				try {
					String baseFolder=new File(".").getAbsolutePath();
					baseFolder=baseFolder.substring(0, baseFolder.length()-1);
					AutoBrowser.property.store(new FileOutputStream(new File(baseFolder+"/config.ini")), null);
				} catch (Exception e) {
					log.error("back up config.ini fail:"+BusinessExceptions.getDetailTrace(e));
				}
				//处理文件夹分类
				String folderPath=chooserText.getText();
				folderPath=folderPath.replace("\\", "/");
				if(!folderPath.endsWith("/")) folderPath+="/";
				folderPath+="hasuploaded_gaoqs_config.ini";
				File typeConfigFile=new File(folderPath);
				String types=typeSelecteCombo.getText();
				//System.out.println("选择的类型："+types);
				if(types.indexOf("-")!=-1 || (folderText.getText()!=null && !"".equals(folderText.getText().trim()))){
					String folder="";
					String parentType="";
					String subType="";
					if(types!=null && types.indexOf("-")!=-1){
						String typesDeatils[]=types.split("-");
						if(typesDeatils!=null && typesDeatils.length>0 && typesDeatils[0]!=null)
							parentType=typesDeatils[0].trim();
						if(typesDeatils!=null && typesDeatils.length>1 && typesDeatils[1]!=null)
							subType=typesDeatils[1].trim();
					}
					if(folderText.getText()!=null && !folderText.getText().trim().equals("")){
						folder=folderText.getText().trim();
					}
					String temp=folderTemplate;
					temp=temp.replace("${folder}",folder);
					temp=temp.replace("${parentType}",parentType);
					temp=temp.replace("${subType}",subType);				
					
					try {
						FileUtils.writeStringToFile(typeConfigFile, temp, "UTF-8");
					} catch (IOException e) {
						log.error("save type file error:"+BusinessExceptions.getDetailTrace(e));
					}
				}else{
					//删除文件
					if(typeConfigFile.exists()){
						try{
							log.warn("delete old type config file:"+typeConfigFile.getAbsolutePath());
							typeConfigFile.delete();
						}catch(Exception e){}
					}
				}
			}
		};
		shell.addShellListener(closeListener);
		
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
		regLabel.setText("注册码*：");
		regText=new Text(shell, SWT.BORDER);
		regText.setLayoutData(gridData);
		regText.setText(BrowserUtil.encodingSecurityMac(machineText.getText()));
		regText.setEditable(false);
		
		userNameLabel=new Label(shell,SWT.NONE);
		userNameLabel.setText("豆丁用户名*：");
		userNameText=new Text(shell, SWT.BORDER);
		userNameText.setLayoutData(gridData);
		
		pwdLabel=new Label(shell,SWT.NONE);
		pwdLabel.setText("豆丁密码*：");
		pwdText=new Text(shell,SWT.PASSWORD | SWT.BORDER);
		pwdText.setLayoutData(gridData);
		
		upLoadPathLabel=new Label(shell,SWT.NONE);
		upLoadPathLabel.setText("上传目录*：");
		chooserText=new Text(shell,SWT.BORDER);
		chooserText.setText("选择目录...");
		chooserText.setEditable(false);
		chooserText.setLayoutData(gridData);
		
		priceLabel=new Label(shell,SWT.NONE);
		priceLabel.setText("文档价格*：");
		priceText=new Text(shell,SWT.BORDER);
		priceText.setEditable(true);
		priceText.setText("5");
		priceText.setLayoutData(gridData);
		priceText.setToolTipText("整数位价格，随机生成2位小数价格");
		
		timeIntervalLabel=new Label(shell,SWT.NONE);
		timeIntervalLabel.setText("间隔上传时间*：");		
		timeIntervalText=new Text(shell,SWT.BORDER);
		timeIntervalText.setEditable(true);
		timeIntervalText.setText("15");
		timeIntervalText.setLayoutData(gridData);
		timeIntervalText.setToolTipText("不要低于15秒，否则有封号可能");
		
		typeSelecteLabel=new Label(shell,SWT.NONE);
		typeSelecteLabel.setText("文章分类：");	
		typeSelecteCombo=new Combo(shell, SWT.READ_ONLY);		
		typeSelecteCombo.setItems(items);
		typeSelecteCombo.select(0);
		
		folderLabel=new Label(shell,SWT.NONE);
		folderLabel.setText("文章目录：");		
		folderText=new Text(shell,SWT.BORDER);
		folderText.setEditable(true);
		folderText.setText("");
		folderText.setLayoutData(gridData);
		folderText.setToolTipText("保证帐号上有同名的目录，将直接分类到相应的目录");
		
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
			
			//初始化读取的数据
			userNameText.setText(AutoBrowser.property.getProperty("user_name",""));
			pwdText.setText(AutoBrowser.property.getProperty("user_pwd",""));
			chooserText.setText(AutoBrowser.property.getProperty("upload_path",""));
			priceText.setText(AutoBrowser.property.getProperty("int_price","5"));
			timeIntervalText.setText(AutoBrowser.property.getProperty("interval_time","15"));
			String path=AutoBrowser.property.getProperty("upload_path","");
			if(path!=null && !path.trim().equals("")){
				String typeConfigFilePath=path;
				typeConfigFilePath=typeConfigFilePath.replace("\\", "/");
				if(!typeConfigFilePath.endsWith("/")) typeConfigFilePath+="/";
				typeConfigFilePath+="hasuploaded_gaoqs_config.ini";
				File typeConfigFile=new File(typeConfigFilePath);
				if(typeConfigFile.exists()){
					//类型文件存在，读取并设置值
					Properties pro=RealPath.loadConfigFile(typeConfigFilePath);
					String parentType=pro.getProperty("type_parent", null);
					String subType=pro.getProperty("type_sub", null);
					String folderType=pro.getProperty("folder", null);
					try{
						parentType = new String(parentType.getBytes("ISO8859-1"), "UTF-8");
						subType = new String(subType.getBytes("ISO8859-1"), "UTF-8");
						folderType = new String(folderType.getBytes("ISO8859-1"), "UTF-8");
					}catch(Exception e){
						log.error("string type parse error:"+BusinessExceptions.getDetailTrace(e));
						parentType=null;
						subType=null;
						folderType=null;
					}
					folderText.setText(folderType);					
					typeSelecteCombo.setText(parentType+"-"+subType);
					
				}
				
			}
			regText.setText("http://www.gaoqs.com");
		}
	}
	
	
 	/**
    * 文件夹（目录）选择对话框
    */
    protected String folderDig(Shell parent){
        //新建文件夹（目录）对话框
        DirectoryDialog folderdlg=new DirectoryDialog(parent);
        //设置文件对话框的标题
        folderdlg.setText("文件选择");
        //设置初始路径
        folderdlg.setFilterPath("SystemDrive");
        //设置对话框提示文本信息
        folderdlg.setMessage("请选择相应的文件夹");
        //打开文件对话框，返回选中文件夹目录
        String selectedDir=folderdlg.open();
        if(selectedDir==null){
        	String basePath=new File(".").getAbsolutePath();
        	basePath=basePath.substring(0,basePath.length()-1);
        	chooserText.setText(basePath);
            return basePath;
        }
        else{
        	chooserText.setText(selectedDir);
           return selectedDir;    
        }  
        
    }
    
    
	private static String items[] = {
		"不选择任何分类",
		"====小说====",
		"小说-武侠小说",
		"小说-官场商战",
		"小说-科幻/魔幻",
		"小说-推理/悬疑",
		"小说-恐怖/惊悚",
		"小说-纪实/传记",
		"小说-言情小说",
		"小说-校园爱情",
		"小说-都市情感",
		"小说-历史/古典",
		"小说-乡土小说",
		"小说-影视小说",
		"小说-外国小说",
		"小说-英文小说",
		"小说-军事小说",
		"小说-其它",
		"====经管励志====",
		"经管励志-励志",
		"经管励志-创业",
		"经管励志-管理",
		"经管励志-理财",
		"经管励志-股票/基金",
		"经管励志-MBA",
		"经管励志-财会税务",
		"经管励志-生产运营",
		"经管励志-企业制度",
		"经管励志-项目管理",
		"经管励志-行业分析",
		"经管励志-管理工具",
		"经管励志-金融贸易",
		"经管励志-人力资源",
		"经管励志-广告媒体",
		"经管励志-市场营销",
		"经管励志-其它",
		"====办公工具====",
		"办公工具-人事文书",
		"办公工具-合同文书",
		"办公工具-总结报告",
		"办公工具-调研报告",
		"办公工具-个人文书",
		"办公工具-简历/职业规划",
		"办公工具-演讲稿",
		"办公工具-名人语录",
		"办公工具-OFFICE模板",
		"办公工具-词典",
		"办公工具-其它",
		"====明星娱乐====",
		"明星娱乐-电影",
		"明星娱乐-电视",
		"明星娱乐-华人明星",
		"明星娱乐-日韩明星",
		"明星娱乐-欧美明星",
		"明星娱乐-幽默搞笑",
		"明星娱乐-卡通动漫",
		"明星娱乐-其它",
		"====汽车====",
		"汽车-养护",
		"汽车-改装",
		"汽车-用品",
		"汽车-评测作业",
		"汽车-赛车/F1",
		"汽车-其它",
		"====体育====",
		"体育-足球/世界杯",
		"体育-篮球/NBA",
		"体育-网球",
		"体育-高尔夫",
		"体育-羽毛球",
		"体育-其它球类",
		"体育-田径",
		"体育-游泳",
		"体育-体育名人",
		"体育-冬季运动",
		"体育-水上运动",
		"体育-棋牌麻将",
		"体育-武术搏击",
		"体育-奥运会",
		"体育-其它",
		"====生活时尚====",
		"生活时尚-房地产",
		"生活时尚-家居/装修",
		"生活时尚-生活指南",
		"生活时尚-美食",
		"生活时尚-健康",
		"生活时尚-美容",
		"生活时尚-服饰",
		"生活时尚-宠物",
		"生活时尚-旅游",
		"生活时尚-说明书/手册",
		"生活时尚-地图",
		"生活时尚-占卜",
		"生活时尚-减肥/健身",
		"生活时尚-户外运动",
		"生活时尚-其它",
		"====计算机====",
		"计算机-图形图像",
		"计算机-三维制作",
		"计算机-源代码",
		"计算机-IT书籍",
		"计算机-电子通讯",
		"计算机-软件工程",
		"计算机-解决方案",
		"计算机-网站策划",
		"计算机-专题技术",
		"计算机-其它",
		"====法律====",
		"法律-法律法规",
		"法律-法学理论",
		"法律-法律文书写作",
		"法律-其它",
		"====教育考试====",
		"教育考试-雅思",
		"教育考试-托福",
		"教育考试-四六级",
		"教育考试-自考",
		"教育考试-成考",
		"教育考试-商务英语",
		"教育考试-公务员考试",
		"教育考试-医学考试",
		"教育考试-司法考试",
		"教育考试-计算机考试",
		"教育考试-注册会计师",
		"教育考试-研究生考试",
		"教育考试-论文报告",
		"教育考试-其它",
		"教育考试-GRE",
		"====科技====",
		"科技-基础科学",
		"科技-工业",
		"科技-农业",
		"科技-生物",
		"科技-航天",
		"科技-运输",
		"科技-建筑施工",
		"科技-其它",
		"====艺术====",
		"艺术-影视",
		"艺术-舞蹈",
		"艺术-戏剧",
		"艺术-摄影",
		"艺术-收藏",
		"艺术-音乐",
		"艺术-书法",
		"艺术-其它",
		"====社科====",
		"社科-哲学",
		"社科-历史",
		"社科-新闻",
		"社科-军事",
		"社科-考古",
		"社科-社会学",
		"社科-心理学",
		"社科-其它"

		};	
	
	private String folderTemplate="#说明：  ${folder}\r\n\r\n#父分类\r\n\r\ntype_parent=${parentType}\r\n\r\n#子分类\r\n\r\ntype_sub=${subType}\r\n\r\n#目录\r\n\r\nfolder=${folder}";
}
