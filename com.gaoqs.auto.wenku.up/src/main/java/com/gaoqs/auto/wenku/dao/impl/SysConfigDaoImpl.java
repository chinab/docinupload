package com.gaoqs.auto.wenku.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gaoqs.auto.wenku.dao.SysConfigDao;
import com.gaoqs.commons.exception.BusinessExceptions;
import com.sun.media.Log;

public class SysConfigDaoImpl extends BaseDaoImpl implements SysConfigDao {

	@SuppressWarnings("unchecked")
	public Map<String, String> getAllSysConfig() {
		Map<String, String> result = new HashMap<String, String>();
		List<Object[]> list = getHibernateTemplate().find("select code,value from SysConfigModel");
		if (list != null && list.size() > 0) {
			for (Object[] obj : list) {
				try {
					result.put(obj[0].toString(), obj[1].toString());
				} catch (Exception e) {
					Log.error("process sysconfig error:"+ BusinessExceptions.getDetailTrace(e));
				}
			}
		}
		return result;
	}
	
	public String getSysconfigValue(String key,String defaultValue){
		String r= getAllSysConfig().get(key);
		if(r==null) return defaultValue;
		return r;
	}
	
}
