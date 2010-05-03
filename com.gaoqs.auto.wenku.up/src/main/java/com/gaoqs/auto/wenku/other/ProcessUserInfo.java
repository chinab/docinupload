package com.gaoqs.auto.wenku.other;

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.model.DocinUserModel;
import com.gaoqs.commons.httpclient.HttpClientProcess;
import com.gaoqs.commons.string.StringProcess;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * 处理用户信息
 * 
 * @author jeff gao 2010-3-29
 */
public class ProcessUserInfo {

	private static Selenium browser ;
	
	public static void main(String args[]) {
		String citys[] = new String[] { "北京", "上海", "深圳", "淮安", "常州", "青海",
				"苏州", "南京", "重庆", "威海", "九江", "大连", "内蒙", "甘肃", "海南", "山西" };
		HttpClientProcess process = new HttpClientProcess();
		// 取网名
		int j = 155;
		for (int i = 7; i < 68; i++) {
			if(j>200) break;
			String contents = process.getEncoding(
					"http://qqgexing.com/name/sex0/" + i + ".html", null,"gb2312", "gb2312");
			// #008080
			// </font>
			List list = StringProcess.processRegx(contents,
					"008080([^td]*)</font>");

			for (Object object : list) {
				object = object.toString().replace("008080'>", "");
				object = object.toString().replace("</font>", "");
				object = object.toString().replace("�", "");
				object = object.toString().replaceAll("&#.*;", "");
				object = object.toString().replace("丶", "");
				object = object.toString().replace("｀", "");
				object = object.toString().replace("、", "");
				object = object.toString().replace("　", "");
				object = object.toString().trim();

				System.out.println(object);

				// userdao
				DocinUserDao dao = DaoFactory.getBean(DocinUserDao.class,"docinUserDao");
				DocinUserModel model = dao.get(DocinUserModel.class, "" + j);
				System.out.println("处理用户：" + j);
				model.setNetName(object.toString());
				// 打开浏览器
				if(browser==null){
					browser = new DefaultSelenium("localhost", 4444,"*iexplore", "http://www.docin.com/app/login");
					browser.start();// 启动服务
				}				
				browser.setTimeout("120000");
				browser.open("http://www.docin.com/app/login");
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				browser.type("username", model.getName());
				browser.type("password", model.getPassword());
				browser.click("xpath=//input[@value='登录']");
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				browser.open("http://www.docin.com/app/user/editUserInfo");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				browser.type("nikeName", object.toString());
				browser.type("realName", object.toString());
				int r=new Random(new Date().getTime()).nextInt();
				if(r%2==0)
					browser.check("sexM");
				else
					browser.check("sexW");
				browser.type("city", citys[j % citys.length]);
				browser.type("profession", object.toString());
				browser.type("userDescription", object.toString());
				browser.type("interest", object.toString());
				browser.type("complexion", object.toString());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				browser.click("Submit");
				dao.saveSimple(model);
				browser.open("http://www.docin.com/app/loginOut");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				j++;

			}
		}
	}
}
