package com.gaoqs.auto.wenku.click;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.gaoqs.auto.wenku.dao.BaseDao;
import com.gaoqs.auto.wenku.dao.DaoFactory;

/**
 * 从数据库得到文档地址
 * @author jeff gao
 *
 */
public class DocUrlGetProcess implements Runnable {

	private BaseDao dao;
	
	public DocUrlGetProcess(){
		new DaoFactory();
		dao = DaoFactory.getBean(BaseDao.class, "baseDao");
		//每次先手动重新处理数据库
		//dao.executeHql("update DocinProcessLog set status='0'");
	}
	
	private void execute() {
		while (true) {
			 if(AutoClickMain.shell.isDisposed()){
				 return;
			 }
			String hql = "select id as id,url as url from DocinProcessLog where status='0'";
			List<Map<String, Object>> list = dao.getMapListByHqlPage(hql, 0, 10);
			int r = new Random(new Date().getTime()).nextInt();
			if (r < 0)  r *= -1;
			if (list == null || list.size() == 0) {
				// 一天只处理一次访问
				return;
			}

			Map<String, Object> mapConfig = list.get(r % list.size());
			String loginUrl = mapConfig.get("url").toString();
			
			AutoClickMain.currentUrl=loginUrl;
			System.out.println("process url:"+loginUrl);
			
			dao.executeHql("update DocinProcessLog set status='1' where id='" + mapConfig.get("id") + "'");

			 try {
				 Thread.sleep(20000);
			 } catch (InterruptedException e) {
				 e.printStackTrace();
			 }
		}
	}

	public void run() {
		execute();
	}

}
