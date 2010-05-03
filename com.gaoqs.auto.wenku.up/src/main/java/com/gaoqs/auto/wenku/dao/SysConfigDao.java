package com.gaoqs.auto.wenku.dao;

import java.util.Map;

public interface SysConfigDao  extends BaseDao{
	/**
	 * 得到所有的系统配置属性
	 * 
	 * @return
	 */
	public Map<String, String> getAllSysConfig();

	/**
	 * 得到某单个系统配置属性值
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getSysconfigValue(String key,String defaultValue);

}
