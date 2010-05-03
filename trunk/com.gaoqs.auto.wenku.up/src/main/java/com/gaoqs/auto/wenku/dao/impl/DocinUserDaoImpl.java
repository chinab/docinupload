package com.gaoqs.auto.wenku.dao.impl;

import java.util.List;
import java.util.Map;

import javax.transaction.Synchronization;

import com.gaoqs.auto.wenku.dao.DocinUserDao;
import com.gaoqs.auto.wenku.model.DocinUserUpModel;

public class DocinUserDaoImpl extends BaseDaoImpl implements DocinUserDao {
	
	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.DocinUserDao#getActiveUpUser(int)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,String>> getActiveUpUser(int number){
		StringBuffer sql=new StringBuffer("select a.id as id,a.user.id as upId,a.folderPath as folderPath,a.includeSub as includeSub,a.docMoney as docMoney from ");
		sql.append("DocinUserUpModel a where a.status='"+DocinUserUpModel.UN_USE+"' order by a.lastProcessDate asc");
		List<Map<String,String>> mapList=getMapListByHql(sql.toString());
		return mapList.subList(0, number);
	}
}
