package com.gaoqs.auto.wenku.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * 豆丁用户上传配置
 * 
 * @author jeff gao 2010-3-24
 */
@Entity
@Table(name = "tbl_docin_user_config")
public class DocinUserUpModel {

	/**
	 * 未使用
	 */
	public static String UN_USE = "0";

	/**
	 * 正在使用
	 */
	public static String IN_USE = "1";

	private String id;
	private DocinUserModel user;
	private String folderPath;
	/**
	 * 上传是否包括子目录
	 */
	private String includeSub;
	/**
	 * 文档定价
	 */
	private String docMoney;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastProcessDate;
	
	private String status;

	@Id
	@Column(length = 50)
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(columnDefinition="varchar(50)",name = "docin_user_id")
	public DocinUserModel getUser() {
		return user;
	}

	public void setUser(DocinUserModel user) {
		this.user = user;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String getIncludeSub() {
		return includeSub;
	}

	public void setIncludeSub(String includeSub) {
		this.includeSub = includeSub;
	}

	public String getDocMoney() {
		return docMoney;
	}

	public void setDocMoney(String docMoney) {
		this.docMoney = docMoney;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastProcessDate() {
		return lastProcessDate;
	}

	public void setLastProcessDate(Date lastProcessDate) {
		this.lastProcessDate = lastProcessDate;
	}

}
