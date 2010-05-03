package com.gaoqs.auto.wenku.other;

import java.util.List;

import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.model.DocinUserModel;
import com.gaoqs.commons.string.StringProcess;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * 批量设置，价格
 * 
 * @author jeff gao 2010-3-29
 */
public class PriceSetting {

	private static Selenium browser;

	public static void main(String args[]) {
		DocinUserDao dao = DaoFactory.getBean(DocinUserDao.class,
				"docinUserDao");
		for (int i = 1; i < 201; i++) {
			DocinUserModel model = dao.get(DocinUserModel.class, "" + i);
			if (browser == null) {
				browser = new DefaultSelenium("localhost", 4444, "*iexplore",
						"http://www.docin.com/app/login");
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
			for (int j = 1;; j++) {
				// 处理同一用户的多页
				browser
						.open("http://www.docin.com/app/my/docin/myBook?styleList=1&orderName=0&orderDate=1&orderVisit=0&orderStatus=0&orderFolder=0&folderId=0&myKeyword=&currentPage="
								+ j);
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String pageContext = browser.getHtmlSource();

				List list = StringProcess.processRegx(pageContext,
						"/p-([^/p-]*)html");
				if (list == null || list.size() == 0) {
					break;
				}

				// 设置价格，并让豆丁自动调整
				browser.type("downPrice", "0.43");
				browser.check("priceflag");
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
