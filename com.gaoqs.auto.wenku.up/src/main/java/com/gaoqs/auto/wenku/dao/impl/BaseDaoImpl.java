package com.gaoqs.auto.wenku.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.gaoqs.auto.wenku.dao.BaseDao;

public class BaseDaoImpl extends HibernateDaoSupport implements BaseDao {

	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#getMapListByHql(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List getMapListByHql(final String hql) {
		return (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql).setResultTransformer(
						Transformers.ALIAS_TO_ENTITY_MAP);
				return q.list();
			}
		});
	}

	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#find(java.lang.String, java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	public List find(String hql, Object... values) {
		return getHibernateTemplate().find(hql, values);
	}

	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#find(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public List find(Object obj) {
		return getHibernateTemplate().findByExample(obj);
	}

	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#get(java.lang.Class, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <C> C get(Class<C> entityClass, String id) {
		return (C) getHibernateTemplate().get(entityClass, id);
	}

	/**
	 * 保存单个po
	 * 
	 * @param object
	 * @throws BusinessExceptions
	 */
	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#saveSimple(java.lang.Object)
	 */
	public void saveSimple(Object object) {
		getHibernateTemplate().saveOrUpdate(object);
	}
	
	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#fieldIsDuplicate(java.lang.Class, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Boolean fieldIsDuplicate(Class clazz, String filed,String value, String subSql) {
		if (filed == null)
			filed = "";
		StringBuffer sb = new StringBuffer("select count(*) from ");
		sb.append(clazz.getSimpleName());
		sb.append(" where 1=1 ");
		if (filed != null)
			sb.append(" and "+filed+"='"+value+"' ");
		if(subSql!=null)
			sb.append(subSql);
		List list=getHibernateTemplate().find(sb.toString());
		if (list.size() > 0) {
			return false;
		} else {
			return true;
		}
	}
	
	public Object executeHql(final String hql){
		return executeBatchHql(new String[]{hql});
	}
	
	public Object executeBatchHql(final String[] hqlList){
		return getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session arg0)
			throws HibernateException, SQLException {
				for (int i = 0; i < hqlList.length; i++) {
					if(hqlList[i]==null || "".equals(hqlList[i].trim())) continue;
					arg0.createQuery(hqlList[i]).executeUpdate();
					arg0.flush();
				}
				return null;
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see com.gaoqs.auto.wenku.dao.BaseDao#getMapListByHqlPage(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> getMapListByHqlPage(final String hql, final int start, final int nums) {
		return (List) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.createQuery(hql).setResultTransformer(
						Transformers.ALIAS_TO_ENTITY_MAP);
				q.setFirstResult(start);
				q.setMaxResults(nums);
				return q.list();
			}
		});
	}
}
