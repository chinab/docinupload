package com.gaoqs.auto.wenku;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gaoqs.auto.wenku.dao.DaoFactory;
import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.dao.SysConfigDao;
import com.gaoqs.auto.wenku.model.DocinUserModel;
import com.gaoqs.commons.exception.BusinessExceptions;

/**
 * 
 * 多线程启动爬虫上传
 * 
 * @author jeff gao
 * 
 */
public class StartWenKuUp {

	private static Log log = LogFactory.getLog(StartWenKuUp.class);

	public static void main(String args[]) {
		// 取得每次允许启动的线程数
		new DaoFactory();
		SysConfigDao sysDao = DaoFactory.getBean(SysConfigDao.class,"sysConfigDao");
		String threadNumStr = sysDao.getSysconfigValue(Constants.DOCIN_UP_THREAD_NUM,null);
		int threadNum = Constants.DEFAULT_UP_THREAD_NUM;
		//将所有的上传配置设为可用
		sysDao.executeHql("update DocinUserUpModel set status=0");
		try {
			if (threadNumStr != null)
				threadNum = Integer.parseInt(threadNumStr);
		} catch (Exception e) {
			threadNum = Constants.DEFAULT_UP_THREAD_NUM;
		}

		for (int i = 1; i <= threadNum; i++) {
			try {
				DocinUserDao docDao = DaoFactory.getBean(DocinUserDao.class,"docinUserDao");
				// 得到一个活动用户
				List<Map<String, String>> upUserConfigList = docDao.getActiveUpUser(1);
				if (upUserConfigList == null || upUserConfigList.size() == 0) {
					log.error("has no active user!!");
					break;
				}
				String upUserId = upUserConfigList.get(0).get("upId");
				DocinUserModel upUserModel = docDao.get(DocinUserModel.class,upUserId);
				if (upUserModel == null) {
					log.error("can't find the upload user of id =" + upUserId);
					break;
				}
				DocinUploadMain up = new DocinUploadMain(upUserConfigList.get(0), upUserModel);
				new Thread(up, "MyUploadThread" + i).start();
				try {
					// 每隔60秒钟启动一个，以错开时间处理
					Thread.sleep(60 * 1000);
				} catch (Exception e) {
					log.error("main thread sleep:"+ BusinessExceptions.getDetailTrace(e));
				}

			} catch (Exception e) {
				log.error("process user error:" + i + "@"+ BusinessExceptions.getDetailTrace(e));
			}
		}
	}

}
