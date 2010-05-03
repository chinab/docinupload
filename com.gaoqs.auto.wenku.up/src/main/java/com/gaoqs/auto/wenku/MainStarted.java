package com.gaoqs.auto.wenku;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.auto.wenku.click.AutoClickMain;
import com.gaoqs.auto.wenku.click.CheckUserDocsMain;

/**
 * 主启动
 * @author jeff gao
 *
 */
public class MainStarted {

	private static Log log=LogFactory.getLog(MainStarted.class);
	
	public static void main(String args[]){
		String chooseStarted="StartWenKuUp";
		if(args!=null && args.length>0){
			chooseStarted=args[0];
			log.warn("use input execute params:"+chooseStarted);
		}
		//下载程序
		if("StartWenKuUp".equals(chooseStarted)){
			StartWenKuUp a=new StartWenKuUp();
			log.warn("execute StartWenKuUp!");
			a.main(args);
		}
		//自动点击程序
		if("AutoClickMain".equals(chooseStarted)){
			AutoClickMain b=new AutoClickMain();
			log.warn("execute AutoClickMain!");
			b.main(args);
		}		
		//自动下载统计程序
		if("CheckUserDocsMain".equals(chooseStarted)){
			CheckUserDocsMain c=new CheckUserDocsMain();
			log.warn("execute CheckUserDocsMain!");
			c.main(args);
		}		
	}
}
