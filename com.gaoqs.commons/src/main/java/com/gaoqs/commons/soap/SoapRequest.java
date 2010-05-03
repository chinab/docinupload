package com.gaoqs.commons.soap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 执行soap请求
 * @author jeff gao
 * 参照
 * http://somecold.cnblogs.com/archive/2006/02/13/329728.html
 */
public class SoapRequest {
	private String SOAPUrl;
	private String xmlFile2Send;
	private String SOAPAction;
	private String cookies;
	private String userAgent;
	
	public SoapRequest(String url,String requestContent,String cookies,String userAgent){
		SOAPUrl=url;
		xmlFile2Send=requestContent;
		SOAPAction="";
		this.cookies=cookies;
		this.userAgent=userAgent;
	}
	
	public SoapRequest(String url,String requestContent,String cookies,String userAgent,String action){
		SOAPUrl=url;
		xmlFile2Send=requestContent;
		SOAPAction=action;
		this.cookies=cookies;
		this.userAgent=userAgent;
	}
	
	public String executeRequest() throws Exception {
		URL url = new URL(SOAPUrl);
		URLConnection connection = url.openConnection();
		HttpURLConnection httpConn = (HttpURLConnection) connection;

		byte[] b =xmlFile2Send.getBytes();
		// Set the appropriate HTTP parameters.
		httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
		httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		httpConn.setRequestProperty("SOAPAction", SOAPAction);
		if(userAgent!=null)
			httpConn.setRequestProperty("User-Agent", userAgent);  
		if(cookies!=null)
			httpConn.setRequestProperty("Cookie", cookies); 
		httpConn.setRequestMethod("POST");
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		// Everything's set up; send the XML that was read in to b.
		OutputStream out = httpConn.getOutputStream();
		out.write(b);
		out.close();
		// Read the response and write it to standard out.
		InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
		BufferedReader in = new BufferedReader(isr);
		String inputLine;
		StringBuffer sb=new StringBuffer();
		while ((inputLine = in.readLine()) != null)
			sb.append(inputLine);
		in.close();
		return sb.toString();
	}

//	// copy method from From E.R. Harold's book "Java I/O"
//	public static void copy(InputStream in, OutputStream out)
//			throws IOException {
//		// do not allow other threads to read from the
//		// input or write to the output while copying is
//		// taking place
//		synchronized (in) {
//			synchronized (out) {
//				byte[] buffer = new byte[256];
//				while (true) {
//					int bytesRead = in.read(buffer);
//					if (bytesRead == -1)
//						break;
//					out.write(buffer, 0, bytesRead);
//				}
//			}
//		}
//	}	
}
