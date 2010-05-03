package com.gaoqs.auto.wenku.dao;

import java.util.List;
import java.util.Map;

public interface DocinUserDao  extends BaseDao{
	/**
	 * 得到上传的用户
	 * @param number
	 * @return
	 */
	public List<Map<String,String>> getActiveUpUser(int number);
}
