package com.gaoqs.auto.wenku.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.gaoqs.commons.exception.BusinessExceptions;

public interface BaseDao {
	/**
	 * 执行hql，返回list<map>
	 * 
	 * @param hql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getMapListByHql(final String hql);
	
	/**
	 * 分页处理
	 * @param hql
	 * @param start
	 * @param nums
	 * @return
	 */
	public List<Map<String,Object>> getMapListByHqlPage(final String hql,final int start,final int nums);


	/**
	 * 根据hql获取列表
	 * 
	 * @param hql
	 * @param values
	 *            参数集合
	 * @return 列表
	 */
	public List find(String hql, Object... values);

	/**
	 * 根据po查找，返回po
	 * 
	 * @param obj
	 * @return
	 */
	public List find(Object obj);

	/**
	 * 获取单个PO
	 * 
	 * @param <C>
	 *            类型
	 * @param entityClass
	 *            类
	 * @param id
	 *            id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <C> C get(Class<C> entityClass, String id);

	/**
	 * 保存单个po
	 * 
	 * @param object
	 * @throws BusinessExceptions
	 */
	public void saveSimple(Object object);

	/**
	 * 判断字段值唯 一
	 * 
	 * @param clazz
	 * @param id
	 * @param value
	 * @param subSql
	 * @return
	 */
	public Boolean fieldIsDuplicate(Class clazz, String filed,String value, String subSql);
	
	/**
	 * 执行hql
	 * @param hql
	 * @return
	 */
	public Object executeHql(final String hql);
	
	/**
	 * 执行批量hql
	 * @param hqlList
	 * @return
	 */
	public Object executeBatchHql(final String[] hqlList);
}
