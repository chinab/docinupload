package com.gaoqs.commons.string;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 汉字互转%unicode
 * @author jeff gao
 *
 */
public class EncodingUtil {

	private static Log log = LogFactory.getLog(EncodingUtil.class);

	public static String getEncode(String s, String charset) {
		if (s == null || "".equals(s))
			return null;
		if (charset == null || "".equals(charset))
			charset = "GB2312";
		try {
			return URLEncoder.encode(s, charset);
		} catch (UnsupportedEncodingException e) {
			log.debug("Encode Exception--&gt;" + e);
		}
		return null;
	}

	public static String getFromEncode(String s, String charset) {
		if (s == null || "".equals(s))
			return null;
		if (charset == null || "".equals(charset))
			charset = "GB2312";
		try {
			return URLDecoder.decode(s, charset);
		} catch (UnsupportedEncodingException e) {
			log.debug("Encode Exception--&gt;" + e);
		}
		return null;
	}

//	public static void main(String args[]) {
//		System.out.println(getEncode(getEncode("带回程的车辆运输路经优化及定价模型", "UTF-8"),"UTF-8"));
//		System.out.println(getFromEncode(getFromEncode("%25E5%25B8%25A6%25E5%259B%259E%25E7%25A8%258B%25E7%259A%2584%25E8%25BD%25A6%25E8%25BE%2586%25E8%25BF%2590%25E8%25BE%2593%25E8%25B7%25AF%25E7%25BB%258F%25E4%25BC%2598%25E5%258C%2596%25E5%258F%258A%25E5%25AE%259A%25E4%25BB%25B7%25E6%25A8%25A1%25E5%259E%258B",
//								"UTF-8"), "UTF-8"));
//	}
}