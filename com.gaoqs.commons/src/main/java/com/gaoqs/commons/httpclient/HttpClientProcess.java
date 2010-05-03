package com.gaoqs.commons.httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.gaoqs.commons.exception.BusinessExceptions;

public class HttpClientProcess {
	
	private static final Log log=LogFactory.getLog(HttpClientProcess.class);
	/**
	 * httpclient
	 */
	public HttpClient httpclient;
	/**
	 * 返回结果
	 */
	public HttpResponse response;
	
	/**
	 * post请求
	 */
	public HttpPost httppost;
	/**
	 * get请求
	 */
	public HttpGet httpGet;
	
	/**
	 * 请求头
	 */
	public Map<String,String> headerMap=new HashMap<String,String>();
	
	public HttpClientProcess() {
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,CookiePolicy.BROWSER_COMPATIBILITY);
	}
	

	/**
	 * 返回cookies的list形式
	 * @return
	 */
	public List<Cookie> getCookies() {
		return ((AbstractHttpClient) httpclient).getCookieStore().getCookies();
	}

	/**
	 * 返回cookies的map形式
	 * @return
	 */
	public Map<String, String> getCookiesMap() {
		Map<String, String> map = new HashMap<String, String>();
		List<Cookie> cookies = ((AbstractHttpClient) httpclient).getCookieStore().getCookies();
		log.warn("session:");
		for (Cookie c : cookies) {
			map.put(c.getName(), c.getValue());
			log.warn(c.getName()+"="+c.getValue());
		}
		return map;
	}

	/**
	 * 从cookies中查找key对应的值
	 * @param key
	 * @return
	 */
	public String getCookiesValue(String key) {
		for (Cookie c : ((AbstractHttpClient) httpclient).getCookieStore().getCookies()) {
			if (c.getName().equals(key)){
				log.warn("get session value:"+c.getName()+"="+c.getValue());
				return c.getValue();
			}
		}
		return null;
	}

	/**
	 * 添加自定义的cookie
	 * @param list
	 */
	public void addCookies(List<Cookie> list){
		CookieStore store=((AbstractHttpClient) httpclient).getCookieStore();
		for (Cookie cookie : list) {
			store.addCookie(cookie);
			log.warn("add cookie:name="+cookie.getName()+"@value="+cookie.getValue());
		}
	}
	
	/**
	 * 得到cookies的字符串，用于手动拼装header
	 * @return
	 */
	public String getCookiesString(){
		List<Cookie> cookies= getCookies();
		StringBuffer sb=new StringBuffer();
		int i=0;
		for (Cookie cookie : cookies) {
			i++;
			if(i!=1){
				sb.append("; ");
			}
			sb.append(cookie.getName()+"="+cookie.getValue());
		}
		return sb.toString();
	}
	/**
	 * get请求 (默认utf-8编码)
	 * 
	 * @param url 
	 * @return
	 * @throws Exception
	 */
	public String get(String url,String params) {
		return getEncoding(url,params,HTTP.UTF_8,HTTP.UTF_8);
	}
	
	/**
	 * 指定编码的get请求
	 * 
	 * @param url
	 * @param paramEncoding 请求参数编码
	 * @param responseEncoding 返回结果编码
	 * @return
	 * @throws Exception
	 */
	public String getEncoding(String url,String params,String paramEncoding,String responseEncoding) {
		String finalUrl=url;
		try{
			if(params!=null){
				finalUrl+="?"+URLEncoder.encode(params, paramEncoding);
			}
		}catch(Exception e ){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		log.warn("get url:"+finalUrl);
		httpGet = new HttpGet(finalUrl);		
		return executeHttpRequest(httpGet,responseEncoding);
	}
	
	/**
	 * post请求(默认utf-8编码)
	 * @param url
	 * @param nvps
	 * @return
	 * @throws Exception
	 */
	public String post(String url,Object params){
		return postEncoding(url,params,null,null);
	}
	
	/**
	 * 指定编码的post请求
	 * 
	 * @param url
	 * @param paramEncoding 请求参数编码
	 * @param responseEncoding 返回结果编码
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String postEncoding(String url,Object params,String paramEncoding,String responseEncoding) {		
		httppost = new HttpPost(url);		
		HttpEntity entity=parseParamsToEntity(params,paramEncoding);		
		if(entity!=null){
			httppost.setEntity(entity);
		}
		log.warn("post url:"+url);
		return executeHttpRequest(httppost,responseEncoding);
	}
	
	/**
	 * 不同的参数类型转成可提交的接口参数
	 * @param params
	 * @param paramEncoding
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HttpEntity parseParamsToEntity(Object params,String paramEncoding){
		List<NameValuePair> nvps=new ArrayList<NameValuePair>();
		HttpEntity entity=null;
		try{
			if(params!=null && params instanceof List){
				//List<NameValuePair> 参数
				entity=new UrlEncodedFormEntity((List<NameValuePair>)nvps, paramEncoding);
			}else if(params!=null && params instanceof Map){
				//Map 参数
				Iterator it = ((Map)params).entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					NameValuePair nvp = new BasicNameValuePair(key, value);
					nvps.add(nvp);
				}
				entity=new UrlEncodedFormEntity((List<NameValuePair>)nvps, paramEncoding);
			}else if(params!=null && params instanceof String){
				//字符串参数
				entity = new StringEntity(params.toString(),paramEncoding);
			}
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		return entity;
	}
	/**
	 * 执行请求并返回结果
	 * 
	 * @param request 请求
	 * @param encoding 编码
	 * @return
	 * @throws Exception
	 */
	public String executeHttpRequest(HttpUriRequest request,String encoding){
		String html = "";
		try{
			appendHeader(request);
			response = httpclient.execute(request);
			HttpEntity httpEntity = response.getEntity();			
			if (httpEntity != null) {
				html = EntityUtils.toString(httpEntity,encoding);
				httpEntity.consumeContent();
			}
		}catch(Exception e ){
			//e.printStackTrace();
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		return html;
	}
	
	/**
	 * 附加请求头
	 * @param request
	 */
	private void appendHeader(HttpUriRequest request){
		if(headerMap!=null){
			Iterator<String> it=headerMap.keySet().iterator();
			while(it.hasNext()){
				String key=it.next();
				if(headerMap.get(key)!=null && !"".equals(headerMap.get(key).toString().trim()))
					request.addHeader(key, headerMap.get(key));
			}
		}
	}
	
	/**
	 * 下载文件
	 * @param url
	 * @param path
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public long getFile(String url, String path,String method,Map<String,String> params,String paramEncoding) {
		HttpUriRequest request;
		try{
			if("post".equals(method)){
				request=new HttpPost(url);	
				if(params!=null){
					//Map 参数
					Iterator it = ((Map)params).entrySet().iterator();
					List<NameValuePair> nvps=new ArrayList<NameValuePair>();
					HttpEntity entity=null;
					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry) it.next();
						String key = (String) entry.getKey();
						String value = (String) entry.getValue();
						NameValuePair nvp = new BasicNameValuePair(key, value);
						nvps.add(nvp);
					}
					entity=new UrlEncodedFormEntity((List<NameValuePair>)nvps, paramEncoding);
					if(entity!=null){
						((HttpPost)request).setEntity(entity);
					}
				}
			}else{
				request= new HttpGet(url);		
			}
			appendHeader(request);
			response = httpclient.execute(request);
			HttpEntity httpEntity = response.getEntity();
			byte[] b = EntityUtils.toByteArray(httpEntity);
			File storeFile = new File(path);
			if(storeFile.exists()){
				log.warn("the file in path exists:"+path);
				return 0;
			}
			log.warn("save file to path:"+path);
			FileOutputStream output = new FileOutputStream(storeFile);
			output.write(b);
			output.close();
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}
			return b.length;
		}catch(Exception e){
			log.error(BusinessExceptions.getDetailTrace(e));
		}
		return 0;
	}
	
	/**
	 * 下载文件
	 * @param url
	 * @param path
	 * @throws IOException
	 */
	public void getFile(String url, String path) {
		getFile(url,path,"get",null,null);
	}

	/**
	 * 关闭
	 * @throws IOException
	 */
	public void close() throws IOException {
		httpclient.getConnectionManager().shutdown();
	}


	public HttpClient getHttpclient() {
		return httpclient;
	}


	public void setHttpclient(HttpClient httpclient) {
		this.httpclient = httpclient;
	}


	public HttpResponse getResponse() {
		return response;
	}


	public void setResponse(HttpResponse response) {
		this.response = response;
	}


	public Map<String, String> getHeaderMap() {
		return headerMap;
	}


	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}
	
	
}